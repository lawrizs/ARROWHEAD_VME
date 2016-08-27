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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.arrowhead.wp5.agg.impl.Aggregator;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferException;
import org.arrowhead.wp5.core.interfaces.FlexOfferAggregatorSubscriberIf;
import org.arrowhead.wp5.core.wrappers.FlexOfferKey;

@Path("/flexoffers")
public class XFlexOfferResource implements FlexOfferAggregatorSubscriberIf {

	Aggregator aggregator;

	public XFlexOfferResource(Aggregator aggregator) {
		this.aggregator = aggregator;
	}
	
	public XFlexOfferResource(){
	    this.aggregator = null;
	}

	@Override
	@Path("/{foaid}")
	@PUT
	@POST
	public int createFlexOffer(@PathParam("foaid") String foaId, FlexOffer flexOffer)
			throws FlexOfferException {

		this.aggregator.addSimpleFlexOffer(flexOffer);

		return 0;
	}

	
	@Override
	@GET
	@Path("/{foaid}")
	public FlexOffer getFlexOffer(@PathParam("foaid")String foaId, int flexOfferId) {
		return this.aggregator.getSimpleFlexOffer(new FlexOfferKey(foaId,
				Integer.toString(flexOfferId)));
	}

	@Override
	@Path("/{foaid}")
	public void setFlexOffer(@PathParam("foaid")String foaId, int flexOfferId, FlexOffer flexOffer)
			throws FlexOfferException {
		// TODO Auto-generated method stub

	}

	@Override
	@Path("/{foaid}")
	public void deleteFlexOffer(@PathParam("foaid")String foaId, int flexOfferId)
			throws FlexOfferException {
		// TODO Auto-generated method stub

	}

	@Override
	public FlexOffer[] getFlexOffers() {
		// TODO Auto-generated method stub
		return null;
	}

}
