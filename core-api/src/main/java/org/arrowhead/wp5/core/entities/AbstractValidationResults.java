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


import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * This represents results of the flexoffer schedule validation  
 * 
 * @author Laurynas
 *
 */
@XmlRootElement
public abstract class AbstractValidationResults implements Serializable {
	private static final long serialVersionUID = -3691524990310140435L;

	public AbstractValidationResults() {		
	}
	
	/***
	 * Get ratio between factual energy and energy in flexoffer schedules 
	 * @return 0..1
	 */
	public abstract double getRelativeValidity();
	
	/**
	 * Get energy delta total 
	 * @return
	 */
	public abstract double getEnergyDeltaTotal();
	
	/**
	 * Get energy deltas at each time interval
	 * @return
	 */
	public abstract TimeSeries getEnergyDeltaDetailed();

}
