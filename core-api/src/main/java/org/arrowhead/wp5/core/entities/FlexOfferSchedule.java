package org.arrowhead.wp5.core.entities;

/*-
 * #%L
 * ARROWHEAD::WP5::Core Data Structures
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


import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The flexoffer schedule
 * 
 * @author IL0021B
 * @version 1.0
 * @created 19-Kov-2014 11:41:17
 */
@XmlRootElement(name="flexOfferSchedule")
@XmlAccessorType(XmlAccessType.FIELD)
public class FlexOfferSchedule implements Serializable {
	private static final long serialVersionUID = -4483570615558363704L;

	/***
	 * Specifies the energy fix. For each EnergyInterval in the original
	 * flex-offer, there is one value (energy fix) in this object. The schedule
	 * is correct if for each energy interval fixed value is between lower and
	 * upper bounds of the EnergyConstraint. The length of this time series must
	 * be equal to the length of the energyIntervals array of the FlexOffer
	 * class.
	 * 
	 * It is not a time series on purpose!!! The time series does not support intervals of 
	 * variable duration! Therefore, using "double[]" would distinct the semantical differences 
	 * from the time series. 
	 */
	@XmlElement
	private double[] energyAmounts;

	/***
	 * The startInterval represents a time fix of the flex-offer. In other
	 * words, this flex-offer is scheduled at the start time of this time
	 * series. For the correct schedule, it must always be within the flex-offer
	 * flexibilities.
	 */
	@XmlElement
	private long startInterval;
	

	/**
	 * Initializes an empty schedule
	 * 
	 */
	public double getEnergyAmount(int i) {
		return this.energyAmounts[i];
	}

	public double[] getEnergyAmounts() {
		return this.energyAmounts;
	}

	public long getStartInterval() {
		return this.startInterval;
	}
	
	@XmlAttribute
	public Date getStartTime() {
		return FlexOffer.toAbsoluteTime(this.startInterval);
	}
	
	public void setStartTime(Date time) {
		this.startInterval = FlexOffer.toFlexOfferTime(time);
	}
		
	/**
	 * Initializes an empty flex-schedule
	 */
	public FlexOfferSchedule() { }
	
	/**
	 * Clones the flex-offer schedule.
	 * 
	 * @return a clone of this instance
	 */
	@Override
	public FlexOfferSchedule clone() {
		return new FlexOfferSchedule(this);
	}
	
	/**
	 * Initializes some random schedule for a provided flex-offer
	 */	
	public FlexOfferSchedule(FlexOffer f) 
	{ 
		this.setStartInterval((f.getStartAfterInterval() + 
						       f.getStartBeforeInterval()) / 2);
		this.setEnergyAmounts(new double[f.getSlices().length]);

		for (int i = 0; i < this.getEnergyAmounts().length; i++) {
			double initialAmount = (f.getSlice(i).getEnergyUpper() + 
							f.getSlice(i).getEnergyLower()) / 2;
			this.getEnergyAmounts()[i] = initialAmount;		
		}
	}
	
	public FlexOfferSchedule(FlexOfferSchedule sch) 
	{ 
		this.setStartInterval(sch.getStartInterval());
		this.setEnergyAmounts(sch.getEnergyAmounts().clone());
	}

	public FlexOfferSchedule(long startInterval, double[] energyAmounts) {
		super();
		this.energyAmounts = energyAmounts;
		this.startInterval = startInterval;
	}

	
	public void setEnergyAmounts(double[] energyAmounts) {
		this.energyAmounts = energyAmounts;
	}

	public void setStartInterval(long startInterval) {
		this.startInterval = startInterval;
	}

	/**
	 * Return the sum of energy defined by this FlexSchedule
	 * 
	 * @return
	 */
	public double getTotalEnergy() {
		double sum = 0;
		for (int i = 0; i < this.energyAmounts.length; i++)
			sum += this.energyAmounts[i];
		return sum;
	}
	
	@Override
	public String toString() {
		final String tab = "\t";
		StringBuilder result = new StringBuilder();
		result.append("S:" + tab + this.startInterval + tab);
		result.append(";E:(" + tab);
		for (int i = 0; i < this.energyAmounts.length; i++)
		{
			result.append(this.energyAmounts[i]);
			if (i < this.energyAmounts.length - 1)
				result.append(",");
		}
		result.append(")");
		return result.toString();
	}
	
	
	public boolean isCorrect(FlexOffer flexOffer) {
		
		if (flexOffer == null) return false;
		
		if (this.getStartInterval() < flexOffer.getStartAfterInterval()
				|| this.getStartInterval() > flexOffer.getStartBeforeInterval()){
			System.out.println("Interval issue: FOSch start=>" + this.getStartInterval() + " FO start after=>" + flexOffer.getStartAfterInterval() + 
					" FO start before=>" + flexOffer.getStartBeforeInterval());
			return false;
		}
		if ((this.getEnergyAmounts() == null)
				|| (this.getEnergyAmounts().length != flexOffer.getSlices().length)){
			System.out.println("Energy Amount issue");
			return false;
		}
		for (int i = 0; i < this.getEnergyAmounts().length; i++){
			if (this.getEnergyAmounts()[i] < flexOffer.getSlices()[i].getEnergyLower()
					|| this.getEnergyAmounts()[i] > flexOffer.getSlices()[i].getEnergyUpper())
			{
				System.out.println("Energy Amount 2 issue");
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(energyAmounts);
		result = prime * result
				+ (int) (startInterval ^ (startInterval >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FlexOfferSchedule other = (FlexOfferSchedule) obj;
		if (!Arrays.equals(energyAmounts, other.energyAmounts))
			return false;
		if (startInterval != other.startInterval)
			return false;
		return true;
	}

}//end FlexOfferSchedule