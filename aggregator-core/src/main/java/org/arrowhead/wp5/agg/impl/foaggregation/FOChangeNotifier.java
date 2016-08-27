package org.arrowhead.wp5.agg.impl.foaggregation;

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


import java.util.Collection;

import org.arrowhead.wp5.agg.api.FOCQcallbackParams;
import org.arrowhead.wp5.agg.api.IFOCQcallback;
import org.arrowhead.wp5.agg.api.IFOContinuousQuery;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferConstraint;

/**
 * A class that specifies a single notifier, which generates flex-offer change notifications to some listener  
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 *
 */
public class FOChangeNotifier {
	private IFOContinuousQuery query = null;
	private FOCQcallbackParams lParams;
	private IFOCQcallback callback;	
	
	// Counters
	private int sumFlexOffers = 0;
	private double sumLowerEnergy = 0;
	private double sumUpperEnergy = 0;
	private double sumTimeFlex = 0;
		
	public FOChangeNotifier(IFOContinuousQuery query, FOCQcallbackParams listenerParams, IFOCQcallback callback)
	{
		this.query = query;
		this.lParams = listenerParams;
		this.callback = callback;	
		this.resetSums();
	}
	
	/**
	 * Resets all sums used for notification generation
	 */
	public void resetSums()
	{
		this.sumFlexOffers = 0;
		this.sumUpperEnergy = 0;
		this.sumLowerEnergy = 0;
		this.sumTimeFlex = 0;
	}
			
	private boolean updateAndCheckSums(FlexOffer f)
	{
		if (this.lParams.getNumFlexOffersThreshold()>0)
			this.sumFlexOffers++;
		FlexOfferConstraint c = f.getSumEnergyConstraints();
		if (this.lParams.getUpperEnergyThreshold() > 0)
			this.sumUpperEnergy += c.getUpper();
		if (this.lParams.getLowerEnergyThreshold() > 0)
			this.sumLowerEnergy += c.getLower();
		if (this.lParams.getTimeFlexThreshold() > 0)
			this.sumTimeFlex += f.getStartBeforeInterval() - f.getStartAfterInterval();
		
		// Check if sums exceed parameters
		if ((this.lParams.getNumFlexOffersThreshold()>0 && this.sumFlexOffers > this.lParams.getNumFlexOffersThreshold()) ||
			(this.lParams.getLowerEnergyThreshold()>0 && this.sumLowerEnergy > this.lParams.getLowerEnergyThreshold()) ||
			(this.lParams.getUpperEnergyThreshold()>0 && this.sumUpperEnergy > this.lParams.getUpperEnergyThreshold()) ||
			(this.lParams.getTimeFlexThreshold()>0 && this.sumTimeFlex > this.lParams.getTimeFlexThreshold()))
			{
				this.resetSums();
				if (this.callback != null)
					this.callback.notifyFoChanges(this.query);
				return true;
			}			
		
		return false;
	}
	
	/** Update sums used for notification generation. It also triggers a callback if needed 
	 * 
	 * @param foAddedRemoved a list of flex-offers that was added or removed from a (micro) flex-offer pool.
	 */
	public void updateSums(Collection<FlexOffer> foAddedRemoved)
	{
		if (foAddedRemoved != null)
			for(FlexOffer f: foAddedRemoved)
				if (this.updateAndCheckSums(f)) return;		
	}	
}
