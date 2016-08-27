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


import org.arrowhead.wp5.core.entities.FlexOffer;


/**
 * A class that represents coordinate of a flex-offer in the flex-offer grid.
 * Its essential functionality is hashing of flex-offer coordinates.   
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 *
 */
public class FGridCellId {
	protected long[] dimId;

	public int getDimCount() {
		return dimId.length;
	}

	public void set(FGridCellId c) {
		assert this.dimId.length == c.dimId.length : "CellId dimensionalities mismatch";
		for (int i = 0; i < this.dimId.length; i++)
			this.dimId[i] = c.dimId[i];
	}

	public FGridCellId(long... cellIds) {
		this.dimId = cellIds;
	}

	public FGridCellId(int numDim) {
		this.dimId = new long[numDim];
	}

	public FGridCellId(FGridDimension[] dims, FlexOffer f) {
		this.dimId = new long[dims.length];
		for (int i = 0; i < dims.length; i++)
			this.dimId[i] = dims[i].getIntervalNr(f);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof FGridCellId))
			return false;
		FGridCellId otherObj = (FGridCellId) obj;
		if (otherObj.dimId.length != this.dimId.length)
			return false;
		for (int i = 0; i < otherObj.dimId.length; i++)
			if (otherObj.dimId[i] != this.dimId[i])
				return false;
		return true;
	}

	@Override
	public String toString() {
		String s = "";
		for (long cId : dimId)
			s += String.valueOf(cId) + ", ";

		if (s.length() > 0)
			return "{" + s.substring(0, s.length() - 2) + "}";
		else
			return "{}";
	}

	@Override
	public int hashCode() {
		// Use least significant bits to generate a hash code
		int leastSignBitCount = 32 / this.dimId.length;
		int bitPattern = (1 << leastSignBitCount) - 1;
		if (dimId.length <= 0)
			return 0;
		int hashCode = (int)dimId[0];
		for (int i = 1; i < dimId.length; i++) {
			hashCode = (hashCode << leastSignBitCount)
					| (bitPattern & (int)dimId[i]);
		}
		return hashCode;
	}
}