package org.arrowhead.wp5.aggtesttool;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;

import javax.ws.rs.core.UriBuilder;

import org.arrowhead.wp5.aggtesttool.resources.FlexOfferResource;
import org.arrowhead.wp5.aggtesttool.xmppresources.XFlexOfferResource;
import org.arrowhead.wp5.com.xmpp.api.HOXTWrapper;
import org.arrowhead.wp5.com.xmpp.api.ResourceManager;
import org.arrowhead.wp5.com.xmpp.clients.flexofferclient.XFlexOfferProviderClient;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferException;
import org.arrowhead.wp5.core.entities.FlexOfferSchedule;
import org.arrowhead.wp5.core.entities.FlexOfferState;
import org.arrowhead.wp5.core.services.AggServiceManager;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bnearit.arrowhead.common.core.service.discovery.exception.ServiceRegisterException;

public class AggregatorTestTool {

    private static final Logger logger = LoggerFactory.getLogger(AggregatorTestTool.class);
    private Properties properties = new Properties();

    private final String id;
    private String password;
    private final String xmppHostname;
    private final String xmppResource;
    private final int xmppPort;
    
    private final boolean ARROWHEAD_COMPLIANT;

    private XMPPTCPConnectionConfiguration config;
    private AggServiceManager aggServiceManager;
    private HOXTWrapper hoxtManager;
    private XFlexOfferProviderClient xFOProvider;

    private Map<String, Map<Integer, FlexOffer>> flexOffers;
    
    /* Local HTTP server */
    private final String BASE_URI_ADDRESS;
    private HttpServer server;
    private final int serverPort;
    private final String httpHostname;
    private final String httpPath;

    private ResourceConfig resourceConfig;
    
    private HashSet<String> httpClients = new HashSet<String>();

    // Loading defaults
    {
        loadProperties();
        
        BASE_URI_ADDRESS = properties.getProperty("foagg.base-uri-address", "http://0.0.0.0/").trim();
        int port = 9998;
        try {
            port = Integer.parseInt(properties.getProperty("foagg.serverPort", Integer.toString(port)));
        } catch (NumberFormatException e) {
        }
        serverPort = port;
        httpHostname = properties.getProperty("foagg.http-hostname", "localhost").trim();
        httpPath = properties.getProperty("foagg.http-path", "api").trim();
        id = properties.getProperty("foagg.id", "wrong").trim();
        password = properties.getProperty("foagg.password", "XXXXX").trim();
        ARROWHEAD_COMPLIANT = Boolean.parseBoolean(properties.getProperty("foagg.arrowhead-compliant", "false"));
        xmppHostname = properties.getProperty("foagg.xmpp-server", "XXXXX.dpt.cs.aau.dk").trim();
        xmppResource = properties.getProperty("foagg.xmpp-resource", "demo").trim();
        port = 5222;
        try {
            port = Integer.parseInt(properties.getProperty("fom.xmpp-port", Integer.toString(port)));
        } catch (NumberFormatException e) {
        }
        xmppPort = port;
    }
    
    private final URI BASE_URI = UriBuilder.fromUri(BASE_URI_ADDRESS)
            .port(serverPort).path(httpPath).build();

    private void loadProperties() {
        String filename = System.getProperty("foagg.configfile", "aggregator-testtool.properties");
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(filename));
            properties = props;
        } catch (FileNotFoundException e) {
            logger.info("Unable to locate configuration file, using defaults.");
        } catch (IOException e) {
            logger.error("Failed to read property file {}.", filename, e);
        }
    }
    
    public static void main(String[] args) {
        new AggregatorTestTool();
        while (true) {
            try {
                Thread.sleep(10000000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public AggregatorTestTool() {

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (ARROWHEAD_COMPLIANT && AggregatorTestTool.this.aggServiceManager != null) {
                    AggregatorTestTool.this.aggServiceManager.shutdown();
                }
            }
        }));

        flexOffers = new HashMap<String, Map<Integer, FlexOffer>>();
        try {
            /* Start the local HTTP server */
            //            initHttpServer();

            /* Initialize XMPP connection */
            initXmpp();
            try {
                initHttpServer();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (ARROWHEAD_COMPLIANT) {
                this.aggServiceManager = new AggServiceManager();
                this.aggServiceManager.publishXMPP((String) config.getUsername(), xmppHostname, xmppPort, config.getResource());
                this.aggServiceManager.publishHTTP((String) config.getUsername(), httpHostname, serverPort, httpPath);
            }

            /* Print a welcome message */
            logger.info("Test tool started!");

            //        } catch (IOException e) {
            //            e.printStackTrace();
        } catch (XMPPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ServiceRegisterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private void initHttpServer() throws IOException {
        /* Initialize the local HTTP server */
        this.resourceConfig = new ResourceConfig();
        // resourceConfig.registerInstances(new
        // LoggingFilter(Logger.getLogger(FlexOfferManager.class.getName()),
        // true));
        this.resourceConfig
                        .register(org.arrowhead.wp5.com.filter.CORSFilter.class);
        /* Register instances */
        this.resourceConfig.registerInstances(new FlexOfferResource(this));
        // Manually register the JSON feature
        this.resourceConfig.register(MoxyJsonFeature.class);
        this.server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI,
                resourceConfig, false);
        
        

        this.server.start();
    }

    private void initXmpp() throws XMPPException {
        /* Initializes XMPP connection */
        //          new JHades().overlappingJarsReport();

        System.setProperty("javax.net.ssl.trustStore",
                "resources/clientstore.jks");
        config = XMPPTCPConnectionConfiguration
                .builder()
                .setUsernameAndPassword(id, password)
                .setServiceName("XXXXX")
                .setHost(xmppHostname)
                .setResource(xmppResource)
                .setKeystorePath("resources/clientstore.jks")
                .setSecurityMode(SecurityMode.required)
                .setCompressionEnabled(false)
                .build();
        ResourceManager resourceManager = new ResourceManager();
        resourceManager.registerInstance(new XFlexOfferResource(this));
        //
        //        /* Start the XMPP server */
        try {
            hoxtManager = new HOXTWrapper(config, resourceManager);
            hoxtManager.init(true);
        } catch (SmackException | IOException | InterruptedException e1) {
            e1.printStackTrace();
        }
        xFOProvider = new XFlexOfferProviderClient(hoxtManager);
    }

    public void createFlexOffer(String ownerId, FlexOffer flexOffer) throws FlexOfferException {
        logger.info("Received new FlexOffer. Owner: {} - OfferedBy: {} - ID: {}", ownerId, flexOffer.getOfferedById(), flexOffer.getId());

        /* Assign a default schedule (baseline) if not set */
        // TODO: remove "|| flexOffer.getDefaultSchedule().getEnergyAmounts() == null" when the XML bind error has been fixed.
        if (flexOffer.getDefaultSchedule() == null || flexOffer.getDefaultSchedule().getEnergyAmounts() == null) {
            FlexOfferSchedule ds = new FlexOfferSchedule();
            // By default, a user aims starting ASAP
            ds.setStartInterval(flexOffer.getStartAfterInterval());
            // By default, a user consumes average
            double[] amounts = new double[flexOffer.getSlices().length];
            for (int i = 0; i < flexOffer.getSlices().length; i++) {
                amounts[i] = flexOffer.getSlice(i).getEnergyLower() + 0.5 * (flexOffer.getSlice(i).getEnergyUpper() -
                        flexOffer.getSlice(i).getEnergyLower());
            }
            ds.setEnergyAmounts(amounts);
            flexOffer.setDefaultSchedule(ds);
        }
        
        if (flexOffer.getFlexOfferSchedule() != null && flexOffer.getFlexOfferSchedule().getEnergyAmounts() == null) {
            logger.warn("getFlexOfferSchedule is not null, but has no energy amounts!");
            FlexOfferState s = flexOffer.getState();
            flexOffer.setFlexOfferSchedule(null);
            flexOffer.setState(s);
        }
        
        if (!ownerId.equals(flexOffer.getOfferedById())) {
            throw new FlexOfferException("ownerId and offeredBy are not equal!");
        }
        if (!flexOffer.isCorrect()) {
            logger.warn("FlexOffer is not correct!");
            // TODO: isCorrect is not completely implemented!
            throw new FlexOfferException("FlexOffer is not correct!");
        }

        Date now = new Date();
        if (flexOffer.getStartBeforeTime().before(now)) {
            throw new FlexOfferException("StartBeforeTime is before now!");
        }
        if (flexOffer.getAcceptanceBeforeInterval() != 0 && flexOffer.getAcceptanceBeforeTime().before(now)) {
            throw new FlexOfferException("AcceptanceBeforeTime is before now!");
        }
        if (flexOffer.getAssignmentBeforeInterval() != 0 && flexOffer.getAssignmentBeforeTime().before(now)) {
            throw new FlexOfferException("AssignmentBeforeTime is before now!");
        }
        //        if (flexOffer.getAssignmentBeforeDurationIntervals() != 0 && flexOffer.getAssignmentBeforeDurationSeconds().before(now)) {
        //            throw new FlexOfferException("AssignmentBeforeDurationTime is before now!");
        //        }

        // TODO: remove this when the XML bind error has been fixed.
        if (flexOffer.getFlexOfferSchedule() != null && flexOffer.getFlexOfferSchedule().getEnergyAmounts() == null) {
            flexOffer.setFlexOfferSchedule(null);
            flexOffer.setState(FlexOfferState.Initial);
        }

        /* By default, schedule flexoffers to follow default schedule */
        /*      if (flexOffer.getFlexOfferSchedule() == null) {
        flexOffer.setFlexOfferSchedule(new FlexOfferSchedule(flexOffer.getDefaultSchedule().getStartInterval(), 
                                                             flexOffer.getDefaultSchedule().getEnergyAmounts().clone()));       
        } */

        if (!flexOffers.containsKey(ownerId)) {
            flexOffers.put(ownerId, new HashMap<Integer, FlexOffer>());
        }
        Map<Integer, FlexOffer> current = flexOffers.get(ownerId);
//        if (current.containsKey(flexOffer.getId())) {
//            throw new FlexOfferException("FlexOffer already added! Can not overwrite FlexOffer!");
//        }
        current.put(flexOffer.getId(), flexOffer);
        
        Timer timer = new Timer();
        
        timer.schedule(new ScheduleTask(flexOffer) {
            
            @Override
            public void run() {
                double[] amounts = new double[flexOffer.getSlices().length];
                for (int i = 0; i < flexOffer.getSlices().length; i++) {
                    amounts[i] = flexOffer.getSlice(i).getEnergyLower() + 0.5 * (flexOffer.getSlice(i).getEnergyUpper() -
                            flexOffer.getSlice(i).getEnergyLower());
                }
                System.out.println("Assign schedule!!!");

                FlexOfferSchedule fos = new FlexOfferSchedule();
                fos.setEnergyAmounts(amounts);
                fos.setStartInterval(flexOffer.getStartAfterInterval());
                flexOffer.setFlexOfferSchedule(fos);
                
                if (!httpClients.contains(flexOffer.getOfferedById())) {
                    System.out.println("Send schedule!!!");
                    try {
                        xFOProvider.setSubscriberId(flexOffer.getOfferedById());
                        xFOProvider.createFlexOfferSchedule(flexOffer.getId(), flexOffer.getFlexOfferSchedule());
                    } catch (FlexOfferException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    System.out.println("Done sending.");
                }
            }
        }, 5*1*1000);
    }

    public FlexOffer getFlexOffer(String ownerId, int flexOfferId) {
        if (flexOffers.containsKey(ownerId)) {
            Map<Integer, FlexOffer> current = flexOffers.get(ownerId);
            return current.get(flexOfferId);
        }
        return null;
    }
    
    public void addToHttpClients(String id) {
        this.httpClients.add(id);
    }
}
