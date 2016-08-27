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

import org.arrowhead.wp5.core.entities.FlexOffer;


/**
 * This interface specifies API of a continuous aggregation query
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 *
 */
public interface IFOContinuousQuery {
	/**
	 * Enables the query. When enabled, flex-offers can be added/deleted to query's micro flex-offer pool  
	 */
	public void aggIncStart();
	/**
	 * Disables the query. When disabled, flex-offers can not be added/deleted to query's micro flex-offer pool
	 */
	public void aggIncStop();
	/**
	 * Reset the tracking of changes in the macro flex-offer pool. After this call, new changes changes will 
	 * be tracked relatively to this state of the macro flex-offer pool.
	 */
	public void aggIncReset();
	
	/** 
	 * Returns flex-offers that has changed since last (aggIncReset) call. 
	 * @return a set of flex-offers and their change descriptors
	 * @see IFOContinuousQuery#aggIncReset
	 */
	public Iterable<ChangeRecOfFlexOffer> aggIncGetChanges();
	/**
	 * Return all flex-offers in the macro flex-offer pool indicating their changes since last
	 * (aggIncReset) call.   
	 * @return a set of flex-offers and their change descriptors
	 * @see IFOContinuousQuery#aggIncReset
	 */
	public Iterable<ChangeRecOfFlexOffer> aggIncGetAll();
	
	/** 
	 * Returns flex-offers that has been added or removed from a non-aggregated flex-offer pool since last (aggIncReset) call.
	 * A flex-offer gets into the non-aggregated flex-offer pool if the "ConstraintAggregate" (FOAggParameters) are too tight
	 * and the flex-offer can not be grouped with other flex-offers. 
	 * @return a set of flex-offers and their change descriptors
	 * @see IFOContinuousQuery#aggIncReset
	 * @see FOAggParameters
	 */
	public Iterable<ChangeRecOfFlexOffer> nonAggFOsGetChanges();
	/** 
	 * Returns all flex-offers in the non-aggregated flex-offer pool. Each flex-offer is tagged with their change information.  
	 * A flex-offer gets into the non-aggregated flex-offer pool if the "ConstraintAggregate" (FOAggParameters) are too tight
	 * and the flex-offer can not be grouped with other flex-offers. 
	 * @return a set of flex-offers and their change descriptors
	 * @see FOAggParameters
	 */	
	public Iterable<ChangeRecOfFlexOffer> nonAggFOsGetAll();
	/**
	 * Returns a collection of all micro flex-offers
	 * 
	 * @return A collection of flex-offers
	 */
	public Collection<FlexOffer> foGetMicroFOs();
	/**
	 * Allows one to be notified about substantial changes in micro and macro flex-offer pools. Note, changes in micro 
	 * flex-offer are practically observed, but changes in macro flex-offer pool are only assumed. 
	 * @param params a notification criteria 
	 * @param callback a notification receiver  
	 */
	public void aggIncSubscribeToChanges(FOCQcallbackParams params, IFOCQcallback callback);
}
