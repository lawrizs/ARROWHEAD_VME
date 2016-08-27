package org.arrowhead.wp5.core.interfaces;

/*-
 * #%L
 * ARROWHEAD::WP5::Core Data Structures
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
import org.arrowhead.wp5.core.entities.FlexOfferException;
import org.arrowhead.wp5.core.entities.FlexOfferSchedule;
import org.arrowhead.wp5.core.entities.FlexOfferState;

/**
 * A generic "flexOffer service" (e.g., FOA, Aggregator) provider/producer interface
 * 
 * @author Laurynas
 *
 */
public interface FlexOfferProviderIf {
	/**
	 * Get a producer flex offer with specified id
	 * @param flexOfferId
	 * @return
	 */
	public FlexOffer getFlexOffer(int flexOfferId) throws FlexOfferException;
	/**
	 * Get a collection of all producer flex offers
	 * @params flexOffers
	 */
	public FlexOffer[] getFlexOffers();
	/** 
	 * Get the state of a flex offer
	 * @param flexOfferId
	 */
	public FlexOfferState getFlexOfferState(int flexOfferId);
	/**
	 * Set the state of a flex offer
	 * @param flexOfferId
	 * @throws FlexOfferException 
	 */
	public void setFlexOfferState(int flexOfferId, FlexOfferState flexOfferState, String stateReason) throws FlexOfferException;	
	/**
	 * Assigns a flex offer schedule to a flex offer
	 * @param flexOfferId
	 * @throws FlexOfferException 
	 */
	public void createFlexOfferSchedule(int flexOfferId, FlexOfferSchedule flexOfferSchedule) throws FlexOfferException;
	/**
	 * Get the schedule of a flex offer
	 * @param flexOfferId
	 * @return
	 */
	public FlexOfferSchedule getFlexOfferSchedule(int flexOfferId);
	/**
	 * Set the schedule of a flex offer
	 * @param flexOfferId
	 * @throws FlexOfferException 
	 */
	public void setFlexOfferSchedule(int flexOfferId, FlexOfferSchedule flexOfferSch) throws FlexOfferException;
	/**
	 * Remove the schedule from a flex offer
	 * @param flexOfferId
	 * @throws FlexOfferException 
	 */
	public void deleteFlexOfferSchedule(int flexOfferId) throws FlexOfferException;
}
