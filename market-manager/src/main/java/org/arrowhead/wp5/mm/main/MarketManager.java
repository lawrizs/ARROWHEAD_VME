package org.arrowhead.wp5.mm.main;

/*-
 * #%L
 * ARROWHEAD::WP5::Market Manager
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
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.core.UriBuilder;

import org.arrowhead.wp5.application.common.ConsoleRedirectionApp;
import org.arrowhead.wp5.application.common.StandAloneApp;
import org.arrowhead.wp5.com.xmpp.api.HOXTWrapper;
import org.arrowhead.wp5.com.xmpp.api.ResourceManager;
import org.arrowhead.wp5.com.xmpp.clients.marketclient.XMarketSubscriberClient;
import org.arrowhead.wp5.core.entities.Bid;
import org.arrowhead.wp5.core.entities.MarketException;
import org.arrowhead.wp5.market.impl.Market;
import org.arrowhead.wp5.mm.resources.MarketResource;
import org.arrowhead.wp5.mm.xresources.XMarketResource;
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

public class MarketManager extends StandAloneApp {

	final Logger logger = LoggerFactory.getLogger(this.getClass());
	private Properties properties = new Properties();

	// Loading defaults
	{
		loadProperties();

		BASE_URI_ADDRESS = properties.getProperty("mm.base-uri-address",
				"http://0.0.0.0/").trim();
		int port = 9996;
		try {
			port = Integer.parseInt(properties.getProperty("mm.serverPort",
					Integer.toString(port)));
		} catch (NumberFormatException e) {
		}
		serverPort = port;
		id = properties.getProperty("mm.id", "market").trim();
		password = properties.getProperty("mm.password", "XXXXX").trim();
		xmppServer = properties.getProperty("mm.xmpp-server",
				"XXXXX.dpt.cs.aau.dk").trim();
		xmppResource = properties.getProperty("mm.xmpp-resource", "demo")
				.trim();
	}

	public void bidDemand(long price, long quantity, boolean isUp,
			String owner, String id) {
		market.bidDemand(new Bid(price, quantity, isUp, owner, id));
	}

	public void bidSupply(long price, long quantity, boolean isUp,
			String owner, String id) {
		market.bidSupply(new Bid(price, quantity, isUp, owner, id));
	}

	private AtomicInteger aint = new AtomicInteger();

	public void demo() {
		market.bidDemand(new Bid(2500, 9000, false, "BRP1", "b"
				+ aint.getAndIncrement()));
		market.bidDemand(new Bid(2100, 8000, false, "BRP3", "b"
				+ aint.getAndIncrement()));
		market.bidDemand(new Bid(8000, 6000, false, "BRP2", "b"
				+ aint.getAndIncrement()));
		market.bidDemand(new Bid(5000, 6000, false, "DSO1", "b"
				+ aint.getAndIncrement()));
		market.bidDemand(new Bid(1300, 12000, false, "DSO2", "b"
				+ aint.getAndIncrement()));
		market.bidDemand(new Bid(1100, 14000, false, "DSO2", "b"
				+ aint.getAndIncrement()));
		market.bidSupply(new Bid(1000, 7000, false, "Prosumer", "b"
				+ aint.getAndIncrement()));
		market.bidSupply(new Bid(1400, 8000, false, "Prosumer", "b"
				+ aint.getAndIncrement()));
		market.bidSupply(new Bid(8000, 8000, false, "Prosumer", "b"
				+ aint.getAndIncrement()));
		market.bidSupply(new Bid(8000, 8000, false, "Prosumer", "b"
				+ aint.getAndIncrement()));
		market.bidSupply(new Bid(8000, 8000, false, "Prosumer", "b"
				+ aint.getAndIncrement()));
		market.bidSupply(new Bid(1500, 8000, false, "Prosumer", "b"
				+ aint.getAndIncrement()));

		market.bidDemand(new Bid(60, 60000, true, "DSO1", "b"
				+ aint.getAndIncrement()));
		market.bidDemand(new Bid(150, 60000, true, "BRP2", "b"
				+ aint.getAndIncrement()));

		market.bidSupply(new Bid(0, 0, true, "Prosumer", "b"
				+ aint.getAndIncrement()));
		market.bidSupply(new Bid(100, 8000, true, "Prosumer", "b"
				+ aint.getAndIncrement()));
		market.bidSupply(new Bid(140, 12000, true, "Prosumer", "b"
				+ aint.getAndIncrement()));
		market.bidSupply(new Bid(160, 21000, true, "Prosumer", "b"
				+ aint.getAndIncrement()));
		market.bidSupply(new Bid(140, 5000, true, "Prosumer", "b"
				+ aint.getAndIncrement()));
		market.bidSupply(new Bid(80, 7000, true, "Prosumer", "b"
				+ aint.getAndIncrement()));
		market.bidSupply(new Bid(0, 0, true, "Prosumer", "b"
				+ aint.getAndIncrement()));
		market.bidSupply(new Bid(150, 7800, true, "Prosumer", "b"
				+ aint.getAndIncrement()));
		market.bidSupply(new Bid(120, 11000, true, "Prosumer", "b"
				+ aint.getAndIncrement()));
		market.bidSupply(new Bid(140, 6500, true, "Prosumer", "b"
				+ aint.getAndIncrement()));
		market.bidSupply(new Bid(160, 4000, true, "Prosumer", "b"
				+ aint.getAndIncrement()));
		market.bidSupply(new Bid(140, 4000, true, "Prosumer", "b"
				+ aint.getAndIncrement()));
		market.bidSupply(new Bid(160, 8000, true, "Prosumer", "b"
				+ aint.getAndIncrement()));
		market.bidSupply(new Bid(180, 3400, true, "Prosumer", "b"
				+ aint.getAndIncrement()));
		market.bidSupply(new Bid(0, 0, true, "Prosumer", "b"
				+ aint.getAndIncrement()));

	}

	/**
	 * The id of this market manager. Used both for the XMPP network and the
	 * uniqueness
	 */
	private String id;
	/** The password used to connect to the XMPP server */
	private String password;
	/** XMPP Server Host */
	private String xmppServer;
	/** XMPP resource */
	private String xmppResource;
	/** The HTTP server */
	private HttpServer server;
	/**
	 * The base URI address for the web server interface (that makes UI
	 * available)
	 */
	private final String BASE_URI_ADDRESS;
	/** Port for the http server, at which the UI will be available */
	private final int serverPort;
	/** Build the base URI for the HTTP server */
	private final URI BASE_URI = UriBuilder.fromUri(BASE_URI_ADDRESS)
			.port(serverPort).path("api").build();
	/** Resouce config for the HTTP server */
	private ResourceConfig resourceConfig;
	/** Used to make the console available from the UI */
	private ConsoleRedirectionApp consoleRedirectionApp;

	private Market market;
	private HOXTWrapper hoxtManager;
	private XMarketSubscriberClient xMarketSubscriber;

	private void loadProperties() {
		String filename = System.getProperty("fom.configfile",
				"flexoffer-manager.properties");
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

	public MarketManager() throws Exception {
		super();

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO: Do cleanup
			}
		}));

		this.market = new Market();

		/* Initialize resource config */
		this.resourceConfig = new ResourceConfig();
		this.resourceConfig
				.register(org.arrowhead.wp5.com.filter.CORSFilter.class);
		// Manually register the JSON feature
		this.resourceConfig.register(MoxyJsonFeature.class);
		/* Register instances */
		this.resourceConfig.registerInstances(new MarketResource(market, this));
	}

	@Override
	public void start() throws Exception {
		this.initXmpp();

		this.initHttpServer();
	}

	public static void main(String[] args) {
		try {
			new JHades()
			// .printClassLoaders()
			// .printClasspath()
					.overlappingJarsReport();
			(new MarketManager()).run(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initXmpp() {
		System.setProperty("javax.net.ssl.trustStore",
				"resources/clientstore.jks");
		ResourceManager resourceManager = new ResourceManager();
		resourceManager.registerInstance(new XMarketResource(market));
		XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration
				.builder()
				.setUsernameAndPassword(id, password)
				.setServiceName("XXXXX")
				.setHost(xmppServer)
				.setKeystorePath("resources/clientstore.jks")
				.setSecurityMode(SecurityMode.required)
				.setResource(xmppResource)
				.setCompressionEnabled(false)
				.build();

		try {
			this.hoxtManager = new HOXTWrapper(config, resourceManager);
			this.hoxtManager.init(true);
		} catch (XMPPException | SmackException | IOException
				| InterruptedException e) {
			logger.error("Failed to initialize XMPP", e);
		}
		this.xMarketSubscriber = new XMarketSubscriberClient(hoxtManager);
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
		WebSocketEngine.getEngine().register("", "/console",
				consoleRedirectionApp);
		/* Add static resource handler - for the Management Console */
		this.server.getServerConfiguration().addHttpHandler(
				new CLStaticHttpHandler(MarketManager.class.getClassLoader(),
						"app/"), "/");
		this.server.start();
	}

	public void sendAcceptedBids() {
		sendAcceptedBids(market.getWinDemandDown());
		sendAcceptedBids(market.getWinDemandUp());
		sendAcceptedBids(market.getWinSupplyDown());
		sendAcceptedBids(market.getWinSupplyUp());
	}

	private void sendAcceptedBids(List<Bid> bids) {
		for (Bid bid : bids) {
			try {
				xMarketSubscriber.acceptBid(bid);
			} catch (MarketException e) {
				logger.debug("Unable to send accepted bid: {}", e.getMessage());
			}
		}
	}

	public String getId() {
		return id;
	}
}
