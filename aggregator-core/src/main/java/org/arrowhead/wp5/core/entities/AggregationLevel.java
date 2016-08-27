package org.arrowhead.wp5.core.entities;

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
 * This class represents the aggregation level
 * 
 * @author Laurynas
 * @author cv
 */
public enum AggregationLevel {
	NotAggregated (0),
	AggAggregated (1),
	BRPAggregated (2),
	MarketAggregated (3);
	
	public static AggregationLevel getAggregationLevel(int id) {
		for(AggregationLevel ag: AggregationLevel.values())
			if(ag.getAggregationLevelId() == id)
				return ag;
		return NotAggregated;
	}
	
	 int aggregationLevelId;
	 
	AggregationLevel(int id) {
		this.aggregationLevelId = id;
	}	
	
	public int getAggregationLevelId() {
		return this.aggregationLevelId;
	}
}
