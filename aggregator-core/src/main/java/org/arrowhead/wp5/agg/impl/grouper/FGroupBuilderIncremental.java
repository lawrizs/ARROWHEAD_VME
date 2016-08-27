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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.arrowhead.wp5.agg.api.ChangeType;
import org.arrowhead.wp5.agg.api.IChangeRecFactory;
import org.arrowhead.wp5.agg.impl.common.AggParamAbstracter;
import org.arrowhead.wp5.agg.impl.common.ChangeRecOfGroup;
import org.arrowhead.wp5.agg.impl.common.ChangeTracker;
import org.arrowhead.wp5.agg.impl.common.FGridRectangle;
import org.arrowhead.wp5.agg.impl.common.FGroup;
import org.arrowhead.wp5.agg.impl.common.IteratorNested;
import org.arrowhead.wp5.agg.impl.common.IteratorNested.InnerGenerator;
import org.arrowhead.wp5.agg.impl.common.IteratorUpcast;
import org.arrowhead.wp5.core.entities.FlexOffer;


/**
 * FGroupBuilderIncremental builds a so-called super groups, which enclose one or more populated cells
 * and represent sets of similar flex-offers. While the FGridMapper updates the populated cell every time 
 * new flex-offers are added or removed, the FGroupBuilderIncremental defers generation of super groups 
 * until requested. A part of the super group generation is the super group optimization, which combines 
 * or splits several super groups if needed 
 *   
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 * @see FGridMapper
 * @see FGroupOptimizer
 * @version 1.1
 * Change list (with respect to D3.3)
 * -  v1.1: "onCellDeleted" accepts a flex-offer that causes the deletion of the cell
 */
public class FGroupBuilderIncremental implements FGridMapper.IGridChangeTracker
{
	// Set in the constructor
	private FGridMapper fgMapper;
		
	private static int GROUP_AUTO_COUNTER = 0;
	
	// The list of all groups
	private HashMap<FGridCellId, FSuperGroup> cellGrpMap = new HashMap<FGridCellId, FSuperGroup>();

	private ChangeTracker<FSuperGroup, ChangeRecOfSuperGroup> grpChanges = null; 
 
	public FGroupBuilderIncremental(FGridMapper fgMapper) {
		this.fgMapper = fgMapper;
		this.grpChanges = new ChangeTracker<FSuperGroup, ChangeRecOfSuperGroup>(
			new IChangeRecFactory<FSuperGroup, ChangeRecOfSuperGroup>() {
				@Override
				public ChangeRecOfSuperGroup getNewChRecInstance(FSuperGroup sg) {
					return new ChangeRecOfSuperGroup(sg);
				}			
			
		});
	}

	@Override
	public void onCellInserted(FGridCellId c) {
		FSuperGroup g = this.getGroupByCellId(c);
		if (g == null) {
			g = new FSuperGroup();
			g.cellAssign(c);
			this.grpChanges.incUpdateChange(g, ChangeType.ctAdded);
		} else 
			assert false : "Cell, which already belongs to a group, can't be added."; 
	}

	@Override
	public void onCellDeleted(FGridCellId c, FlexOffer fo) {
		FSuperGroup g = this.getGroupByCellId(c);
		if (g != null){
			g.cellUnassign(c);
			// The v1.1 update 			
			if (g.cellCount() == 0)
				this.grpChanges.incUpdateChange(g, ChangeType.ctDeleted);
			else 
				this.grpChanges.incUpdateChange(g, ChangeType.ctDownsized).foWasRemoved(fo);
		}		
	}

	@Override
	public void onCellUpsized(FGridCellId c, FlexOffer fo) {
		FSuperGroup g = this.getGroupByCellId(c);
		if (g != null)
			this.grpChanges.incUpdateChange(g, ChangeType.ctUpsized).foWasAdded(fo);
	}
	
	@Override
	public void onCellDownsized(FGridCellId c, FlexOffer fo) {
		FSuperGroup g = this.getGroupByCellId(c);
		if (g != null)
			this.grpChanges.incUpdateChange(g, ChangeType.ctDownsized).foWasRemoved(fo);			
	}
	
	@Override
	public void onGridCleared() {
		this.cellGrpMap.clear();
		this.grpChanges.clearChanges();
	}
	
	private FSuperGroup getGroupByCellId(FGridCellId cId)
	{
		return this.cellGrpMap.get(cId);
	}
	
	private Collection<ChangeRecOfSuperGroup> getChangedGroupsLocked()
	{			
		// We don't return grpChanges directly as it's not safe during iterations and modifications  
		return this.grpChanges.getAllChangesLocked();
	}
			
	private Collection<ChangeRecOfSuperGroup> getChangedGroupsUnlocked()
	{			
		// We don't return grpChanges directly as it's not safe during iterations and modifications  
		return this.grpChanges.getAllChangesUnlocked();
	}
	
	public void resetChanges() {
		for(ChangeRecOfSuperGroup cg : this.getChangedGroupsUnlocked())
			if (cg.getChangeType() == ChangeType.ctDeleted)
				cg.getGroup().deleteGroup();

		this.grpChanges.clearChanges();
	}
	
	public void optimizeChangedGroups(FGroupOptimizer optimizer)
	{
		optimizer.optimizeGroups(this.getChangedGroupsLocked());
	}
	
	public Iterator<ChangeRecOfGroup> getIteratorOfChangesUnlocked() {
		return new IteratorUpcast<ChangeRecOfGroup, ChangeRecOfSuperGroup>(this.getChangedGroupsUnlocked().iterator());		
	}
	
	public void splitGroup(FSuperGroup grp, List<FGroup> cellsForNewGroup) {
		if (cellsForNewGroup.size() > 0) {
			FSuperGroup newGrp = new FSuperGroup();
// v1.1. update. ChangeRecOfSuperGroup sgcNewGrp = 
			this.grpChanges.incUpdateChange(newGrp, ChangeType.ctAdded);
			ChangeRecOfSuperGroup sgcGrpGrp = this.grpChanges.incUpdateChange(grp, ChangeType.ctDownsized);

			for (int i = 0; i < cellsForNewGroup.size(); i++) {
				int ind = grp.cellIndexOf(cellsForNewGroup.get(i));
				assert (ind != -1) : "Super group does not have a cell to be removed!";
				FGridCellId cId = grp.cellGet(ind);

				grp.cellDelete(ind);
				newGrp.cellAssign(cId);
				
				// Add new flex-offers
				for(FlexOffer f : cellsForNewGroup.get(i))
				{
// v1.1. update. Optimization tweak. No need to add, as a new group is being created: sgcNewGrp.foWasAdded(f);
					sgcGrpGrp.foWasRemoved(f);
				}
			}
		}
	}
	
	private void mergeGroup(FSuperGroup grp, FSuperGroup grpToMerge)
	{
		assert(grp!=grpToMerge) :"Can not merge two groups which are same!";
		if (grpToMerge.cellCount() > 0) {			
			this.grpChanges.incUpdateChange(grpToMerge, ChangeType.ctDeleted);
			ChangeRecOfSuperGroup sgc = this.grpChanges.incUpdateChange(grp, ChangeType.ctUpsized);
			
			while (grpToMerge.cellCount() > 0) {
				FGridCellId cId = grpToMerge.cellGet(0);
				grpToMerge.cellDelete(0);
				grp.cellAssign(cId);
				// Add new flex-offers
				for(FlexOffer f : this.fgMapper.getCellPopulated(cId))
					sgc.foWasAdded(f);
			}
		}	
	}


	class FSuperGroup extends FGroup {
		public int gID;
		public List<FGridCellPopulated> cellsUsed = new ArrayList<FGridCellPopulated>();
		
		public FSuperGroup()
		{
			this.gID = FGroupBuilderIncremental.GROUP_AUTO_COUNTER++;
		}

		void deleteGroup() {
			while (this.cellsUsed.size() > 0)
				   this.cellUnassign(this.cellsUsed.get(this.cellsUsed.size() - 1).getCellId());
		}

		void cellAssign(FGridCellId cid) {			
			assert (!FGroupBuilderIncremental.this.cellGrpMap.containsKey(cid)) : "Singe cell is assigned to more than 1 group";			
			this.cellsUsed.add(FGroupBuilderIncremental.this.fgMapper.getCellPopulated(cid));			
			FGroupBuilderIncremental.this.cellGrpMap.put(cid, this);
		}

		void cellUnassign(FGridCellId cid) {
			// Do linear scan and delete
			for(int i=this.cellsUsed.size()-1; i>=0; i--)
				if (this.cellsUsed.get(i).getCellId().equals(cid))
				{
					this.cellDelete(i);
					break;
				}			
		}
		
		public int cellCount() {
			return this.cellsUsed.size();
		}
		
		public void cellDelete(int index)
		{
			FGroupBuilderIncremental.this.cellGrpMap.remove(this.cellsUsed.get(index).getCellId());
			this.cellsUsed.remove(index);			
		}
		
		public int cellIndexOf(Object fGridCellPopulated) {
			return this.cellsUsed.indexOf(fGridCellPopulated);
		}
		
		public FGridCellId cellGet(int index)
		{
			return this.cellsUsed.get(index).getCellId();
		}

		@Override
		public Iterator<FlexOffer> iterator() {
			return new IteratorNested<FGridCellPopulated, FlexOffer>(this.cellsUsed.iterator(),  
					new InnerGenerator<FGridCellPopulated, FlexOffer>() {
						@Override
						public Iterator<FlexOffer> generateItems(FGridCellPopulated cell) {
							return cell.iterator();
						}
			});			
		}

		@Override
		public int getGId() {
			return this.gID;
		}
		
		@Override
		public List<FGroup> getNeighbours()
		{
			LinkedHashSet<FGroup> nbrGrps = new LinkedHashSet<FGroup>();
			for(FGridCellPopulated c : this.cellsUsed)
			{
				List<FGridCellPopulated> nbrs = c.getPopulatedNeighbours();
				if (nbrs!=null)
					for(FGridCellPopulated nbr : nbrs)
					{
						FGroup nGrp = FGroupBuilderIncremental.this.getGroupByCellId(nbr.getCellId());
						if (nGrp != this && 
							FGroupBuilderIncremental.this.grpChanges.getChange(nGrp)!= ChangeType.ctDeleted)
								nbrGrps.add(nGrp);
					}
			}
			
			return new ArrayList<FGroup>(nbrGrps);
		}
		
		@Override
		public List<FGroup> getPartitions()
		{
			return new ArrayList<FGroup>(cellsUsed);		
		}
		
		@Override
		public double getGroupWeight(AggParamAbstracter fa) 
		{
			double w = 0;
			for(FGridCellPopulated c : this.cellsUsed)
				w += c.getGroupWeight(fa);
			return w;
		}
		
		@Override
		public FGridRectangle getMBR(AggParamAbstracter fa)
		{
			FGridRectangle mbr = new FGridRectangle(fa.getDimCount());
			mbr.clearRectangle();
			for (FGridCellPopulated c : this.cellsUsed)
				 mbr.mergeRectangle(c.getMBR(fa));
			return mbr;
		}
		
		@Override
		public void splitGroup(List<FGroup> partitionsForNewGroup) {
			FGroupBuilderIncremental.this.splitGroup(this, partitionsForNewGroup);
		}		
		@Override
		public void mergeWithGroup(FGroup otherGrp){	
			FGroupBuilderIncremental.this.mergeGroup(this, (FSuperGroup) otherGrp);
		}
		
		@Override
		public void mergeWithGroupPart(FGroup otherGrp, FGroup otherGrpPart) { 
			throw new UnsupportedOperationException();
		}
	}
}

