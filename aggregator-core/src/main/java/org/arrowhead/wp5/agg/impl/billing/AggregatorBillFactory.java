package org.arrowhead.wp5.agg.impl.billing;

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

import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferSlice;

/**
 * A class for generating Aggregator's bills 
 * 
 * @author Laurynas
 *
 */
public class AggregatorBillFactory {
		
	/**
	 * Checks if a flexoffer is exectured correctly  
	 */
	public static boolean isExecutedCorrectly(FlexOffer fo) {
		return true; // @TODO: make a proper flexoffer execution validation 
	}
	
	/* This calculates the expected value of a given flexoffer */
	public static double getFlexOfferExpectedValue(AggregatorContract contract, FlexOffer f) {
		double value = 0;
		
		/* Add time flexibility part */
		value += contract.getTimeFlexReward() * (f.getStartBeforeInterval() - f.getStartAfterInterval());
		/* Add energy flexibility part */
		for(FlexOfferSlice s : f.getSlices()) {
			value += contract.getEnergyFlexReward() * (s.getEnergyUpper() - s.getEnergyLower());
		}
		
		/* Add scheduling part */
		if ((f.getDefaultSchedule() != null) && (f.getFlexOfferSchedule() != null) &&
			(f.getFlexOfferSchedule().getEnergyAmounts().length == f.getSlices().length) &&					
			(f.getFlexOfferSchedule().getEnergyAmounts().length == f.getDefaultSchedule().getEnergyAmounts().length) &&
			(!f.getFlexOfferSchedule().equals(f.getDefaultSchedule()))) {
			
			// Check if there're baseline deviations
			boolean deviations = f.getDefaultSchedule().getStartInterval() != f.getFlexOfferSchedule().getStartInterval();
			for(int sid = 0; sid < f.getSlices().length; sid++) {
				if (Math.abs(f.getDefaultSchedule().getEnergyAmount(sid) - 
							 f.getFlexOfferSchedule().getEnergyAmount(sid)) > 1e-3) {
					deviations = true;
					break;
				}
			}		
			
			if (deviations) {			
				/* Incremenent custom schedule activation count */
				value += contract.getSchedulingFixedReward();
				
				value += contract.getSchedulingStartTimeReward() * Math.abs(f.getFlexOfferSchedule().getStartInterval() - 
																		    f.getDefaultSchedule().getStartInterval());
								
				/* ... for energy deviations */
				for(int i=0; i<f.getFlexOfferSchedule().getEnergyAmounts().length; i++) {
					value += contract.getSchedulingEnergyReward() * Math.abs(f.getFlexOfferSchedule().getEnergyAmount(i) - 
																			 f.getDefaultSchedule().getEnergyAmount(i));
				}				
			}
		}			
		
		return value;
	}
 
	/* Generate bill for the customer, after flexoffer execution */
	public static AggregatorBill generateBill(String customerId, AggregatorContract contract, Collection<FlexOffer> flexOffers) {
		AggregatorBill bill = new AggregatorBill();

		bill.setCustomerId(customerId);
		
		// Calculate statistics 
		for(FlexOffer f : flexOffers) {
			
			if(!isExecutedCorrectly(f)) {	
				continue;		/* Flexoffer was not executed correctly - no reward is provided */
			}
			
			/* Increment flexoffers*/
			bill.setNumFlexOffers(bill.getNumFlexOffers() + 1);
				
			/* Add time flexibility part */
			bill.setTotalTimeFlex(bill.getTotalTimeFlex() + (f.getStartBeforeInterval() - f.getStartAfterInterval()));
			
			/* Add energy flexibility part */
			for(FlexOfferSlice s : f.getSlices()) {
				bill.setTotalEnergyFlex(bill.getTotalEnergyFlex() + (s.getEnergyUpper() - s.getEnergyLower()));
			}
			
			/* Add scheduling part */
			if ((f.getDefaultSchedule() != null) && (f.getFlexOfferSchedule() != null) &&
				(f.getFlexOfferSchedule().getEnergyAmounts().length == f.getSlices().length) &&					
				(f.getFlexOfferSchedule().getEnergyAmounts().length == f.getDefaultSchedule().getEnergyAmounts().length) &&
				(!f.getFlexOfferSchedule().equals(f.getDefaultSchedule()))) {
				
				// Check if there're baseline deviations
				boolean deviations = f.getDefaultSchedule().getStartInterval() != f.getFlexOfferSchedule().getStartInterval();
				for(int sid = 0; sid < f.getSlices().length; sid++) {
					if (Math.abs(f.getDefaultSchedule().getEnergyAmount(sid) - 
								 f.getFlexOfferSchedule().getEnergyAmount(sid)) > 1e-3) {
						deviations = true;
						break;
					}
				}		
				
				if (deviations) {					
				
					/* Incremenent custom schedule activation count */
					bill.setNumCustomScheduleActivations(bill.getNumCustomScheduleActivations() + 1);
				
					/* Give a reward depending on deviations from default ... */				
					/* ... for start-time deviation */
					bill.setTotalStartTimeDeviations(bill.getTotalStartTimeDeviations() + 
							Math.abs(f.getFlexOfferSchedule().getStartInterval() - f.getDefaultSchedule().getStartInterval()));
									
					/* ... for energy deviations */
					for(int i=0; i<f.getFlexOfferSchedule().getEnergyAmounts().length; i++) {
						bill.setTotalEnergyDeviations(bill.getTotalEnergyDeviations() + Math.abs(f.getFlexOfferSchedule().getEnergyAmount(i) - 
																	    f.getDefaultSchedule().getEnergyAmount(i)));
					}	
				}	
			}
		}
		
		/* Compute actual reward amounts */		
		bill.setRewardFixed(flexOffers.isEmpty() ? 0 : contract.getFixedReward());
		bill.setRewardTotalTimeFlex(bill.getTotalTimeFlex() * contract.getTimeFlexReward());
		bill.setRewardTotalEnergyFlex(bill.getTotalEnergyFlex() * contract.getEnergyFlexReward());		
		bill.setRewardTotalSchedFixed(bill.getNumCustomScheduleActivations() * contract.getSchedulingFixedReward());
		bill.setRewardTotalSchedEST(bill.getTotalStartTimeDeviations() * contract.getSchedulingStartTimeReward());
		bill.setRewardTotalSchedEnergy(bill.getTotalEnergyDeviations() * contract.getSchedulingEnergyReward());
		bill.setRewardTotal(bill.getRewardFixed() + 
						    bill.getRewardTotalEnergyFlex() + 
						    bill.getRewardTotalSchedEnergy() +
						    bill.getRewardTotalSchedEST() + 
						    bill.getRewardTotalSchedFixed() + 
						    bill.getRewardTotalTimeFlex());
		
		return bill;
	}
}
