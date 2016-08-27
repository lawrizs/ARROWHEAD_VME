package org.arrowhead.wp5.com.xmpp.api;

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

import java.lang.reflect.Method;
import java.util.Map;

import org.glassfish.jersey.uri.UriTemplate;
import org.jivesoftware.smackx.hoxt.packet.HttpMethod;

public class Resource {
	String url;
	UriTemplate uriTemplate;
	Method methods[];
	Object inst = null;
	
	public Resource(String url){
		this(url, null);
	}
	
	public Resource(String url, Object inst){
		this.inst = inst;
		this.url = url;
		this.uriTemplate = new UriTemplate(url);
		methods = new Method[HttpMethod.values().length];
	}
	
	public Object getInst() {
		return inst;
	}

	public void setInst(Object inst) {
		this.inst = inst;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public UriTemplate getUriTemplate() {
		return uriTemplate;
	}

	public void setUriTemplate(UriTemplate uriTemplate) {
		this.uriTemplate = uriTemplate;
	}

	public Method[] getMethods() {
		return methods;
	}

	public void setMethod(HttpMethod type, Method method){
		 methods[type.ordinal()] = method;
	}
	
	public boolean matchUri(String uri, Map<String, String> params){
		return this.uriTemplate.match(uri, params);
	}
}
