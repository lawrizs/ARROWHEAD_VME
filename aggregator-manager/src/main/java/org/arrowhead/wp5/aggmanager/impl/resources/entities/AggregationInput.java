package org.arrowhead.wp5.aggmanager.impl.resources.entities;

/*-
 * #%L
 * ARROWHEAD::WP5::Aggregator Manager
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


import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.arrowhead.wp5.agg.api.FOAggParameters;
import org.arrowhead.wp5.core.entities.FlexOffer;

@XmlRootElement(name="aggregationInput")
public class AggregationInput {
	private List<FlexOffer> flexOffers;
	private FOAggParameters params;
	
	public List<FlexOffer> getFlexOffers() {
		return flexOffers;
	}
	public void setFlexOffers(List<FlexOffer> flexOffers) {
		this.flexOffers = flexOffers;
	}
	public FOAggParameters getParams() {
		return params;
	}
	public void setParams(FOAggParameters params) {
		this.params = params;
	}	
}
