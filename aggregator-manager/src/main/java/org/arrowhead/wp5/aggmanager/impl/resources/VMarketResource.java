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


import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.arrowhead.wp5.agg.impl.Aggregator;
import org.arrowhead.wp5.agg.impl.billing.MarketCommitment;
import org.arrowhead.wp5.aggmanager.impl.AggregatorManager;
import org.arrowhead.wp5.core.adapters.DateAdapter;
import org.arrowhead.wp5.core.entities.BidV2;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferException;
import org.arrowhead.wp5.core.entities.MarketInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.ClientResponse.Status;



@Path("/vmarket")
public class VMarketResource {
	private Aggregator agg;
	private AggregatorManager man;
	
	final static Logger logger = LoggerFactory.getLogger(VMarketResource.class);
	
	public VMarketResource(AggregatorManager man) {
		this.man = man;
		this.agg = man.getAggregator();
	}
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public MarketInfo getMarketInfo() {
		return this.man.getMarketInfo();
		/*MarketInfo info = this.man.getMarketInfo();
		
		if (info != null) {
			return Response.ok(info).build();
		} else {
			return Response.ok(null).build();
		}			*/
	}
	
	@GET
	@Path("/generateBidFo")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public FlexOffer getBidFlexOffer(@QueryParam("bidStartTime") String bidStartTimeString, @QueryParam("bidEndTime") String bidEndTimeString) {
		Date dtStart;
		Date dtEnd;
		try {
			DateAdapter da = new DateAdapter();
			dtStart = da.unmarshal(bidStartTimeString);
			dtEnd = da.unmarshal(bidEndTimeString);
		}
		catch(Exception e) {
			dtStart = new Date();	
			dtEnd = new Date();
		}
		
		long periodStart = FlexOffer.toFlexOfferTime(dtStart);
		long periodEnd = FlexOffer.toFlexOfferTime(dtEnd);
		BidV2 bid = this.agg.generate_maketV2_bid(periodStart, periodEnd);
		
		return bid.getBidFlexOffer();
	}
	
	@POST
	@Path("/sendMarketBid")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response sendBidFlexOffersToMarket(FlexOffer mFo) {
		try{
			// man.bidMaketFO(mFo);
			return Response.ok().build();
		}catch (Exception e) {
			return Response.status(-1).build();
		}
	}
	
	
	@GET
	@Path("/commitments")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public MarketCommitment[] getMarketCommitments()
			throws FlexOfferException {
		return this.agg.getMarket_commitments().toArray(new MarketCommitment[]{});
	}


	public AggregatorManager getMan() {
		return man;
	}
	
//	@POST
//	@Path("/bidSchedule")
//	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	public TimeSeries getBidScheduleEnergy(MarketFlexOffer mf)  {
//		TimeSeries ts = new TimeSeries(new double[] {});
//		Collection<FlexOffer> fos = this.agg.cloneSimpleFlexOffers();
//
//		double [] qs = new double[mf.getNumBiddingIntervals()];
//		for (int i = 0; i< mf.getNumBiddingIntervals(); i++) {
//			qs[i] = mf.getBidQuantity() / mf.getNumBiddingIntervals();
//		}
//		mf.bidWon(0, qs); 	/* Emulate bid winning, with uniformly distributed quantities across intervals */
//		
//		this.agg.generateMarketFlexOfferSchedules(mf, fos);
//		
//		for(FlexOffer f : fos) {
//			TimeSeries ft = new TimeSeries(f, TimeSeriesType.tstScheduledEnergy);				
//			ts._extend(ft)._plus(ft);
//		}
//		return ts;		
//	}
	
/*	
	@POST
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getMarketFlexOffers(MarketFlexOffer mb) {
		return Response.ok().build();
	}
	*/

}
