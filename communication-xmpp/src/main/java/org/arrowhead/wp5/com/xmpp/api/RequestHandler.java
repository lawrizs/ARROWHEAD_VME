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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.xml.bind.JAXBException;

import org.arrowhead.wp5.com.xmpp.util.HOXTUtil;
import org.jivesoftware.smack.packet.NamedElement;
import org.jivesoftware.smackx.hoxt.packet.AbstractHttpOverXmpp;
import org.jivesoftware.smackx.hoxt.packet.HttpOverXmppReq;
import org.jivesoftware.smackx.hoxt.packet.HttpOverXmppResp;

public final class RequestHandler {

    private RequestHandler() {
    }

    public static HttpOverXmppResp handleRequest(HttpOverXmppReq request,
            ResourceManager rm) {
        HttpOverXmppResp response = new HttpOverXmppResp();
        String uri = request.getResource();
        Map<String, String> params = new HashMap<String, String>();
        Resource r = rm.getResourceMatch(uri, params);
        if (r == null) {
            return HOXTUtil.set404(response);
        }
        Method method = r.methods[request.getMethod().ordinal()];
        if (method == null) {
            return HOXTUtil.set405(response);
        }
        method.setAccessible(true);
        Object ret = null;
        try {
            if (r.getInst() != null) {
                synchronized (r.getInst()) {
                    ret = invokeMethod(r.getInst(), method, params, request);
                }
            } else {
                ret = invokeMethod(null, method, params, request);
            }
            if (ret != null && ret.getClass() != Integer.class && ret.getClass() != String.class) {
                response.setData(HOXTUtil.getDataFromObject(ret.getClass(), ret));
                HOXTWrapper.addRequired(response, false);
            } else if (ret != null && ret.getClass() == String.class) {
                AbstractHttpOverXmpp.Text child = new AbstractHttpOverXmpp.Text((String) ret);
                AbstractHttpOverXmpp.Data data = new AbstractHttpOverXmpp.Data(child);
                response.setData(data);
                HOXTWrapper.addRequired(response, false);
            } else {
                HOXTWrapper.addRequired(response, true);
            }
        } catch (JAXBException e) {
            return HOXTUtil.set500(response);
        } catch (WebApplicationException e) {
            return HOXTUtil.setStatus(response, e.getMessage(), e.getResponse().getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            return HOXTUtil.set500(response);
        }
        response.setStatusCode(200);
        return response;
    }

    private static Object invokeMethod(Object inst, Method method,
            Map<String, String> params, HttpOverXmppReq request)
            throws InstantiationException, IllegalAccessException,
            JAXBException, IllegalArgumentException, InvocationTargetException {
        if (inst == null) {
            inst = method.getDeclaringClass().newInstance();
        }
        Annotation annotations[][] = method.getParameterAnnotations();
        Class<?> types[] = method.getParameterTypes();
        Object args[] = new Object[types.length];
        for (int i = 0; i < annotations.length; i++) {
            for (int j = 0; j < annotations[i].length; j++) {
                if (annotations[i][j] instanceof PathParam) {
                    String paramName = ((PathParam) annotations[i][j]).value();
                    if (types[i] == Integer.TYPE) {
                        args[i] = Integer.parseInt(params.get(paramName));
                    } else {
                        args[i] = params.get(paramName);
                    }
                }
            }
            if (args[i] == null) {
                NamedElement child = request.getData().getChild();
                if (child instanceof AbstractHttpOverXmpp.Xml) {
                    String s = ((AbstractHttpOverXmpp.Xml) child).getText();
                    s = s.replace("nil=\"true\"", "xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
                    args[i] = HOXTUtil.getEntity(s, types[i]);
                } else if (child instanceof AbstractHttpOverXmpp.Text) {
                    args[i] = ((AbstractHttpOverXmpp.Text) child).getText();
                } else {
                    // process other AbstractHttpOverXmpp.DataChild subtypes
                }
            }
        }
        Object o = null;
        try {
            o = method.invoke(inst, args);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof WebApplicationException) {
                throw (WebApplicationException) e.getCause();
            }
        }
        return o;
    }
}
