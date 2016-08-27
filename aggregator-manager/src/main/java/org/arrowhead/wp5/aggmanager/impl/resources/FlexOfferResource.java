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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.arrowhead.wp5.agg.impl.Aggregator;
import org.arrowhead.wp5.aggmanager.impl.AggregatorManager;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferException;
import org.arrowhead.wp5.core.wrappers.FlexOfferKey;

@Path("/flexoffers")
public class FlexOfferResource {
    private AggregatorManager aggMng;
    private Aggregator agg;

    public FlexOfferResource(AggregatorManager aggMng) {
        this.aggMng = aggMng;
        this.agg = aggMng.getAggregator();
    }
    
    public FlexOfferResource(){}

    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public FlexOffer[] getFlexOffers()
            throws FlexOfferException {
        return this.agg.getAllSimpleFlexOffers().toArray(new FlexOffer[0]);
    }

    /**
     * Does not make sense since it is the producer
     * 
     * @PUT
     * @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}) public
     *                                        void setFlexOffers(JAXBElement<
     *                                        FlexOffer[]> msg) throws
     *                                        FlexOfferException { FlexOffer[]
     *                                        flexOffers = msg.getValue();
     *                                        FOAStore
     *                                        .instance.getFlexOfferAgent
     *                                        ().setFlexOffers(flexOffers); }
     */

    /**
     * Does not make sense since it is the producer
     * 
     * @DELETE
     * @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}) public
     *                                        void deleteFlexOffers() throws
     *                                        FlexOfferException {
     *                                        FOAStore.instance
     *                                        .getFlexOfferAgent
     *                                        ().deleteFlexOffers(); }
     */

    /**
     * Does not make sense since it is the producer
     * 
     * @POST
     * @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}) public
     *                                        void createFlexOfferResource(
     *                                        JAXBElement<FlexOffer> msg) throws
     *                                        FlexOfferException { FlexOffer
     *                                        flexOffer = msg.getValue();
     *                                        FOAStore
     *                                        .instance.getFlexOfferAgent
     *                                        ().createFlexOffer(flexOffer); }
     */

    @Path("/{foaid}")
    @POST
    public Response createFlexOffer(@PathParam("foaid") String foaId, FlexOffer flexOffer)
            throws FlexOfferException {
        if (!foaId.equals(flexOffer.getOfferedById())) throw new WebApplicationException(400);
        
        this.agg.addSimpleFlexOffer(flexOffer);
        this.aggMng.addHttpClientId(foaId);
        
        return Response.ok().build();
    }

    @GET
    @Path("/{foaid}/{foid}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public FlexOffer getFlexOffer(@PathParam("foaid") String ownId, @PathParam("foid") String id) throws FlexOfferException {
        FlexOffer fo = this.agg.getSimpleFlexOffer(new FlexOfferKey(ownId, id));
        if (fo == null) {
            throw new WebApplicationException(404);
        }

        return fo;
    }
    //
    //	@PUT @Path("/{foid}")
    //	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    //	public void setFlexOffer(@PathParam("foid") Integer flexOfferId, JAXBElement<FlexOffer> msg) throws FlexOfferException{
    //		FlexOffer flexOffer = msg.getValue();
    //		FOAStore.instance.getFlexOfferAgent().setFlexOffer(flexOfferId, flexOffer);
    //	}
    //
    //	@DELETE @Path("/{foid}")
    //	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    //	public void deleteFlexOffer(@PathParam("foid") Integer flexOfferId) throws FlexOfferException {
    //		FOAStore.instance.getFlexOfferAgent().deleteFlexOffer(flexOfferId);
    //	}
    //
    //
    //	@GET @Path("/{foid}/state")
    //	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    //	public FlexOfferState getFlexOfferState(@PathParam("foid") Integer flexOfferId)
    //			throws FlexOfferException {
    //		return FOAStore.instance.getFlexOfferAgent().getFlexOfferState(flexOfferId);
    //	}
    //
    //	@PUT @Path("/{foid}/state")
    //	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    //	public void setFlexOfferState(@PathParam("foid") Integer flexOfferId,
    //			JAXBElement<FlexOfferState> msg)
    //			throws FlexOfferException {
    //		FlexOfferState flexOfferState = msg.getValue();
    //		FOAStore.instance.getFlexOfferAgent().setFlexOfferState(flexOfferId, flexOfferState, null);
    //	}
    //
    //
    //	@GET @Path("/{foid}/schedule")
    //	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    //	public FlexOfferSchedule getFlexOfferSchedule(@PathParam("foid") Integer flexOfferId)
    //			throws FlexOfferException {
    //		return FOAStore.instance.getFlexOfferAgent().getFlexOfferSchedule(flexOfferId);
    //	}
    //
    //	@PUT @Path("/{foid}/schedule")
    //	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    //	public void setFlexOfferSchedule(@PathParam("foid") Integer flexOfferId,
    //			JAXBElement<FlexOfferSchedule> msg) throws FlexOfferException {
    //		FlexOfferSchedule flexOfferSch = msg.getValue();
    //		FOAStore.instance.getFlexOfferAgent().setFlexOfferSchedule(flexOfferId, flexOfferSch);
    //		
    //	}
    //
    //	@DELETE @Path("/{foid}/schedule")
    //	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    //	public void deleteFlexOfferSchedule(@PathParam("foid") Integer flexOfferId)
    //			throws FlexOfferException {
    //		FOAStore.instance.getFlexOfferAgent().deleteFlexOfferSchedule(flexOfferId);
    //	}

}
