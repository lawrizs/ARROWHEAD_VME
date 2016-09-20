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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bnearit.arrowhead.common.core.service.discovery.ServiceDiscovery;
import se.bnearit.arrowhead.common.core.service.discovery.dnssd.ServiceDiscoveryDnsSD;
import se.bnearit.arrowhead.common.core.service.discovery.endpoint.HttpEndpoint;
import se.bnearit.arrowhead.common.core.service.discovery.endpoint.TcpEndpoint;
import se.bnearit.arrowhead.common.core.service.orchestration.data.OrchestrationConfig;
import se.bnearit.arrowhead.common.core.service.orchestration.ws.rest.OrchestrationServiceTypes;
import se.bnearit.arrowhead.common.core.service.orchestration.ws.rest.OrchestrationStoreConsumerREST_WS;
import se.bnearit.arrowhead.common.service.ServiceIdentity;
import se.bnearit.arrowhead.common.service.ServiceInformation;
import se.bnearit.arrowhead.common.service.ServiceMetadata;
import se.bnearit.arrowhead.common.service.ws.rest.ClientFactoryREST_WS;

public class ArrowheadXMPPServiceManager {
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	ServiceDiscovery sd;
	private String hostname;
	private int port;
	private String resource;
	private String aggId;
	
	/** Certificates */
	private String keystoreFilename;
	private String keystorePassword;
	private String truststoreFilename;
	private String truststorePassword;

	public ArrowheadXMPPServiceManager(Properties properties) {
		keystoreFilename = properties.getProperty("fom.keystoreFilename", "alpha.jks").trim();
		keystorePassword = properties.getProperty("fom.keystorePassword", "XXXXX").trim();
		truststoreFilename = properties.getProperty("fom.truststoreFilename", "alpha.jks").trim();
		truststorePassword = properties.getProperty("fom.truststorePassword", "XXXXX").trim();
	}
	
	public ArrowheadXMPPServiceManager(String keystoreFilename, String keystorePassword,
			String truststoreFilename, String truststorePassword) {
		this.keystoreFilename = keystoreFilename;
		this.keystorePassword = keystorePassword;
		this.truststoreFilename = truststoreFilename;
		this.truststorePassword = truststorePassword;
	}

	public void start() {
		sd = new ServiceDiscoveryDnsSD();
	}
	
	public void shutdown(){
		
	}

	public boolean fetchInfo() {
		boolean result = false;
		
		List<String> rules = getOrchestration();
		
		List<ServiceIdentity> services = sd.getServicesByType(ArrowheadConstants.AGG_XMPP_TYPE);
		for (ServiceIdentity si : services) {
			if (rules.contains(si.getId())) {
				useServiceInfo(si);
				result = true;
			}
		}

		if (!result) {
			logger.info("No orchestrated aggregator found, trying non-orchestrated.");
			if (services.size() > 0){
				useServiceInfo(services.get(0));
				result = true;
			}
		}
		return result;
	}

	private void useServiceInfo(ServiceIdentity si) {
		ServiceInformation serviceInfo = sd.getServiceInformation(si, TcpEndpoint.ENDPOINT_TYPE);
		TcpEndpoint ep = (TcpEndpoint)serviceInfo.getEndpoint();
		hostname = ep.getHost();
		port = ep.getPort();

		ServiceMetadata metadataSet = serviceInfo.getMetadata();
		resource = metadataSet.get(ArrowheadConstants.RESOURCE_NAME);
		aggId = metadataSet.get(ArrowheadConstants.JABBER_ID_NAME);

		logger.info("Fetched: {}@{}:{}/{}", aggId, hostname, Integer.toString(port), resource);
	}

	private List<String> getOrchestration() {
		List<String> rules = new ArrayList<String>();
		List<ServiceIdentity> orchServices = sd.getServicesByType(OrchestrationServiceTypes.REST_WS_ORCHESTRATION_STORE_SECURE);
		logger.debug("Got Arrowhead Orchestration Services. Num: {}", orchServices.size());
		for (ServiceIdentity si : orchServices){
			ServiceInformation s = sd.getServiceInformation(si, HttpEndpoint.ENDPOINT_TYPE);
			ClientFactoryREST_WS clientFactory = new ClientFactoryREST_WS(truststoreFilename, truststorePassword, keystoreFilename, keystorePassword);
			OrchestrationStoreConsumerREST_WS orchestration = new OrchestrationStoreConsumerREST_WS((HttpEndpoint)s.getEndpoint(), clientFactory);
			
			OrchestrationConfig oc = orchestration.getActiveConfiguration();
			logger.debug("Performed getActiveConfiguration on {}. The result is {}", s, oc);
			if (oc == null) {
				logger.debug("OrchestrationConfig is null!");
				continue;
			}
			rules = oc.getRules();
			
//			for (String rule : rules) {
//				logger.info("Rule: {}", rule);
//			}
			return rules;
		}
		return rules;
	}

	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}

	public String getResource() {
		return resource;
	}

	public String getAggId() {
		return aggId;
	}
}
