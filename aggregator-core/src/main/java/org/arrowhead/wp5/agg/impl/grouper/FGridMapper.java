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


import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.arrowhead.wp5.agg.impl.common.AggParamAbstracter;
import org.arrowhead.wp5.agg.impl.common.FGridVector;
import org.arrowhead.wp5.agg.impl.common.FlexOfferWeightDelegate;
import org.arrowhead.wp5.core.entities.FlexOffer;


/**
 * An implementation of a very coarse N-dimensional grid, where flex-offers are mapped.
 * Only flex-offer populated cells are stored in memory
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 * @version 1.1 
 * Change list (with respect to D3.3): 
 *  -  v1.1: "onCellDeleted" notifies about a flex-offer that caused the deletion of a cell  
 *
 */
public class FGridMapper implements Iterable<FGridCellPopulated>
{	
	private FGridVector queryVector;
	private AggParamAbstracter aggParAbstracter;
	
	private FGridDimension[] dims = null;
	private boolean dimsRebuildNeeded;	// When true, the dimensions have to be rebuilt
	protected HashMap<FGridCellId, FGridCellPopulated> popCellsHash = null;
	private IGridChangeTracker changeTracker = null;		
	
	public int getNumOfDimensions() {
		return this.dims.length;
	}
	
	protected FGridVector getQueryVector()
	{		
		return this.queryVector;
	}
	
	//@Todo Check if method needed 	
	protected FlexOfferWeightDelegate getCellWeightDelegate() {
		return this.aggParAbstracter.getWeightDlg();
	}
	
	public IGridChangeTracker getChangeTracker()
	{
		return this.changeTracker;
	}

	public void setChangeTracker(IGridChangeTracker t)
	{
		this.changeTracker = t;
	}
		
	public FGridMapper(AggParamAbstracter aggParAbstracter) {
		this.aggParAbstracter = aggParAbstracter;
		this.queryVector = aggParAbstracter.getQueryVector();
		assert(aggParAbstracter.getDimValDlg().length == queryVector.getDimCount()): "Flex-offer and query vector dimentionalities must match!";
		this.rebuildDimensions(null);
		this.dimsRebuildNeeded = true;
		// Initialize has hash with default capacity and load-factor
		this.popCellsHash = new HashMap<FGridCellId, FGridCellPopulated>();
		this.changeTracker = null;
	}
	
	protected FGridCellPopulated getCellPopulated(FGridCellId cid)
	{
		return this.popCellsHash.get(cid);
	}

	protected FGridCellId getFlexOfferCellId(FlexOffer f) {
		return new FGridCellId(this.dims, f);
	}

	protected FGridVector getFlexOfferVector(FlexOffer f) {
		return new FGridVector(this.dims, f);
	}
	
	public void foClear() {
		popCellsHash.clear();
		if (this.changeTracker!=null)
			this.changeTracker.onGridCleared();
		this.dimsRebuildNeeded = true;
	}

	private void rebuildDimensions(FlexOffer firstFlexOffer)
	{
		// Build dimensions
		double offset;
		this.dims = new FGridDimension [this.queryVector.getDimCount()];
		for(int i = 0; i< this.queryVector.getDimCount(); i++)
		{
			if (firstFlexOffer!=null)
				offset = this.aggParAbstracter.getDimValDlg()[i].getValue(firstFlexOffer);
			else 
				offset = 0;			
			this.dims[i] = new FGridDimension(offset, queryVector.getValues()[i], aggParAbstracter.getDimValDlg()[i]);
		}
		this.dimsRebuildNeeded = false;
	}
	
	public void foAdd(Collection<FlexOffer> fl) {	
		FGridCellId c;
		for (FlexOffer f : fl) {
			// Rebuild dimensions if needed
			if (this.dimsRebuildNeeded && f!=null)
				rebuildDimensions(f);
			
			c = getFlexOfferCellId(f);
			// Get the cell from hash
			FGridCellPopulated cp = this.popCellsHash.get(c);
			// Add new cell
			if (cp == null) {
				cp = new FGridCellPopulated(c);
				cp.findNeighbours(this);
				this.popCellsHash.put(c, cp);
				if (this.changeTracker!=null)
					this.changeTracker.onCellInserted(c);
			} else 
				if (this.changeTracker!=null)
					this.changeTracker.onCellUpsized(c, f);
			cp.foAdd(this.aggParAbstracter, f);
		}
	}

	public void foDel(Collection<FlexOffer> fl) {
		FGridCellId c;
		for (FlexOffer f : fl) {
			c = getFlexOfferCellId(f);
			// Get the cell from hash
			FGridCellPopulated cp = this.popCellsHash.get(c);
			// Add new cell
			if (cp != null) {
				cp.foDel(this.aggParAbstracter, f);
				// Cell became empty - destroying the cell
				if (cp.foCount() == 0)
				{
					cp.clearNeighbours();
					this.popCellsHash.remove(c);
					if (this.changeTracker!=null)
						this.changeTracker.onCellDeleted(c, f);
				} else 
					if (this.changeTracker!=null)
						this.changeTracker.onCellDownsized(c, f);
			}			
		}

	}

	@Override
	public Iterator<FGridCellPopulated> iterator() {
		return this.popCellsHash.values().iterator();	// Might be slow
	}
	
	public interface IGridChangeTracker
	{
		// Cell notifications
		public void onCellInserted(FGridCellId c);
		public void onCellDeleted(FGridCellId c, FlexOffer fo);
		// This will tell by which flex-offer a cell was upsized or downsized
		public void onCellUpsized(FGridCellId c, FlexOffer fo);
		public void onCellDownsized(FGridCellId c,  FlexOffer fo);
		
		// Grid notifications
		public void onGridCleared();
	}
}


