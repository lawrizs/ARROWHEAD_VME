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

/**
 * A generic "FlexOffer service" (e.g., FOA, Aggregator) subscriber/consumer interface 
 * 
 * @author Laurynas
 *
 */
public interface FlexOfferSubscriberIf {
	/**
	 * Insert a flex offer
	 * @param flexOffer
	 * @throws FlexOfferException
	 * @return An ID of an inserted flex-offer 
	 */
	public int createFlexOffer(String ownerId, FlexOffer flexOffer) throws FlexOfferException;
	
	/**
	 * Get a flex offer with specified id
	 * @param flexOfferId
	 * @return A flex-offer
	 */
	public FlexOffer getFlexOffer(String ownerId, int flexOfferId);
	/**
	 * Update a flex-offer
	 * @param oldFOs
	 * @param newFOs
	 * @throws FlexOfferException 
	 */
	public void setFlexOffer(String ownerId, int flexOfferId, FlexOffer flexOffer) throws FlexOfferException;
	/**
	 * Delete flex-offer
	 */
	public void deleteFlexOffer(String ownerId, int flexOfferId) throws FlexOfferException;

	/**
	 * Get a collection of flex offers
	 * @params flexOffers
	 */
	
	public FlexOffer[] getFlexOffers();

	// Laurynas: I found these redundand, so just to minimize the implementation efforts, I comment it out	
//	/**
//	 * Inserts a collection of flex offers
//	 * @params flexOffers
//	 * @return IDs of flex-offers
//	 */
//	public int[] createFlexOffers(ArrayList<FlexOffer> flexOffers) throws FlexOfferException;
//	/**
//	 * Replace a collection of flex offers
//	 * @params flexOffersId
//	 * @param flexOffers
//	 */
//	public void setFlexOffers(FlexOffer[] flexOffers) throws FlexOfferException;
//	/**
//	 * Deletes a collection of flex offers
//	 * @params flexOffers id
//	 */
//	public void deleteFlexOffers() throws FlexOfferException;
}
