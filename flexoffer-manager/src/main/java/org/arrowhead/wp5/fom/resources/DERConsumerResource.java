package org.arrowhead.wp5.fom.resources;

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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.arrowhead.wp5.core.entities.AbstractDER;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferException;
import org.arrowhead.wp5.core.interfaces.DERSubscriberIf;

public class DERConsumerResource implements DERSubscriberIf  {
	private AbstractDER der;
	private DERSubscriberIf der_consumer;
	
	public DERConsumerResource(AbstractDER der, DERSubscriberIf der_consumer) {
		this.der = der;
		this.der_consumer = der_consumer;
	}
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public AbstractDER getDER()
	{
		return this.der;
	}

	@POST
	@Path("flexoffers")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Override
	public int createFlexOffer(String ownerId, FlexOffer flexOffer) throws FlexOfferException {
		return this.der_consumer.createFlexOffer(ownerId, flexOffer);
	}

	@GET
	@Path("flexoffers/{foid}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Override
	public FlexOffer getFlexOffer(String foaId, int flexOfferId) {
		return this.der_consumer.getFlexOffer(foaId, flexOfferId);
	}

	@PUT
	@Path("flexoffers/{foaid}/{foid})")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })	
	@Override
	public void setFlexOffer(@PathParam("foaid") String foaId, @PathParam("foid") int flexOfferId, FlexOffer flexOffer)
			throws FlexOfferException {
		this.der_consumer.setFlexOffer(foaId, flexOfferId, flexOffer);		
	}

	@DELETE
	@Path("flexoffers/{foaid}/{foid}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Override
	public void deleteFlexOffer(@PathParam("foaid") String foaId, int flexOfferId) throws FlexOfferException {
		this.der_consumer.deleteFlexOffer(foaId, flexOfferId);		
	}

	@GET
	@Path("flexoffers")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Override
	public FlexOffer[] getFlexOffers() {
		return this.der_consumer.getFlexOffers();
	}	
	
}
