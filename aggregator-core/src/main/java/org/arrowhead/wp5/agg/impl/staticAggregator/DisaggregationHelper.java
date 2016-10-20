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

import java.util.List;

import org.arrowhead.wp5.core.entities.FlexOffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Laurynas
 *
 */
public class DisaggregationHelper {
	final static Logger logger = LoggerFactory.getLogger(DisaggregationHelper.class);

	/**
	 * Enforces the total energy constraint if any during schedule
	 * disaggregation
	 * 
	 * @param fos
	 */
	public static void enforceTotalEnergyConstraint(List<FlexOffer> fos) {
		final int NUM_ITER = 10; 
		double totalDeltaEn = 0;
		
		// Create an iterator array
		EnergyIntervalIterator [] sI = new EnergyIntervalIterator[fos.size()];
		
		for(int t=0; t < NUM_ITER; t++ ) {
			totalDeltaEn = 0;
				
			for (int i=0; i<fos.size(); i++) {
				FlexOffer f = fos.get(i);
				
				if (f.getTotalEnergyConstraint() == null) { 	continue; /* No total en. constraint */ }			
				
				double totalEn = getTotalEnergyValue(f);
				
				if (f.getTotalEnergyConstraint().getLower() - 1e-6 <= totalEn && totalEn <= f.getTotalEnergyConstraint().getUpper() + 1e-6 ) {
					continue; /* The constraint is satisfied */
				}
				
				double deltaEn = totalEn < f.getTotalEnergyConstraint().getLower() ? f.getTotalEnergyConstraint().getLower() - totalEn :
																					 f.getTotalEnergyConstraint().getUpper() - totalEn;
				
				// Initialize the iterators 
				for(int j=0; j<fos.size(); j++) {
					sI[j] = new EnergyIntervalIterator(fos.get(j), fos.get(j).getFlexOfferSchedule().getStartInterval() - f.getFlexOfferSchedule().getStartInterval());
				}
					
				/* Now, redistribute the excess energy to other flex-offers */	
				while(sI[i].getTsBoundState() != TimeStepState.tsProfileOutOfRange) {
					int sliceNr = (int) sI[i].getRunningIntervalNr();
					
					/* Deallocate energy */
					for (int j=0; j<fos.size(); j++) {
						
						if (j == i || sI[j].getTsBoundState() == TimeStepState.tsProfileOutOfRange) { continue; }
						
						double energy = f.getFlexOfferSchedule().getEnergyAmounts()[sliceNr];
						
						double deltaEnTs = Math.min(f.getSlice(sliceNr).getEnergyUpper() - energy, 
								           Math.max(f.getSlice(sliceNr).getEnergyLower() - energy, deltaEn));					
						
						double deallocValue = deallocateEnergy(fos.get(j),(int) sI[j].getRunningIntervalNr(), -deltaEnTs);
						
						f.getFlexOfferSchedule().getEnergyAmounts()[sliceNr] = energy - deallocValue;
						
						deltaEn+=deallocValue;					
					}
					
				
					// Goto the next interval
					for (EnergyIntervalIterator si : sI) { si.nextTS(); }
				}
				
				if (Math.abs(deltaEn)>1e-6) {
					totalDeltaEn += deltaEn; 
				}
				
			}
			
			if (Math.abs(totalDeltaEn)<=1e-6 ) {
				break;
			}
		}
		
		if (Math.abs(totalDeltaEn)>1e-6) {
			logger.warn("Total Constraint cannot be enfoced for one of the flex-offers during the disaggregation");
		}
	}
	

	
	private static double deallocateEnergy(FlexOffer f, int sliceNr, double deallocateRequest) {
		double energy = f.getFlexOfferSchedule().getEnergyAmounts()[sliceNr];
		double newenergy = energy + deallocateRequest;
		
		if (!isTotalEnergyConstraintSatisfied(f)) { return 0; }
		
		if (f.getTotalEnergyConstraint() != null) {
			double totalEnergy = getTotalEnergyValue(f) + newenergy - energy;
			
			if (totalEnergy < f.getTotalEnergyConstraint().getLower()) {
				newenergy += f.getTotalEnergyConstraint().getLower() - totalEnergy;
			}
			
			if (totalEnergy > f.getTotalEnergyConstraint().getUpper()) {
				newenergy += f.getTotalEnergyConstraint().getUpper() - totalEnergy;
			}
		}
		
		if (newenergy > f.getSlice(sliceNr).getEnergyUpper()) {
			newenergy = f.getSlice(sliceNr).getEnergyUpper();
		}
		
		if (newenergy < f.getSlice(sliceNr).getEnergyLower()) {
			newenergy = f.getSlice(sliceNr).getEnergyLower();
		}		
		
		f.getFlexOfferSchedule().getEnergyAmounts()[sliceNr] = newenergy; 
		
		return newenergy - energy;
	}

	private static double getTotalEnergyValue(FlexOffer f) {
		double value = 0;

		for (int i = 0; i < f.getSlices().length; i++) {
			value += f.getFlexOfferSchedule().getEnergyAmount(i);
		}

		return value;
	}
	
	private static boolean isTotalEnergyConstraintSatisfied(FlexOffer f) {
		
		if (f.getTotalEnergyConstraint() == null) {
			return true;
		} else {
				
			double totalEn = getTotalEnergyValue(f);
			
			return f.getTotalEnergyConstraint().getLower() <= totalEn && totalEn <= f.getTotalEnergyConstraint().getUpper();
		}
}

}
