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
import org.arrowhead.wp5.core.impl.FlexOfferAgent;
import org.arrowhead.wp5.core.interfaces.DERSubscriberIf;

@Path("/ders")
public class DERResource implements DERSubscriberIf{
	private FlexOfferAgent agent;

	public DERResource(FlexOfferAgent agent) {
		this.agent = agent;
	}	
	
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public AbstractDER [] getDers(){	
		AbstractDER[] ders = new AbstractDER[agent.getAgentDers().size()];
		return agent.getAgentDers().toArray(ders);
	}	
	
	/**
	 * Sub-resource, of a specific DER-consumer (end-point)
	 */
	@Path("/{derid}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public AbstractDER getDERconsumer(@PathParam("derid") int derid) {
		AbstractDER ader = this.agent.getAgentDer(derid);
		return ader;
    }

	@POST
	@Path("/{derid}/flexoffers")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Override
	public int createFlexOffer(@PathParam("derid")String ownerId, FlexOffer flexOffer) throws FlexOfferException {
		return this.agent.createFlexOffer(ownerId, flexOffer);
	}

	@GET
	@Path("/{derid}/flexoffers/{foid}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Override
	public FlexOffer getFlexOffer(@PathParam("derid")String derId, @PathParam("foid")int flexOfferId) {
		return this.agent.getFlexOffer(derId, flexOfferId);
	}

	@PUT
	@Path("/{derid}/flexoffers/{foid})")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })	
	@Override
	public void setFlexOffer(@PathParam("derid") String derId, @PathParam("foid") int flexOfferId, FlexOffer flexOffer)
			throws FlexOfferException {
		this.agent.setFlexOffer(derId, flexOfferId, flexOffer);		
	}

	@DELETE
	@Path("/{derid}/flexoffers/{foid}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Override
	public void deleteFlexOffer(@PathParam("derid") String derId, int flexOfferId) throws FlexOfferException {
		this.agent.deleteFlexOffer(derId, flexOfferId);		
	}

	@GET
	@Path("/{derid}/flexoffers")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Override
	public FlexOffer[] getFlexOffers() {
		return this.agent.getFlexOffers();
	}	
}
