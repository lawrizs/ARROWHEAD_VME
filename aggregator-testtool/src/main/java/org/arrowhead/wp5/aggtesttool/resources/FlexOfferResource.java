package org.arrowhead.wp5.aggtesttool.resources;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.arrowhead.wp5.aggtesttool.AggregatorTestTool;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/flexoffers")
public class FlexOfferResource {
    private static final Logger logger = LoggerFactory.getLogger(FlexOfferResource.class);

    AggregatorTestTool att;

    public FlexOfferResource(AggregatorTestTool att) {
        this.att = att;
    }

    public FlexOfferResource() {

    }

//    @GET
//    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    public FlexOffer[] getFlexOffers()
//            throws FlexOfferException {
//        return this.agg.getAllSimpleFlexOffers().toArray(new FlexOffer[0]);
//    }

    @Path("/{foaid}")
    @POST
    public Response createFlexOffer(@PathParam("foaid") String foaId, FlexOffer flexOffer)
            throws FlexOfferException {
        logger.debug("Creating flex-offer");

        if (!foaId.equals(flexOffer.getOfferedById())) {
            throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
                    .entity("IDs do not match!").type(MediaType.TEXT_PLAIN)
                    .build());
        }

        this.att.addToHttpClients(foaId);
        try {
            this.att.createFlexOffer(foaId, flexOffer);
        } catch (FlexOfferException e) {
            throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
                    .entity(e.getMessage()).type(MediaType.TEXT_PLAIN)
                    .build());
        }

        return Response.ok().build();
    }

    @GET
    @Path("/{foaid}/{foid}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public FlexOffer getFlexOffer(@PathParam("foaid") String ownId, @PathParam("foid") String id) throws FlexOfferException {
        logger.debug("Fetching flex-offer");

        FlexOffer fo = this.att.getFlexOffer(ownId, Integer.parseInt(id));
        if (fo == null) {
            throw new WebApplicationException(Response.status(Status.NOT_FOUND)
                    .entity("Flex-offer not found.").type(MediaType.TEXT_PLAIN)
                    .build());
        }

        return fo;
    }

}
