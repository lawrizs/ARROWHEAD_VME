package org.arrowhead.wp5.fom.service;

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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import se.bnearit.arrowhead.common.service.ServiceEndpoint;
import se.bnearit.arrowhead.common.service.ws.rest.ClientFactoryREST_WS;
import se.bnearit.resource.ResourceAllocator;

public class FOAggregatorServiceConsumerFactory implements ServiceConsumerFactory {
	private static final Logger LOG = Logger.getLogger(FOAggregatorServiceConsumerFactory.class.getName());
	
	private List<String> supportedTypes;
	private List<FOAggregatorServiceConsumer> consumers;

	private ClientFactoryREST_WS clientFactoryREST_WS;

	private int pollingInterval;

	public FOAggregatorServiceConsumerFactory(ClientFactoryREST_WS clientFactoryREST_WS, int pollingInterval) {
		this.clientFactoryREST_WS = clientFactoryREST_WS;
		this.pollingInterval = pollingInterval;
		supportedTypes = Arrays.asList(FOAggregatorServiceTypes.XMPP_FOAGG_SECURE);
		consumers = new ArrayList<>();
	}
	
	@Override
	public List<String> getSupportedServiceTypes() {
		return Collections.unmodifiableList(supportedTypes);
	}

	@Override
	public FOAggregatorServiceConsumer createServiceConsumer(String name,
			String type, ServiceEndpoint endpoint) {
		FOAggregatorServiceConsumer result = null;
		
		LOG.info(String.format("Trying to create service consumer with name:%s type:%s endpoint:%s", name, type, endpoint));
		
		if (supportedTypes.contains(type)) {
			switch (type) {
				case FOAggregatorServiceTypes.XMPP_FOAGG_SECURE:
					result = new FOAggregatorConsumerREST_XMPP(name, endpoint, clientFactoryREST_WS, pollingInterval, ResourceAllocator.getInstance().allocateResourceId());
					consumers.add(result);
					break;
				default:
					LOG.severe(String.format("FOAggregator service consumer of type %s not supported (name: %s)", type, name));
					break;
			}
		}
		
		return result;
	}

	@Override
	public List<FOAggregatorServiceConsumer> getAllServiceConsumers() {
		return Collections.unmodifiableList(consumers);
	}

	@Override
	public void destroyAllConsumers() {
		System.out.println("destroying consumers");
		for (FOAggregatorServiceConsumer cons : consumers) {
			System.out.println("stopping " + cons.getName());
			cons.stop();
		}
		consumers.clear();		
	}
}
