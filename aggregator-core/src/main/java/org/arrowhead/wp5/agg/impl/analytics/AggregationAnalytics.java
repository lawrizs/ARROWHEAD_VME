package org.arrowhead.wp5.agg.impl.analytics;

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

import org.arrowhead.wp5.core.entities.AggregatedFlexOffer;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferSlice;


public final class AggregationAnalytics {
	public AggregationLoss calculateAllLosses(Collection<FlexOffer> microFOS, Collection<AggregatedFlexOffer> macroFOS)
	{
		AggregationLoss losses = new AggregationLoss();
		
		this.calcFlexilibityLoss(losses, microFOS, macroFOS);
		this.calcPeakReductionPotentialLoss(losses, microFOS, macroFOS);
			
		return losses;
	}
	
	public AggregationLoss calculateFlexibilityLosses(Collection<FlexOffer> microFOS, Collection<AggregatedFlexOffer> macroFOS)
	{
		AggregationLoss losses = new AggregationLoss();
		
		this.calcFlexilibityLoss(losses, microFOS, macroFOS);
			
		return losses;
	}
	
	public AggregationLoss calculatePeakReductionLosses(Collection<FlexOffer> microFOS, Collection<AggregatedFlexOffer> macroFOS)
	{
		AggregationLoss losses = new AggregationLoss();
		
		this.calcPeakReductionPotentialLoss(losses, microFOS, macroFOS);
			
		return losses;
	}

	private double calcEnergyFlexibility(FlexOfferSlice [] eints)
	{
		double enFlexibility = 0;
		for (int i = 0; i < eints.length; i++)
			enFlexibility += eints[i].getEnergyUpper() - eints[i].getEnergyLower();
		
		return enFlexibility;
	}
	
	private void calcFlexilibityLoss(AggregationLoss losses, Collection<FlexOffer> microFOS, Collection<AggregatedFlexOffer> macroFOS)
	{
			long timeflexBeforeAgg = 0;
			long timeflexAfterAgg = 0;
			double enflexBeforeAgg = 0;
			double enflexAfterAgg = 0;
			int cntFOBeforeAgg = 0;
			int cntFOAfterAgg = 0;
	
			for(FlexOffer f : microFOS)
			{
				timeflexBeforeAgg +=  f.getStartBeforeInterval() - f.getStartAfterInterval();
				enflexBeforeAgg += calcEnergyFlexibility(f.getSlices());
				cntFOBeforeAgg ++;
			}
			
			for(AggregatedFlexOffer f : macroFOS)
			{
				timeflexAfterAgg +=  f.getStartBeforeInterval() - f.getStartAfterInterval();
				enflexAfterAgg += calcEnergyFlexibility(f.getSlices());
				cntFOAfterAgg ++;
			}
			
			losses.setEnergyFlexibilityAbsoluteLoss(enflexBeforeAgg - enflexAfterAgg);
			
			losses.setTimeFlexibilityAbsoluteLoss(timeflexBeforeAgg - timeflexAfterAgg);
			losses.setTimeFlexibilityPerFOLoss(((double)timeflexBeforeAgg / cntFOBeforeAgg) - 
											   ((double)timeflexAfterAgg / cntFOAfterAgg));
			
	}
	
	private void minMaxIntervalAmounts(FlexOffer f, int t, double [] minMaxValues)
	{		
		long iLow =  Math.max(t - f.getStartBeforeInterval() + 1, 0);
		long iHigh = Math.min(t - f.getStartAfterInterval() + 1, f.getSlices().length);
		
		minMaxValues[0] = Double.MAX_VALUE;
		minMaxValues[1] = 0;

		for (long i = iLow; i < iHigh; i++)
		{
			assert(f.getSlices()[(int)i].getDuration() == 1) : "Intervals longer than 1 time stamp is unsupported yet!";
			
			if (minMaxValues[0] > f.getSlices()[(int)i].getEnergyLower())
				minMaxValues[0] = f.getSlices()[(int)i].getEnergyLower();
			if (minMaxValues[1] < f.getSlices()[(int)i].getEnergyUpper())
				minMaxValues[1] = f.getSlices()[(int)i].getEnergyUpper();
		}
		
		if (minMaxValues[0] == Double.MAX_VALUE)
			minMaxValues[0] = 0;
		// Check if min value can be further reduced
		if (t >= f.getStartAfterInterval() && t < f.getStartBeforeInterval())
			minMaxValues[0] = 0;
		if (t >= f.getEndAfterInterval() && t < f.getEndBeforeInterval())
			minMaxValues[0] = 0;
	}
	
	private void calcPeakReductionPotentialLoss(AggregationLoss losses,
			Collection<FlexOffer> microFOS,
			Collection<AggregatedFlexOffer> macroFOS) {
			
			long fIntLow = Integer.MAX_VALUE;
			long fIntHigh = -Integer.MAX_VALUE;
			
			// First, calculate time interval range on micro and macro flex-offers
			for(FlexOffer f : microFOS)
			{
				if (f.getStartAfterInterval()<fIntLow)
					fIntLow = f.getStartAfterInterval();
				if (f.getEndBeforeInterval() > fIntHigh)
					fIntHigh = f.getEndBeforeInterval();
			}			
			for(FlexOffer f : macroFOS)
			{
				if (f.getStartAfterInterval()<fIntLow)
					fIntLow = f.getStartAfterInterval();
				if (f.getEndBeforeInterval() > fIntHigh)
					fIntHigh = f.getEndBeforeInterval();
			}

			// Calculate peak reduction potential loss
			double [] minMaxValues = new double[2];
			double [] sumMacroValues = new double[2];
			double [] sumMicroValues = new double[2];
			double [] pExtLossValues = new double[2];
			double [] pAvgLossValues = new double[2];
			pExtLossValues[0] = -Double.MAX_VALUE;
			pExtLossValues[1] = -Double.MAX_VALUE;
			pAvgLossValues[0] = 0;
			pAvgLossValues[1] = 0;
			
			for (int t = (int)fIntLow; t <= (int)fIntHigh; t++ )
			{
				sumMicroValues[0] = 0;
				sumMicroValues[1] = 0;
				for(FlexOffer f : microFOS)
				{
					this.minMaxIntervalAmounts(f, t, minMaxValues);
					sumMicroValues[0] +=  minMaxValues[0];				
					sumMicroValues[1] +=  minMaxValues[1];
				}
				
				sumMacroValues[0] = 0;
				sumMacroValues[1] = 0;
				for(FlexOffer f : macroFOS)
				{
					this.minMaxIntervalAmounts(f, t, minMaxValues);
					sumMacroValues[0] +=  minMaxValues[0];
					sumMacroValues[1] +=  minMaxValues[1];
				}
				
				if (pExtLossValues[0] < (sumMacroValues[0] - sumMicroValues[0]))
					pExtLossValues[0] = (sumMacroValues[0] - sumMicroValues[0]);
				pAvgLossValues[0] += (sumMacroValues[0] - sumMicroValues[0]);
				
				if (pExtLossValues[1] < (sumMicroValues[1] - sumMacroValues[1]))
					pExtLossValues[1] = (sumMicroValues[1] - sumMacroValues[1]);
				
				pAvgLossValues[1] += (sumMicroValues[1] - sumMacroValues[1]);
			}
			
			if (fIntHigh>=fIntLow)
			{
				pAvgLossValues[0] /= (fIntHigh-fIntLow)+1;
				pAvgLossValues[1] /= (fIntHigh-fIntLow)+1;
			}
			
			losses.setLowerPeakReductionPotentialExtremeLoss(pExtLossValues[0]);
			losses.setUpperPeakReductionPotentialExtremeLoss(pExtLossValues[1]);
					
			losses.setLowerPeakReductionPotentialPerTSLoss(pAvgLossValues[0]);
			losses.setUpperPeakReductionPotentialPerTSLoss(pAvgLossValues[1]);
	}
}
