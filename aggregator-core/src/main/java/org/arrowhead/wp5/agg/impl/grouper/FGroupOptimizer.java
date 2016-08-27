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
import java.util.Iterator;
import java.util.List;

import org.arrowhead.wp5.agg.api.ChangeType;
import org.arrowhead.wp5.agg.impl.common.AggParamAbstracter;
import org.arrowhead.wp5.agg.impl.common.ChangeRecOfGroup;
import org.arrowhead.wp5.agg.impl.common.FGridRectangle;
import org.arrowhead.wp5.agg.impl.common.FGridVector;
import org.arrowhead.wp5.agg.impl.common.FGroup;
import org.arrowhead.wp5.agg.impl.common.FlexOfferWeightDelegate;
import org.arrowhead.wp5.agg.impl.common.IteratorNested;
import org.arrowhead.wp5.agg.impl.common.IteratorNested.InnerGenerator;
import org.arrowhead.wp5.core.entities.FlexOffer;

/**
 * This is an implementation of the super group optimizer. Super groups can be merged if the maximum 
 * distance between them is shorted then a query vector.  Super groups can be spited if it grew to a size larger 
 * than the query vector  
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 * @version 1.1
 * Change list (with respect to D3.3): 
 *  -  v1.1:  Complete re-implementation of the optimizer. The new version of optimizer involves 
 *            the hierarchical clustering on a group split. The group merge is also improved.
 *  	
 */
public class FGroupOptimizer {
	private OptimizationParams params;
	private AggParamAbstracter aggParAbstracter;
	private FGridVector qVector; 
	
	public FGroupOptimizer(OptimizationParams params, AggParamAbstracter aggParAbstracter)
	{
		this.params = params;
		this.aggParAbstracter = aggParAbstracter;
		this.qVector = aggParAbstracter.getQueryVector(); 
	}
		
	public void optimizeGroups(Collection<? extends ChangeRecOfGroup> grps)
	{
		for(ChangeRecOfGroup g : grps)
			if (g.getChangeType() != ChangeType.ctDeleted)
				optimizeGroup(g);
	}
	
	public void optimizeGroup(ChangeRecOfGroup g)
	{
		if (this.params.autoMergeNeighbours) this.optimizeAutoIncSize(g);
		// Optimization of group based on the min weights is a future work
		// if (this.params.ensureMinGroupWeight>0) this.optimizeMinWeight(g);
	}
	
	private void optimizeAutoIncSize(ChangeRecOfGroup g)
	{
		if (g.getChangeType() != ChangeType.ctUpsized) {
			// Prepares the current group's neighbor list
			List<FGroup> nGrps = g.getGroup().getNeighbours();
			if (nGrps == null || nGrps.size() == 0)
				return; // If a group has no neighbors, we don't do optimization
			
			FGroup mGrp = g.getGroup(); 
				
			boolean canMerge = false;
			do
			{
				int closestNind = -1;
				FGridVector distClosest = new FGridVector(this.aggParAbstracter.getDimCount());
				distClosest.setToMAX();
				
				for (int i = 0; i < nGrps.size(); i++) {
					// Get distance between MBR's of two groups
					FGridVector dist = mGrp.distanceBetweenMBRs(this.aggParAbstracter, nGrps.get(i));
					if (dist.isLowerEqualThan(this.qVector) &&
						dist.isLowerEqualThan(distClosest))
					{
						distClosest = dist;
						closestNind = i;
					}
				}
				
				canMerge = closestNind > -1;
				if (canMerge)
				{
					// Merge smaller group into a bigger
					if (mGrp.getGroupWeight(this.aggParAbstracter) >= nGrps.get(closestNind).getGroupWeight(this.aggParAbstracter))
						mGrp.mergeWithGroup(nGrps.get(closestNind));
					else
					{
						nGrps.get(closestNind).mergeWithGroup(g.getGroup());
						mGrp = nGrps.get(closestNind);
					}
					nGrps.remove(closestNind);
				}
			} while (canMerge);
		}
		
		if (g.getChangeType() != ChangeType.ctDownsized)
			{
				if (!g.getGroup().getMBR(this.aggParAbstracter).getSize()
						.isLowerEqualThan(this.qVector))
				{
					assert g.getGroup().getPartitions().size() > 1 : "Super group is oversized but cannot be splitted as it has only 1 partition!";
					
					// All partitions are initially seen as individual groups
					List<FGroupOfGroups> grps = new ArrayList<FGroupOfGroups>();					
					for (int i = 0; i < g.getGroup().getPartitions().size(); i++)
					{						
						FGroupOfGroups sg = new FGroupOfGroups();
						sg.getGrList().add(g.getGroup().getPartitions().get(i));
						assert sg.getMBR(this.aggParAbstracter).getSize().isLowerEqualThan(this.qVector) : "A single partition cannot be larger than a query vector!";
						grps.add(sg);						
					}
					
					// Iteratively merge all groups using the hierarchical clustering algorithm
					boolean canMerge = false;
					do
					{
						int closestG1 = 0;	// Ids of the two closest groups 
						int closestG2 = 0;
						FGridVector distClosest = new FGridVector(this.aggParAbstracter.getDimCount());
						distClosest.setToMAX();
						
						for (int i = 0; i < grps.size(); i++)
							for(int j = i + 1; j < grps.size(); j++)
							{
								// Compute the distance between two groups
								FGridRectangle grp1rect = grps.get(i).getMBR(this.aggParAbstracter);
								grp1rect.mergeRectangle(grps.get(j).getMBR(this.aggParAbstracter));							
								if (grp1rect.getSize().isLowerEqualThan(distClosest))
								{
									distClosest = grp1rect.getSize();
									closestG1 = i;
									closestG2 = j;
								}
							}	
						
						canMerge = distClosest.isLowerEqualThan(this.qVector);
						if (canMerge)							
						{
							// Merge two closest groups
							grps.get(closestG1).mergeWithGroup(grps.get(closestG2));
							grps.remove(closestG2);
						}						
					} while (canMerge);
					
					// Once the clustering is done, split the group	
					Collections.sort(grps, new Comparator<FGroupOfGroups>(){
						@Override
						public int compare(FGroupOfGroups g1, FGroupOfGroups g2) {
							return Double.compare(g2.getGroupWeight(aggParAbstracter), g1.getGroupWeight(aggParAbstracter));
						}					
					});
					
					for(int i=1; i< grps.size(); i++)		// Leave the biggest part as the main group
						g.getGroup().splitGroup(grps.get(i).getGrList());		// Split the actual group
				}			
			}
	}
	
	
	public final static class OptimizationParams {
		// The group builder will try to merge neighbors even in the weight limit is reached
		public boolean autoMergeNeighbours = true;
		// The group builder will try to build groups of size higher than "ensureMinGroupWeight"
		public double ensureMinGroupWeight = -1f;
		// Eager merge parts option:
		// Merges parts of other groups if needed to satisfy "ensureMinGroupWeight" requirement
		public boolean eagerMergeGroupParts = false; 
	}

	public final class FGroupOfGroups extends FGroup
	{		
		private  List<FGroup> grList;
		
		public FGroupOfGroups()
		{
			this.grList = new ArrayList<FGroup>();
		}
		
		public List<FGroup> getGrList()
		{
			return this.grList;
		}
	
		@Override
		public Iterator<FlexOffer> iterator() {
			return new IteratorNested<FGroup, FlexOffer>(this.grList.iterator(), new InnerGenerator<FGroup, FlexOffer>()
					{
						@Override
						public Iterator<FlexOffer> generateItems(
								FGroup g) {
							return g.iterator();
						}					
					});
		}
		
		@Override
		public void mergeWithGroup(FGroup otherGrp) {		
			this.grList.addAll(((FGroupOfGroups)otherGrp).getGrList());
		}
		
		// Efficient way to compute MBR
		@Override
		public FGridRectangle getMBR(AggParamAbstracter fa) {
			FGridRectangle rect = new FGridRectangle(fa.getDimCount());
			rect.clearRectangle();
			for(FGroup g  : this.grList)
				rect.mergeRectangle(g.getMBR(fa));
			
			return rect; 
		}
		
		@Override
		public double getGroupWeight(AggParamAbstracter fa) {
			FlexOfferWeightDelegate dlg = fa.getWeightDlg();
			double weight = dlg.getZeroWeight();
			for (FGroup g : this.grList)
				weight = dlg.addTwoWeights(weight, g.getGroupWeight(fa));
			return weight;
		}

		@Override
		public int getGId() {
			return 0;
		}	
	}
	
}


