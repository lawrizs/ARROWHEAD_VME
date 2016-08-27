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


import org.arrowhead.wp5.agg.impl.grouper.FGridDimension;
import org.arrowhead.wp5.core.entities.FlexOffer;

/**
 * A structure that represents a N-dimensional point
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 *
 */
public class FGridVector {
	private double[] values;

	public FGridVector(int numDim) {
		this.setValues(new double[numDim]);
	}
	
	public void setValues(double[] values) {
		this.values = values;
	}

	public double[] getValues() {
		return values;
	}
	
	public int getDimCount()
	{
		return this.values.length;
	}

	public FGridVector(double... values) {
		this.setValues(values);
	}
	
	public FGridVector(FGridVector v)
	{
		this.setValues(new double[v.getValues().length]);
		for(int i=0; i< this.getValues().length; i++)
			this.getValues()[i] = v.getValues()[i];
	}

	public FGridVector(FGridDimension[] dims, FlexOffer f) {
		this.setValues(new double[dims.length]);
		for (int i = 0; i < dims.length; i++)
			this.getValues()[i] = dims[i].getDimValue(f);
	}
	
	public void substract(FGridVector v2)
	{
		assert (this.getValues().length == v2.getValues().length) : "Vector dimensionalities mismatch";
		for (int i = 0; i < this.getValues().length; i++)
			this.getValues()[i] -= v2.getValues()[i];
	}
	
	public void setToVector(FGridVector v2)
	{
		assert (this.getValues().length == v2.getValues().length) : "Vector dimensionalities mismatch";
		for (int i = 0; i < this.getValues().length; i++)
			this.getValues()[i] = v2.getValues()[i];
	}
	
	// Set current vector's
	public void setMinValue(FGridVector v2) {
		assert (this.getValues().length == v2.getValues().length) : "Vector dimensionalities mismatch";
		for (int i = 0; i < this.getValues().length; i++)
			if (v2.getValues()[i] < this.getValues()[i])
				this.getValues()[i] = v2.getValues()[i];
	}

	public void setMaxValue(FGridVector v2) {
		assert (this.getValues().length == v2.getValues().length) : "Vector dimensionalities mismatch";
		for (int i = 0; i < this.getValues().length; i++)
			if (v2.getValues()[i] > this.getValues()[i])
				this.getValues()[i] = v2.getValues()[i];
	}
	
	public void setToMAX()
	{
		for (int i = 0; i < this.getValues().length; i++)
			this.getValues()[i] = Double.MAX_VALUE;
	}
	
	public void setToMIN()
	{
		for (int i = 0; i < this.getValues().length; i++)
			this.getValues()[i] = -Double.MAX_VALUE;
	}		
	
	public boolean isLowerEqualThan(FGridVector v2)
	{
		assert (this.getValues().length == v2.getValues().length) : "Vector dimensionalities mismatch";
		for (int i = 0; i < this.getValues().length; i++)
			if (this.getValues()[i] > v2.getValues()[i]) return false;
		return true;
	}
}