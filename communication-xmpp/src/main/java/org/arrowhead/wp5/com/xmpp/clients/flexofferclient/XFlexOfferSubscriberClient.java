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
import org.arrowhead.wp5.core.interfaces.FlexOfferSubscriberIf;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smackx.hoxt.packet.HttpMethod;
import org.jivesoftware.smackx.hoxt.packet.HttpOverXmppReq;
import org.jivesoftware.smackx.hoxt.packet.HttpOverXmppResp;
import org.jxmpp.stringprep.XmppStringprepException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XFlexOfferSubscriberClient implements FlexOfferSubscriberIf {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    String aggregatorId;
    HOXTWrapper hoxtWrapper;

    public XFlexOfferSubscriberClient(String aggregatorId,
            HOXTWrapper hoxtWrapper) {
        this.aggregatorId = aggregatorId;
        this.hoxtWrapper = hoxtWrapper;
    }

    public void setAggregatorId(String aggregatorId) {
        this.aggregatorId = aggregatorId;
    }

    @Override
    public int createFlexOffer(String ownerId, FlexOffer flexOffer)
            throws FlexOfferException {
        if (hoxtWrapper == null)
            throw new FlexOfferException("Not connected.");
        flexOffer.setOfferedById(ownerId);

        HttpOverXmppReq request = new HttpOverXmppReq(HttpMethod.POST,
                "/flexoffers/" + ownerId);
        /** TODO use response for error handling */
        // HTTPResponse response;

        try {
            request.setData(HOXTUtil.getDataFromObject(FlexOffer.class,
                    flexOffer));
            HOXTWrapper.addRequired(request, false);
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        HttpOverXmppResp resp = null;
        // send request of a flex-offer to the aggregator and handle the
        // response
        try {
			resp = this.hoxtWrapper.sendRequest(request, hoxtWrapper.makeID(aggregatorId));
        } catch (NotConnectedException | HOXTException | XmppStringprepException e) {
            logger.error("Error sending request to create FlexOffer", e);
            return -1;
        }

        if (resp != null && resp.getStatusCode() == 400) {
            logger.debug("Status message: {}", resp.getStatusMessage());
            throw new FlexOfferException(resp.getStatusMessage());
        }

        String id = Integer.toString(flexOffer.getId());

        logger.info("A flex-offer is successfully sent to Aggregator. Received Id is "
                + id);
        return Integer.parseInt(id);
    }

    @Override
    public FlexOffer getFlexOffer(String foaId, int flexOfferId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setFlexOffer(String foaId, int flexOfferId, FlexOffer flexOffer)
            throws FlexOfferException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteFlexOffer(String foaId, int flexOfferId)
            throws FlexOfferException {
        // TODO Auto-generated method stub

    }

    @Override
    public FlexOffer[] getFlexOffers() {
        // TODO Auto-generated method stub
        return null;
    }

}
