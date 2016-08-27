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


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.arrowhead.wp5.agg.api.AggregationException;
import org.arrowhead.wp5.agg.api.FOAggParameters;
import org.arrowhead.wp5.agg.api.FOAggParameters.ConstraintAggregateType;
import org.arrowhead.wp5.agg.api.FOAggParameters.ConstraintType;
import org.arrowhead.wp5.agg.api.FOAggParameters.ProfileShape;
import org.arrowhead.wp5.agg.impl.common.AggParamAbstracter;
import org.arrowhead.wp5.agg.impl.common.FGridVector;
import org.arrowhead.wp5.agg.impl.common.FlexOfferDimValueDelegate;
import org.arrowhead.wp5.agg.impl.common.FlexOfferWeightDelegate;
import org.arrowhead.wp5.agg.impl.foaggregation.FOAggregation;
import org.arrowhead.wp5.agg.impl.grouper.FGridMapper;
import org.arrowhead.wp5.agg.impl.staticAggregator.StaticFOadder;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferConstraint;
import org.arrowhead.wp5.core.entities.FlexOfferSlice;



public class UtilsFoDummies {
	public static AggParamAbstracter generate2DflexofferAbstracter() throws AggregationException
	{
		final FlexOfferDimValueDelegate [] dlgts = new FlexOfferDimValueDelegate [2];
		dlgts[0] = new FlexOfferDimValueDelegate() {			
			@Override
			public double getValue(FlexOffer f) {
				return FlexOffer_GetDim1(f);
			}
		};
		dlgts[1] =  new FlexOfferDimValueDelegate() {			
			@Override
			public double getValue(FlexOffer f) {
				return FlexOffer_GetDim2(f);
			}
			};	
		
		FlexOfferWeightDelegate wDlg = new FlexOfferWeightDelegate() {			
			@Override
			public double getWeight(FlexOffer f) {
				return FlexOffer_GetWeight(f);
			}

			@Override
			public double addTwoWeights(double weight1, double weight2) {
				return weight1 + weight2;
			}

			@Override
			public double getZeroWeight() {
				return 0;
			}
		};
		
		AggParamAbstracter fa = new AggParamAbstracter(new FGridVector(1, 1), 
																	   dlgts, 
																	    wDlg, 
													   new StaticFOadder(ProfileShape.psAlignStart));
				
		return fa;
	}
	
	// Used in testing to set weight 
	public static void FlexOffer_SetWeight(FlexOffer f, double value)
	{
		if (f.getSlices() == null || f.getSlices().length == 0)
		{
			f.setSlices(new FlexOfferSlice[1]);
			f.getSlices()[0] = new FlexOfferSlice(); 
		}
		f.getSlices()[0].setEnergyConstraint(new FlexOfferConstraint(value, value));
	}
	public static double FlexOffer_GetWeight(FlexOffer f)
	{
		if (f.getSlices()!=null)
			return f.getSlices()[0].getEnergyLower();
		else 
			return 0;
	}
	
	// Used in testing to set dim 1
	public static void FlexOffer_SetDim1(FlexOffer f, double value)
	{
		if (f.getTotalEnergyConstraint() == null)
			f.setTotalEnergyConstraint(new FlexOfferConstraint(0,0));
		f.getTotalEnergyConstraint().setLower(value);
	}
	
	public static double FlexOffer_GetDim1(FlexOffer f)
	{
		if (f.getTotalEnergyConstraint() == null)
			f.setTotalEnergyConstraint(new FlexOfferConstraint(0,0));
		
		return f.getTotalEnergyConstraint().getLower();
	}
	
	// Used in testing to set dim 2
	public static void FlexOffer_SetDim2(FlexOffer f, double value)
	{
		if (f.getTotalEnergyConstraint() == null)
			f.setTotalEnergyConstraint(new FlexOfferConstraint(0,0));
		
		f.getTotalEnergyConstraint().setUpper(value);
	}
	public static double FlexOffer_GetDim2(FlexOffer f)
	{
		if (f.getTotalEnergyConstraint() == null)
			f.setTotalEnergyConstraint(new FlexOfferConstraint(0,0));
		
		return f.getTotalEnergyConstraint().getUpper();
	}
	
	public static FOAggParameters generateAggregationParameters(double dim1Tolerance, double dim2Tolerance, double weightMin, double weightMax)
	{
		FOAggParameters par = new FOAggParameters();
		if (dim1Tolerance>0)
		{
			par.getConstraintPair().startAfterToleranceType = ConstraintType.acSet;
			par.getConstraintPair().startAfterTolerance = dim1Tolerance;
		}
		if (dim2Tolerance>0)
		{
			par.getConstraintPair().startBeforeToleranceType = ConstraintType.acSet;
			par.getConstraintPair().startBeforeTolerance = dim2Tolerance;
		}
		if (weightMin>0 || weightMax >0)
		{
			par.getConstraintAggregate().aggConstraint = ConstraintAggregateType.acTotalEnMin;
			par.getConstraintAggregate().valueMin = weightMin;
			par.getConstraintAggregate().valueMax = weightMax;
		}
		
		return par;		
	}
	
	public static List<FlexOffer> generateUniform2DFlexOffers(long seed, int count, double dim1low, double dim1high,
			double dim2low, double dim2high, double weightlow, double weighthigh)
	{
		Random r = new Random();
		r.setSeed(seed);
				
		List<FlexOffer> fl = new ArrayList<FlexOffer>();
		for(int i=0; i< count; i++)
		{
			FlexOffer f = new FlexOffer();
			UtilsFoDummies.FlexOffer_SetWeight(f, r.nextDouble() * (weighthigh - weightlow) + weightlow);
			UtilsFoDummies.FlexOffer_SetDim1(f, r.nextDouble() * (dim1high - dim1low) + dim1low);
			UtilsFoDummies.FlexOffer_SetDim2(f, r.nextDouble() * (dim2high - dim2low) + dim2low);
			fl.add(f);
		}
		return fl;
	}
	
	public static FlexOfferModStats generateRandomModifications(AggParamAbstracter fa, List<FlexOffer> fos, Object acOrmapper, double numModifications, double addRemoveRatio)
	{
		List<FlexOffer> hsExisting = new ArrayList<FlexOffer>();
		FlexOfferModStats stats = new FlexOfferModStats();
		stats.addedWeight = 0;
		stats.removedWeight = 0;
		stats.addedFOS = new ArrayList<FlexOffer>();
		stats.removedFOS = new ArrayList<FlexOffer>();
		List<FlexOffer> f = new ArrayList<FlexOffer>();
		
		Random r = new Random(12354);
		for(int i = 0; i< numModifications; i++)
		{
			f.clear();
			f.add(fos.get(i % fos.size()));
			stats.addedFOS.add(f.get(0));
			stats.addedWeight += fa.getWeightDlg().getWeight(f.get(0));
			if (acOrmapper instanceof FOAggregation)
				((FOAggregation) acOrmapper).foAdd(f);
			if (acOrmapper instanceof FGridMapper)
				((FGridMapper) acOrmapper).foAdd(f);
			
			hsExisting.add(f.get(0));
			if (r.nextDouble()> addRemoveRatio)
			{				
				f.clear();				
				f.add(hsExisting.get(r.nextInt(hsExisting.size())));
				hsExisting.remove(f.get(0));
				stats.removedFOS.add(f.get(0));
				stats.removedWeight += fa.getWeightDlg().getWeight(f.get(0));
				if (acOrmapper instanceof FOAggregation)
					((FOAggregation) acOrmapper).foDel(f);
				if (acOrmapper instanceof FGridMapper)
					((FGridMapper) acOrmapper).foDel(f);
			}
		}
		return stats;
	}	
}

class FlexOfferModStats
{
	public List<FlexOffer> addedFOS;
	public double addedWeight;
	
	public List<FlexOffer> removedFOS;
	public double removedWeight;		
}
