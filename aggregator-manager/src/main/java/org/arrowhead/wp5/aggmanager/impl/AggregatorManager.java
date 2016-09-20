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
import org.arrowhead.wp5.core.interfaces.FlexOfferUpdateListener;
import org.arrowhead.wp5.core.services.AggServiceManager;
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

    private HOXTWrapper hoxtManager;

    private static final String id = "aggregator";
    private static final String marketId = "market";
    private String password = "XXXXX";
    //	private static final String id = "schneider-agg";
    //	private static final String id = "mondragon-agg";
    private static final String XMPPhostname = "XXXXX.dpt.cs.aau.dk";
    private static final int XMPPport = 5222;

    private XMPPTCPConnectionConfiguration config;

    private static final boolean ARROWHEAD_COMPLIANT = false;
    private static final boolean CONNECT_TO_MARKET = false;

    private XFlexOfferProviderClient xFOProvider;
    private XMarketProviderClient xMProvider;

    private Aggregator agg;

    /* Local HTTP server */
    private static final String BASE_URI_ADDRESS = "http://0.0.0.0/";
    private HttpServer server;
    private final static int serverPort = 9998;
    private final static URI BASE_URI = UriBuilder.fromUri(BASE_URI_ADDRESS)
            .port(serverPort).path("api").build();

    /* Resouce managers/configs */
    private ResourceConfig resourceConfig;
    private ConsoleRedirectionApp consoleRedirectionApp;

    /* Arrowhead Subsystem */
    //	private ArrowheadSubsystem arrowheadSubsystem;
    private AggServiceManager aggServiceManager;

    //	private List<HashMap<String, String>> producersList;
    //	private OrchestrationConfig orchestrationConfig; 
    
    /* Set of http client ids for which schedule should not be sent through XMPP */
    private HashSet<String> httpClients;

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
            @SuppressWarnings("unused")
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

        /* Initializes Arrowhead Subsystem */
        //		this.arrowheadSubsystem = new ArrowheadSubsystem(agg);
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

        System.setProperty("javax.net.ssl.trustStore",
                "resources/clientstore.jks");
        config = XMPPTCPConnectionConfiguration
                .builder()
                .setUsernameAndPassword(id, password)
                .setServiceName("XXXXX")
                .setHost(XMPPhostname)
                .setResource("demo")
                .setKeystorePath("resources/clientstore.jks")
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
        xFOProvider = new XFlexOfferProviderClient(
                hoxtManager);
        if (CONNECT_TO_MARKET) {
            xMProvider = new XMarketProviderClient(marketId, hoxtManager);
            MarketInfo info;
            try {
                info = xMProvider.getInfo();

                logger.debug("Area: {}", info.getArea());
                logger.debug("Interval: {}", info.getInterval());
                logger.debug("Next Period: {}", info.getNextPeriod());

                /* bidSupplyUp(10, 20);
                bidSupplyDown(15, 25); */
            } catch (MarketException e) {
                e.printStackTrace();
            }
        }
    }

    //	public void bidSupplyUp(long price, long quantity) {
    //		try {
    //			xMProvider.bidSupply(bid(price, quantity, true));
    //		} catch (MarketException e) {
    //			e.printStackTrace();
    //		}
    //	}
    //	
    //	public void bidSupplyDown(long price, long quantity) {
    //		try {
    //			xMProvider.bidSupply(bid(price, quantity, false));
    //		} catch (MarketException e) {
    //			e.printStackTrace();
    //		}
    //	}
    //
    //	private AtomicInteger aint = new AtomicInteger();
    //	private Bid bid(long price, long quantity, boolean isUp) {
    //		return new Bid(price, quantity, isUp, id, "bid" + aint.getAndIncrement());
    //	}

//    public void bidMaketFO(MarketFlexOffer mFo) {
//        this.agg.addMarketFlexOffer(mFo);
//        try {
//            xMProvider.bidSupply(mFo.getAsBid());
//        } catch (MarketException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void acceptBid(Bid bid) {
//        logger.debug("received accepted bid: p: {} wp: {} q: {} wq: {}", bid.getPrice(), bid.getWinPrice(), bid.getQuantity(), bid.getWinQuantity());
//        logger.debug("  o: {} isUp: {} id: {}", bid.getOwner(), bid.isUp(), bid.getId());
//
//        this.agg.setWinningMarketFlexOffers(bid.getOwner(), bid.getId(), bid.getWinPrice(), new double[] { bid.getWinQuantity() });
//    }

    @Override
    public void start() throws ArrowheadException {

        //		if (ARROWHEAD_COMPLIANT) {
        //			this.arrowheadSubsystem.init();
        //			
        //			this.configureArrowheadCompliantApp();
        //		}

        try {
            /* Start the local HTTP server */
            initHttpServer();

            /* Initialize XMPP connection */
            initXmpp();

            if (ARROWHEAD_COMPLIANT) {
                //				this.arrowheadSubsystem.init();
                this.aggServiceManager = new AggServiceManager();
                this.aggServiceManager.publishXMPP((String) config.getUsername(), XMPPhostname, XMPPport, config.getResource());
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
        if (fo.getFlexOfferSchedule() != null && !httpClients.contains(fo.getOfferedById())) {
            try {
                xFOProvider.setSubscriberId(fo.getOfferedById());
                xFOProvider.createFlexOfferSchedule(fo.getId(),
                        fo.getFlexOfferSchedule());
            } catch (FlexOfferException e) {
                // TODO Need to handle this error based on HTTP return code if
                // any
                e.printStackTrace();
            }
            
        }

    }
    
    public void demoCommit() {
    	long dateFrom = this.agg.getFlexOffers()[0].getStartAfterInterval();
    	long dateTo = dateFrom + 1;
    	
        BidV2 bid = this.agg.generate_maketV2_bid(dateFrom, dateTo);
        
        MarketCommitment mc = new MarketCommitment();
        mc.setLocation(0);
        mc.setWinning_bid(bid);
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
            long mEST = (long) (mFrom + 15 * Math.random() * (mTo - mFrom));
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

            for (int k = 0; k < maxSlices; k++) {
                double minE, maxE;

                if (EFmax > 0) {
                    minE = Emin + Math.random() * (Emax - Emin);
                    maxE = minE + Math.random() * EFmax;
                } else {
                    maxE = Emin + Math.random() * (Emax - Emin);
                    minE = maxE + Math.random() * EFmax;
                }

                sl.add(new FlexOfferSlice(1, 1, minE, maxE));
            }

            f.setSlices(sl.toArray(new FlexOfferSlice[] {}));

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

    /*
     * Configure Arrowhead compliant application
     */
    //	public void configureArrowheadCompliantApp() throws ArrowheadException
    //	{
    //		List<String> producersDiscovered = new ArrayList<>();
    //		List<String> orchestratedProducers = new ArrayList<>();
    //		
    //		// get all producers from Service Discovery
    //		this.producersList = this.arrowheadSubsystem.lookupServiceProducers(FOAgentServiceTypes.XMPP_FOA_SECURE);
    //		
    //		for (int i = 0; i < this.producersList.size(); i++)
    //		{
    //			producersDiscovered.add(this.producersList.get(i).get("name"));
    //		}
    //		
    //		System.out.println(producersDiscovered);
    //		
    //		// get active configuration
    //		this.orchestrationConfig = this.arrowheadSubsystem.checkActiveConfiguration();
    //		
    //		for (int i = 0; i < this.orchestrationConfig.getRules().size(); i++)
    //		{
    //			String orchestratedProducer = this.orchestrationConfig.getRules().get(i);
    //			orchestratedProducers.add(orchestratedProducer.split("\\.")[0]);
    //		}
    //		
    //		System.out.println(orchestratedProducers);
    //		
    //		producersDiscovered.retainAll(orchestratedProducers);
    //		
    //		System.out.println(producersDiscovered);
    //		
    //		if (!producersDiscovered.isEmpty())
    //		{
    //			Random randomGenerator = new Random();
    //			
    //			int index = randomGenerator.nextInt(producersDiscovered.size());
    //            this.xFOProvider.setSubscriberId(producersDiscovered.get(index));
    //		}
    //	}

    public void schedule() throws FlexOfferException {
        for (FlexOffer aggFo : this.agg.getFlexOffers())
            this.agg.createFlexOfferSchedule(aggFo.getId(), new FlexOfferSchedule(aggFo));
    }
    
    public void addHttpClientId(String id) {
        this.httpClients.add(id);
    }

}
