package org.arrowhead.wp5.aggmanager.impl;

/*-
 * #%L
 * ARROWHEAD::WP5::Aggregator Manager
 * %%
 * Copyright (C) 2016 The ARROWHEAD Consortium
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferSchedule;
import org.arrowhead.wp5.core.entities.FlexOfferSlice;

public class HTTPClientTest {

    public static String id = "myid";
    public static String BASE_URI = "http://iliving.cs.aau.dk:80";
//    public static String BASE_URI = "http://localhost:9998";

    public static void main(String[] args) {
        FlexOffer flexOffer = new FlexOffer();
        /*
         * Here we just use static value to populate the slices of the flexoffer
         */
        List<FlexOfferSlice> slices = new ArrayList<FlexOfferSlice>();
        int loadLow[] = new int[] { -500, 1700, -1050, 1100, 500, 1400, 400 };
        int loadHigh[] = new int[] { 500, 1732, -500, 1168, 552, 1471, 453 };
        int sliceDuration = 1;

        for (int i = 0; i < loadLow.length; i++) {
            slices.add(new FlexOfferSlice(sliceDuration, 1, loadLow[i], loadHigh[i]));
        }

        flexOffer.setSlices(slices.toArray(flexOffer.getSlices()));

        /*Need to set some time parameter of the flexoffer*/
        Date ct = new java.util.Date();
        flexOffer.setCreationTime(ct);

        Calendar cal = Calendar.getInstance();
        cal.setTime(ct);

        cal.add(Calendar.MINUTE, 30);

        cal.add(Calendar.MINUTE, 1);
        /*Time before aggregator needs to send back acceptance*/
        flexOffer.setAcceptanceBeforeTime(cal.getTime());

        cal.add(Calendar.MINUTE, 1);
        /*Time before aggregator needs to send back schedule*/
        flexOffer.setAssignmentBeforeTime(cal.getTime());

        cal.add(Calendar.MINUTE, 10);
        /*Minimum time at which the consumption pattern can start*/
        flexOffer.setStartAfterTime(cal.getTime());

        cal.add(Calendar.HOUR, 1);
        /*Maximum time for the consumption pattern to start*/
        flexOffer.setStartBeforeTime(cal.getTime());
        flexOffer.setOfferedById(id);
        flexOffer.setId(1);
        flexOffer.setDefaultSchedule(new FlexOfferSchedule(flexOffer));

        Client client = ClientBuilder.newClient();

        boolean json = false;

        String mtype;
        Entity<FlexOffer> ent;
        if (json) {
            mtype = MediaType.APPLICATION_JSON;
            ent = Entity.json(flexOffer);
        } else {
            mtype = MediaType.APPLICATION_XML;
            ent = Entity.xml(flexOffer);
        }

        WebTarget target = client.target(BASE_URI).path("api").path("flexoffers").path(id);
        System.out.println(target.getUri());
        Response resp = target.request().accept(mtype).post(ent);
        System.out.println(resp.getStatus());
        System.out.println(resp.getStatusInfo());
        System.out.println(resp.readEntity(String.class));


        if (resp.getStatus() == 200) {
            FlexOfferSchedule fos = null;
            while ((fos = flexOffer.getFlexOfferSchedule()) == null) {
                target = client.target(BASE_URI).path("api").path("flexoffers").path(id).path(Integer.toString(flexOffer.getId()));
                resp = target.request().accept(mtype).get();
                System.out.println(resp.getStatus());
                flexOffer = resp.readEntity(FlexOffer.class);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("This should not have happened!");
                }
            }
            /*Do something with the schedule (apply it)*/
            System.out.println("Total Energy: " + fos.getTotalEnergy());
        }
    }

}
