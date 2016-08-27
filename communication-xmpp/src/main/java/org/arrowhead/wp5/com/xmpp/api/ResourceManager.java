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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.jivesoftware.smackx.hoxt.packet.HttpMethod;
import org.reflections.Reflections;

public class ResourceManager {

	List<Resource> resources;

	public ResourceManager() {
		resources = new ArrayList<Resource>();
	}

	public ResourceManager(String pkgName) {
		this();
		Reflections reflections = new Reflections(pkgName);
		Set<Class<?>> beans = reflections.getTypesAnnotatedWith(Path.class);

		for (Class<?> c : beans) {
			this.addBeanResources(c, null);
		}
	}

	public void registerInstance(Object inst) {
		addBeanResources(inst.getClass(), inst);
	}

	private void addBeanResources(Class<?> c, Object inst) {
		Annotation[] annotations = c.getAnnotations();
		for (Annotation a : annotations) {
			if (a instanceof Path) {
				Resource r = new Resource(((Path) a).value(), inst);
				resources.add(r);
				addResourceMethods(c, r);
			}
		}
	}

	private void addResourceMethods(Class<?> c, Resource r) {
		Map<String, Resource> tmpResources = new HashMap<String, Resource>();
		for (Method method : c.getMethods()) {
			if (method.getAnnotations() != null) {
				List<HttpMethod> types = new ArrayList<HttpMethod>();
				Resource tmpResource = r;
				for (Annotation a : method.getAnnotations()) {
					if (a instanceof Path) {
						if (!tmpResources.containsKey(r.getUrl() + ((Path) a).value())) {
							tmpResource = new Resource(r.getUrl()
									+ ((Path) a).value(), r.getInst());
						} else {
							tmpResource = tmpResources.get(r.getUrl() + ((Path) a).value());
						}
					} else if (a instanceof GET) {
						types.add(HttpMethod.GET);
					} else if (a instanceof PUT) {
						types.add(HttpMethod.PUT);
					} else if (a instanceof POST) {
						types.add(HttpMethod.POST);
					} else if (a instanceof DELETE) {
						types.add(HttpMethod.DELETE);
					} else if (a instanceof HEAD) {
						types.add(HttpMethod.HEAD);
					}
				}
				if (!types.isEmpty()) {
					for (HttpMethod type : types) {
						tmpResource.setMethod(type, method);
					}
					tmpResources.put(tmpResource.getUrl(), tmpResource);
				}
			}
		}
		for (Resource tmpR : tmpResources.values()) {
			resources.add(tmpR);
		}
	}

	public Resource getResourceMatch(String uri, Map<String, String> params) {
		for (Resource r : resources) {
			if (r.matchUri(uri, params)) {
				return r;
			}
		}
		return null;
	}

}
