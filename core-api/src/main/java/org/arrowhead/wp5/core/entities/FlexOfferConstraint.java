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
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A representation of constraint specified as a continuous range 
 *  
 * @author Laurynas
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FlexOfferConstraint implements Serializable {
	private static final long serialVersionUID = -4389233123533413103L;
	/*
	 * energy constrains
	 */
	@XmlAttribute
	private double lower;
	@XmlAttribute
	private double upper;
		
	public FlexOfferConstraint()
	{
		this.lower = 0;
		this.upper = 0;		
	}
	
	/**
	 * Initializes the energy constraint using only two numbers 
	 * @param lowerN
	 * @param upperN
	 * @author Laurynas
	 */
	public FlexOfferConstraint(double lowerN, double upperN)
	{
		this.lower = lowerN;
		this.upper = upperN;		
	}

	/**
	 * Initializes the energy constraint using the given energyConstraint.
	 * 
	 * @param constraint the given energy constraint
	 */
	public FlexOfferConstraint(FlexOfferConstraint constraint) {
		this.lower = constraint.getLower();
		this.upper = constraint.getUpper();
	}

	/**
	 * Clones the energy constraint.
	 * 
	 * @return a clone of this instance
	 */
	@Override
	public FlexOfferConstraint clone() {
		return new FlexOfferConstraint(this);
	}

	/**
	 * Returns the lower energy bound.
	 * 
	 * @return the lower energy bound
	 */
	public double getLower() {
		return lower;
	}

	/**
	 * Sets the lower energy bound.
	 * 
	 * @param lowerN new value for the lower energy bound
	 */
	public void setLower(double lowerN) {
		lower = lowerN + 0.0;
	}

	/**
	 * Returns the upper energy bound.
	 * 
	 * @return the upper energy bound
	 */
	public double getUpper() {
		return upper;
	}

	/**
	 * Sets the upper energy bound.
	 * 
	 * @param upperN new value for the upper energy bound
	 */
	public void setUpper(double upperN) {
		upper = upperN + 0.0;
	}

	/**
	 * Returns true if only one energy bound is given and false otherwise.
	 * Meaning that there is no energy flexibility
	 * 
	 * @return true if only one energy bound is given and false otherwise
	 */
	public boolean isSingle() {
		return (Math.abs(lower - upper) < 1E-5);
	}
		
	@Override
	public String toString() {
		return "{"+lower+","+upper+"}";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(lower);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(upper);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		FlexOfferConstraint other = (FlexOfferConstraint) obj;
		if (Double.doubleToLongBits(lower) != Double
				.doubleToLongBits(other.lower))
			return false;
		if (Double.doubleToLongBits(upper) != Double
				.doubleToLongBits(other.upper))
			return false;
		return true;
	}	

}
