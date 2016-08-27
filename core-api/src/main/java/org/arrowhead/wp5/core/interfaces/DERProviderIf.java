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

/**
 * This is a DER service provider interface
 * 
 * @author Laurynas
 *
 */
public interface DERProviderIf {
	/**
	 * Get a textual description of a DER type
	 * 
	 * @return
	 */
	public String getType();
	
	/**
	 * Get a name of a DER instance. 
	 * 
	 * @return
	 */
	public String getName();	
	
	/**
	 * Get a DER flexoffer 
	 * 
	 * @return
	 */	
	public void generateFlexOffer();
	/**
	 * Update the schedule of a DER. The schedule is used to update the main schedule of the DER
	 * @param fo - a flex-offer which shedule is to be updated
	 */
	public void updateSchedule(FlexOffer fo);	
}
