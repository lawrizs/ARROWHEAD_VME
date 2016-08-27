package org.arrowhead.wp5.aggmanager.service;

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


import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import se.bnearit.arrowhead.common.service.ServiceEndpoint;
import se.bnearit.arrowhead.common.service.exception.ConsumerNotAuthorisedException;
import se.bnearit.arrowhead.common.service.ws.rest.ClientFactoryREST_WS;

public class FOAgentConsumerREST_XMPP implements FOAgentServiceConsumer {
@SuppressWarnings("unused")
private static final Logger LOG = Logger.getLogger(FOAgentConsumerREST_XMPP.class.getName());
	
	private String name;
	private int resourceId;
	private ServiceEndpoint endpoint;
	@SuppressWarnings("unused")
	private ClientFactoryREST_WS clientFactoryREST_WS;
	private boolean consuming;

	private ScheduledFuture<?> scheduledTask;

	private ScheduledExecutorService scheduler;

	@SuppressWarnings("unused")
	private int pollingInterval;

	public FOAgentConsumerREST_XMPP(String name, ServiceEndpoint endpoint, ClientFactoryREST_WS clientFactoryREST_WS, int pollingInterval, int resourceId) {
		this.name = name;
		this.endpoint = endpoint;
		this.clientFactoryREST_WS = clientFactoryREST_WS;
		this.pollingInterval = pollingInterval;
		this.resourceId = resourceId;
		
		consuming = false;
	}
	
	@Override
	public int getResourceId() {
		return resourceId;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getServiceType() {
		return FOAggregatorServiceTypes.XMPP_FOAGG_SECURE;
	}

	@Override
	public ServiceEndpoint getConfiguredEndpoint() {
		return endpoint;
	}
	
	@Override
	public boolean isConsuming() {
		return consuming;
	}

	@Override
	public void start() throws ConsumerNotAuthorisedException {
		/*getLocation();
		
		scheduler = Executors.newScheduledThreadPool(1);
		scheduledTask = scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					getReading();
				} catch (ConsumerNotAuthorisedException e) {
					LOG.warning(name + " is not authorised to consume " + endpoint);
				}
			}
		}, 0, pollingInterval, TimeUnit.SECONDS);*/
	}
	
	@Override
	public void stop() {
		if (scheduledTask != null) {
			scheduledTask.cancel(true);
			scheduledTask = null;
		}
		if (scheduler != null) {
			scheduler.shutdown();
			scheduler = null;
		}
	}
	
	/*private void getReading() throws ConsumerNotAuthorisedException {
		if (endpoint instanceof HttpEndpoint) {
			Client client = null;
			try {
				HttpEndpoint httpEndpoint = (HttpEndpoint)endpoint;
				client = clientFactoryREST_WS.createClient(httpEndpoint.isSecure());
				WebTarget target = client.target(httpEndpoint.toURL().toURI()).path("reading");
				URL targetUrl = target.getUri().toURL();
				LOG.info(name + " requesting " + targetUrl.toString());
				TemperatureData response = target.request(MediaType.APPLICATION_XML_TYPE).get(TemperatureData.class);
				LOG.info(String.format("%s got response from %s: %s%n", name, targetUrl.toString(), response));
			} catch (WebApplicationException e) {
				LOG.warning(String.format("Request to %s failed with exception %s%n", endpoint, e.getMessage()));
				if (e.getResponse().equals(Response.Status.FORBIDDEN)) {
					throw new ConsumerNotAuthorisedException("Not authorised to access " + endpoint);
				}
			} catch (MalformedURLException | URISyntaxException | ProcessingException e) {
				LOG.warning(String.format("Request to %s failed with exception %s%n", endpoint, e.getMessage()));
			} finally {
				try {
					if (client != null) {
						client.close();
					}
				} catch (Exception e) {}
			}
		} else {
			LOG.warning("Service consumer " + name + " does not support endpoint type " + endpoint.getType());
		}
	}*/
}
