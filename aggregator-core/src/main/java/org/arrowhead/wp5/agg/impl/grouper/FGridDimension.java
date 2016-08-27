package org.arrowhead.wp5.agg.impl.grouper;

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


import org.arrowhead.wp5.agg.impl.common.FlexOfferDimValueDelegate;
import org.arrowhead.wp5.core.entities.FlexOffer;


/**
 * Represents one dimension of the flex-offer grid 
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 * @see FGridMapper
 */
public class FGridDimension {
	private static final double PRECISION = 1E-5;
	private double origin = 0f;
	private double stepInterval = 1f;
	private boolean useValuesAsIntNr = true;
	private FlexOfferDimValueDelegate valFunction = null;

	public double getDimValue(FlexOffer f) {
		return this.valFunction.getValue(f);
	}
	
	public double getStepInterval()
	{
		return this.stepInterval;
	}
	
	public double getOrigin()
	{
		return this.origin;
	}	

	public long getIntervalNr(FlexOffer f) {
		if (this.useValuesAsIntNr)
			// This needs to be tested for overflow
			return (long) (getDimValue(f) / PRECISION);
		return (long) Math.floor(((getDimValue(f) - origin) / stepInterval));
	}

	protected FGridDimension(double origin, double stepInterval,
			FlexOfferDimValueDelegate valFunction) {
		this.origin = origin;
		this.stepInterval = stepInterval;
		this.valFunction = valFunction;
		// Is the step size is too small, we use values as interval Nr.
		this.useValuesAsIntNr = (Math.abs(this.stepInterval) <= PRECISION);
	}
}