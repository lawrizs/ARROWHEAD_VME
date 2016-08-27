package org.arrowhead.wp5.agg.junit;

/*-
 * #%L
 * ARROWHEAD::WP5::Aggregator Manager
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


import java.util.Random;

import org.arrowhead.wp5.core.entities.AggregatedFlexOffer;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferSchedule;
import org.arrowhead.wp5.core.entities.FlexOfferSlice;

public class UtilsFlexOffers {
	public static FlexOffer generateRealisticFlexOffer(int creationTime, int timeFlexibility,
						double priceForEnergyUnit, 
						double energyFlexibility, int [] enIntDurations, double [] enIntAmounts	)
	{
		assert(enIntDurations.length == enIntAmounts.length): "Incorrect arguments provided";
		
		FlexOffer f = new FlexOffer();
		f.setCreationInterval(creationTime);
		f.setAcceptanceBeforeInterval(creationTime + 4*12); // 12 hours ahead
		f.setAssignmentBeforeInterval(creationTime + 4*13); // 13 hours ahead
		//f.setAcceptedById(12345);	// Legal entity 1
		
		@SuppressWarnings("unused")
		double maxEnergy = 0;
		for (double a : enIntAmounts)
			maxEnergy += (a + energyFlexibility / 2) ;
		
//		f.setMinTariff(maxEnergy * priceForEnergyUnit );	// Min selling price
//		f.setMaxTariff(maxEnergy * priceForEnergyUnit); // Max buying price
		f.setStartAfterInterval(creationTime + 4*13+1); // 13 hours and 15 minutes
		f.setStartBeforeInterval(creationTime + 4*13+1 + timeFlexibility); // 13 hours and 15 minutes + timeFlexibility
		
		FlexOfferSlice [] eInts = new FlexOfferSlice[enIntAmounts.length];
		
		for (int i = 0; i< eInts.length; i++)
			eInts[i]= new FlexOfferSlice(enIntDurations[i],  priceForEnergyUnit,
					      				 enIntAmounts[i] - energyFlexibility / 2, 
						                 enIntAmounts[i] + energyFlexibility / 2 );			
		f.setSlices(eInts);
		
		return f;
	}
	
	public static FlexOfferSchedule getRandomFix(FlexOffer f)
	{
		Random rnd = new Random();
		FlexOfferSchedule s = new FlexOfferSchedule();
		s.setStartInterval((int)(rnd.nextDouble() * (f.getStartBeforeInterval() - f.getStartAfterInterval()) + f.getStartAfterInterval()));
		
		double [] enAmounts = new double [f.getSlices().length];
		for(int i = 0; i< f.getSlices().length; i++)
			enAmounts[i] = rnd.nextDouble() * (f.getSlices()[i].getEnergyUpper() - f.getSlices()[i].getEnergyLower()) + 
											   f.getSlices()[i].getEnergyLower();
		s.setEnergyAmounts(enAmounts);
		
		f.setFlexOfferSchedule(s);
		
		return s;
	}
	
	public static FlexOfferSchedule getRandomFix(AggregatedFlexOffer f)
	{
		Random rnd = new Random();
		FlexOfferSchedule s = new FlexOfferSchedule();
		s.setStartInterval((int)(rnd.nextDouble() * (f.getStartBeforeInterval() - f.getStartAfterInterval()) + f.getStartAfterInterval()));
		
		double [] enAmounts = new double [f.getSlices().length];
		for(int i = 0; i< f.getSlices().length; i++)
			enAmounts[i] = rnd.nextDouble() * (f.getSlices()[i].getEnergyUpper() - f.getSlices()[i].getEnergyLower()) + 
											   f.getSlices()[i].getEnergyLower();
		s.setEnergyAmounts(enAmounts);
		
		f.setFlexOfferSchedule(s);
		
		return s;
	}
	
	public static double calcTotalAssignedEnergy(FlexOfferSchedule fs)
	{
		double enTotal = 0;
		for (double en : fs.getEnergyAmounts())
			enTotal += en;
		return enTotal;
	}
	
}
