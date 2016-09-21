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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.arrowhead.wp5.agg.api.AggregationException;
import org.arrowhead.wp5.agg.api.ChangeRecOfFlexOffer;
import org.arrowhead.wp5.agg.api.FOAggParameters.ProfileShape;
import org.arrowhead.wp5.agg.impl.common.ChangeRecOfGroup;
import org.arrowhead.wp5.agg.impl.common.FGroup;
import org.arrowhead.wp5.agg.impl.common.IFlexOfferNto1Adder;
import org.arrowhead.wp5.core.entities.AggregatedFlexOffer;
import org.arrowhead.wp5.core.entities.AggregatedFlexOfferMetaData;
import org.arrowhead.wp5.core.entities.AggregationLevel;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferConstraint;
import org.arrowhead.wp5.core.entities.FlexOfferSchedule;
import org.arrowhead.wp5.core.entities.FlexOfferSlice;


/**
 * This is a primary class that implements a concrete similar flex-offer aggregation and disaggregation.
 * It offers the support for the incremental approach. 
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 *
 */
public class StaticFOadder implements IFlexOfferNto1Adder {
	private ProfileShape profileShape;
	private LinkedHashMap<FGroup, AggregatedFlexOffer> aggFOhash = new LinkedHashMap<FGroup, AggregatedFlexOffer>();
	
	/* Balance alignment stuff */
	/*	private List<int[]> alignmentsListPermutations= new ArrayList<int[]>();
	private int[] o;// = new int[n.length];
	private AggregatedFlexOffer aggFoPermutations = null;
 	private double bestAbsoluteBalance=Double.MAX_VALUE;	 
 	private List<FlexOffer> folGreedy = new ArrayList<FlexOffer>();
 	private ArrayList<FlexOffer> folReturn = new ArrayList<FlexOffer>();
 	private List<FlexOffer> returnFOsList = new ArrayList<FlexOffer>();
	private List<FlexOffer> fol = new ArrayList<FlexOffer>(); */
	
	public StaticFOadder(ProfileShape profileShape) throws AggregationException
	{
		if (profileShape!=ProfileShape.psAlignStart && 
			profileShape != ProfileShape.psAlignBaseline)
			throw new AggregationException(profileShape.toString()+" flex-offer profile alignment is not supported yet!");
		this.profileShape = profileShape;		
	}
	
	@Override
	public ChangeRecOfFlexOffer incUpdateAggregate(ChangeRecOfGroup g) {
		AggregatedFlexOffer f ;
		ChangeRecOfFlexOffer fc = new ChangeRecOfFlexOffer();
		fc.setChangeType(g.getChangeType());
		switch(g.getChangeType()){
		case ctAdded:
			f = this.aggregateUsingStaticMethodInc(null, g.getGroup(), null);
			this.aggFOhash.put(g.getGroup(), f);
			fc.setFlexOffer(f);
			break;
		case ctDeleted:
			f = this.aggFOhash.get(g.getGroup());
			assert(f!=null) : "Group has been deleted for unexisting flex-offer";
			this.aggFOhash.remove(g.getGroup());
			fc.setFlexOffer(f);
			break;
		case ctUpsized:		
		case ctDownsized:
		case ctModified:
			f = this.aggFOhash.get(g.getGroup());			
			assert(f!=null) : "Updates comes for non-existing aggregate flex-offer";
			AggregatedFlexOffer fN = this.aggregateUsingStaticMethodInc(f, g.getAddedFlexOffers(), g.getRemovedFlexOffers());
			fc.setFlexOffer(fN);
			if (fN != f)
				this.aggFOhash.put(g.getGroup(), fN);
			
			break;
		default:
			break;
		}
		return fc;
	}
	
	@Override
	public Iterator<AggregatedFlexOffer> getAllAggregates() {	
		return this.aggFOhash.values().iterator();
	}
	
	private boolean removeFlexOfferfromAggregated(AggregatedFlexOffer f, FlexOffer foToRemove)
	{		
		if (f.getSubFoMetas() != null)
			for (int i = 0; i < f.getSubFoMetas().size(); i++) {
				AggregatedFlexOfferMetaData m = f.getSubFoMetas().get(i);
				if (m.getSubFlexOffer().equals(foToRemove)) {
					f.getSubFoMetas().remove(i);
					return true;
				}
			}
		return false;
	}
	
	// Methods to do actual 1-N aggregation	
	private AggregatedFlexOffer aggregateUsingStaticMethodInc(AggregatedFlexOffer aggFo, Iterable<FlexOffer> addedFOs, Iterable<FlexOffer> removedFOs)
	{
		// We do not support incremental deletion of flex-offer. Thus we collapse the aggregated flex-offer and build it 
		/// from scratch
		boolean rebuildNeeded = false;
		
		if (removedFOs != null)
			for(FlexOffer f : removedFOs)
			{
				assert(aggFo != null) : "Trying to update a NULL flex-offer";
				boolean success = removeFlexOfferfromAggregated(aggFo, f);
				assert (success) : "Was trying to remove a non-existing flex-offer from the aggregate!";
				rebuildNeeded |= success;				
			}
		
		if (rebuildNeeded)
		{
			FlexOffer [] orphans = new FlexOffer [aggFo.getSubFoMetas().size()];
			for (int i = 0; i < aggFo.getSubFoMetas().size(); i++)
				orphans[i] = aggFo.getSubFoMetas().get(i).getSubFlexOffer();			
			
			aggFo = null;	// Clear all metas
		
			// Finally we rebuild an aggregate			
			for (FlexOffer f : orphans)
				aggFo = subFoAdd(aggFo, f);
			
			if (addedFOs != null)
				for(FlexOffer f : addedFOs)
					aggFo = subFoAdd(aggFo, f);
		} else {						
			// First, apply deleting
			if (addedFOs != null)
				for(FlexOffer f : addedFOs)
					aggFo = subFoAdd(aggFo, f);
		}
		return aggFo;
	}
	
	private AggregatedFlexOffer subFoAdd(AggregatedFlexOffer aggFo, FlexOffer subFo)
	{
		// Check if the aggregation is possible
		assert (subFo!=null) : "Trying to aggregate with the NULL flex-offer.";
		assert (subFo.isConsumption() || subFo.isProduction()) : "Trying to aggregate with neigher production nor consumption flex-offer!";
		// First, mirror the flex-offer
		if (aggFo == null || aggFo.getSubFoMetas() == null || (aggFo.getSubFoMetas().size() == 0))
		{
			aggFo = new AggregatedFlexOffer();
			// Copy all attributes from the original flex-offer
			aggFo.setCreationInterval(subFo.getCreationInterval());
			aggFo.setOfferedById(subFo.getOfferedById());
			aggFo.setAcceptanceBeforeInterval(subFo.getAcceptanceBeforeInterval());
			aggFo.setAssignmentBeforeInterval(subFo.getAssignmentBeforeInterval());
			// 2010-01-27 AssignmentBefore duration update
			aggFo.setAssignmentBeforeDurationIntervals(subFo.getAssignmentBeforeDurationIntervals());
			
			aggFo.setStartAfterInterval(subFo.getStartAfterInterval());
			aggFo.setStartBeforeInterval(subFo.getStartBeforeInterval());
			//aggFo.setProductType(subFo.getProductType());

			aggFo.setTotalEnergyConstraint(subFo.getTotalEnergyConstraint() != null ? subFo.getTotalEnergyConstraint().clone() : null);
			aggFo.setSlices(subFo.getSlices() != null ? subFo.getSlices().clone() : null);

			if (subFo.getDefaultSchedule() != null) {
				aggFo.setDefaultSchedule(new FlexOfferSchedule(subFo.getDefaultSchedule().getStartInterval(), subFo.getDefaultSchedule().getEnergyAmounts().clone()));
			}
		
			//aggFo.setFlexibilityPrice(subFo.getFlexibilityPrice());

			// Set-up the meta
			if (aggFo.getSubFoMetas() == null)
				aggFo.setSubFoMetas(new ArrayList<AggregatedFlexOfferMetaData>());
			else 
				aggFo.getSubFoMetas().clear();

			// Set-up basic information
			aggFo.setCreationInterval(subFo.getCreationInterval());
			
			// Increase the aggregation level
			if (subFo instanceof AggregatedFlexOffer && 
			    ((AggregatedFlexOffer)subFo).getAggregationLevel() != null) 
				aggFo.setAggregationLevel(AggregationLevel.getAggregationLevel(((AggregatedFlexOffer)subFo).getAggregationLevel().getAggregationLevelId() + 1));	// Increase the aggregation level
			else 
				aggFo.setAggregationLevel(AggregationLevel.BRPAggregated);			
			
			// Set-up aggregation meta
			AggregatedFlexOfferMetaData meta = new AggregatedFlexOfferMetaData();
			meta.setSubFlexOffer(subFo);
			meta.setTimeShiftTS(0);		// There's no time shift when aggregating 1 flex-offer
			aggFo.getSubFoMetas().add(meta);			
		} else 
		{
			// Set-up all flex-offer attribute values in the conservative fashion
			assert(   !(aggFo.isConsumption() ^ subFo.isConsumption())
			   	  &&  !(aggFo.isProduction() ^ subFo.isProduction())) : 
			   		  "Aggregating flex-offers of mismatching types (production/consumption)";

			aggFo.setCreationInterval(Math.min(aggFo.getCreationInterval(), subFo.getCreationInterval()));
			aggFo.setOfferedById("aggregator");
			aggFo.setAcceptanceBeforeInterval(Math.min(aggFo.getAcceptanceBeforeInterval(), subFo.getAcceptanceBeforeInterval()));
			aggFo.setAssignmentBeforeInterval(Math.min(aggFo.getAssignmentBeforeInterval(), subFo.getAssignmentBeforeInterval()));
			// 2010-01-27 AssignmentBefore duration update
			aggFo.setAssignmentBeforeDurationIntervals(Math.max(aggFo.getAssignmentBeforeDurationIntervals(), subFo.getAssignmentBeforeDurationIntervals()));

			// Generalize the type
			/*aggFo.setProductType(EnergyType.getEnergyType(EntityType.generalizeWithType(aggFo.getProductType().getEnergyTypeId(), 
																			     subFo.getProductType().getEnergyTypeId())));*/
			// Just in case the type was generalized incorrectly
			// if (aggFo.getProductType() == null)
				// aggFo.setProductType(EnergyType.Energy);

			// aggFo.setFlexibilityPrice(Math.max(aggFo.getFlexibilityPrice(), subFo.getFlexibilityPrice()));
						
			// Update total constraints
			if (subFo.getTotalEnergyConstraint() != null)
			{
				if (aggFo.getTotalEnergyConstraint() == null)
				{
					FlexOfferConstraint ec = new FlexOfferConstraint(aggFo.getSumEnergyConstraints());
					energyConstraintsAdd(ec, subFo.getTotalEnergyConstraint());
					aggFo.setTotalEnergyConstraint(ec);
				}
				else 
					energyConstraintsAdd(aggFo.getTotalEnergyConstraint(), subFo.getTotalEnergyConstraint());
			} else 
				if (aggFo.getTotalEnergyConstraint() != null)
					energyConstraintsAdd(aggFo.getTotalEnergyConstraint(), subFo.getSumEnergyConstraints());		
				
			// Set-up the meta
			AggregatedFlexOfferMetaData meta = new AggregatedFlexOfferMetaData();
			meta.setSubFlexOffer(subFo);

			// Update the profile
			switch (profileShape)
			{
				case psAlignStart: 
					addTwoFlexOfferProfilesAlignStart(aggFo, subFo); 
					meta.setTimeShiftTS(0);	// In case of the left alignment, we always fix profiles to the left 
					break;
				case psAlignBaseline:
					long oldApos = aggFo.getDefaultSchedule().getStartInterval() - aggFo.getStartAfterInterval();
					long oldSpos = subFo.getDefaultSchedule().getStartInterval() - subFo.getStartAfterInterval();
					
					addTwoFlexOfferProfilesAlignBaseline(aggFo, subFo); 
					long newApos = aggFo.getDefaultSchedule().getStartInterval() - aggFo.getStartAfterInterval();
					
					
					// In case of the baseline alignment, we always fix profiles to the baseline start
					// (int)(subFo.getDefaultSchedule().getStartInterval() - subFo.getStartAfterInterval())
					if (newApos < oldApos) {
						for(AggregatedFlexOfferMetaData m : aggFo.getSubFoMetas()) {
							m.setTimeShiftTS((int)(m.getTimeShiftTS()+oldApos-newApos));
						}
					} 
					meta.setTimeShiftTS((int)(oldSpos - newApos));
					break;
				default:
					assert (false) : "Other then psAlignStart flex-offer profile alignments are not supported yet!"; 
			}		
			
			aggFo.getSubFoMetas().add(meta);			
		}
		return aggFo;
	}
	

	private void energyConstraintsAdd(FlexOfferConstraint ecTo, FlexOfferConstraint ecFrom)
	{
		ecTo.setLower(ecTo.getLower() + ecFrom.getLower());
		ecTo.setUpper(ecTo.getUpper() + ecFrom.getUpper());
	}
	
	private void addTwoFlexOfferProfilesAlignStart(FlexOffer aggFo, FlexOffer subFo)
	{		
		boolean isAggFoConsumption = aggFo.isConsumption();
		// 2011-11-15 Updated to work with the discrete time
		long tIntLeft = Math.min(aggFo.getStartAfterInterval(), subFo.getStartAfterInterval());
		long tFlex = Math.min(aggFo.getStartBeforeInterval() - aggFo.getStartAfterInterval(), 
							 subFo.getStartBeforeInterval() - subFo.getStartAfterInterval());
		
		// The profile update algorithm which takes into account intervals with variable durations		
	    EnergyIntervalIterator aIter = new EnergyIntervalIterator(aggFo, tIntLeft - aggFo.getStartAfterInterval());
	    EnergyIntervalIterator sIter = new EnergyIntervalIterator(subFo, tIntLeft - subFo.getStartAfterInterval());
	    List<FlexOfferSlice> newProfile = new ArrayList<FlexOfferSlice>();
	    FlexOfferSlice newInterval = null;
	    	    
	    while( !(aIter.IsEndOfProfile() && sIter.IsEndOfProfile()))
	    {
	    	if ((aIter.IsEnIntervalBoundCrossed() || sIter.IsEnIntervalBoundCrossed()))
	    	{
	    		newInterval = new FlexOfferSlice();
	    		newInterval.setEnergyConstraint(new FlexOfferConstraint(0, 0));
	    		newInterval.setDuration(1);
	    		
	    		if (isAggFoConsumption)
	    			newInterval.setCostPerEnergyUnitLimit(Double.MAX_VALUE);
	    		else
	    			newInterval.setCostPerEnergyUnitLimit(Double.MIN_VALUE);
	    			
//	    		if (sIter.getRunningInterval() != null &&
//	    			sIter.getRunningInterval().getTariffConstraint() != null)
//	    			newInterval.setTariffConstraint(new TariffConstraint(Double.MAX_VALUE, Double.MIN_VALUE));
//	    		else 
//	    			newInterval.setTariffConstraint(null);
	    			//setCostPerEnergyUnit(0);
	    		newProfile.add(newInterval);	// Add new interval
	    	}

	    	if (newInterval!=null)
	    	{
	    		// Add info from aggregated flex-offer first
	    		if (aIter.getTsBoundState() != TimeStepState.tsProfileOutOfRange)
	    		{
	    			// Energy inclusion
	    			newInterval.getEnergyConstraint().setLower(
	    					newInterval.getEnergyConstraint().getLower() + 
	    					(aIter.getRunningInterval().getEnergyConstraint().getLower() / 
	    							aIter.getRunningInterval().getDuration()));
	    			newInterval.getEnergyConstraint().setUpper(
	    					newInterval.getEnergyConstraint().getUpper() + 
	    					(aIter.getRunningInterval().getEnergyConstraint().getUpper() / 
	    							aIter.getRunningInterval().getDuration()));
	    			
	    			if (isAggFoConsumption)		    				
		    			newInterval.setCostPerEnergyUnitLimit(
		    					Math.min(newInterval.getCostPerEnergyUnitLimit(), 
		    							 aIter.getRunningInterval().getCostPerEnergyUnitLimit()));
	    			else
		    			newInterval.setCostPerEnergyUnitLimit(
		    					Math.max(newInterval.getCostPerEnergyUnitLimit(), 
		    							 aIter.getRunningInterval().getCostPerEnergyUnitLimit()));
	    			
	    			// Tariff inclusion	    			
	    			if (aIter.getRunningInterval().getTariffConstraint() != null &&
	    				newInterval.getTariffConstraint() != null)
	    			{
	    				if (isAggFoConsumption) 
	    				{
		    				newInterval.getTariffConstraint().setLower(Math.min(
    								newInterval.getTariffConstraint().getLower(),
    								aIter.getRunningInterval().getTariffConstraint().getLower()));
    				
		    				newInterval.getTariffConstraint().setUpper(Math.min(
									newInterval.getTariffConstraint().getUpper(),
									aIter.getRunningInterval().getTariffConstraint().getUpper()));					    				

	    				} else {
		    				newInterval.getTariffConstraint().setLower(Math.max(
		    								newInterval.getTariffConstraint().getLower(),
		    								aIter.getRunningInterval().getTariffConstraint().getLower()));
		    				
		    				newInterval.getTariffConstraint().setUpper(Math.max(
											newInterval.getTariffConstraint().getUpper(),
											aIter.getRunningInterval().getTariffConstraint().getUpper()));		
	    				}
	    			}
	    		}

	    		// Add info from aggregated flex-offer first
	    		if (sIter.getTsBoundState() != TimeStepState.tsProfileOutOfRange)
	    		{
	    			// Energy inclusion
	    			newInterval.getEnergyConstraint().setLower(
	    					newInterval.getEnergyConstraint().getLower() + 
	    					(sIter.getRunningInterval().getEnergyConstraint().getLower() / 
	    							sIter.getRunningInterval().getDuration()));
	    			newInterval.getEnergyConstraint().setUpper(
	    					newInterval.getEnergyConstraint().getUpper() + 
	    					(sIter.getRunningInterval().getEnergyConstraint().getUpper() / 
	    							sIter.getRunningInterval().getDuration()));
	    			
	    			if (isAggFoConsumption)	
		    			newInterval.setCostPerEnergyUnitLimit(
		    					Math.min(newInterval.getCostPerEnergyUnitLimit(), 
		    							 sIter.getRunningInterval().getCostPerEnergyUnitLimit()));
	    			else 
		    			newInterval.setCostPerEnergyUnitLimit(
		    					Math.max(newInterval.getCostPerEnergyUnitLimit(), 
		    							 sIter.getRunningInterval().getCostPerEnergyUnitLimit()));	    				
	    			// Tariff inclusion
	    			if (sIter.getRunningInterval().getTariffConstraint() != null &&
		    				newInterval.getTariffConstraint() != null)
	    			{
	    				if (isAggFoConsumption) {
		    				newInterval.getTariffConstraint().setLower(Math.min(
    								newInterval.getTariffConstraint().getLower(),
    								aIter.getRunningInterval().getTariffConstraint().getLower()));
    				
		    				newInterval.getTariffConstraint().setUpper(Math.min(
									newInterval.getTariffConstraint().getUpper(),
									aIter.getRunningInterval().getTariffConstraint().getUpper()));					    				

	    				} else {
		    				newInterval.getTariffConstraint().setLower(Math.max(
		    								newInterval.getTariffConstraint().getLower(),
		    								aIter.getRunningInterval().getTariffConstraint().getLower()));
		    				
		    				newInterval.getTariffConstraint().setUpper(Math.max(
											newInterval.getTariffConstraint().getUpper(),
											aIter.getRunningInterval().getTariffConstraint().getUpper()));		
	    				}

	    			}
	    		}

	    		if ( (!aIter.IsEnIntervalBoundCrossed()) &&
	    				(!sIter.IsEnIntervalBoundCrossed()))
	    			newInterval.setDuration(newInterval.getDuration() + 1);
	    	}

	    	aIter.nextTS();
	    	sIter.nextTS();
	    }
	    
	    // Commit changes into the aggregated flex-offer
	    aggFo.setSlices(newProfile.toArray(new FlexOfferSlice[0]));
	    // 2011-11-16 An update related to the migration to absolute time.
	    // An improved version with more precise time bounds
	    aggFo.setStartAfterInterval(tIntLeft);
	    aggFo.setStartBeforeInterval(tIntLeft + tFlex); 
	    		
	    // 2011-11-14 An update related to the migration to absolute time
//	    aggFo.setStartAfterTime(TimeUtils.mapInt2Time(tIntLeft, TimeAlign.taIntervalRight));
//	    aggFo.setStartBeforeTime(TimeUtils.mapInt2Time(tIntLeft + tFlex, TimeAlign.taIntervalLeft));	    
//	    aggFo.setStartAfterTime(tIntLeft);
//	    aggFo.setStartBeforeTime(tIntLeft + tFlex); 
	}
		
	/**
	 * Aggregate 2 FOs using baseline alignment 
	 * @param aggFo
	 * @param subFo
	 * @return Offseft of the sub-FO
	 */
	private int addTwoFlexOfferProfilesAlignBaseline(FlexOffer aggFo, FlexOffer subFo)
	{		
		boolean isAggFoConsumption = aggFo.isConsumption();
		
		/* Calculate time flexibility and offset */ 
		
		long baseStart = Math.min(aggFo.getDefaultSchedule().getStartInterval(), 
							      subFo.getDefaultSchedule().getStartInterval());
		
		long tFlexLeft = Math.min(aggFo.getDefaultSchedule().getStartInterval() - aggFo.getStartAfterInterval(), 
								  subFo.getDefaultSchedule().getStartInterval() - subFo.getStartAfterInterval());
		long tFlexRight = Math.min(aggFo.getStartBeforeInterval() - aggFo.getDefaultSchedule().getStartInterval(), 
								   subFo.getStartBeforeInterval() - subFo.getDefaultSchedule().getStartInterval());
		
		int subFOoffset = (int) (subFo.getDefaultSchedule().getStartInterval() - aggFo.getDefaultSchedule().getStartInterval());
		
		// long tFlex = tFlexRight - tFlexLeft;
		
		long tIntLeft = Math.min(aggFo.getStartAfterInterval(), subFo.getStartAfterInterval());
		
		// The profile update algorithm which takes into account intervals with variable durations		
	    EnergyIntervalIterator aIter = new EnergyIntervalIterator(aggFo, tIntLeft - aggFo.getDefaultSchedule().getStartInterval());
	    EnergyIntervalIterator sIter = new EnergyIntervalIterator(subFo, tIntLeft - subFo.getDefaultSchedule().getStartInterval());
	    List<FlexOfferSlice> newProfile = new ArrayList<FlexOfferSlice>();
	    FlexOfferSlice newInterval = null;	    
	    List<Double> baselineValues = new ArrayList<Double>();
	    	    
	    while( !(aIter.IsEndOfProfile() && sIter.IsEndOfProfile()))
	    {
	    	if ((aIter.IsEnIntervalBoundCrossed() || sIter.IsEnIntervalBoundCrossed()))
	    	{
	    		newInterval = new FlexOfferSlice();
	    		newInterval.setEnergyConstraint(new FlexOfferConstraint(0, 0));
	    		newInterval.setDuration(1);
	    		
	    		if (isAggFoConsumption)
	    			newInterval.setCostPerEnergyUnitLimit(Double.MAX_VALUE);
	    		else
	    			newInterval.setCostPerEnergyUnitLimit(Double.MIN_VALUE);
	    			
//	    		if (sIter.getRunningInterval() != null &&
//	    			sIter.getRunningInterval().getTariffConstraint() != null)
//	    			newInterval.setTariffConstraint(new TariffConstraint(Double.MAX_VALUE, Double.MIN_VALUE));
//	    		else 
//	    			newInterval.setTariffConstraint(null);
	    			//setCostPerEnergyUnit(0);
	    		newProfile.add(newInterval);	// Add new interval	    		
	    		baselineValues.add(new Double(0));
	    	}

	    	if (newInterval!=null)
	    	{
	    		// Add info from aggregated flex-offer first
	    		if (aIter.getTsBoundState() != TimeStepState.tsProfileOutOfRange)
	    		{
	    			// Energy inclusion
	    			newInterval.getEnergyConstraint().setLower(
	    					newInterval.getEnergyConstraint().getLower() + 
	    					(aIter.getRunningInterval().getEnergyConstraint().getLower() / 
	    							aIter.getRunningInterval().getDuration()));
	    			newInterval.getEnergyConstraint().setUpper(
	    					newInterval.getEnergyConstraint().getUpper() + 
	    					(aIter.getRunningInterval().getEnergyConstraint().getUpper() / 
	    							aIter.getRunningInterval().getDuration()));
	    			
	    			int snr = baselineValues.size()-1;
	    			baselineValues.set(snr, baselineValues.get(snr) + aggFo.getDefaultSchedule().getEnergyAmount((int)aIter.getRunningIntervalNr()) / 
	    															  aIter.getRunningInterval().getDuration()); 
	    			
	    			if (isAggFoConsumption)		    				
		    			newInterval.setCostPerEnergyUnitLimit(
		    					Math.min(newInterval.getCostPerEnergyUnitLimit(), 
		    							 aIter.getRunningInterval().getCostPerEnergyUnitLimit()));
	    			else
		    			newInterval.setCostPerEnergyUnitLimit(
		    					Math.max(newInterval.getCostPerEnergyUnitLimit(), 
		    							 aIter.getRunningInterval().getCostPerEnergyUnitLimit()));
	    			
	    			// Tariff inclusion	    			
	    			if (aIter.getRunningInterval().getTariffConstraint() != null &&
	    				newInterval.getTariffConstraint() != null)
	    			{
	    				if (isAggFoConsumption) 
	    				{
		    				newInterval.getTariffConstraint().setLower(Math.min(
    								newInterval.getTariffConstraint().getLower(),
    								aIter.getRunningInterval().getTariffConstraint().getLower()));
    				
		    				newInterval.getTariffConstraint().setUpper(Math.min(
									newInterval.getTariffConstraint().getUpper(),
									aIter.getRunningInterval().getTariffConstraint().getUpper()));					    				

	    				} else {
		    				newInterval.getTariffConstraint().setLower(Math.max(
		    								newInterval.getTariffConstraint().getLower(),
		    								aIter.getRunningInterval().getTariffConstraint().getLower()));
		    				
		    				newInterval.getTariffConstraint().setUpper(Math.max(
											newInterval.getTariffConstraint().getUpper(),
											aIter.getRunningInterval().getTariffConstraint().getUpper()));		
	    				}
	    			}
	    		}

	    		// Add info from aggregated flex-offer first
	    		if (sIter.getTsBoundState() != TimeStepState.tsProfileOutOfRange)
	    		{
	    			// Energy inclusion
	    			newInterval.getEnergyConstraint().setLower(
	    					newInterval.getEnergyConstraint().getLower() + 
	    					(sIter.getRunningInterval().getEnergyConstraint().getLower() / 
	    							sIter.getRunningInterval().getDuration()));
	    			newInterval.getEnergyConstraint().setUpper(
	    					newInterval.getEnergyConstraint().getUpper() + 
	    					(sIter.getRunningInterval().getEnergyConstraint().getUpper() / 
	    							sIter.getRunningInterval().getDuration()));
	    			
	    			int snr = baselineValues.size()-1;
	    			baselineValues.set(snr, baselineValues.get(snr) + subFo.getDefaultSchedule().getEnergyAmount((int)sIter.getRunningIntervalNr()) / 
	    															  sIter.getRunningInterval().getDuration());	    			
	    			
	    			if (isAggFoConsumption)	
		    			newInterval.setCostPerEnergyUnitLimit(
		    					Math.min(newInterval.getCostPerEnergyUnitLimit(), 
		    							 sIter.getRunningInterval().getCostPerEnergyUnitLimit()));
	    			else 
		    			newInterval.setCostPerEnergyUnitLimit(
		    					Math.max(newInterval.getCostPerEnergyUnitLimit(), 
		    							 sIter.getRunningInterval().getCostPerEnergyUnitLimit()));	    				
	    			// Tariff inclusion
	    			if (sIter.getRunningInterval().getTariffConstraint() != null &&
		    				newInterval.getTariffConstraint() != null)
	    			{
	    				if (isAggFoConsumption) {
		    				newInterval.getTariffConstraint().setLower(Math.min(
    								newInterval.getTariffConstraint().getLower(),
    								aIter.getRunningInterval().getTariffConstraint().getLower()));
    				
		    				newInterval.getTariffConstraint().setUpper(Math.min(
									newInterval.getTariffConstraint().getUpper(),
									aIter.getRunningInterval().getTariffConstraint().getUpper()));					    				

	    				} else {
		    				newInterval.getTariffConstraint().setLower(Math.max(
		    								newInterval.getTariffConstraint().getLower(),
		    								aIter.getRunningInterval().getTariffConstraint().getLower()));
		    				
		    				newInterval.getTariffConstraint().setUpper(Math.max(
											newInterval.getTariffConstraint().getUpper(),
											aIter.getRunningInterval().getTariffConstraint().getUpper()));		
	    				}

	    			}
	    		}

	    		if ( (!aIter.IsEnIntervalBoundCrossed()) &&
	    				(!sIter.IsEnIntervalBoundCrossed()))
	    			newInterval.setDuration(newInterval.getDuration() + 1);
	    	}

	    	aIter.nextTS();
	    	sIter.nextTS();
	    }
	    
	    // Commit changes into the aggregated flex-offer
	    aggFo.setSlices(newProfile.toArray(new FlexOfferSlice[0]));
	    // 2011-11-16 An update related to the migration to absolute time.
	    // An improved version with more precise time bounds
	    aggFo.setStartAfterInterval(baseStart-tFlexLeft);
	    aggFo.setStartBeforeInterval(baseStart + tFlexRight); 
	    aggFo.getDefaultSchedule().setStartInterval(baseStart);	
	    
	    double [] baseLinevals = new double[baselineValues.size()];
	    for(int k=0; k < baselineValues.size(); k++) {
	    	baseLinevals[k] = baselineValues.get(k);	
	    }
	    aggFo.getDefaultSchedule().setEnergyAmounts(baseLinevals);
	    		
	    // 2011-11-14 An update related to the migration to absolute time
//	    aggFo.setStartAfterTime(TimeUtils.mapInt2Time(tIntLeft, TimeAlign.taIntervalRight));
//	    aggFo.setStartBeforeTime(TimeUtils.mapInt2Time(tIntLeft + tFlex, TimeAlign.taIntervalLeft));	    
//	    aggFo.setStartAfterTime(tIntLeft);
//	    aggFo.setStartBeforeTime(tIntLeft + tFlex); 
	    return subFOoffset;
	}
	
	// The disaggregation method
	public static List<FlexOffer> Disaggregate(AggregatedFlexOffer aggFo) throws AggregationException
	{		
		FlexOfferSchedule aggSch = aggFo.getFlexOfferSchedule();
				
		if (aggFo.getSubFoMetas() == null)
			throw new AggregationException("Trying to disaggregate a flex-offer, but it has no aggregation metadata!");
		
		// Create an iterator array
		EnergyIntervalIterator [] sI = new EnergyIntervalIterator[aggFo.getSubFoMetas().size()];
		FlexOfferSchedule [] sA = new FlexOfferSchedule [aggFo.getSubFoMetas().size()];
		
		for(int i = 0; i < aggFo.getSubFoMetas().size(); i++)
		{
			FlexOffer subFo = aggFo.getSubFoMetas().get(i).getSubFlexOffer();
			// Initialize micro flex-offer assignments
			sA[i] = new FlexOfferSchedule();
			subFo.setFlexOfferSchedule(sA[i]);
			
			// Disaggregate start time
			long offsetStart = aggSch.getStartInterval() - aggFo.getStartAfterInterval();
			
			sA[i].setStartInterval(
					subFo.getStartAfterInterval() + 
					offsetStart + 
					aggFo.getSubFoMetas().get(i).getTimeShiftTS());
			
			// Disaggregate price tariff. Prices are equal for all flex-offers
//	2011-10-26 Don't need to disaggregate prices anymore
//			sA[i].getFlexSchedule().setEnergyPrices(aggFa.getFlexSchedule().getEnergyPrices());
			
			// Initialize empty schedule for micro flex-offer
			sA[i].setEnergyAmounts(new double[subFo.getSlices().length]);
			for(int j=0; j < sA[i].getEnergyAmounts().length; j++)
				sA[i].getEnergyAmounts()[j] = 0;
			
			assert(subFo.getStartAfterInterval() <= sA[i].getStartInterval() && 
					sA[i].getStartInterval() <= subFo.getStartBeforeInterval()) :
						"Disaggregation of time flexibility failed: StartTime  run out of the StartAfter and StartBefore bound!";			
			
			// Initialize iterators for profile traversals
			sI[i] = new EnergyIntervalIterator(
					subFo,aggFo.getStartAfterInterval() -
					subFo.getStartAfterInterval() - 
					aggFo.getSubFoMetas().get(i).getTimeShiftTS());
		}
		
		EnergyIntervalIterator aIter = new EnergyIntervalIterator(aggFo, 0);	// We start iterating from agg. fo first time interval
		
		// The idea is that we want to disaggregate amount "x" so that it would equally fill min max ranges of micro flex-offers,
		// i.e. p_i = (x_i - l_i) / (h_i - l_i), 0 <= p <= 1; we require that p_i = p_j, i != j, sum_{i}(x_i) = x  
		while (aIter.getTsBoundState() != TimeStepState.tsProfileOutOfRange)
		{
			// enAmount is the "x"
			double enAmount = aggSch.getEnergyAmount((int)aIter.getRunningIntervalNr()) / 
													                  aIter.getRunningInterval().getDuration();
			
			double sumLi = 0, sumD = 0; 	// Here D is "h" - "l".
			for (int i = 0; i < sI.length; i++)
				if (sI[i].getTsBoundState() != TimeStepState.tsProfileOutOfRange)
				{
					sumLi += sI[i].getRunningInterval().getEnergyLower() / sI[i].getRunningInterval().getDuration();
					sumD  += (sI[i].getRunningInterval().getEnergyUpper() - sI[i].getRunningInterval().getEnergyLower()) /
							  sI[i].getRunningInterval().getDuration();
				}
			
			// The fraction of energy, which will occupy all micro flex-offer relevant energy intervals
			double p = 1;
			if (sumD != 0)
				p = (enAmount - sumLi) / sumD;
				
			// Allocate "p" fraction of "x" to all micro flex-offers
			for (int i = 0; i < sI.length; i++)
				if (sI[i].getTsBoundState() != TimeStepState.tsProfileOutOfRange)
					sA[i].getEnergyAmounts()[(int)sI[i].getRunningIntervalNr()] += 
							(sI[i].getRunningInterval().getEnergyLower() + 	
							  p *	(sI[i].getRunningInterval().getEnergyUpper() - sI[i].getRunningInterval().getEnergyLower())
							 ) / sI[i].getRunningInterval().getDuration(); 
			
			// Goto next TS on all iterators
			aIter.nextTS();
			for (EnergyIntervalIterator si : sI)
				si.nextTS();
		}
		
		/* Added 2016-09-21 */
		List<FlexOffer> fos = Arrays.asList(aggFo.getSubFlexOffers());
		
		/* Total constraint treatment */
		DisaggregationHelper.enforceTotalEnergyConstraint(fos);
		
		return fos;
	}

	@Override
	public void foClear() {
		this.aggFOhash.clear();		
	}
	
	@Override
	public AggregatedFlexOffer aggregateSingleFo(FlexOffer f) {
		AggregatedFlexOffer aggFo;
		aggFo = this.subFoAdd(null, f);
		return aggFo;
	}	
}

