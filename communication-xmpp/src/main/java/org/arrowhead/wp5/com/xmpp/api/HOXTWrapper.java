package org.arrowhead.wp5.com.xmpp.api;

/*-
 * #%L
 * ARROWHEAD::WP5::Communication XMPP
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.arrowhead.wp5.com.xmpp.exceptions.HOXTException;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.hoxt.packet.AbstractHttpOverXmpp;
import org.jivesoftware.smackx.hoxt.packet.HttpOverXmppReq;
import org.jivesoftware.smackx.hoxt.packet.HttpOverXmppResp;
import org.jivesoftware.smackx.hoxt.provider.HttpOverXmppReqProvider;
import org.jivesoftware.smackx.hoxt.provider.HttpOverXmppRespProvider;
import org.jivesoftware.smackx.shim.packet.Header;
import org.jivesoftware.smackx.shim.packet.HeadersExtension;
import org.jxmpp.stringprep.XmppStringprepException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object that enables sending and receiving HTTP requests, and answering to
 * them
 * 
 * @author Thibaut Le Guilly
 *
 */
public class HOXTWrapper {
    private static final Logger logger = LoggerFactory.getLogger(HOXTWrapper.class);

    @SuppressWarnings("unused")
    private static final int packetReplyTimeout = 30000; // millis

    /**
     * Concurrent hashmap to stored currently pending requests.
     */
    private ConcurrentHashMap<String, AbstractHttpOverXmpp> reqMap = new ConcurrentHashMap<String, AbstractHttpOverXmpp>();

    /**
     * Used to generate id for created requests
     */
    private AtomicInteger reqId = new AtomicInteger(0);

    /**
     * Smack API class
     */
    private AbstractXMPPConnection connection;
    private XMPPTCPConnectionConfiguration conConfig;

    /**
     * Manages jax.rs resources if used.
     */
    private ResourceManager resourceManager;

    /**
     * Constructor
     * 
     * @param server
     *            The XMPP server address to connect to.
     * @param username
     *            The username corresponding to an existing user on the server
     * @param password
     *            The password corresponding to the given user
     * @param resource
     *            The name of the resource used for this connection (e.g.
     *            user@server/resource)
     * @param requestListener
     *            An object implementing the RequestListener interface that will
     *            be notified of incoming requests
     */
    public HOXTWrapper(XMPPTCPConnectionConfiguration conConfig,
            ResourceManager resourceManager) {
        this.conConfig = conConfig;
        this.resourceManager = resourceManager;
    }

    public static void addRequired(AbstractHttpOverXmpp req, boolean addData) {
        List<Header> set = new ArrayList<Header>();
        set.add(new Header("Content-Type", "application/text"));
        req.setHeaders(new HeadersExtension(set));
        req.setVersion("1.1");
        if (addData) {
            AbstractHttpOverXmpp.Text child = new AbstractHttpOverXmpp.Text("test");
            AbstractHttpOverXmpp.Data data = new AbstractHttpOverXmpp.Data(child);
            req.setData(data);
        }
    }

    private void initClient() {
        ProviderManager.addIQProvider("resp", "urn:xmpp:http",
                new HttpOverXmppRespProvider());
        StanzaFilter resFilter = new StanzaFilter() {
            public boolean accept(Stanza packet) {
                if (packet instanceof HttpOverXmppResp) {
                    return true;
                }
                return false;
            }
        };
        connection.addAsyncStanzaListener(new XmppResListener(), resFilter);
    }

    private void initServer() {
        ProviderManager.addIQProvider("req", "urn:xmpp:http",
                new HttpOverXmppReqProvider());
        StanzaFilter reqFilter = new StanzaFilter() {
            public boolean accept(Stanza packet) {
                if (packet instanceof HttpOverXmppReq) {
                    return true;
                }
                return false;
            }
        };
        connection.addAsyncStanzaListener(new XmppReqListener(), reqFilter);
    }

    /**
     * Initialize the XMPP manager, first function to call after constructor
     * 
     * @throws XMPPException
     * @throws IOException
     * @throws SmackException
     * @throws InterruptedException
     */
    public void init(boolean initClient) throws XMPPException, SmackException,
            IOException, InterruptedException {

        logger.info("Initializing connection to server {}",
                conConfig.getServiceName());
        // + conConfig.getXMPPServiceDomain()));

        // SmackConfiguration.setDefaultPacketReplyTimeout(packetReplyTimeout);
        connection = new XMPPTCPConnection(conConfig);
        connection.setPacketReplyTimeout(30000);
        connection.connect();
        ReconnectionManager.getInstanceFor(connection).enableAutomaticReconnection();
        /* To avoid exception when looking for Roster */
        Roster.getInstanceFor(connection).setRosterLoadedAtLogin(false);

        connection.login();
        logger.info("Connected: {}", connection.isConnected());

        if (SmackConfiguration.DEBUG == true) {
            ServiceDiscoveryManager discoManager = ServiceDiscoveryManager
                    .getInstanceFor(connection);
            // Get the information of a given XMPP entity
            DiscoverInfo discoInfo = discoManager.discoverInfo(this.connection
                    .getUser());
            // Check if room is HOXT is supported
            logger.info("Test hoxt supported: {}",
                    discoInfo.containsFeature("urn:xmpp:http"));
        }

        if (resourceManager != null) {
            initServer();
        }
        if (initClient) {
            initClient();
        }
    }

    public void destroy() {
        if (connection != null && connection.isConnected()) {
            connection.disconnect();
        }
    }

    public boolean isConnected() {
        return (connection != null) && (connection.isConnected());
    }

    /**
     * Send an HTTP request to the node specified by dest (e.g.
     * user@server/resource)
     * 
     * @param request
     * @param dest
     * @return
     * @throws HOXTException
     * @throws NotConnectedException
     * @throws XmppStringprepException
     */
    public HttpOverXmppResp sendRequest(HttpOverXmppReq request, String dest)
            throws HOXTException, NotConnectedException,
            XmppStringprepException {
        /** Reset the id value in case of overflow */
        if (reqId.get() < 0) {
            reqId.set(0);
        }
        String id = Integer.toString(reqId.incrementAndGet());
        try {
            /** TODO use the timed out version poll **/
            synchronized (reqMap) {
                reqMap.put(id, request);
            }

            request.setStanzaId(id);
            request.setTo(dest);
            request.setFrom(id);
            // request.setTo(JidCreate.from(dest));
            // request.setFrom(JidCreate.from(id));
            /* Need to set version otherwise NullPointerException */
            request.setVersion("1.1");
            logger.info(connection.getUser());
            connection.sendStanza(request);
            synchronized (request) {
                /** TODO set a time out */
                // request.wait(5000);
                request.wait();
            }
        } catch (InterruptedException e) {
            synchronized (reqMap) {
                reqMap.remove(id);
                throw new HOXTException("Request was not performed");
            }
        }

        AbstractHttpOverXmpp tmp = reqMap.get(id);
        if (!(tmp instanceof HttpOverXmppResp)) {
            throw new HOXTException("No response received or error");
        }
        HttpOverXmppResp response = (HttpOverXmppResp) reqMap.get(id);
        if (response == null) {
            throw new HOXTException("Destination " + dest + " not available");
        }

        if (response.getStatusCode() == 404) {
            throw new HOXTException("404 - Resource not foud on server.");
        }

        return response;
    }

    /**
     * Respond to an HTTP request
     * 
     * @param response
     * @param dest
     * @param packetId
     * @throws NotConnectedException
     * @throws InterruptedException
     * @throws XmppStringprepException
     */
    private void sendResponse(HttpOverXmppResp response, String dest,
            String packetId) throws NotConnectedException,
            InterruptedException, XmppStringprepException {
        response.setStanzaId(packetId);
        response.setTo(dest);
        // response.setTo(JidCreate.from(dest));

        response.setFrom(this.connection.getUser());
        /* Need to set version otherwise NullPointerException */
        response.setVersion("1.1");
        /* Need to set header, otherwise it crashes */
        /* Cannot use Set as in documentation, HeadersExtension won't accept it */
        List<Header> set = new ArrayList<Header>();
        set.add(new Header("Host", "test.com"));
        response.setHeaders(new HeadersExtension(set));
        connection.sendStanza(response);
    }

    private void handleRequestError(HttpOverXmppReq request) {
        synchronized (reqMap) {
            request = (HttpOverXmppReq) reqMap.get(request.getStanzaId());
        }

        synchronized (request) {
            request.notify();
        }
    }

    /**
     * Handles a HttpOverXmppReq
     * 
     * @param packet
     * @throws NotConnectedException
     * @throws InterruptedException
     * @throws XmppStringprepException
     */
    private void handleRequest(Stanza packet) throws NotConnectedException,
            InterruptedException, XmppStringprepException {
        HttpOverXmppReq request = (HttpOverXmppReq) packet;
        if (request.getError() != null) {
            handleRequestError(request);
        }
        HttpOverXmppResp res = RequestHandler.handleRequest(request,
                resourceManager);
        this.sendResponse(res, request.getFrom().toString(),
                request.getStanzaId());
    }

    /**
     * Handles a HttpOverXmppResp
     * 
     * @param packet
     */
    private void handleResponse(Stanza packet) {
        HttpOverXmppResp response = (HttpOverXmppResp) packet;
        HttpOverXmppReq request;
        synchronized (reqMap) {
            request = (HttpOverXmppReq) reqMap.get(response.getStanzaId());
            reqMap.put(request.getStanzaId(), response);
        }

        synchronized (request) {
            request.notify();
        }

    }

    /**
     * Sub-class to handle incoming packet corresponding to HTTP requests or
     * responses
     *
     */
    class XmppReqListener implements StanzaListener {

        @Override
        public void processPacket(Stanza packet) {
            try {
                HOXTWrapper.this.handleRequest(packet);
            } catch (NotConnectedException e) {
                // This method cannot be called if not connected
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (XmppStringprepException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    class XmppResListener implements StanzaListener {
        @Override
        public void processPacket(Stanza packet) {
            HOXTWrapper.this.handleResponse(packet);
        }
    }

}