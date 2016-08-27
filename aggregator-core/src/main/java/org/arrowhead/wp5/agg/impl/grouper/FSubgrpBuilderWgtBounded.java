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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.arrowhead.wp5.agg.api.ChangeRecOfFlexOffer;
import org.arrowhead.wp5.agg.api.ChangeType;
import org.arrowhead.wp5.agg.impl.common.AggParamAbstracter;
import org.arrowhead.wp5.agg.impl.common.ChangeRecOfGroup;
import org.arrowhead.wp5.agg.impl.common.ChangeTracker;
import org.arrowhead.wp5.agg.impl.common.FGroup;
import org.arrowhead.wp5.agg.impl.common.IteratorTransforming;
import org.arrowhead.wp5.core.entities.FlexOffer;

/**
 * An implementation of the incremental sub-group builder. It partitions a super group to subgroups 
 * so that total weights of all subgroup falls into specified weight range.
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 *
 */
public class FSubgrpBuilderWgtBounded implements IFSubgrpBuilder {
	private AggParamAbstracter aggParAbstracter; 
	private BinPackingParams binPackerParams = null;

	// Non-aggregated flex-offers list and its changes 
	private LinkedHashSet<FlexOffer> foNotAggregated = new LinkedHashSet<FlexOffer> ();	
	private ChangeTracker<FlexOffer, ChangeRecOfFlexOffer> foNotAggregatedChanges = null;

	// A hash map of group to sub-group 
	private HashMap<FGroup, FSubGroupList> subgroupHash = new HashMap<FGroup, FSubGroupList>();
	
	
	public double getWeightMin() {
		return this.aggParAbstracter.getWeightMinValue();
	}

	public double getWeightMax() {
		return this.aggParAbstracter.getWeightMaxValue();
	}
	
	@Override
	public void resetChanges() {
		this.foNotAggregatedChanges.clearChanges();						
	}
	
	@Override
	public void foClear()
	{
		this.foNotAggregated.clear();
		this.foNotAggregatedChanges.clearChanges();
		this.subgroupHash.clear();
	}
	
	public FSubgrpBuilderWgtBounded(AggParamAbstracter aggParAbstracter, BinPackingParams params) {
		assert(params!=null): "Correct bin packing parameters must be provided";
		this.aggParAbstracter = aggParAbstracter;		
		this.binPackerParams = params;
		// Create change tracker
		this.foNotAggregatedChanges = new ChangeTracker<FlexOffer, ChangeRecOfFlexOffer>(
											ChangeRecOfFlexOffer.getFactory());
	}
	
	// Bin packing attributes/methods 	
	private enum PackingStatus
	{
		psCanPack,
		psMaxExceeded,
		psMinUncreached
	};
	
	private double getFOweight(FlexOffer f)
	{
		return this.aggParAbstracter.getWeightDlg().getWeight(f);
	}
	
	private double addTwoWeights(double weight1, double weight2)
	{
		return this.aggParAbstracter.getWeightDlg().addTwoWeights(weight1, weight2);
	}
	
	private double getZeroWeight()
	{
		return this.aggParAbstracter.getWeightDlg().getZeroWeight();
	}
	
	private class PackingResult
	{
		public PackingStatus status;
		public FSubGroup subgroup = null;
	}
	
	private double calcFlexOfferTotalWeight(Collection<FlexOffer> fo)
	{
		double sumWeight = this.getZeroWeight();
		for(FlexOffer f : fo)
			sumWeight = addTwoWeights(sumWeight, this.getFOweight(f));
		return sumWeight;
	}
	
	// The actual bin-packing
	public void allocateFosToGroups (Iterable<FlexOffer> newFos, FSubGroupList foGrps, List<FlexOffer> nonAggregatedFOs, ChangeTracker<FGroup, ChangeRecOfGroup> changeTracker)	
	{
		// Sort the original flex-offer list based on weights		
		List<FlexOffer> sortedFOlist = new LinkedList<FlexOffer>();
		for(FlexOffer f: newFos)
			sortedFOlist.add(f);
		Collections.sort(sortedFOlist, new Comparator<FlexOffer>(){
			@Override
			public int compare(FlexOffer f1, FlexOffer f2) {
				return (getFOweight(f1) > getFOweight(f2) ? 0 : 1);
			}			
		});
		
		// Step 1 - get rid of flex-offers that can't be packed anywhere
		while (sortedFOlist.size()>0 && (this.getFOweight(sortedFOlist.get(0))>this.getWeightMax()))
		{
			if (nonAggregatedFOs!=null)
				nonAggregatedFOs.add(sortedFOlist.get(0));

			sortedFOlist.remove(0);
		}		
		
		// Step 2 - pack flex-offers to existing groups, if possible
		if (this.binPackerParams.isUpsizeAllowed() && tryPackingToExistingGroups(sortedFOlist, foGrps,
						changeTracker))
			return;
		else {

			// Step 3 - packing of all flex-offers is not possible, generate new
			// groups
			FSubGroupList newGrps = new FSubGroupList(foGrps.getSuperGroup());

			PackingResult pr;
			// First, check if minimum condition can be reached
			// Second, pack flex-offers into new groups
			while ((calcFlexOfferTotalWeight(sortedFOlist) >= this.getWeightMin())
					&& (pr = packIntoValidSubgroup(sortedFOlist, 0, this.getZeroWeight())).status == PackingStatus.psCanPack) {
				newGrps.add(pr.subgroup);
				sortedFOlist.removeAll(pr.subgroup.getFlexOfferList());

				// Track changes if requested
				if (changeTracker != null)
					changeTracker.incUpdateChange(pr.subgroup,
							ChangeType.ctAdded);
			}

			// Try to distribute the remaining flex-offers to the old and new
			// flex-offer groups

			for (FlexOffer f : sortedFOlist) {
				// Loops through the new groups
				for (FSubGroup sg : newGrps)
					if (this.addTwoWeights(sg.getGroupWeight(this.aggParAbstracter), this.getFOweight(f)) 
							<= this.getWeightMax()) 
					{
						sg.addFlexOffer(f);
						break;
					}
				// Loops through the old groups, if upsizing is allowed
				if (this.binPackerParams.isUpsizeAllowed())
					for (FSubGroup sg : foGrps)
						if (this.addTwoWeights(sg.getGroupWeight(this.aggParAbstracter),
								this.getFOweight(f)) <= this.getWeightMax()) 
						{
							sg.addFlexOffer(f);
							if (changeTracker != null)
								changeTracker.incUpdateChange(sg, ChangeType.ctUpsized)
									.foWasAdded(f);
							break;
						}
				// If the flex-offer does not fit anywhere, add it to the non
				// bin packed list
				if (nonAggregatedFOs != null)
					nonAggregatedFOs.add(f);
			}

			// Merge two groups
			for(FSubGroup g : newGrps)
			{
				foGrps.add(g);
				if (changeTracker != null)
					changeTracker.incUpdateChange(g, ChangeType.ctAdded);					
			}
		}
	}
		
	private boolean tryPackingToExistingGroups(Collection<FlexOffer> sortedFL, FSubGroupList foGrps, ChangeTracker<FGroup, ChangeRecOfGroup> changeTracker)
	{	
		if (foGrps.size() == 0)
			return false;
		
		double [] weights = new double[foGrps.size()];
		double sumMax = this.getZeroWeight();
		double sumGrpWgt = this.getZeroWeight();
		for(int i = 0; i< foGrps.size(); i++)
		{
			weights [i] = foGrps.get(i).getGroupWeight(this.aggParAbstracter);
			sumMax = this.addTwoWeights(sumMax, this.getWeightMax());
			sumGrpWgt = this.addTwoWeights(sumGrpWgt, weights[i]);			 
		}
		
		double flWeight = this.getZeroWeight();
		for (FlexOffer f : sortedFL)
			flWeight = this.addTwoWeights(flWeight, this.getFOweight(f));
			//emptySpace -= this.getFOweight(f);
		
		if (sumMax < this.addTwoWeights(sumGrpWgt, flWeight)) return false;				// Can't fit flex-offers into existing groups
			
		// Otherwise, try to add to existing groups
		List<FlexOffer> copyOfFL = new LinkedList<FlexOffer>(sortedFL);
		FSubGroup [] grpSupplements = new FSubGroup[weights.length];  
		
		for(int i = weights.length-1; i>=0; i--)
		{
			PackingResult pr = this.packIntoValidSubgroup(copyOfFL, 0, weights[i]);
			if (pr.status == PackingStatus.psCanPack)
			{
				copyOfFL.removeAll(pr.subgroup.getFlexOfferList());
				grpSupplements[i] = pr.subgroup;
			} else 
				grpSupplements[i] = null;
			if (copyOfFL.size() == 0)
				break;
		}
		
		// Packing succeeded, let's reset changes to existing groups
		if (copyOfFL.size() == 0)
		{
			for(int i = 0; i< weights.length; i++)
				if (grpSupplements[i] != null)
				{
					foGrps.get(i).addFlexOffers(grpSupplements[i]);
					if (changeTracker!=null)
					{
						ChangeRecOfGroup c = changeTracker.incUpdateChange(foGrps.get(i), ChangeType.ctUpsized);
						for (FlexOffer f : grpSupplements[i])
							c.foWasAdded(f);
					}
				}
			return true;
		} else return false;
	}

	private PackingResult packIntoValidSubgroup(List<FlexOffer> sortedFL, int foIndex, double runningWeight)
	{		
		if (runningWeight > this.getWeightMax())
		{
			PackingResult pr = new PackingResult();
			pr.status = PackingStatus.psMaxExceeded;
			return pr;
		}
		// Find the next item that fits
		while(foIndex < sortedFL.size() && 
			 (this.addTwoWeights(runningWeight, this.getFOweight(sortedFL.get(foIndex))) > this.getWeightMax()))
					foIndex++;
		
		// If not found
		if (foIndex >= sortedFL.size())
		{	
			PackingResult pr = new PackingResult();
			pr.status = runningWeight<this.getWeightMin() ? 
						PackingStatus.psMinUncreached : PackingStatus.psMaxExceeded;
			return pr;
		}
		
		PackingResult pr = null;
		
		do {			
			double sumWeight = this.addTwoWeights(runningWeight, this.getFOweight(sortedFL.get(foIndex)));

			if (sumWeight >= this.getWeightMin()) {
				// Already found solution, but try to refine it
				pr = new PackingResult();
				pr.status = PackingStatus.psCanPack;
				pr.subgroup = new FSubGroup();
				pr.subgroup.addFlexOffer(sortedFL.get(foIndex));
				
				// Tries to fill the group as max as possible
				if (this.binPackerParams.isForceFillMaximally())
				{
					PackingResult pr2 = packIntoValidSubgroup(sortedFL, foIndex + 1, sumWeight);
					if (pr2.status == PackingStatus.psCanPack) {
						pr.subgroup.addFlexOffers(pr2.subgroup);
					}
				}
			} else {
				pr = packIntoValidSubgroup(sortedFL, foIndex + 1, sumWeight);
				if (pr.status == PackingStatus.psCanPack) {
					pr.subgroup.addFlexOffer(sortedFL.get(foIndex));
				}
			}				
		} while ((pr.status != PackingStatus.psCanPack) && 
				 (this.binPackerParams.isEagerBinPacking() && (++ foIndex) < sortedFL.size()));

		return pr;		
	}
	

	public Iterator<ChangeRecOfFlexOffer> getNonAggregatedFOchanges() {
		return this.foNotAggregatedChanges.iterator();
	}
	
	public Iterator<ChangeRecOfFlexOffer> getNonAggregatedFoAll()
	{
		return new IteratorTransforming<FlexOffer, ChangeRecOfFlexOffer>(this.foNotAggregated.iterator()) {
			@Override
			public ChangeRecOfFlexOffer apply(FlexOffer f) {
				ChangeRecOfFlexOffer cf = new ChangeRecOfFlexOffer(f);
				cf.setChangeType(FSubgrpBuilderWgtBounded.this.foNotAggregatedChanges.getChange(f));
				return cf;
			}
		};
	}
	
	// inGroup - Input group;
	protected FSubGroupList incSubGroupsAddNew(FGroup addedGroup,
			ChangeTracker<FGroup, ChangeRecOfGroup> changeTracker) {
		FSubGroupList sgl = new FSubGroupList(addedGroup);
		// Update a non-aggregated flex-offer list
		List<FlexOffer> nonAggregatedFOs = new ArrayList<FlexOffer>();
		try {
			this.allocateFosToGroups(addedGroup, sgl, nonAggregatedFOs,
					changeTracker);
			for (FlexOffer f : nonAggregatedFOs) { // Adds non aggregated
													// flex-offers into the
													// global list
				this.foNotAggregated.add(f);
				this.foNotAggregatedChanges.incUpdateChange(f, ChangeType.ctAdded);
			}
		} finally {
			nonAggregatedFOs.clear();
		}
		return sgl;
	}
	
	protected void incSubGroupsMaintain(FSubGroupList grpList, ChangeTracker<FGroup, ChangeRecOfGroup> changeTracker) {
		// Fill hash of flex-offers
		LinkedHashSet<FlexOffer> foSet = new LinkedHashSet<FlexOffer>();
		final List<FSubGroup> grpsToRebuild = new ArrayList<FSubGroup>();
		List<FlexOffer> nonAggregatedFOs = new ArrayList<FlexOffer>();

		try {
			for (FlexOffer f : grpList.getSuperGroup())
				foSet.add(f);

			// Make a list of changes subgroups
			for (FSubGroup g : grpList)
				for (int i = g.getFlexOfferList().size() - 1; i >= 0; i--) {
					FlexOffer f = g.getFlexOfferList().get(i);
					// The flex-offer was deleted from a subgroup
					if (foSet.contains(f))
						foSet.remove(f); // foNonBinPackedHs will contain
											// non-added
											// flex-offers.
					else { // Try to take the flex-offer from the group out, and
							// check if no constraints are violated
						
						// Verify this - after the abstraction is made
						if (g.getGroupWeight(this.aggParAbstracter)  >= 
							this.addTwoWeights(this.getFOweight(f), this.getWeightMin())) 
						{
							g.getFlexOfferList().remove(i);
							changeTracker.incUpdateChange(g, ChangeType.ctDownsized).foWasRemoved(f);
						} else {
							grpsToRebuild.add(g);
							changeTracker.incUpdateChange(g, ChangeType.ctDeleted);
						}
					}
				}

			// Remove old groups
			grpList.removeAll(grpsToRebuild);

			// Prepare a list of flex-offers, that needs to be packed to the
			// subgroups
			for (FSubGroup g : grpsToRebuild)
				foSet.addAll(g.getFlexOfferList());

			if (foSet.size() > 0) {	
				// Allocate non-bin packed flex-offers to new or existing groups
				this.allocateFosToGroups(foSet, grpList, nonAggregatedFOs,
						changeTracker);

				// Update the non-aggregated flex-offer list
				for (FlexOffer f : nonAggregatedFOs) {
					if (!this.foNotAggregated.contains(f)) {
						this.foNotAggregated.add(f);
						this.foNotAggregatedChanges
								.incUpdateChange(f, ChangeType.ctAdded);
					}
					foSet.remove(f);
				}
				for (FlexOffer f : foSet)
					if (this.foNotAggregated.contains(f)) {
						this.foNotAggregated.remove(f);
						this.foNotAggregatedChanges
								.incUpdateChange(f,	ChangeType.ctDeleted);
					}
			}
		} finally {
			nonAggregatedFOs.clear();
			foSet.clear();
			grpsToRebuild.clear();
			nonAggregatedFOs.clear();
		}
	}

	protected void incSubGroupsRemoveAll(FSubGroupList sgs, ChangeTracker<FGroup, ChangeRecOfGroup> gct) {
		for (FSubGroup g : sgs)
		{
			for (FlexOffer f : g)
				if (this.foNotAggregated.contains(f)) {
					this.foNotAggregated.remove(f);
					this.foNotAggregatedChanges
							.incUpdateChange(f, ChangeType.ctDeleted);
				}
			gct.incUpdateChange(g, ChangeType.ctDeleted);
		}
		sgs.clear();	
	}

	// The main method that converts group changes into sub-group changes
	@Override
	public Iterator<ChangeRecOfGroup> updateSingleGroup(
			ChangeRecOfGroup changedSuperGroup) {	
		FSubGroupList sgs;
		ChangeTracker<FGroup, ChangeRecOfGroup> gct = new ChangeTracker<FGroup, ChangeRecOfGroup>(
				ChangeRecOfGroup.getFactory());

		
		switch (changedSuperGroup.getChangeType())	{
		case ctAdded:
			sgs = this.incSubGroupsAddNew(changedSuperGroup.getGroup(), gct);
			this.subgroupHash.put(changedSuperGroup.getGroup(), sgs);			
			break;
		case ctDeleted:
			sgs = this.subgroupHash.get(changedSuperGroup.getGroup());
			if (sgs != null)
			{
				this.incSubGroupsRemoveAll(sgs, gct);			
				this.subgroupHash.remove(changedSuperGroup.getGroup()); // Remove from subgroup hash	
			}
			break;
		default:	// All other cases
			sgs = this.subgroupHash.get(changedSuperGroup.getGroup());
			if (sgs != null)
				this.incSubGroupsMaintain(sgs, gct);
			break;
		}
		return gct.iterator();
	}
	
	public final static class BinPackingParams {
		private boolean eagerBinPacking = true; // If true, it tries all possible combinations to fill the bin
		private boolean upsizeAllowed = true; 	// If true, flex-offers are allowed to be added to existing group.
												// Otherwise, a new group is created every time
		private boolean forceFillMaximally = true;
		
		/**
		 * @return the eagerBinPacking
		 */
		public boolean isEagerBinPacking() {
			return eagerBinPacking;
		}
		/**
		 * @param eagerBinPacking the eagerBinPacking to set
		 */	
		public void setEagerBinPacking(boolean eagerBinPacking) {
			this.eagerBinPacking = eagerBinPacking;
		}
		
		public boolean isUpsizeAllowed()
		{
			return this.upsizeAllowed;
		}
		
		public void setUpsizeAllowed(boolean upsizeAllowed)
		{
			this.upsizeAllowed = upsizeAllowed;
		}

		/**
		 * @return the forceFillMaximally
		 */
		public boolean isForceFillMaximally() {
			return forceFillMaximally;
		}
		/**
		 * @param forceFillMaximally the forceFillMaximally to set
		 */
		public void setForceFillMaximally(boolean forceFillMaximally) {
			this.forceFillMaximally = forceFillMaximally;
		}	
	}	

}

