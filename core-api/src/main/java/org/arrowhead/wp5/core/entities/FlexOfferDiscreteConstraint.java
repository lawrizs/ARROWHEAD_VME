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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A representation of constraint specified as a discrete range 
 *  
 * @author Laurynas
 *
 **/
@XmlRootElement
public class FlexOfferDiscreteConstraint implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8442203644376078376L;
	/*
	 * energy constrains
	 */
	private long lower;
	private long upper;
	
	/**
	 * Initializes the energy constraint using only two numbers 
	 * @param lowerN
	 * @param upperN
	 * @author Laurynas
	 */
	public FlexOfferDiscreteConstraint(long lowerN, long upperN)
	{
		this.lower = lowerN;
		this.upper = upperN;		
	}

	/**
	 * Initializes the energy constraint using the given energyConstraint.
	 * 
	 * @param constraint the given energy constraint
	 */
	public FlexOfferDiscreteConstraint(FlexOfferDiscreteConstraint constraint) {
		this.lower = constraint.getLower();
		this.upper = constraint.getUpper();
	}

	/**
	 * Clones the energy constraint.
	 * 
	 * @return a clone of this instance
	 */
	@Override
	public FlexOfferDiscreteConstraint clone() {
		return new FlexOfferDiscreteConstraint(this);
	}

	/**
	 * Returns the lower energy bound.
	 * 
	 * @return the lower energy bound
	 */
	public long getLower() {
		return lower;
	}

	/**
	 * Sets the lower energy bound.
	 * 
	 * @param lowerN new value for the lower energy bound
	 */
	public void setLower(long lowerN) {
		lower = lowerN;
	}

	/**
	 * Returns the upper energy bound.
	 * 
	 * @return the upper energy bound
	 */
	public long getUpper() {
		return upper;
	}

	/**
	 * Sets the upper energy bound.
	 * 
	 * @param upperN new value for the upper energy bound
	 */
	public void setUpper(long upperN) {
		upper = upperN;
	}

	/**
	 * Returns true if only one energy bound is given and false otherwise.
	 * Meaning that there is no energy flexibility
	 * 
	 * @return true if only one energy bound is given and false otherwise
	 */
	public boolean isSingle() {
		return lower == upper;
	}
		
	@Override
	public String toString() {
		return "{"+lower+","+upper+"}";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (lower ^ (lower >>> 32));
		result = prime * result + (int) (upper ^ (upper >>> 32));
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
		FlexOfferDiscreteConstraint other = (FlexOfferDiscreteConstraint) obj;
		if (lower != other.lower)
			return false;
		if (upper != other.upper)
			return false;
		return true;
	}
}
