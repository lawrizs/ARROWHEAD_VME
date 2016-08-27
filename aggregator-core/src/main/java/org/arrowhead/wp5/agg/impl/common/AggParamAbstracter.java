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


import org.arrowhead.wp5.agg.impl.grouper.FSubgrpBuilderWgtBounded.BinPackingParams;
import org.arrowhead.wp5.core.entities.FlexOffer;


/**
 * A class that abstracts flex-offer aggregation parameters.
 * Similarity tolerances are represented as a vector. Delegates are used to retrieve  
 * flex-offer attributes values that are used for similarity and weight bounding calculations.
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 *
 */
public class AggParamAbstracter {
	// Delegates, which return a value of a specific flex-offer dimension
	private FlexOfferDimValueDelegate [] dimValDlg = null;
	private FGridVector qVector;
	
	private FlexOfferWeightDelegate weightDlg = null;
	private double weightMinValue = 0;
	private double weightMaxValue = -1;
	private BinPackingParams binPackerParams = null;
	

	// The adder which adds many similar flex-offers into 1 big 
	private IFlexOfferNto1Adder foAdder = null; 
		
	public AggParamAbstracter(FGridVector qVector, FlexOfferDimValueDelegate [] dimValDlg, 
							  FlexOfferWeightDelegate weightDlg, IFlexOfferNto1Adder foAdder)							   
	{
		assert (dimValDlg!=null) : "Dimension value delegates are not specified!";
		assert (foAdder!=null)  : "Flex-offer adder is not specified!";
		
		this.dimValDlg = dimValDlg;
		this.qVector = qVector;
		if (weightDlg == null)
			this.weightDlg = new DefaultWeightDelegate(); 
		else 
			this.weightDlg = weightDlg;
		this.foAdder = foAdder;
	}
	
	public void setWeightMinMaxValues(double weightMinValue, double weightMaxValue)
	{
		//assert(weightDlg != null) : "Cannot set weight min and max values if no weight delegate is set!";
		assert(weightMinValue <= weightMaxValue) : "Weight min value can not be higher than weight max value!";
		this.weightMinValue = weightMinValue;
		this.weightMaxValue = weightMaxValue;
	}
	
	public void weightBoundDisable()
	{
		this.weightMinValue = 0;
		this.weightMaxValue = -1;
	}
	
	public boolean isWeightBoundEnabled()
	{
		return (this.weightMaxValue>=this.weightMinValue);
	}
	
	public BinPackingParams getBinPackerParams() {
		return binPackerParams;
	}

	public void setBinPackerParams(BinPackingParams binPackerParams) {
		this.binPackerParams = binPackerParams;
	}

	
	public FlexOfferWeightDelegate getWeightDlg() {
		return weightDlg;
	}
	
	public FlexOfferDimValueDelegate [] getDimValDlg() {
		return this.dimValDlg;
	}

	public double getWeightMinValue() {
		return weightMinValue;
	}

	public double getWeightMaxValue() {
		return weightMaxValue;
	}

	public FGridVector getQueryVector() {
		return qVector;
	}
	
	public IFlexOfferNto1Adder getFoNto1adder()
	{
		return this.foAdder;
	}
	
	public int getDimCount()
	{
		return dimValDlg.length;
	}
	
	public FGridVector getFlexOfferVector(FlexOffer f) {
		FGridVector v =  new FGridVector(this.getDimCount());
		for(int i=0; i< v.getDimCount(); i++)
			v.getValues()[i] = this.dimValDlg[i].getValue(f);
		return v;
	}	
	
	// When the custom weight delegate is not specified, we use the default one.
	private static class DefaultWeightDelegate implements FlexOfferWeightDelegate
	{
		@Override
		public double getWeight(FlexOffer f) {
			return 1;
		}

		@Override
		public double addTwoWeights(double weight1, double weight2) {
			return weight1 + weight2;
		}

		@Override
		public double getZeroWeight() {
			return 0;
		}		
	}
}
