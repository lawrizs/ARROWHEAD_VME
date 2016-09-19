package org.arrowhead.wp5.aggmanager.impl.xmppresources;

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

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.arrowhead.wp5.aggmanager.impl.AggregatorManager;
import org.arrowhead.wp5.core.entities.Bid;
import org.arrowhead.wp5.core.entities.BidV2;
import org.arrowhead.wp5.core.entities.MarketException;
import org.arrowhead.wp5.core.interfaces.MarketSubscriberIf;

@Path("/market")
public class XMarketResource implements MarketSubscriberIf {

	AggregatorManager man;

	public XMarketResource(AggregatorManager man) {
		this.man = man;
	}

	@Override
	@Path("/acceptBid")
	@PUT
	@POST
	public void acceptBid(Bid bid) throws MarketException {
		new UnsupportedOperationException("MarketV1 bids are not longer handled by Aggregator");
	}

	@Override
	public void acceptBidV2(BidV2 bid) throws MarketException {
		this.man.acceptBidV2(bid);		
	}
}
