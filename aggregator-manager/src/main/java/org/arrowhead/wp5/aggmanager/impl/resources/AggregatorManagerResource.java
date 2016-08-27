package org.arrowhead.wp5.aggmanager.impl.resources;

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


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.arrowhead.wp5.agg.api.FOAggParameters;
import org.arrowhead.wp5.aggmanager.impl.AggregatorManager;
import org.arrowhead.wp5.aggmanager.impl.resources.entities.FlexOfferAggregatorStats;
import org.arrowhead.wp5.application.entities.BooleanWrapper;
import org.arrowhead.wp5.application.entities.StringWrapper;
import org.arrowhead.wp5.com.xmpp.api.XmppConnectionDetails;
import org.arrowhead.wp5.core.entities.FlexOffer;

@Path("/manager")
public class AggregatorManagerResource {
	private AggregatorManager man;
	
	public AggregatorManagerResource(AggregatorManager man) {
		this.man = man;
	}	
	
	/* Connection status */	
	@GET
	@Path("/connection")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public BooleanWrapper isConnected() {
		return new BooleanWrapper(true);
		// return new BooleanWrapper(this.aggregator.isConnected());
	}
	
	/* Aggregator ID */
	@GET
	@Path("/id")
	@Produces({MediaType.APPLICATION_JSON})
	public StringWrapper getId() {
		return new StringWrapper(this.man.getAggregator().getId());
	}
	
	/* XMPP server parameters */
	@GET
	@Path("/xmppparams")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public XmppConnectionDetails getXMPPParams()
	{
//		return this.man.getConDetails();
		return null;
	}
	
	@POST
	@Path("/xmppparams")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public void setAggParams(XmppConnectionDetails xmppParams)
	{
//		this.man.setConDetails(xmppParams);
	}
	
	
	/* Aggregation parameters */
	@GET
	@Path("/aggparams")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public FOAggParameters getAggParams()
	{
		return this.man.getAggregator().getAggParameters();
	}
	
	@POST
	@Path("/aggparams")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public void setAggParams(FOAggParameters aggParams)
	{
		this.man.getAggregator().setAggParameters(aggParams);
	}
	
	/* Generates FOA the most essential statistics */
	@GET
	@Path("/stats")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public FlexOfferAggregatorStats getStats(@Context HttpServletRequest request) {
		FlexOfferAggregatorStats stats = new FlexOfferAggregatorStats();

		stats.incNumSimpleFOs(this.getFlexOffers().length);
		stats.incNumAggFOs(this.getAggFlexOffers().length);
		
		for (FlexOffer fo : this.man.getAggregator().getFlexOffers()) {
			switch (fo.getState()) {
			case Initial:
				stats.incNumInitial(1);
				break;
			case Offered:
				stats.incNumOffered(1);
				break;
			case Accepted:
				stats.incNumAccepted(1);
				break;
			case Assigned:
				stats.incNumAssigned(1);
				break;
			case Executed:
				stats.incNumExecuted(1);
				break;
			case Rejected:
				stats.incNumRejected(1);
				break;
			}
		}

		return stats;
	}


	/* Simple flexOffer access */
	@GET
	@Path("/flexoffers")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public FlexOffer[] getFlexOffers() {
		return this.man.getAggregator().getAllSimpleFlexOffers().toArray(new FlexOffer[0]);
	}
	
	@GET
	@Path("/flexoffers/{foid}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public FlexOffer getFlexOffer(@PathParam("foid") int flexOfferId) {
		// if using arrowhead core services		
		for(FlexOffer f : this.getFlexOffers())
		{
			if (f.getId() == flexOfferId) {
				return f;
			}
		}
		return null;
	}
	
	/* Aggregated flexOffer access */
	@GET
	@Path("/aggflexoffers")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public FlexOffer[] getAggFlexOffers() {
		return this.man.getAggregator().getFlexOffers();
	}
	
	@GET
	@Path("/aggflexoffers/{foid}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public FlexOffer getAggFlexOffer(@PathParam("foid") int flexOfferId) {
		// if using arrowhead core services		
		for(FlexOffer f : this.getAggFlexOffers())
		{
			if (f.getId() == flexOfferId) {
				return f;
			}
		}
		return null;
	}
	
}
