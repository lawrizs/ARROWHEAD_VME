package org.arrowhead.wp5.com.xmpp.clients.flexofferclient;

/*-
 * #%L
 * ARROWHEAD::WP5::Communication XMPP
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

import javax.xml.bind.JAXBException;

import org.arrowhead.wp5.com.xmpp.api.HOXTWrapper;
import org.arrowhead.wp5.com.xmpp.exceptions.HOXTException;
import org.arrowhead.wp5.com.xmpp.util.HOXTUtil;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferException;
import org.arrowhead.wp5.core.entities.FlexOfferSchedule;
import org.arrowhead.wp5.core.entities.FlexOfferState;
import org.arrowhead.wp5.core.interfaces.FlexOfferProviderIf;
import org.arrowhead.wp5.core.wrappers.FlexOfferStateWrapper;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smackx.hoxt.packet.HttpMethod;
import org.jivesoftware.smackx.hoxt.packet.HttpOverXmppReq;
import org.jivesoftware.smackx.hoxt.packet.HttpOverXmppResp;
import org.jxmpp.stringprep.XmppStringprepException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XFlexOfferProviderClient implements FlexOfferProviderIf {

	final Logger logger = LoggerFactory.getLogger(this.getClass());
	private HOXTWrapper hoxtWrapper;

	private String subscriberId = null;

	public XFlexOfferProviderClient(HOXTWrapper hoxtWrapper) {
		this.hoxtWrapper = hoxtWrapper;
	}

	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}

	@Override
	public FlexOffer getFlexOffer(int flexOfferId) throws FlexOfferException {
		throw new UnsupportedOperationException(
				"FOA does YET support this operation");
	}

	@Override
	public FlexOffer[] getFlexOffers() {
		throw new UnsupportedOperationException(
				"FOA does YET support this operation");
	}

	@Override
	public FlexOfferState getFlexOfferState(int flexOfferId) {
		throw new UnsupportedOperationException(
				"FOA does YET support this operation");
	}

	@Override
	public void setFlexOfferState(int flexOfferId,
			FlexOfferState flexOfferState, String stateReason)
			throws FlexOfferException {
		if (this.subscriberId == null) {
			throw new FlexOfferException("No subsriber id set");
		}
		HttpOverXmppReq req = new HttpOverXmppReq(HttpMethod.PUT, "/flexoffers/" + flexOfferId + "/state");
		HttpOverXmppResp resp;
		try {
			req.setData(HOXTUtil.getDataFromObject(FlexOfferStateWrapper.class,
					new FlexOfferStateWrapper(flexOfferState)));
			HOXTWrapper.addRequired(req, true);
			resp = this.hoxtWrapper.sendRequest(req, subscriberId
					+ "@delling/demo");
			if (resp.getStatusCode() != 200) {
				/** Should not go there! */
				/** TODO better error handling */
				logger.warn("Bad request!");
			}

		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotConnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HOXTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmppStringprepException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	@Override
	public void createFlexOfferSchedule(int flexOfferId,
			FlexOfferSchedule flexOfferSchedule) throws FlexOfferException {
		if (this.subscriberId == null) {
			throw new FlexOfferException("No subsriber id set");
		}
		HttpOverXmppReq request = new HttpOverXmppReq(HttpMethod.POST, "/flexoffers/" + Integer.toString(flexOfferId)
				+ "/schedule");
		HttpOverXmppResp resp;
		try {
			request.setData(HOXTUtil.getDataFromObject(FlexOfferSchedule.class, flexOfferSchedule));
			HOXTWrapper.addRequired(request, false);
			resp = this.hoxtWrapper.sendRequest(request, subscriberId
					+ "@delling/demo");
			if (!(resp.getStatusCode() == 201 || resp.getStatusCode() == 200)) {
				/** Should not go there! */
				/** TODO better error handling */
				logger.debug("Bad request!");
				logger.debug("status code: {}", resp.getStatusCode());
				logger.debug("status msg: {}", resp.getStatusMessage());
				logger.debug("status error: {}", resp.getError());
			}
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotConnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HOXTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmppStringprepException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	@Override
	public FlexOfferSchedule getFlexOfferSchedule(int flexOfferId) {
		throw new UnsupportedOperationException(
				"FOA does YET support this operation");
	}

	@Override
	public void setFlexOfferSchedule(int flexOfferId,
			FlexOfferSchedule flexOfferSch) throws FlexOfferException {
		throw new UnsupportedOperationException(
				"FOA does YET support this operation");
	}

	@Override
	public void deleteFlexOfferSchedule(int flexOfferId)
			throws FlexOfferException {
		throw new UnsupportedOperationException(
				"FOA does YET support this operation");
	}

}
