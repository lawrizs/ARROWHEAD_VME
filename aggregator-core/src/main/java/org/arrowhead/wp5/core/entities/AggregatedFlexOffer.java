package org.arrowhead.wp5.core.entities;

/*-
 * #%L
 * ARROWHEAD::WP5::Aggregator Core
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "AggregatedFlexOffer")
public class AggregatedFlexOffer extends FlexOffer {
	private static final long serialVersionUID = 3075566218427044616L;
	
	@XmlElement
	private AggregationLevel aggregationLevel = AggregationLevel.NotAggregated;
	@XmlElement
	private List<AggregatedFlexOfferMetaData> subFoMetas = null;
	
	public AggregatedFlexOffer() { super(); }

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AggregatedFlexOffer other = (AggregatedFlexOffer) obj;
		if (aggregationLevel != other.aggregationLevel)
			return false;
		if (subFoMetas == null) {
			if (other.subFoMetas != null)
				return false;
		} else if (!subFoMetas.equals(other.subFoMetas))
			return false;
		return true;
	}
	
	public AggregationLevel getAggregationLevel() {
		return aggregationLevel;
	}
	
	public FlexOffer[] getSubFlexOffers() {
		if (subFoMetas != null) {
			FlexOffer[] fL = new FlexOffer[this.subFoMetas.size()];
			for (int i = 0; i < this.subFoMetas.size(); i++)
				fL[i] = this.subFoMetas.get(i).getSubFlexOffer();
			return fL;
		} else
			return null;
	}
		
	public List<AggregatedFlexOfferMetaData> getSubFoMetas() {
		return subFoMetas;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((aggregationLevel == null) ? 0 : aggregationLevel.hashCode());
		result = prime * result
				+ ((subFoMetas == null) ? 0 : subFoMetas.hashCode());
		return result;
	}

	public void setAggregationLevel(AggregationLevel aggregationLevel)
	{
		this.aggregationLevel = aggregationLevel;
	}


	public void setSubFoMetas(List<AggregatedFlexOfferMetaData> subFoMetas) {
		this.subFoMetas = subFoMetas;
	}
	
}