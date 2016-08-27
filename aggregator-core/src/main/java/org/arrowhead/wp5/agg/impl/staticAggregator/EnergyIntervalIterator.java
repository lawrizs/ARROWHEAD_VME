package org.arrowhead.wp5.agg.impl.staticAggregator;

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


import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferSlice;


/**
 * A class that allows iterating through all energy constraint intervals of a flex-offer
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 *
 */
public class EnergyIntervalIterator {
		private FlexOffer flexOffer = null;		
		// Running time step 
		private long runningTS = -1;
		// Running interval		
		private FlexOfferSlice runningInterval = null;
		private long runningIntervalNr = -1;
		// Running  interval start TS
		private long runningIntervalStartTS = -1;
		// True, if after last nextTs call interval bound has been crossed 
		private boolean isEnIntervalBoundCrossed = false;
		// Running interval crossing state
		private TimeStepState boundState = TimeStepState.tsProfileOutOfRange;
						
		public EnergyIntervalIterator(FlexOffer flexOffer)
		{
			this(flexOffer, 0);
		}		
		
		public EnergyIntervalIterator(FlexOffer flexOffer, long startTs)
		{
			this.flexOffer = flexOffer;
			this.runningTS = startTs-1;
			this.runningIntervalNr = -1;
			this.nextTS();
		}	
		
		public FlexOffer getFlexOffer()
		{
			return this.flexOffer;
		}
	
		public long getRelativeTS()
		{
			return this.runningTS;
		}
		
		public TimeStepState getTsBoundState() {
			return boundState;
		}
		
		public FlexOfferSlice getRunningInterval()
		{
			if (this.flexOffer.getSlices()!=null &&
					this.runningIntervalNr >=0 && this.runningIntervalNr < this.flexOffer.getSlices().length)
				return this.flexOffer.getSlices()[(int) this.runningIntervalNr];
			else 
				return null;
		}
		
		public long getRunningIntervalNr()
		{
			return this.runningIntervalNr;
		}
		
		// Indicates, if during the last step, flex-offers interval was crossed 
		public boolean IsEnIntervalBoundCrossed ()
		{
			return this.isEnIntervalBoundCrossed;
		}
		
		public boolean IsEndOfProfile()
		{
			return this.flexOffer.getSlices()==null || 
					(this.runningIntervalNr >= this.flexOffer.getSlices().length);
		}
		
		private void nextInterval()
		{	
			this.runningIntervalNr ++;				
			if (this.flexOffer.getSlices() == null || this.runningIntervalNr >= this.flexOffer.getSlices().length)			
				this.boundState = TimeStepState.tsProfileOutOfRange;
			else			
			{
				this.runningInterval = this.flexOffer.getSlices()[(int) this.runningIntervalNr];
				this.runningIntervalStartTS = this.runningTS;
				this.boundState = TimeStepState.tsIntervalStarted;
				this.isEnIntervalBoundCrossed = true;
			};
		}
		
		public void nextTS()
		{
			this.runningTS ++;
			this.isEnIntervalBoundCrossed = false;
			if (this.runningTS < 0 || this.flexOffer.getSlices() == null)
				this.boundState = TimeStepState.tsProfileOutOfRange;
			else
				if (this.runningInterval!=null)
				{
					// Detection of end of profile
					if (this.runningIntervalNr == this.flexOffer.getSlices().length-1 &&
						this.runningTS - this.runningIntervalStartTS == this.runningInterval.getDuration())
						this.isEnIntervalBoundCrossed = true;

					if (this.runningTS - this.runningIntervalStartTS >= this.runningInterval.getDuration())
						this.nextInterval();
					else 
						this.boundState = TimeStepState.tsIntervalSpanned;					
				} else 
					nextInterval();
		}	
		
}
