package org.arrowhead.wp5.mm.xresources;

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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.arrowhead.wp5.core.entities.Bid;
import org.arrowhead.wp5.core.entities.BidV2;
import org.arrowhead.wp5.core.entities.MarketException;
import org.arrowhead.wp5.core.entities.MarketInfo;
import org.arrowhead.wp5.core.interfaces.MarketProviderIf;
import org.arrowhead.wp5.market.impl.Market;

@Path("/market")
public class XMarketResource implements MarketProviderIf {
	private Market market;
	
	public XMarketResource(Market market) {
		this.market = market;
	}
	
	@Override
	@Path("/info")
	@GET
	public MarketInfo getInfo() throws MarketException {
		return market.getInfo();
	}

	@Override
	@Path("/supply")
	@POST
	public void bidSupply(Bid bid) throws MarketException {
		market.bidSupply(bid);
	}

	@Override
	@Path("/demand")
	@POST
	public void bidDemand(Bid bid) throws MarketException {
		market.bidDemand(bid);
	}

	@Override
	public void bidV2Supply(BidV2 bid) throws MarketException {
		new UnsupportedOperationException("Market V2 bids are not supported!");		
	}

	@Override
	public void bidV2Demand(BidV2 bid) throws MarketException {
		new UnsupportedOperationException("Market V2 bids are not supported!");		
	}
}
