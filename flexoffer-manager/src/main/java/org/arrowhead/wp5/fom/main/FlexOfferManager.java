package org.arrowhead.wp5.fom.main;

/*-
 * #%L
 * ARROWHEAD::WP5::FlexOffer Manager
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


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import javax.ws.rs.core.UriBuilder;

import org.arrowhead.wp5.application.common.ConsoleRedirectionApp;
import org.arrowhead.wp5.application.common.StandAloneApp;
import org.arrowhead.wp5.com.xmpp.api.HOXTWrapper;
import org.arrowhead.wp5.com.xmpp.api.ResourceManager;
import org.arrowhead.wp5.com.xmpp.api.XmppConnectionDetails;
import org.arrowhead.wp5.com.xmpp.clients.flexofferclient.XFlexOfferSubscriberClient;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferException;
import org.arrowhead.wp5.core.entities.FlexOfferSlice;
import org.arrowhead.wp5.core.entities.FlexOfferState;
import org.arrowhead.wp5.core.impl.FlexOfferAgent;
import org.arrowhead.wp5.core.interfaces.FlexOfferUpdateListener;
import org.arrowhead.wp5.core.services.ArrowheadXMPPServiceManager;
import org.arrowhead.wp5.fom.resources.DERResource;
import org.arrowhead.wp5.fom.resources.FlexOfferAgentResource;
import org.arrowhead.wp5.fom.xresources.XFlexOfferResource;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketEngine;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.jhades.JHades;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlexOfferManager extends StandAloneApp implements
		FlexOfferUpdateListener {

	final Logger logger = LoggerFactory.getLogger(this.getClass());
	private Properties properties = new Properties();
	
	/** The base URI address for the web server interface (that makes UI available) */
	private final String BASE_URI_ADDRESS;
	/** Port for the http server, at which the UI will be available */
	private final int serverPort;
	/** The id of this flexoffer manager. Used both for the XMPP network and the uniqueness of flex offers */
	private String id;

	/** TODO this should be obtained through service discovery */
	private String aggId;

	/** The HTTP server */
	private HttpServer server;
	
	// Loading defaults
	{
		loadProperties();
		
		BASE_URI_ADDRESS = properties.getProperty("fom.base-uri-address", "http://0.0.0.0/").trim();
		int port = 9997;
		try {
			port = Integer.parseInt(properties.getProperty("fom.serverPort", Integer.toString(port)));
		} catch (NumberFormatException e) {
		}
		serverPort = port;
		id = properties.getProperty("fom.id", "wmfom").trim(); // "schneider-fom" "mondragon-fom"
		password = properties.getProperty("fom.password", "XXXXX").trim(); // "arrowhead"
		aggId = properties.getProperty("fom.aggId", "aggregator").trim(); // "schneider-agg" "mondragon-agg"
		ARROWHEAD_COMPLIANT = Boolean.parseBoolean(properties.getProperty("fom.arrowhead-compliant", "false"));
		xmppServer = properties.getProperty("fom.xmpp-server", "XXXXX.dpt.cs.aau.dk").trim();
		xmppResource = properties.getProperty("fom.xmpp-resource", "demo").trim();
	}
	
	/** Build the base URI for the HTTP server */
	private final URI BASE_URI = UriBuilder.fromUri(BASE_URI_ADDRESS).port(serverPort).path("api").build();
	/** Flag to activate/deactivate arrowhead connection */
	private final boolean ARROWHEAD_COMPLIANT;
	
	/**The password used to connect to the XMPP server*/
	private String password;
	/** Object that contains the information about the XMPP connection*/
	private XmppConnectionDetails xmppConDetails;
	/** XMPP Server Host*/
	private String xmppServer;
	/** XMPP resource */
	private String xmppResource;


	/** Resouce config for the HTTP server */
	private ResourceConfig resourceConfig;
	/** Used to make the console available from the UI */
	private ConsoleRedirectionApp consoleRedirectionApp;

	/** The object that encapsulate XMPP functionalities*/
	private HOXTWrapper hoxtWrapper;
	/** The object that provides the functionalities to send flexoffers to the aggregator*/
	private XFlexOfferSubscriberClient xfosc;

	/** Boolean determining if the Xmpp network is connected **/
	private boolean isXmppConnected = true;

	/** Encapsulated flex-offer agent */
	private FlexOfferAgent agent;

	/* Arrowhead Subsystem */

	private ArrowheadXMPPServiceManager foServiceManager;
//	private ArrowheadSubsystem arrowheadSubsystem;
//	private List<HashMap<String, String>> producersList;
//	private OrchestrationConfig orchestrationConfig; 
	
	public HttpServer getHTTPServer() {
		return this.server;
	}

	public ResourceConfig getResourceConfig() {
		return resourceConfig;
	}

	public FlexOfferAgent getFlexOfferAgent() {
		return this.agent;
	}

	public XmppConnectionDetails getXmppConnectionDetails() {
		return this.xmppConDetails;
	}

	public void setXmppConnectionDetails(XmppConnectionDetails conDetails) {
		this.xmppConDetails = conDetails;
	}

	public String getId() {
		return id;
	}

	public boolean isXmppConnected() {
		return this.isXmppConnected;
	}

	public void setXmppConnected(boolean con) {
		if (!isXmppConnected && con) {
			this.initXmpp();
			try {
				this.sendFOstoSubscriber();
			} catch (FlexOfferException e) {
				logger.error("Failed to send flexoffer", e);
			}
		} else if (isXmppConnected && !con) {
			this.deinitXmpp();
		}
	}

	public FlexOfferManager() throws Exception {
		super();

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				if (ARROWHEAD_COMPLIANT && FlexOfferManager.this.foServiceManager != null) {
					FlexOfferManager.this.foServiceManager.shutdown();
				}
			}
		}));

		logger.debug("id: {}", id);
		logger.debug("password: {}", password);
		logger.debug("resource: {}", xmppResource);
		logger.debug("xmppServer: {}", xmppServer);
		
		/* Initialize flex-offer agent */
		this.agent = new FlexOfferAgent(id, this);
		/* Initialize resource config */
		this.resourceConfig = new ResourceConfig();
		// resourceConfig.registerInstances(new
		// LoggingFilter(Logger.getLogger(FlexOfferManager.class.getName()),
		// true));
		this.resourceConfig
				.register(org.arrowhead.wp5.com.filter.CORSFilter.class);
		// Manually register the JSON feature
		this.resourceConfig.register(MoxyJsonFeature.class);

		/* Initializes Arrowhead Subsystem */
		this.foServiceManager = new ArrowheadXMPPServiceManager(properties);
//		this.arrowheadSubsystem = new ArrowheadSubsystem();

		this.xmppConDetails = new XmppConnectionDetails(
				xmppServer, id, password, xmppResource);

		/* Register instances */
		this.resourceConfig.registerInstances(new DERResource(this.agent),
				new FlexOfferAgentResource(this.agent, this));
	}

	public void initHttpServer() throws IOException {
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
		WebSocketEngine.getEngine().register("", "/console", consoleRedirectionApp);
		/* Add static resource handler - for the Management Console */
		this.server.getServerConfiguration().addHttpHandler(
				new CLStaticHttpHandler(
						FlexOfferManager.class.getClassLoader(), "app/"), "/");
		this.server.start();
	}

	public void initXmpp() {
		System.setProperty("javax.net.ssl.trustStore",
				"resources/clientstore.jks");
		ResourceManager resourceManager = new ResourceManager();
		resourceManager.registerInstance(new XFlexOfferResource(agent));
		XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration
				.builder()
				.setUsernameAndPassword(id, password)
				.setServiceName("XXXXXX")
				.setHost(xmppServer)
				.setKeystorePath("resources/clientstore.jks")
				.setSecurityMode(SecurityMode.required)
				.setResource(xmppResource)
				.setConnectTimeout(30000)
				.setCompressionEnabled(false).build();
		this.hoxtWrapper = new HOXTWrapper(config, resourceManager);
		try {
			this.hoxtWrapper.init(true);
		} catch (XMPPException | SmackException | IOException | InterruptedException e) {
			logger.error("Failed to initialize XMPP", e);
		}
		this.xfosc = new XFlexOfferSubscriberClient(aggId, hoxtWrapper);
	}

	public void deinitXmpp() {
		this.hoxtWrapper.destroy();
		this.xfosc = null;
	}

	@Override
	public void start() throws Exception {

		if (ARROWHEAD_COMPLIANT) {
			this.foServiceManager.start();
			
			if (this.foServiceManager.fetchInfo()) {
				aggId = this.foServiceManager.getAggId();
				xmppServer = this.foServiceManager.getHostname();
				xmppResource = this.foServiceManager.getResource();
				logger.info("Aggregator ID set from Arrowhead framework: {}", aggId);
			} else {
				logger.warn("Failed to discover aggregator through Arrowhead framework. Using default config");
			}
			
//			this.arrowheadSubsystem.init();
			
//			this.configureArrowheadCompliantApp();
		}
		
		initHttpServer();

		if (isXmppConnected) {
			/* Initializes XMPP connection */
			this.initXmpp();
		}

		/* Print a welcome message */
		System.out.println("The HTTP server has started. Open \""
				+ UriBuilder.fromUri(BASE_URI_ADDRESS).port(serverPort)
				+ "\" in your browser to see the FlexOfferManager GUI.");
	}

	private void loadProperties() {
		String filename = System.getProperty("fom.configfile", "flexoffer-manager.properties");
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
	
	@Override
	public void stop() {
		this.server.shutdownNow();
		// this.aggregator.disconnect();
		if (ARROWHEAD_COMPLIANT)
		{
			this.foServiceManager.shutdown();
		}
	}

	public static void main(String[] args) {
		try {
			new JHades()
			//.printClassLoaders()
			//.printClasspath()
			.overlappingJarsReport();
			//.multipleClassVersionsReport();
			(new FlexOfferManager()).run(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onFlexOfferScheduleUpdate(FlexOffer fo) {
		// TODO Auto-generated method stub

	}

	private static int foid = 0;
	public void test() throws FlexOfferException {
		FlexOffer fo = new FlexOffer();
		fo.setId(foid++);
		fo.setSlices(new FlexOfferSlice[]{new FlexOfferSlice(1, 1, 1, 4)});
		this.xfosc.createFlexOffer(id, fo);
	}

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

	/*
	 * Send all flex-offers in the initial state to the aggregator. To be used
	 * in the offline-mode
	 */
	private void sendFOstoSubscriber() throws FlexOfferException {
		for (FlexOffer fo : this.agent.getFlexOffers()) {
			if (fo.getState() == FlexOfferState.Initial) {
				// this.sendFOtoSubscriber(fo, null); /* Add new flex-offers */
				this.xfosc.createFlexOffer(id, fo);
			}
		}
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
//		this.producersList = this.arrowheadSubsystem.lookupServiceProducers(FOAggregatorServiceTypes.XMPP_FOAGG_SECURE);
//		
//		for (int i = 0; i < this.producersList.size(); i++)
//		{
//			producersDiscovered.add(this.producersList.get(i).get("name"));
//		}
//		
//		System.out.println("[Service Discovery] Producers Discovered: " + producersDiscovered);
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
//		System.out.println("[Orchestration] Orchestrated Service Producers: " + orchestratedProducers);
//		
//		producersDiscovered.retainAll(orchestratedProducers);
//		
//		System.out.println("Advised Service Producers: " + producersDiscovered);
//		
//		if (!producersDiscovered.isEmpty())
//		{
//			Random randomGenerator = new Random();
//			
//			int index = randomGenerator.nextInt(producersDiscovered.size());
//            this.aggId = producersDiscovered.get(index);
//            System.out.println("Chosen Service Producer: " + this.aggId);
//		}
//	}

	/*
	 * Send a single DER's flex-offer to a subscriber, subject to an old
	 * flex-offer if any
	 */
	// protected void sendFOtoSubscriber(FlexOffer newFo, FlexOffer oldFo)
	// throws FlexOfferException {
	// /* Do not pass flex-offers to the subscriber if in "offline" mode */
	// if (this.getMode() != FlexOfferAgentMode.fmOnlineActive)
	// return;
	//
	// if (this.getFoa_subscriber() == null)
	// throw new FlexOfferException(
	// "No FlexofferAgent service subscriber is set.");
	//
	// /* Check the insertion/deletion/updation cases */
	// if (newFo != null && oldFo == null) {
	// /* The insertion case */
	// newFo.setState(FlexOfferState.Offered);
	//
	// int subscriberFoId = this.getFoa_subscriber()
	// .createFlexOffer(this.id, newFo);
	// this.foa_subscriber_fo_ids.put(newFo.getId(), subscriberFoId);
	//
	// } else if (newFo == null && oldFo != null) {
	// /* The deletion case */
	// Integer subscriberFoId = this.foa_subscriber_fo_ids.get(oldFo
	// .getId());
	//
	// if (subscriberFoId != null) {
	// oldFo.setState(FlexOfferState.Initial);
	//
	// this.getFoa_subscriber().deleteFlexOffer(id,
	// subscriberFoId.intValue());
	// this.foa_subscriber_fo_ids.remove(oldFo.getId());
	// }
	//
	// } else if (newFo != null && oldFo != null) {
	// /* The updation case */
	// Integer subscriberFoId = this.foa_subscriber_fo_ids.get(oldFo
	// .getId());
	//
	// if (subscriberFoId != null) {
	// newFo.setState(FlexOfferState.Offered);
	//
	// this.getFoa_subscriber().setFlexOffer(id,
	// subscriberFoId.intValue(), newFo);
	// this.foa_subscriber_fo_ids.remove(oldFo.getId());
	// this.foa_subscriber_fo_ids.put(newFo.getId(),
	// subscriberFoId.intValue());
	// }
	// }
	// }
}