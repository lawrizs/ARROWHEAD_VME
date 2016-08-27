package org.arrowhead.wp5.com.xmpp.clients.marketclient;

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
import org.arrowhead.wp5.core.entities.Bid;
import org.arrowhead.wp5.core.entities.MarketException;
import org.arrowhead.wp5.core.entities.MarketInfo;
import org.arrowhead.wp5.core.interfaces.MarketProviderIf;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smackx.hoxt.packet.AbstractHttpOverXmpp.Xml;
import org.jivesoftware.smackx.hoxt.packet.HttpMethod;
import org.jivesoftware.smackx.hoxt.packet.HttpOverXmppReq;
import org.jivesoftware.smackx.hoxt.packet.HttpOverXmppResp;
import org.jxmpp.stringprep.XmppStringprepException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMarketProviderClient implements MarketProviderIf {
	private HOXTWrapper hoxtWrapper;
	private String marketId;

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	public XMarketProviderClient(String marketId, HOXTWrapper hoxtWrapper) {
		this.hoxtWrapper = hoxtWrapper;
		this.marketId = marketId;
	}

	@Override
	public MarketInfo getInfo() throws MarketException {
		if (hoxtWrapper == null || !hoxtWrapper.isConnected())
			throw new MarketException("Not connected.");

		HttpOverXmppReq request = new HttpOverXmppReq(HttpMethod.GET,
				"/market/info");
		HOXTWrapper.addRequired(request, true);
		HttpOverXmppResp resp;
		MarketInfo result = null;
		try {
			resp = this.hoxtWrapper.sendRequest(request, marketId
					+ "@delling/demo");
			Xml xml = (Xml) resp.getData().getChild();
			result = HOXTUtil.getEntity(xml.getText(), MarketInfo.class);
		} catch (JAXBException | NotConnectedException | HOXTException
				| XmppStringprepException e) {
			throw new MarketException(e.getMessage());
		}

		return result;
	}

	@Override
	public void bidSupply(Bid bid) throws MarketException {
		bid(bid, "supply");
	}

	@Override
	public void bidDemand(Bid bid) throws MarketException {
		bid(bid, "demand");
	}

	private void bid(Bid bid, String uri) throws MarketException {
		if (hoxtWrapper == null)
			throw new MarketException("Not connected.");
		logger.debug("Bid: {}", bid);
		HttpOverXmppReq request = new HttpOverXmppReq(HttpMethod.POST,
				"/market/" + uri);
		try {
			request.setData(HOXTUtil.getDataFromObject(Bid.class, bid));
			HOXTWrapper.addRequired(request, false);
		} catch (JAXBException e1) {
			e1.printStackTrace();
			throw new MarketException(e1.getMessage());
		}

		try {
			this.hoxtWrapper.sendRequest(request, marketId + "@delling/demo");
		} catch (NotConnectedException | HOXTException
				| XmppStringprepException e) {
			throw new MarketException(e.getMessage());
		}

		/** TODO use response for error handling */

	}
}
