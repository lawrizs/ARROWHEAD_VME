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

import java.io.IOException;

import org.arrowhead.wp5.com.xmpp.api.HOXTWrapper;
import org.arrowhead.wp5.com.xmpp.api.ResourceManager;
import org.arrowhead.wp5.com.xmpp.clients.flexofferclient.XFlexOfferSubscriberClient;
import org.arrowhead.wp5.core.entities.AbstractDER;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferException;
import org.arrowhead.wp5.core.impl.FlexOfferAgent;
import org.arrowhead.wp5.core.interfaces.FlexOfferUpdateListener;
import org.arrowhead.wp5.core.services.ArrowheadXMPPServiceManager;
import org.arrowhead.wp5.fom.xresources.XFlexOfferResource;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyFlexibleResource implements FlexOfferUpdateListener {

    final static Logger logger = LoggerFactory.getLogger(MyFlexibleResource.class);

    /** Encapsulated flex-offer agent, the entity managing the flexoffers */
    private FlexOfferAgent agent;
    
    /** The FlexibleDER that needs to generate the FlexOffers **/
    private AbstractDER flexDER;

    /**
     * The id of this flexoffer manager. Used both for the XMPP network and the
     * uniqueness of flex offers
     */
    private String id = "request-it";

    /** The id of the aggregator found by using Arrowhead service discovery **/
    private String aggId = "aggregator";

    /** The password used to connect to the XMPP server */
    private String password = "wrong";
    /** XMPP Server Host */
    private String xmppServer = "delling.dpt.cs.aau.dk";
    /** XMPP Server Port */
    private int xmppPort = 5222;
    /** XMPP Service */
    private String xmppService = "delling";
    /** XMPP resource */
    private String xmppResource = "demo";
    /** The object that encapsulates HTTP over XMPP functionalities */
    private HOXTWrapper hoxtWrapper;
    /**
     * The object that provides the functionalities to send flexoffers to the
     * aggregator
     */
    private XFlexOfferSubscriberClient xfosc;

    /** Interface to use the Arrowhead Service discovery **/
    private ArrowheadXMPPServiceManager foServiceManager;
    private static final boolean ARROWHEAD_COMPLIANT = false;

    public MyFlexibleResource() {
        this.agent = new FlexOfferAgent(id, this);
        this.foServiceManager = new ArrowheadXMPPServiceManager("alpha.jks", "abc1234", "alpha.jks", "XXXXX");
        this.flexDER = new MyDER(this.agent);
    }

    public static void main(String[] args) {
        MyFlexibleResource flexResource = new MyFlexibleResource();
        if (ARROWHEAD_COMPLIANT) {
            flexResource.doAggregatorSD();
        }
        flexResource.initXmpp();
        flexResource.flexDER.generateFlexOffer();
        synchronized (flexResource) {
            try {
                flexResource.wait();
            } catch (InterruptedException e) {
                System.out.println("FlexOffer received?");
            }
        }
    }

    private void doAggregatorSD() {
        this.foServiceManager.start();

        if (this.foServiceManager.fetchInfo()) {
            aggId = this.foServiceManager.getAggId();
            xmppServer = this.foServiceManager.getHostname();
            xmppPort = this.foServiceManager.getPort();
            xmppResource = this.foServiceManager.getResource();
            xmppService = this.foServiceManager.getService();
            logger.info("Aggregator ID set from Arrowhead framework: {}", aggId);
        } else {
            logger.warn("Failed to discover aggregator through Arrowhead framework. Using default config");
        }
    }

    public void initXmpp() {
        /**
         * Need to load the trustStore to connect to the XMPP server Can be done
         * in the code like here or using runtime parameter
         * **/
        System.setProperty("javax.net.ssl.trustStore",
                "resources/clientstore.jks");
        /*Manages REST resources for HTTP over XMPP*/
        ResourceManager resourceManager = new ResourceManager();
        /*Register the interface to the flex offer (implements FlexOfferAgentProviderIf)*/
        resourceManager.registerInstance(new XFlexOfferResource(agent));
        /*Smack connection, set up password, server...*/
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration
                .builder()
                .setUsernameAndPassword(id, password)
                .setServiceName(xmppService)
                .setHost(xmppServer)
                .setPort(xmppPort)
                .setSecurityMode(SecurityMode.required)
                .setResource(xmppResource)
                .setCompressionEnabled(false).build();
        /*A wrapper for Http over XMPP connection*/
        this.hoxtWrapper = new HOXTWrapper(config, resourceManager);
        try {
            /*Initialize the wraper, true means that we will be using 
             * both server and client functionalities.
             */
            this.hoxtWrapper.init(true);
        } catch (XMPPException | SmackException | IOException | InterruptedException e) {
            logger.error("Failed to initialize XMPP", e);
        }
        /*This is the client interface to the aggregator, to send flexoffer for example*/
        this.xfosc = new XFlexOfferSubscriberClient(aggId, hoxtWrapper);
    }

    public void deinitXmpp() {
        /*Clean up the wrapper*/
        this.hoxtWrapper.destroy();
        this.xfosc = null;
    }

    @Override
    public void onFlexOfferScheduleUpdate(FlexOffer fo) {
        /* Forwards the FlexOffer with the Schedule to the FlexibleDER */
        flexDER.updateSchedule(fo);
        logger.info("Received schedule!");
        synchronized (this) {
            this.notify();
        }
    }

    /* Callback for the FlexOfferAgent when it creates a flexoffer
     * We send the flexoffer to the aggregator throught the XMPP interface
     * */
    @Override
    public void onFlexOfferCreate(FlexOffer fo) {

        try {
            if (this.xfosc != null) {
                this.xfosc.createFlexOffer(id, fo);
            }
        } catch (FlexOfferException e) {
            // TODO Handle error
            logger.error("Error creating flexoffer.", e);
        }

    }

}
