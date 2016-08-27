package org.arrowhead.wp5.com.xmpp.util;

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

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jivesoftware.smackx.hoxt.packet.AbstractHttpOverXmpp;
import org.jivesoftware.smackx.hoxt.packet.HttpOverXmppResp;

public final class HOXTUtil {
	private HOXTUtil(){}
	
	public static AbstractHttpOverXmpp.Data getDataFromObject(Class<?> c, Object entity) throws JAXBException {
			JAXBContext ctx = JAXBContext.newInstance(c);
			Marshaller m = ctx.createMarshaller();
			m.setProperty(Marshaller.JAXB_FRAGMENT, true);
			StringWriter writer = new StringWriter();
			m.marshal(entity, writer);
			System.out.println("Test: " + writer.toString());
			AbstractHttpOverXmpp.Xml child = new AbstractHttpOverXmpp.Xml(writer.toString());
			AbstractHttpOverXmpp.Data data = new AbstractHttpOverXmpp.Data(child);
			return data;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getEntity(String data, Class<?> c) throws JAXBException {
		JAXBContext ctx = JAXBContext.newInstance(c);
		Unmarshaller u = ctx.createUnmarshaller();
		StringReader reader = new StringReader(data);
		return (T) u.unmarshal(reader);
	}
	
	public static HttpOverXmppResp setStatus(HttpOverXmppResp response, String text, int code){
		response.setStatusCode(code);
		AbstractHttpOverXmpp.Text child = new AbstractHttpOverXmpp.Text(text);
		AbstractHttpOverXmpp.Data data = new AbstractHttpOverXmpp.Data(child);
		response.setData(data);
		return response;
	}
	
	public static HttpOverXmppResp set404(HttpOverXmppResp response){
		
		return setStatus(response, "Not Found", 404);
	}
	
	public static HttpOverXmppResp set405(HttpOverXmppResp response){
		return setStatus(response, "Method Not Allowed", 405);
	}
	
	public static HttpOverXmppResp set500(HttpOverXmppResp response){
		return setStatus(response, "Internal Server Error", 500);
	}
}
