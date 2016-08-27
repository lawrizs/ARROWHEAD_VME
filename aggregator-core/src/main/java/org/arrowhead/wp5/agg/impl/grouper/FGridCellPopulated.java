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
import java.util.Iterator;
import java.util.List;

import org.arrowhead.wp5.agg.impl.common.AggParamAbstracter;
import org.arrowhead.wp5.agg.impl.common.FGridRectangle;
import org.arrowhead.wp5.agg.impl.common.FGridVector;
import org.arrowhead.wp5.agg.impl.common.FGroup;
import org.arrowhead.wp5.core.entities.FlexOffer;


/**
 * The representation of a flex-offer grid cell, which is populated with 1 or more flex-offers
 * It is a subclass of FGroup 
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 * @see FGridMapper
 * @see FGroup
 */
public class FGridCellPopulated extends FGroup {
	private FGridCellId cellId;
	private FGridRectangle cellMBR = null;
	// Max 3^N - 1 neighboring cells. N - number of dimensions
	private List<FGridCellPopulated> populatedNeighbours = null;
	private Double cellWeight = null; // Weight of the cell 
	private List<FlexOffer> flexOfferList;

	protected FGridCellPopulated(FGridCellId cid) {
		this.cellId = cid;
		flexOfferList = new ArrayList<FlexOffer>();
	}
	
	public FGridCellId getCellId()
	{
		return this.cellId;
	}
	
	@Override
	public Iterator<FlexOffer> iterator() {
		return this.flexOfferList.iterator();
	}

	@Override
	public int getGId() {					// ID of the group corresponds to has code of cellId 
		return this.cellId.hashCode();
	}
	
	@Override
	public double getGroupWeight(AggParamAbstracter fa)
	{
		if (this.cellWeight == null)			// No weights has been computed
			this.cellWeight = super.getGroupWeight(fa); 

		return this.cellWeight;		
	}
	
	@Override
	public FGridRectangle getMBR(AggParamAbstracter fa) {
		updateMBRinternally(fa);

		if (this.cellMBR == null) {
			FGridRectangle r = new FGridRectangle(fa.getDimCount());
			r.clearRectangle();
			if (this.flexOfferList.size() == 1) {
				FGridVector v2 = fa.getFlexOfferVector(this.flexOfferList
						.get(0));
				r.getVecHigh().setToVector(v2);
				r.getVecLow().setToVector(v2);
			}
			return r;
		} else
			return new FGridRectangle(this.cellMBR);
	}
	
	//@Override
	@Override
	public List<FGroup> getNeighbours()
	{
		List<FGridCellPopulated> popNeighbours = this.getPopulatedNeighbours(); 
		return popNeighbours == null ? new ArrayList<FGroup>() : new ArrayList<FGroup>(popNeighbours);
	}
	
	private void updateMBRinternally(AggParamAbstracter fa) {
		if (flexOfferList.size() > 1) {
			if (this.cellMBR == null) {
				this.cellMBR = new FGridRectangle(fa.getDimCount());
				this.cellMBR.clearRectangle();

				this.cellMBR.clearRectangle();
				for (FlexOffer f : flexOfferList)
					cellMBR.extendMBR(fa.getFlexOfferVector(f));
			}
		} else
			this.cellMBR = null; // To save memory, we keep this null
	}
	
	public List<FGridCellPopulated> getPopulatedNeighbours()
	{
		return this.populatedNeighbours;
	}

	protected void findNeighbours(FGridMapper fgMapper) {
		
		FGridCellId nbrCellId = new FGridCellId(fgMapper.getNumOfDimensions());
		// Build existing neighbors array
			
		for (int i = 1; i < fgMapper.getNumOfDimensions(); i++)
			nbrCellId.dimId[i] = this.cellId.dimId[i] - 1;

		int j = 0;			
		nbrCellId.dimId[0] = this.cellId.dimId[0] - 2;
		while (j < fgMapper.getNumOfDimensions()) {
			nbrCellId.dimId[j]++;
			if (nbrCellId.dimId[j] > this.cellId.dimId[j] + 1) {
				nbrCellId.dimId[j] = this.cellId.dimId[j] - 1;
				j++;
			} else {
				// Checking if such neighbor exists
				if (!nbrCellId.equals(this.cellId)) {
					j = 0;
					boolean found = false;
					if (this.populatedNeighbours != null) {
						for (FGridCellPopulated pc : this.populatedNeighbours)
							if (pc.cellId.equals(nbrCellId)) {
								found = true;
								break;
							}
					}
					if (!found) {
						FGridCellPopulated nbrCell = fgMapper.popCellsHash.get(nbrCellId);
						if (nbrCell != null) {
							// Add it mutually
							if (this.populatedNeighbours == null)
								this.populatedNeighbours = new ArrayList<FGridCellPopulated>();
							this.populatedNeighbours.add(nbrCell);
							if (nbrCell.populatedNeighbours == null)
								nbrCell.populatedNeighbours = new ArrayList<FGridCellPopulated>();
							nbrCell.populatedNeighbours.add(this);
						}
					}
				}
			}
		}
	}
	
	protected void clearNeighbours() {
		if (this.populatedNeighbours!=null)
		for (FGridCellPopulated pn : this.populatedNeighbours)
		{
			pn.populatedNeighbours.remove(this);
			if (pn.populatedNeighbours.size() == 0)
				pn.populatedNeighbours = null;
		}
		this.populatedNeighbours = null;
	}


	protected void updateMBRonFOadd(AggParamAbstracter fa, FlexOffer newAdded) {
		updateMBRinternally(fa);
		if (this.cellMBR != null)
			this.cellMBR.extendMBR(fa.getFlexOfferVector(newAdded));
	}

	protected void foAdd(AggParamAbstracter fa, FlexOffer f) {
		this.flexOfferList.add(f);
		this.updateMBRonFOadd(fa, f);
		// Update weight
//		if (fa.isWeightBoundEnabled())
		if (this.cellWeight!=null)
			this.cellWeight = fa.getWeightDlg().addTwoWeights(this.cellWeight, fa.getWeightDlg().getWeight(f));
//		this.cellWeight += fa.getWeightDlg().getWeight(f);
	}

	protected void foDel(AggParamAbstracter fa, FlexOffer f) {
		if (this.flexOfferList.remove(f)) {
			this.cellMBR = null;
			// Update weight
//			if (fa.isWeightBoundEnabled())
			this.cellWeight = null;	// When null, it will be recomputed on demand
				//this.cellWeight -= fa.getWeightDlg().getWeight(f);
		} else 
			assert(false) : "Trying to remove unexisting flex-offer";
	}
	
	protected int foCount()
	{
		return this.flexOfferList.size();
	}

}