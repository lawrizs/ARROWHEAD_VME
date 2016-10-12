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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.arrowhead.wp5.agg.api.FOAggParameters.ConstraintType;
import org.arrowhead.wp5.agg.impl.Aggregator;
import org.arrowhead.wp5.agg.impl.billing.MarketCommitment;
import org.arrowhead.wp5.aggmanager.impl.resources.AggFoResource;
import org.arrowhead.wp5.aggmanager.impl.resources.AggregatorManagerResource;
import org.arrowhead.wp5.aggmanager.impl.resources.AnalyticsResource;
import org.arrowhead.wp5.aggmanager.impl.resources.BillingResource;
import org.arrowhead.wp5.aggmanager.impl.resources.EnergyResource;
import org.arrowhead.wp5.aggmanager.impl.resources.FlexOfferResource;
import org.arrowhead.wp5.aggmanager.impl.resources.VMarketResource;
import org.arrowhead.wp5.aggmanager.impl.xmppresources.XFlexOfferResource;
import org.arrowhead.wp5.aggmanager.impl.xmppresources.XMarketResource;
import org.arrowhead.wp5.application.common.ConsoleRedirectionApp;
import org.arrowhead.wp5.application.common.StandAloneApp;
import org.arrowhead.wp5.com.xmpp.api.HOXTWrapper;
import org.arrowhead.wp5.com.xmpp.api.ResourceManager;
import org.arrowhead.wp5.com.xmpp.clients.flexofferclient.XFlexOfferProviderClient;
import org.arrowhead.wp5.com.xmpp.clients.marketclient.XMarketProviderClient;
import org.arrowhead.wp5.core.entities.ArrowheadException;
import org.arrowhead.wp5.core.entities.BidV2;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferException;
import org.arrowhead.wp5.core.entities.FlexOfferSchedule;
import org.arrowhead.wp5.core.entities.FlexOfferSlice;
import org.arrowhead.wp5.core.entities.MarketException;
import org.arrowhead.wp5.core.entities.MarketInfo;
import org.arrowhead.wp5.core.entities.TimeSeries;
import org.arrowhead.wp5.core.entities.TimeSeriesType;
import org.arrowhead.wp5.core.interfaces.FlexOfferUpdateListener;
import org.arrowhead.wp5.core.services.AggServiceManager;
import org.arrowhead.wp5.core.util.FOConfig;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketEngine;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.wadl.WadlFeature;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AggregatorManager extends StandAloneApp implements
        FlexOfferUpdateListener {
    final static Logger logger = LoggerFactory.getLogger(AggregatorManager.class);
    
    private static final String PROPERTY_FILE_KEY = "foagg.configfile";
    private static final String PROPERTY_FILE_DEFAULT = "aggregator-manager.properties";

    private HOXTWrapper hoxtManager;

    private final String id;
    private final String marketId;
    private String password;
    private final String xmppHostname;
    private final String xmppService;
    private final String xmppResource;
    private final int xmppPort;
    private final String xmppCertificate;

    private XMPPTCPConnectionConfiguration config;

    private final boolean ARROWHEAD_COMPLIANT;    
    private final boolean CONNECT_TO_MARKET;

    private XFlexOfferProviderClient xFOProvider;
    private XMarketProviderClient xMProvider = null;

    private Aggregator agg;

    /* Local HTTP server */
    private final String BASE_URI_ADDRESS;
    private HttpServer server;
    private final int serverPort;
    private final String httpPath;
    private final URI BASE_URI;

    /* Resouce managers/configs */
    private ResourceConfig resourceConfig;
    private ConsoleRedirectionApp consoleRedirectionApp;

    /* Arrowhead Subsystem */
    //	private ArrowheadSubsystem arrowheadSubsystem;
    private AggServiceManager aggServiceManager;
    
    /* Set of http client ids for which schedule should not be sent through XMPP */
    private HashSet<String> httpClients;
    
    
    // Loading defaults
    {
        // @formatter:off
        FOConfig config = new FOConfig(PROPERTY_FILE_KEY, PROPERTY_FILE_DEFAULT);

        BASE_URI_ADDRESS =    config.getString( "foagg.base-uri-address",    "http://0.0.0.0/");
        serverPort =          config.getInt(    "foagg.serverPort",          9998);        
        httpPath =            config.getString( "foagg.http-path",           "api");
        id =                  config.getString( "foagg.id",                  "aggregator");
        password =            config.getString( "foagg.password",            "");
        ARROWHEAD_COMPLIANT = config.getBoolean("foagg.arrowhead-compliant", "false");
        
        CONNECT_TO_MARKET  =  config.getBoolean("foagg.connect-to-market",   "false");
        marketId = 			  config.getString( "foagg.marketid",            "market");
        xmppHostname =        config.getString( "foagg.xmpp-server",         "");
        xmppService =         config.getString( "foagg.xmpp-service",        "");
        xmppResource =        config.getString( "foagg.xmpp-resource",       "");
        xmppPort =            config.getInt(    "foagg.xmpp-port",           5222);
        xmppCertificate =     config.getString( "foagg.xmpp-certificate",    "");

        BASE_URI = UriBuilder.fromUri(BASE_URI_ADDRESS).port(serverPort).path(httpPath).build();
        // @formatter:on
    }

    public HttpServer getHTTPServer() {
        return this.server;
    }

    public ResourceConfig getResourceConfig() {
        return resourceConfig;
    }

    public Aggregator getAggregator() {
        return this.agg;
    }

    public XMPPTCPConnectionConfiguration getConDetails() {
        return config;
    }

    public void setConDetails(XMPPTCPConnectionConfiguration conDetails) {
        this.config = conDetails;
    }

    public AggregatorManager() throws Exception {
        super();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (ARROWHEAD_COMPLIANT && AggregatorManager.this.aggServiceManager != null) {
                    AggregatorManager.this.aggServiceManager.shutdown();
                }
            }
        }));

        /* Instantiate Aggregator */
        this.agg = new Aggregator(id, this);
        /* Initialize resource config */
        this.resourceConfig = new ResourceConfig();

        /* Register resource features */
        this.resourceConfig.register(org.arrowhead.wp5.com.filter.CORSFilter.class);
        // Register the JSON feature
        this.resourceConfig.register(MoxyJsonFeature.class);
        /* Register the Wadl feature */
        this.resourceConfig.register(WadlFeature.class);

        /* Register instances */
        this.resourceConfig.registerInstances(new AggregatorManagerResource(this),
                new EnergyResource(this.agg),
                new AggFoResource(this.agg),
                new FlexOfferResource(this),
                /* new ScheduleResource(this.agg), */
                new AnalyticsResource(this.agg),
                new BillingResource(this.agg),
                new VMarketResource(this));

        this.httpClients = new HashSet<String>();
    }

    private void initHttpServer() throws IOException {
        /* Initialize the local HTTP server */
        this.server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI,
                resourceConfig, false);

        /* Enable Web-socket support */
        final WebSocketAddOn addon = new WebSocketAddOn();
        for (NetworkListener listener : server.getListeners()) {
            listener.registerAddOn(addon);
        }
        /* Activate console redirection */
        this.consoleRedirectionApp = new ConsoleRedirectionApp(this.getBsh());
        WebSocketEngine.getEngine().register("", "/console",
                consoleRedirectionApp);
        /* Add static resource handler - for the Management Console */
        this.server.getServerConfiguration().addHttpHandler(
                new CLStaticHttpHandler(
                        AggregatorManager.class.getClassLoader(), "app/"), "/");

        this.server.start();
    }

    private void initXmpp() throws XMPPException {
        /* Initializes XMPP connection */
        //		new JHades().overlappingJarsReport();

        System.setProperty("javax.net.ssl.trustStore", xmppCertificate);
        
        config = XMPPTCPConnectionConfiguration
                .builder()
                .setUsernameAndPassword(id, password)
                .setServiceName(xmppService)
                .setHost(xmppHostname)
                .setResource(xmppResource)
                .setKeystorePath(xmppCertificate)
                .setSecurityMode(SecurityMode.required)
                .setCompressionEnabled(false)
                .build();
        ResourceManager resourceManager = new ResourceManager();
        resourceManager.registerInstance(new XFlexOfferResource(this.agg));
        if (CONNECT_TO_MARKET) {
            resourceManager.registerInstance(new XMarketResource(this));
        }
        
        /* Start the XMPP server */
        try {
            hoxtManager = new HOXTWrapper(config, resourceManager);
            hoxtManager.init(true);
        } catch (SmackException | IOException | InterruptedException e1) {
            e1.printStackTrace();
        }
        xFOProvider = new XFlexOfferProviderClient(hoxtManager);
        
        if (CONNECT_TO_MARKET) {
            xMProvider = new XMarketProviderClient(marketId, hoxtManager);
            
            MarketInfo info = this.getMarketInfo();
            
            if (info != null) {
	            logger.debug("Area: {}", info.getArea());
	            logger.debug("Interval: {}", info.getInterval());
	            logger.debug("Next Period: {}", info.getNextPeriod());
            }            
        }
    }
    
    /* Sending/receiving bids from the market */    
	public MarketInfo getMarketInfo() {
		 try {
             return xMProvider.getInfo();
         } catch (Exception e) {
        	 logger.error("No market info can be retrieved!");
        	 return null;
         }		 
	}

    public void bidSupplySendToMarket(BidV2 bid) throws MarketException {
			if (xMProvider != null) {
				xMProvider.sendBidV2(bid);
			} else {
				throw new MarketException("Not connected to the market!");
			}
    }    	

    public void acceptBidV2(BidV2 bid) {
        logger.debug("received accepted bid: p: {}", bid.getAvgUnitPrice());
        
        MarketCommitment mc = new MarketCommitment();
        mc.setContract(this.agg.getMarketContract());
        mc.setLocation(0 /* No location tag*/);
        mc.setWinning_bid(bid);

        this.agg.addMarketCommitment(mc);  
    }

    @Override
    public void start() throws ArrowheadException {

        try {
            /* Start the local HTTP server */
            initHttpServer();

            /* Initialize XMPP connection */
            initXmpp();

            if (ARROWHEAD_COMPLIANT) {
                //				this.arrowheadSubsystem.init();
                this.aggServiceManager = new AggServiceManager();
                this.aggServiceManager.publishAggXMPP((String) config.getUsername(), xmppHostname, xmppPort, config.getResource(), xmppService);
            }

            /* Print a welcome message */
            logger.info("The HTTP server has started. Open \""
                    + UriBuilder.fromUri("http://localhost").port(serverPort)
                    + "\" in your browser to see the AggregatorManager GUI.");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ArrowheadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        this.server.shutdownNow();
        if (this.aggServiceManager != null) {
            this.aggServiceManager.shutdown();
        }
        // this.aggregator.disconnect();
    }

    public static void main(String[] args) {
        try {
            (new AggregatorManager()).run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
	public void onFlexOfferScheduleUpdate(FlexOffer fo) {

		if (fo.getFlexOfferSchedule() == null) {
			logger.info("Trying to execute flex-offer with no schedule");
			return;
		}

		if (!httpClients.contains(fo.getOfferedById())) {
			logger.info("No HTTP client ({}) found for returning the schedule", fo.getOfferedById());
			return;
		}

		try {
			xFOProvider.setSubscriberId(fo.getOfferedById());
			xFOProvider.createFlexOfferSchedule(fo.getId(),
					fo.getFlexOfferSchedule());
		} catch (FlexOfferException e) {
			// TODO Need to handle this error based on HTTP return code if any
			e.printStackTrace();
		}
	}
    
    public void demoCommit() {
    	long dateFrom = this.agg.getFlexOffers()[0].getStartAfterInterval();
    	long dateTo = dateFrom + 1;
    	
        BidV2 bid = this.agg.generate_maketV2_bid(dateFrom, dateTo);
        
        TimeSeries baseline = new TimeSeries(bid.getBidFlexOffer(), TimeSeriesType.tstBaselineEnergy);
        
        bid.setWinQuantities(baseline .getData());
        bid.setWinPrices(baseline.mul(0).getData());
        
        MarketCommitment mc = new MarketCommitment();
        mc.setLocation(0);
        mc.setWinning_bid(bid);
        mc.setContract(this.agg.getMarketContract());
        
        this.agg.addMarketCommitment(mc);
    }

    /* Test methods for populating Aggregator with meaninful data */
    public void demo(int numFos) {
        this.getAggregator().getAggParameters().getConstraintPair().timeFlexibilityTolerance = 10;
        this.getAggregator().getAggParameters().getConstraintPair().startAfterTolerance = 4 * 5;
        this.getAggregator().getAggParameters().getConstraintPair().timeFlexibilityToleranceType = ConstraintType.acSet;
        this.getAggregator().getAggParameters().getConstraintPair().startAfterToleranceType = ConstraintType.acSet;
        this.demoConsumptionFOs(numFos);
        this.demoProductionFOs(numFos);
    }

    public void demoConsumptionFOs(int numFos) {
        demoConsumptionFOs(numFos, 20, 25, 10);
    }

    public void demoProductionFOs(int numFos) {
        demoProductionFOs(numFos, 20, 25, 10);
    }

    public void demoConsumptionFOs(int numFos, double TWfactor, double TFTmax, double enFactor) {
        Calendar cal = Calendar.getInstance();
        Date dateFrom = cal.getTime();
        cal.add(Calendar.MINUTE, (int) (1 * TWfactor));
        Date dateTo = cal.getTime();

        this.demoFOs(numFos, dateFrom, dateTo, 10, TFTmax, 0, 1 * enFactor, 0.4 * enFactor, false);
    }

    public void demoProductionFOs(int numFos, double TWfactor, double TFTmax, double enFactor) {
        Calendar cal = Calendar.getInstance();
        Date dateFrom = cal.getTime();
        cal.add(Calendar.MINUTE, (int) (1 * TWfactor));
        Date dateTo = cal.getTime();

        this.demoFOs(numFos, dateFrom, dateTo, 10, TFTmax, -1 * enFactor, 0, -0.4 * enFactor, false);
    }

    static int idl = 0;

    public void demoFOs(int numFos, Date dateFrom, Date dateTo, int maxSlices, double TFTmax, double Emin, double Emax, double EFmax, boolean schedule) {
        Calendar cal = Calendar.getInstance();

        for (int i = 0; i < numFos; i++) {
            cal.setTime(dateFrom);
            long mFrom = cal.getTimeInMillis();
            cal.setTime(dateTo);
            long mTo = cal.getTimeInMillis();
            long mEST = (long) (mFrom + Math.random() * (mTo - mFrom));
            cal.setTimeInMillis(mEST);

            FlexOffer f = new FlexOffer();
            f.setId(idl++);
            f.setOfferedById("SELF");
            f.setStartAfterTime(cal.getTime());
            cal.add(Calendar.MINUTE, (int) (15 * Math.random() * TFTmax));
            f.setStartBeforeTime(cal.getTime());
            cal.setTimeInMillis(mEST);
            cal.add(Calendar.MINUTE, -15);
            f.setAssignmentBeforeTime(cal.getTime());
            cal.add(Calendar.MINUTE, -15);
            f.setAcceptanceBeforeTime(cal.getTime());
            cal.add(Calendar.MINUTE, -30);
            f.setCreationTime(cal.getTime());

            List<FlexOfferSlice> sl = new ArrayList<FlexOfferSlice>();

            double totalMinE=0, totalMaxE=0;
            for (int k = 0; k < maxSlices; k++) {
                double minE, maxE;

                if (EFmax > 0) {
                    minE = Emin + Math.random() * (Emax - Emin);
                    maxE = minE + Math.random() * EFmax;
                } else {
                    maxE = Emin + Math.random() * (Emax - Emin);
                    minE = maxE + Math.random() * EFmax;
                }

                totalMinE += minE;
                totalMaxE += maxE;
                sl.add(new FlexOfferSlice(1, 1, minE, maxE));
            }

            f.setSlices(sl.toArray(new FlexOfferSlice[] {}));
            /*
            double perc = 0.5; // Math.random();
            
            if ( i % 2 == 0){
	            f.setTotalEnergyConstraint(new FlexOfferConstraint(totalMinE + (totalMaxE - totalMinE)*perc, 
	            		                                           totalMinE + (totalMaxE - totalMinE)*perc));
            }*/

            // f.setDefaultSchedule(new FlexOfferSchedule(f)); /* Set default schedule */

            if (schedule) {
                f.setFlexOfferSchedule(new FlexOfferSchedule(f));
            }
            ;
            this.agg.addSimpleFlexOffer(f);
        }
    }

    public void demoClearFOs() {
        this.getAggregator().deleteSimpleFlexOffers();
    }

    @Override
    public void onFlexOfferCreate(FlexOffer fo) {
        // TODO Auto-generated method stub

    }
    
    public void schedule() throws FlexOfferException {
        for (FlexOffer aggFo : this.agg.getFlexOffers())
            this.agg.createFlexOfferSchedule(aggFo.getId(), new FlexOfferSchedule(aggFo));
    }
    
    public void addHttpClientId(String id) {
        this.httpClients.add(id);
    }

}
