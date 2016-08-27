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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * A single slice (interval) in the flexoffer profile. 
 * @author Laurynas
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FlexOfferSlice implements Serializable {	
	private static final long serialVersionUID = -1249370306408067834L;
	/**
	 * Span of slice
	 */
	@XmlTransient
	private long duration=1;
	@XmlElement
	private double costPerEnergyUnitLimit;
	@XmlElement
	private FlexOfferConstraint energyConstraint;
	@XmlElement
	private FlexOfferConstraint tariffConstraint;
	
	@Override
	public String toString() {
		return energyConstraint.toString()+"["+duration+"] ("+costPerEnergyUnitLimit+"â‚¬)";
	}
	
	public FlexOfferSlice() {
		super();
	}

	/**
	 * Initializes the energy slice.
	 *
	 * @param durationN duration of the slice
	 * @param costPerEnergyUnitN cost per energy unit for this slice
	 * @param lowerBoundN the lower bound for this slice
	 * @param upperBoundN the upper bound for this slice
	 */
	public FlexOfferSlice(long durationN, double costPerEnergyUnitN, double lowerBoundN, double upperBoundN) {
		duration = durationN;
		costPerEnergyUnitLimit = costPerEnergyUnitN;
		tariffConstraint = new FlexOfferConstraint(costPerEnergyUnitN*lowerBoundN, costPerEnergyUnitN*upperBoundN);
		energyConstraint = new FlexOfferConstraint(lowerBoundN, upperBoundN);
	}

	/**
	 * Initializes the energy slice using the given slice.
	 * 
	 * @param energySlice the given energy slice
	 */
	public FlexOfferSlice(FlexOfferSlice energySlice) {
		this.duration = energySlice.getDuration();
		this.costPerEnergyUnitLimit = energySlice.getCostPerEnergyUnitLimit();
		this.tariffConstraint = new FlexOfferConstraint(energySlice.getTariffConstraint());
		this.energyConstraint = new FlexOfferConstraint(energySlice.getEnergyConstraint());
	}

	/**
	 * Returns the duration of the energy slice.
	 * 
	 * @return the duration of the energy slice
	 */
	public long getDuration() {
		return duration;
	}
	
	@XmlAttribute
	public long getDurationSeconds() {
		return duration * FlexOffer.numSecondsPerInterval();
	}
	
	/***
	 * Sets new duration
	 * 
	 * @param duration
	 * @author Laurynas
	 */
	public void setDuration(long duration)
	{
		this.duration = duration;
	}
	
	public void setDurationSeconds(long seconds)
	{
		this.duration = seconds / FlexOffer.numSecondsPerInterval();
	}
	 
	/**
	 * Returns the cost per energy unit for this interval.
	 * 
	 * @return the cost per energy unit for this interval
	 */
	public double getCostPerEnergyUnitLimit() {
		return costPerEnergyUnitLimit;
	}
	
	public void setCostPerEnergyUnitLimit(double costPerEnergyUnit)
	{
		this.costPerEnergyUnitLimit = costPerEnergyUnit;
	}
	
	/**
	 * Sets the energy constraint of this interval.
	 * 
	 * @param energyConstraint the energy constraint to set
	 */
	public void setEnergyConstraint(FlexOfferConstraint energyConstraint) {
		this.energyConstraint = energyConstraint;
	}
	
	/**
	 * Returns the energy constraint of this interval.
	 * 
	 * @return the energy constraint of this interval
	 */
	public FlexOfferConstraint getEnergyConstraint() {
		return energyConstraint;
	}

	/**
	 * Sets the tariff constraint of this interval.
	 * 
	 * @param tairffConstraint the tariff constraint to set
	 */
	public void setTariffConstraint(FlexOfferConstraint tariffConstraint) {
		this.tariffConstraint = tariffConstraint;
	}
	
	/**
	 * Returns the energy constraint of this interval.
	 * 
	 * @return the energy constraint of this interval
	 */
	public FlexOfferConstraint getTariffConstraint() {
		return tariffConstraint;
	}

	/**
	 * Returns the lower bound for this interval.
	 * 
	 * @return the lower bound for this interval
	 */
	public double getEnergyLower() {
		return energyConstraint.getLower();
	}

	/**
	 * Returns the upper bound for this interval.
	 * 
	 * @return the upper bound for this interval
	 */
	public double getEnergyUpper() {
		return energyConstraint.getUpper();
	}
	
	/**
	 * Checks if the slice is for consumption
	 * @return
	 */
	public boolean isConsumption() {
		return energyConstraint.getLower() < 0;
	}
	
	/**
	 * Checks if the slice is for production
	 * @return
	 */
	public boolean isProduction() {
		return energyConstraint.getUpper() > 0;
		
	}


	/**
	 * Sets the lower and upper energy of the slice to the given value and sets the single value to true.
	 * Laurynas: consider renaming the method
	 * @param energy
	 */
	public void setToSingularEnergy(double energy) {
		energyConstraint.setLower(energy);
		energyConstraint.setUpper(energy);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(costPerEnergyUnitLimit);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (duration ^ (duration >>> 32));
		result = prime
				* result
				+ ((energyConstraint == null) ? 0 : energyConstraint.hashCode());
		result = prime
				* result
				+ ((tariffConstraint == null) ? 0 : tariffConstraint.hashCode());
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
		FlexOfferSlice other = (FlexOfferSlice) obj;
		if (Double.doubleToLongBits(costPerEnergyUnitLimit) != Double
				.doubleToLongBits(other.costPerEnergyUnitLimit))
			return false;
		if (duration != other.duration)
			return false;
		if (energyConstraint == null) {
			if (other.energyConstraint != null)
				return false;
		} else if (!energyConstraint.equals(other.energyConstraint))
			return false;
		if (tariffConstraint == null) {
			if (other.tariffConstraint != null)
				return false;
		} else if (!tariffConstraint.equals(other.tariffConstraint))
			return false;
		return true;
	}	
}
