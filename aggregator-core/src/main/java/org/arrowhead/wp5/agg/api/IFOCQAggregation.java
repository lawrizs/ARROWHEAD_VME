package org.arrowhead.wp5.agg.api;

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
import java.util.List;

import org.arrowhead.wp5.core.entities.FlexOffer;


/**
 * This interface specifies API for the incremental flex-offer aggregation.
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 *  
 */
public interface IFOCQAggregation {
	// Query operations
	/**
	 * Registers a continuous flex-offer aggregation query using specified aggregation parameters.
	 * 
	 * @param aggParam aggregation parameters for a query
	 * @see IFOContinuousQuery 
	 */
	public IFOContinuousQuery queryCreate(FOAggParameters aggParam) throws AggregationException;
	/**
	 * Unregisters a continuous flex-offer aggregation query
	 * @param query an object of the query that needs to be unregistered 
	 * @return true is the query was removed successfully
	 */
	public boolean queryDrop(IFOContinuousQuery query);
	/**
	 * Unregisters all continuous flex-offer aggregation queries 
	 */
	public void queryClear();
	
	// Flex-offer operations
	/**
	 * Clears micro flex-offer pools of all queries
	 */
	public void foClear();
	/** 
	 * Gets all added flex-offers 
	 * */
	public Collection<FlexOffer> foGetAll();	
	/**
	 * Adds a single micro flex-offer to micro flex-offer pools of all queries	
	 * @param fo a flex-offer to be added
	 */
	public void foAdd(FlexOffer fo);
	/**
	 * Adds a set of micro flex-offers to micro flex-offer pools of all queries
	 * @param fl a set of flex-offer to be added
	 */
	public void foAdd(List<FlexOffer> fl);
	/**
	 * Deletes a single flex-offer from micro flex-offer pools of all queries
	 * @param fo a flex-offer to be deleted
	 */
	public void foDel(FlexOffer fo);
	/**
	 * Deletes a set of micro flex-offers from micro flex-offer pools of all queries
	 * @param fl a set of flex-offers to be deleted
	 */	
	public void foDel(List<FlexOffer> fl);
}


