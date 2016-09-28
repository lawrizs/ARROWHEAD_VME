package org.arrowhead.wp5.core.services;

/*-
 * #%L
 * arrowhead-common
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

import java.util.HashSet;
import java.util.Set;

import org.arrowhead.wp5.core.entities.ArrowheadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bnearit.arrowhead.common.core.service.discovery.dnssd.ServiceDiscoveryDnsSD;
import se.bnearit.arrowhead.common.core.service.discovery.endpoint.TcpEndpoint;
import se.bnearit.arrowhead.common.core.service.discovery.exception.MetadataException;
import se.bnearit.arrowhead.common.core.service.discovery.exception.ServiceRegisterException;
import se.bnearit.arrowhead.common.service.ServiceIdentity;
import se.bnearit.arrowhead.common.service.ServiceInformation;
import se.bnearit.arrowhead.common.service.ServiceMetadata;

public class AggServiceManager {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    private ServiceDiscoveryDnsSD sd;
    private Set<ServiceInformation> sis;

    public AggServiceManager() {
        /*TODO in next version of Smack use getXmppServiceDomain*/

        //DnsSdHostname.setHostname();

        sd = new ServiceDiscoveryDnsSD();
        sis = new HashSet<ServiceInformation>();
    }

    public void publishMarketXMPP(String username, String xmppHostname, int xmppPort, String resource, String service) throws ArrowheadException {
        publishXMPP(username, xmppHostname, xmppPort, resource, service, ArrowheadConstants.MARKET_XMPP_TYPE);
    }

    public void publishAggXMPP(String username, String xmppHostname, int xmppPort, String resource, String service) throws ArrowheadException {
        publishXMPP(username, xmppHostname, xmppPort, resource, service, ArrowheadConstants.AGG_XMPP_TYPE);
    }

    private void publishXMPP(String username, String xmppHostname, int xmppPort, String resource, String service, String xmppType) throws ArrowheadException {
        try {
            for (ServiceIdentity si : sd.getServicesByType(xmppType)) {
                String[] s = si.getId().split("\\.");
                if (s.length > 0 && username.equals(s[0])) {
                    logger.info("Service already registered, unregistering...");
                    sd.unpublish(si);
                }
            }

            String idd = sd.createServiceName(username, xmppType);

            ServiceMetadata metadata = new ServiceMetadata();
            metadata.put(ArrowheadConstants.JABBER_ID_NAME, username);// + "@" + xmppHostname);
            metadata.put(ArrowheadConstants.RESOURCE_NAME, resource);
            metadata.put(ArrowheadConstants.SERVICE_NAME, service);

            ServiceInformation si = new ServiceInformation(
                    new ServiceIdentity(idd, xmppType),
                    new TcpEndpoint(xmppHostname, xmppPort),
                    metadata);

            sis.add(si);

            publish(si);
        } catch (ServiceRegisterException sre) {
            throw new ArrowheadException(sre);
        }
    }

    public void publishMarketHTTP(String username, String httpHostname, int httpPort, String path) throws ArrowheadException {
        publishHTTP(username, httpHostname, httpPort, path, ArrowheadConstants.MARKET_HTTP_TYPE);
    }

    public void publishAggHTTP(String username, String httpHostname, int httpPort, String path) throws ArrowheadException {
        publishHTTP(username, httpHostname, httpPort, path, ArrowheadConstants.AGG_HTTP_TYPE);
    }

    private void publishHTTP(String username, String httpHostname, int httpPort, String path, String httpType) throws ArrowheadException {
        try {
            for (ServiceIdentity si : sd.getServicesByType(httpType)) {
                String[] s = si.getId().split("\\.");
                if (s.length > 0 && username.equals(s[0])) {
                    logger.info("Service already registered, unregistering...");
                    sd.unpublish(si);
                }
            }

            String idd = sd.createServiceName(username, httpType);

            ServiceMetadata metadata = new ServiceMetadata();
            metadata.put(ArrowheadConstants.PATH_NAME, path);// + "@" + xmppHostname);

            ServiceInformation si = new ServiceInformation(
                    new ServiceIdentity(idd, httpType),
                    new TcpEndpoint(httpHostname, httpPort),
                    metadata);

            sis.add(si);

            publish(si);
        } catch (ServiceRegisterException sre) {
            throw new ArrowheadException(sre);
        }
    }

    public void shutdown() {
        for (ServiceInformation si : sis) {
            if (isPublished(si)) {
                unpublish(si);
            }
        }
    }

    private boolean isPublished(ServiceInformation si) {
        return sd.isPublished(si);
    }

    private void publish(ServiceInformation si) throws ServiceRegisterException {
        try {
            sd.publish(si);
        } catch (MetadataException e) {
            // This should only happen if new end points are implemented in ServiceDiscoveryDnsSD
            logger.error("MetadataException: Some metadata keys are trying to override EndPoint keys.", e);
            e.printStackTrace();
        }
    }

    private void unpublish(ServiceInformation si) {
        try {
            sd.unpublish(si.getIdentity());
        } catch (ServiceRegisterException e) {
            logger.warn("Unpublish failed!", e);
            e.printStackTrace();
        }
    }
}
