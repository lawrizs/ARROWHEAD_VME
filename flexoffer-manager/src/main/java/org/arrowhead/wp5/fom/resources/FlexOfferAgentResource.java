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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.arrowhead.wp5.application.entities.BooleanWrapper;
import org.arrowhead.wp5.com.xmpp.api.XmppConnectionDetails;
import org.arrowhead.wp5.core.entities.ArrowheadException;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferException;
import org.arrowhead.wp5.core.entities.FlexOfferSchedule;
import org.arrowhead.wp5.core.entities.FlexOfferState;
import org.arrowhead.wp5.core.impl.FlexOfferAgent;
import org.arrowhead.wp5.core.impl.FlexOfferAgentMode;
import org.arrowhead.wp5.core.interfaces.FlexOfferAgentProviderIf;
import org.arrowhead.wp5.fom.entities.FlexOfferAgentStats;
import org.arrowhead.wp5.fom.main.FlexOfferManager;
import org.jivesoftware.smack.XMPPException;

import se.bnearit.arrowhead.common.service.exception.ServiceNotStartedException;

/**
 * This is the resource associated to the FlexOfferAgent service
 * 
 * @author Laurynas
 */
@Path("/foa")
public class FlexOfferAgentResource implements FlexOfferAgentProviderIf {

	private FlexOfferAgent agent;
	private FlexOfferManager fom;

	public FlexOfferAgentResource(FlexOfferAgent agent, FlexOfferManager fom) {
		this.agent = agent;
		this.fom = fom;
	}

	/* Generates FOA the most essential statistics */
	@GET
	@Path("/stats")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public FlexOfferAgentStats getStats(@Context HttpServletRequest request) {
		FlexOfferAgentStats stats = new FlexOfferAgentStats();

		stats.incNumGeneratedFOs(agent.getFlexOffers().length);
		for (FlexOffer fo : agent.getFlexOffers()) {
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

	@GET
	@Path("/connectiondetails")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public XmppConnectionDetails getXmppConnDetails() {
		return this.fom.getXmppConnectionDetails();
	}

	@POST
	@Path("/connectiondetails")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public void setXmppConnDetails(XmppConnectionDetails conDetails) {
		this.fom.setXmppConnectionDetails(conDetails);
	}

	@GET
	@Path("/connection")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public BooleanWrapper isConnected() {
		return new BooleanWrapper(this.fom.isXmppConnected());
	}

	@POST
	@Path("/connection")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public void connect(BooleanWrapper value) throws XMPPException {
		this.fom.setXmppConnected(value.getValue());
	}

	/* Allows accessing FOA mode */
	@GET
	@Path("/mode")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public FlexOfferAgentMode getFOAmode() {
		return this.agent.getMode();
	}

	@POST
	@Path("/mode")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public void setFOAmode(FlexOfferAgentMode mode) {
		this.agent.setMode(mode);
	}

	@GET
	@Path("/flexoffers/{foid}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Override
	public FlexOffer getFlexOffer(@PathParam("foid") int flexOfferId)
			throws FlexOfferException {
//		// if using arrowhead core services
//		if (null != this.arrowheadSubsystem) {
//			boolean authorized = false;
//
//			// check if authorized
//			String dn = "C=SE,ST=Sweden,L=Lulea,O=BnearIT,OU=Development,CN=SomeToBeAuthorised"; // *** needs to be changed by aggregator certificate info ***
//			authorized = this.arrowheadSubsystem.checkAuthorisation(dn, FOAggregatorServiceTypes.XMPP_FOAGG_SECURE, null);
//
//			if (!authorized) {
//				// not authorized
//				return null;
//			}
//		}
		return this.agent.getFlexOffer(flexOfferId);
	}

	@GET
	@Path("/flexoffers")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Override
	public FlexOffer[] getFlexOffers() {
		return this.agent.getFlexOffers();
	}

	@GET
	@Path("/flexoffers/{foid}/state")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Override
	public FlexOfferState getFlexOfferState(@PathParam("foid") int flexOfferId) {
//		// if using arrowhead core services
//		if (null != this.arrowheadSubsystem) {
//			boolean authorized = false;
//
//			// check if authorized
//			// authorized = this.arrowheadSubsystem.checkAuthorisation(dn,
//			// serviceType, serviceId);
//
//			if (!authorized) {
//				// not authorized
//				return null;
//			}
//		}
		return this.agent.getFlexOfferState(flexOfferId);
	}

	@PUT
	@Path("/flexoffers/{foid}/state")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Override
	public void setFlexOfferState(@PathParam("foid") int flexOfferId,
			@QueryParam("state") FlexOfferState flexOfferState,
			@QueryParam("reason") String stateReason) throws FlexOfferException {
		this.agent.setFlexOfferState(flexOfferId, flexOfferState, stateReason);
	}

	@POST
	@Path("/flexoffers/{foid}/schedule")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Override
	public void createFlexOfferSchedule(@PathParam("foid") int flexOfferId,
			FlexOfferSchedule flexOfferSchedule) throws FlexOfferException {
		this.agent.createFlexOfferSchedule(flexOfferId, flexOfferSchedule);
	}

	@GET
	@Path("/flexoffers/{foid}/schedule")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Override
	public FlexOfferSchedule getFlexOfferSchedule(
			@PathParam("foid") int flexOfferId) {
//		// if using arrowhead core services
//		if (null != this.arrowheadSubsystem) {
//			boolean authorized = false;
//
//			// check if authorized
//			// authorized = this.arrowheadSubsystem.checkAuthorisation(dn,
//			// serviceType, serviceId);
//
//			if (!authorized) {
//				// not authorized
//				return null;
//			}
//		}
		return this.agent.getFlexOfferSchedule(flexOfferId);
	}

	@PUT
	@Path("/flexoffers/{foid}/schedule")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Override
	public void setFlexOfferSchedule(@PathParam("foid") int flexOfferId,
			FlexOfferSchedule flexOfferSch) throws FlexOfferException {
		this.agent.setFlexOfferSchedule(flexOfferId, flexOfferSch);
	}

	@DELETE
	@Path("flexoffers/{foid}/schedule")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Override
	public void deleteFlexOfferSchedule(@PathParam("foid") int flexOfferId)
			throws FlexOfferException {
		this.agent.deleteFlexOfferSchedule(flexOfferId);
	}

	/*
	 * Arrowhead Framework
	 */

	@GET
	@Path("/connectionArrowhead")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public BooleanWrapper isConnectedArrowhead() {
		return new BooleanWrapper(false);
//		return new BooleanWrapper(
//				this.arrowheadSubsystem.isConnectedArrowhead());
	}

	@POST
	@Path("/connectionArrowhead")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public void connectArrowhead(BooleanWrapper value) throws XMPPException,
			ArrowheadException {
//		if (value.getValue()) {
//			this.arrowheadSubsystem.init();
//		} else {
//			this.arrowheadSubsystem.shutdownArrowheadApp();
//		}
	}

	@POST
	@Path("/publishServiceProducer")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public BooleanWrapper publishServiceProducer(String producerName)
			throws ArrowheadException {
		// this.arrowheadSubsystem.publishService(producerName);
		return new BooleanWrapper(true);
	}

	@POST
	@Path("/unPublishServiceProducer")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public BooleanWrapper unPublishServiceProducer(String producerName)
			throws ArrowheadException {
		// this.arrowheadSubsystem.unPublishService(producerName);
		return new BooleanWrapper(true);
	}

	@GET
	@Path("/localServiceProducers")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public String localServiceProducers() throws ServiceNotStartedException {
		return "";
		// return this.arrowheadSubsystem.getLocalServiceProducers();
	}

	@GET
	@Path("/lookupServiceProducers")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public String lookupArrowheadProducers() throws ArrowheadException {
		return "{}";
		// return "{\"producersList\":"+new Gson().toJson(this.arrowheadSubsystem.lookupServiceProducers(FOAggregatorServiceTypes.XMPP_FOAGG_SECURE))+"}";
	}

	// @GET
	// @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	// public FlexOffer[] getFlexOffers() throws FlexOfferException {
	// return agent.getFlexOffers();
	// }
	//
	// @DELETE
	// public Response deleteFlexOffers() {
	// agent.deleteFlexOffers();
	// return Response.ok().build();
	// }
	//
	// @GET
	// @Path("/{foid}/execute")
	// @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	// public boolean executeFlexOfferResource(
	// @PathParam("foid") Integer flexOfferId) throws FlexOfferException {
	//
	// agent.setFlexOfferState(flexOfferId, FlexOfferState.Executed, "");
	// // Fix the following call to get the specific DER and not the first one
	// // (getDers().get(0)...);
	// agent.getAgentDers()
	// .get(0)
	// .updateSchedule(
	// agent.getFlexOffer(
	// flexOfferId));
	// return true;
	// }
	//
	// @GET
	// @Path("/{foid}")
	// @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	// public FlexOffer getFlexOffer(@PathParam("foid") Integer flexOfferId)
	// throws FlexOfferException {
	// return agent.getFlexOffer(flexOfferId);
	// }
	//
	// @PUT
	// @Path("/{foid}")
	// @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	// public void setFlexOffer(@PathParam("foid") Integer flexOfferId,
	// JAXBElement<FlexOffer> msg) throws FlexOfferException {
	// FlexOffer flexOffer = msg.getValue();
	// agent.setFlexOffer(flexOfferId, flexOffer);
	// }
	//
	// @DELETE
	// @Path("/{foid}")
	// @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	// public void deleteFlexOffer(@PathParam("foid") Integer flexOfferId)
	// throws FlexOfferException {
	// agent.deleteFlexOffer(flexOfferId);
	// }
	//
	// @GET
	// @Path("/{foid}/state")
	// @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	// public FlexOfferState getFlexOfferState(
	// @PathParam("foid") Integer flexOfferId) throws FlexOfferException {
	// return agent.getFlexOfferState(flexOfferId);
	// }
	//
	// @PUT
	// @Path("/{foid}/state")
	// @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	// public void setFlexOfferState(@PathParam("foid") Integer flexOfferId,
	// JAXBElement<FlexOfferState> msg) throws FlexOfferException {
	// FlexOfferState flexOfferState = msg.getValue();
	// agent.setFlexOfferState(flexOfferId, flexOfferState, null);
	// }
	//
	// @GET
	// @Path("/{foid}/schedule")
	// @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	// public FlexOfferSchedule getFlexOfferSchedule(
	// @PathParam("foid") Integer flexOfferId) throws FlexOfferException {
	// return agent.getFlexOfferSchedule(flexOfferId);
	// }
	//
	// @PUT
	// @Path("/{foid}/schedule")
	// @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	// public void setFlexOfferSchedule(@PathParam("foid") Integer flexOfferId,
	// JAXBElement<FlexOfferSchedule> msg) throws FlexOfferException {
	// FlexOfferSchedule flexOfferSch = msg.getValue();
	// agent.setFlexOfferSchedule(flexOfferId, flexOfferSch);
	//
	// }
	//
	// @DELETE
	// @Path("/{foid}/schedule")
	// @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	// public void deleteFlexOfferSchedule(@PathParam("foid") Integer
	// flexOfferId)
	// throws FlexOfferException {
	// agent.deleteFlexOfferSchedule(flexOfferId);
	// }

}
