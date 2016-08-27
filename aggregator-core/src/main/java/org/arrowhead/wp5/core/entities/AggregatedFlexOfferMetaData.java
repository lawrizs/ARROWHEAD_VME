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


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This generic class represents the meta data of an Aggregated flex-Offer.
 * 
 * @author cv
 * @author Laurynas
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "DiscreteAggFoMetaData")
public class AggregatedFlexOfferMetaData
{
	@XmlAttribute
	private int timeShiftTS; // time shift on sub flex-offer in the aggregate in time stamps
	@XmlElement
	private FlexOffer subFlexOffer = null;
	
	/**
	 * This method clones an Aggregated FlexOffer.
	 */
	@Override
	public AggregatedFlexOfferMetaData clone() {
		AggregatedFlexOfferMetaData m = new AggregatedFlexOfferMetaData();
		m.setTimeShiftTS(this.timeShiftTS);
		m.setSubFlexOffer(this.subFlexOffer);
		return m;
	}
	
	/**
	 * This method returns the subFlexOffer of the Aggregated FlexOffer.	
	 * @return flexOffer
	 */
	
	public FlexOffer getSubFlexOffer() {
		return this.subFlexOffer;
	}
	
	/**
	 * This method returns the timeShiftTS of the Aggregated FlexOffer.
	 * @return timeShiftTS
	 */
	public int getTimeShiftTS() {
		return timeShiftTS;
	}
	
	/**
	 * This method sets the subFlexOffer to f.
	 * @param f
	 */
	public void setSubFlexOffer(FlexOffer f) {
		this.subFlexOffer = f;	
	}
	
	/**
	 * This method sets the timeShiftTs of the Aggregated Flex-Offer to timeShiftTS
	 * @param timeShiftTS
	 */
	public void setTimeShiftTS(int timeShiftTS) {
		this.timeShiftTS = timeShiftTS;
	}
}