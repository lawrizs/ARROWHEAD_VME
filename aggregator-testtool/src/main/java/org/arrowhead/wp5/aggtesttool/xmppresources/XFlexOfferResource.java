package org.arrowhead.wp5.aggtesttool.xmppresources;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.arrowhead.wp5.aggtesttool.AggregatorTestTool;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferException;
import org.arrowhead.wp5.core.interfaces.FlexOfferAggregatorSubscriberIf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/flexoffers")
public class XFlexOfferResource implements FlexOfferAggregatorSubscriberIf {

    private static final Logger logger = LoggerFactory.getLogger(XFlexOfferResource.class);

    AggregatorTestTool att;

    public XFlexOfferResource(AggregatorTestTool att) {
        this.att = att;
    }

    @Override
    @Path("/{foaid}")
    @PUT
    @POST
    public int createFlexOffer(@PathParam("foaid") String ownerId, FlexOffer flexOffer) throws FlexOfferException {
        try {
            att.createFlexOffer(ownerId, flexOffer);
        } catch (FlexOfferException e) {
            logger.error("createFlexOffer: {}", e.getMessage());
            WebApplicationException ex = new WebApplicationException(Response.status(Status.BAD_REQUEST)
                    .entity(e.getMessage()).type(MediaType.TEXT_PLAIN)
                    .build());
            logger.debug("webappex: {}", ex.getMessage());
            throw ex;
        }
        return flexOffer.getId();
    }

    @Override
    @GET
    @Path("/{foaid}")
    public FlexOffer getFlexOffer(@PathParam("foaid") String ownerId, int flexOfferId) {
        return att.getFlexOffer(ownerId, flexOfferId);
    }

    @Override
    @Path("/{foaid}")
    public void setFlexOffer(@PathParam("foaid") String ownerId, int flexOfferId, FlexOffer flexOffer) throws FlexOfferException {
        throw new FlexOfferException("Can not update FlexOffer!");
    }

    @Override
    @Path("/{foaid}")
    public void deleteFlexOffer(@PathParam("foaid") String ownerId, int flexOfferId) throws FlexOfferException {
        throw new FlexOfferException("Can not delete FlexOffer!");
    }

    @Override
    public FlexOffer[] getFlexOffers() {
        return null;
    }

}
