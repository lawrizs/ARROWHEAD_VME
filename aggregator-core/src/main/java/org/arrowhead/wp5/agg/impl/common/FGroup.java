package org.arrowhead.wp5.agg.impl.common;

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


/**
 * An abstract class that represent a set (group) of flex-offers
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 *
 */
public abstract class FGroup implements Iterable<FlexOffer> {
	
	public abstract int getGId();
	
	@Override
	public int hashCode() {
		return this.getGId();
	}

	// Inefficient way to calculate weight
	public double getGroupWeight(AggParamAbstracter fa) {
		FlexOfferWeightDelegate dlg = fa.getWeightDlg();
		double weight = dlg.getZeroWeight();
		for (FlexOffer f : this)
			weight = dlg.addTwoWeights(weight, dlg.getWeight(f));
		return weight;
	}	

	// Inefficient way to calculate MBR
	public FGridRectangle getMBR(AggParamAbstracter fa)
	{
		FGridRectangle r = new FGridRectangle(fa.getDimValDlg().length);
		r.clearRectangle();
		for (FlexOffer f : this)
			r.extendMBR(fa.getFlexOfferVector(f));
		return r;
	}
	
	public FGridVector distanceBetweenMBRs(AggParamAbstracter fa, FGroup otherGroup)
	{
		FGridRectangle r1= this.getMBR(fa);
		FGridRectangle r2= otherGroup.getMBR(fa);
		// Check for the current cell
		if (r1 == null || r2 == null) {
			r1.getVecHigh().setToMAX();
			return r1.getVecHigh(); // No distance can be computed
		}		
		r1.mergeRectangle(r2);
		
		r1.getVecHigh().substract(r1.getVecLow());
		return r1.getVecHigh();
	}
	
	public List<FGroup> getNeighbours()
	{
		throw new UnsupportedOperationException("This flex-offer group does not support neighbour retrieval!");
	}
	
	public List<FGroup> getPartitions()
	{
		throw new UnsupportedOperationException("This flex-offer group does not support group partitioning!");
	}
	
	// Functionality that enables group optimization
	public void splitGroup(List<FGroup> partitionsForNewGroup) { }
	public void mergeWithGroup(FGroup otherGrp){ }	
	public void mergeWithGroupPart(FGroup otherGrp, FGroup otherGrpPart) { }	
}

