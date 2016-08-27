package org.arrowhead.wp5.fom.xresources;

/*-
 * #%L
 * flexoffer-tutorial
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

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferException;
import org.arrowhead.wp5.core.entities.FlexOfferSchedule;
import org.arrowhead.wp5.core.entities.FlexOfferState;
import org.arrowhead.wp5.core.impl.FlexOfferAgent;
import org.arrowhead.wp5.core.interfaces.FlexOfferAgentProviderIf;

/*Set the path where for the resource*/
@Path("/flexoffers")
public class XFlexOfferResource implements FlexOfferAgentProviderIf{

	/*We need the flexoffer agent to interface with flexoffers*/
	FlexOfferAgent flexOfferAgent;
	
	public XFlexOfferResource(FlexOfferAgent flexOfferAgent) {
		this.flexOfferAgent = flexOfferAgent;
	}
	
	@Override
	public FlexOffer getFlexOffer(int flexOfferId) throws FlexOfferException {
		// Not needed for now
		return null;
	}

	@Override
	public FlexOffer[] getFlexOffers() {
		// Not needed for now
		return null;
	}

	@Override
	public FlexOfferState getFlexOfferState(int flexOfferId) {
		// Not needed for now
		return null;
	}

	@Override
	public void setFlexOfferState(int flexOfferId,
			FlexOfferState flexOfferState, String stateReason)
			throws FlexOfferException {
		// Not needed for now
	}

	/*Used when receiving schedule*/
	@Override
	@Path("/{foid}/schedule")
	@POST
	public void createFlexOfferSchedule(@PathParam("foid") int flexOfferId,
			FlexOfferSchedule flexOfferSchedule) throws FlexOfferException {
		this.flexOfferAgent.createFlexOfferSchedule(flexOfferId, flexOfferSchedule);
	}

	@Override
	public FlexOfferSchedule getFlexOfferSchedule(int flexOfferId) {
		// Not needed for now
		return null;
	}

	@Override
	public void setFlexOfferSchedule(int flexOfferId,
			FlexOfferSchedule flexOfferSch) throws FlexOfferException {
		// Not needed for now		
	}

	@Override
	public void deleteFlexOfferSchedule(int flexOfferId)
			throws FlexOfferException {
		// Not needed for now		
	}

}
