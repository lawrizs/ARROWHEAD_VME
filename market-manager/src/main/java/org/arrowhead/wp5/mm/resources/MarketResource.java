package org.arrowhead.wp5.mm.resources;

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
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.arrowhead.wp5.application.entities.BooleanWrapper;
import org.arrowhead.wp5.application.entities.StringWrapper;
import org.arrowhead.wp5.core.entities.Bid;
import org.arrowhead.wp5.core.entities.BidV2;
import org.arrowhead.wp5.core.entities.MarketException;
import org.arrowhead.wp5.core.entities.MarketInfo;
import org.arrowhead.wp5.core.interfaces.MarketProviderIf;
import org.arrowhead.wp5.market.impl.Market;
import org.arrowhead.wp5.mm.main.MarketManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/market")
public class MarketResource implements MarketProviderIf {
	private Market market;
	private MarketManager manager;
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	public MarketResource(Market market, MarketManager manager) {
		this.market = market;
		this.manager = manager;
	}
	
	@Override
	@GET
	@Path("/info")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public MarketInfo getInfo() throws MarketException {
		return market.getInfo();
	}

	@GET
	@Path("/clear")
	public void clear() throws MarketException {
		logger.debug("clear");
		market.clear();
		manager.sendAcceptedBids();
	}
	
	@GET
	@Path("/id")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public StringWrapper getId() {
		return new StringWrapper(manager.getId());
	}

	@GET
	@Path("/connection")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public BooleanWrapper isConnected() {
		return new BooleanWrapper(true);
	}

	@Path("/winSupplyUp")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Bid[] getWinSupplyUp() {
		return market.getWinSupplyUp().toArray(new Bid[0]);
	}

	@Path("/winDemandUp")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Bid[] getWinDemandUp() {
		return market.getWinDemandUp().toArray(new Bid[0]);
	}

	@Path("/winSupplyDown")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Bid[] getWinSupplyDown() {
		return market.getWinSupplyDown().toArray(new Bid[0]);
	}

	@Path("/winDemandDown")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Bid[] getWinDemandDown() {
		return market.getWinDemandDown().toArray(new Bid[0]);
	}

	@Path("/supplyUp")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Bid[] getSupplyUp() {
		return market.getSupplyUp().toArray(new Bid[0]);
	}

	@Path("/demandUp")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Bid[] getDemandUp() {
		return market.getDemandUp().toArray(new Bid[0]);
	}

	@Path("/supplyDown")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Bid[] getSupplyDown() {
		return market.getSupplyDown().toArray(new Bid[0]);
	}

	@Path("/demandDown")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Bid[] getDemandDown() {
		return market.getDemandDown().toArray(new Bid[0]);
	}

	@Path("/supply")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Bid[] getSupply() {
		return market.getSupply().toArray(new Bid[0]);
	}

	@Path("/demand")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Bid[] getDemand() {
		return market.getDemand().toArray(new Bid[0]);
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
