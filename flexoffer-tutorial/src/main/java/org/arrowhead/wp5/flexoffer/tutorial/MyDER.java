package org.arrowhead.wp5.flexoffer.tutorial;

/*-
 * #%L
 * flexoffer-tutorial
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.arrowhead.wp5.core.entities.AbstractDER;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferException;
import org.arrowhead.wp5.core.entities.FlexOfferSchedule;
import org.arrowhead.wp5.core.entities.FlexOfferSlice;
import org.arrowhead.wp5.core.impl.FlexOfferAgent;

/*This is the part that you need to implement*/
public class MyDER extends AbstractDER {

    private static final long serialVersionUID = -8899409979845372732L;

    FlexOfferAgent foa;
    
    public MyDER(FlexOfferAgent foa) {
        super("myname", "mytype");
        this.foa = foa;
        this.foa.registerDer(this);
    }
    
    /**
     * Example of a method to generate a FlexOffer
     * @param start
     * @param end
     * @return
     */
    public void generateFlexOffer() {
        FlexOffer flexOffer = new FlexOffer();
        /*
         * Here we just use static value to populate the slices of the flexoffer
         */
        List<FlexOfferSlice> slices = new ArrayList<FlexOfferSlice>();
        int loadLow[] = new int[] { -500, 1700, -1050, 1100, 500, 1400, 400 };
        int loadHigh[] = new int[] { 500, 1732, -500, 1168, 552, 1471, 453 };
        int sliceDuration = 4;

        for (int i = 0; i < loadLow.length; i++) {
            slices.add(new FlexOfferSlice(sliceDuration, 1, loadLow[i], loadHigh[i]));
        }

        flexOffer.setSlices(slices.toArray(flexOffer.getSlices()));

        /*Need to set some time parameter of the flexoffer*/
        Date ct = new java.util.Date();
        flexOffer.setCreationTime(ct);

        Calendar cal = Calendar.getInstance();
        
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

        
        try {
            foa.createFlexOffer(id, flexOffer);
        } catch (FlexOfferException e) {
            System.out.println("Something went wrong!");
        }
    }

    @Override
    public void updateSchedule(FlexOffer fo) {
        /*Apply the schedule to the object so that it follows 
         *the assigned consumption pattern*/
        @SuppressWarnings("unused")
		FlexOfferSchedule schedule = fo.getFlexOfferSchedule();
    }

}
