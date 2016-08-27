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


import java.util.Iterator;

import org.arrowhead.wp5.agg.api.ChangeRecOfFlexOffer;
import org.arrowhead.wp5.core.entities.AggregatedFlexOffer;
import org.arrowhead.wp5.core.entities.FlexOffer;

/**
 * An interface that generalizes the N to 1 flex-offer adder.
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 *
 */
public interface IFlexOfferNto1Adder {
	/**
	 * Applies flex-offer group updates to a respective macro flex-offer.  
	 * 
	 * @param grpChange a change record of a flex-offer group 
	 * @return a change record of a (macro) flex-offer
	 */
	public ChangeRecOfFlexOffer incUpdateAggregate(ChangeRecOfGroup grpChange);
	/**
	 * Retrieves a whole macro flex-offer pool. No change records are used. 
	 * @return a list of macro flex-offers
	 */
	public Iterator<AggregatedFlexOffer> getAllAggregates();
	/**
	 * Wraps a single non-aggregated flex-offer into aggregated flex-offer
	 */
	public AggregatedFlexOffer aggregateSingleFo(FlexOffer f);
	/**
	 * Called when all flex-offers are cleared
	 */
	public void foClear();
}
