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


/**
 * A structure that represents a N-dimensional rectangle using two N-dimensional points 
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 *
 */
public class FGridRectangle {
	private FGridVector vecLow = null;
	private FGridVector vecHigh = null;
	

	public void setVecHigh(FGridVector vecHigh) {
		this.vecHigh = vecHigh;
	}

	public FGridVector getVecHigh() {
		return vecHigh;
	}

	public void setVecLow(FGridVector vecLow) {
		this.vecLow = vecLow;
	}

	public FGridVector getVecLow() {
		return vecLow;
	}

	public FGridRectangle(int numDim) {
		setVecLow(new FGridVector(numDim));
		setVecHigh(new FGridVector(numDim));
	}
	
	public FGridRectangle(FGridRectangle r) {
		setVecLow(new FGridVector(r.getVecLow()));
		setVecHigh(new FGridVector(r.getVecHigh()));
	}

	public void extendMBR(FGridVector v) {
		getVecLow().setMinValue(v);
		getVecHigh().setMaxValue(v);
	}

	public void clearRectangle() {
		this.getVecLow().setToMAX();
		this.getVecHigh().setToMIN();		
	}
	
	public void mergeRectangle(FGridRectangle r2)
	{
		this.getVecLow().setMinValue(r2.getVecLow());
		this.getVecHigh().setMaxValue(r2.getVecHigh());
	}
	
	public void setRectangle(FGridRectangle r2)
	{
		this.getVecLow().setToVector(r2.getVecLow());
		this.getVecHigh().setToVector(r2.getVecHigh());
	}
	
	public FGridVector getSize()
	{
		FGridVector d = new FGridVector(this.getVecHigh());
		d.substract(this.getVecLow());
		return d;
	}
}
