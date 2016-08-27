/**
 * 
 */
package org.arrowhead.wp5.core.entities;

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


import javax.xml.bind.annotation.XmlEnum;

/** 
 * Possible flexoffer states
 * 
 * @author Laurynas
 *
 */
@XmlEnum
public enum FlexOfferState {
	/* The initial state of a FlexOffer object. The object has been constructed but not yet offered.  */
	Initial,
	/* The FlexOffer object is constructed and associated and is communicated. */
	Offered, 
	/* The FlexOffer object is accepted; the flexibilities expressed in FlexOffer object will be used. */
	Accepted, 
	/* The FlexOffer object is rejected; the flexibilities expressed in FlexOffer object will not be used. */
	Rejected, 
	/* An assignment is expressed for the  FlexEnergy object, i. e. 
	 * a ‘choice’ is made for all the flexibilities expressed in the FlexOffer object and 
	 * this ‘choice’ (a FlexOfferSchedule object) is communicated. */
	Assigned,
	/* The FlexOffer object is executed by DER */
	Executed;
}
