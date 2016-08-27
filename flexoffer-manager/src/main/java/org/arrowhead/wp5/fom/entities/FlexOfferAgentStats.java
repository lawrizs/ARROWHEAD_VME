package org.arrowhead.wp5.fom.entities;

/*-
 * #%L
 * ARROWHEAD::WP5::FlexOffer Manager
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
 * This class specifies flexoffer agent basic statistics 
 * 
 * @author Laurynas
 *
 */
@XmlRootElement
public class FlexOfferAgentStats implements Serializable {
	private static final long serialVersionUID = 6866750839232069565L;
	/* A number of generated flex-offers */
	int numGeneratedFOs = 0;	
	/* A number of flex-offers send to a FlexOfferAgent consumer (aggregator) */
	int numSendFlexOffers = 0;
	/* A number of flex-offers in different states */
	int numInitial = 0;
	int numOffered = 0;
	int numAccepted = 0;
	int numAssigned = 0;
	int numExecuted = 0;
	int	numRejected = 0;

	public int getNumInitial() {
		return numInitial;
	}

	public void setNumInitial(int numInitial) {
		this.numInitial = numInitial;
	}

	public void incNumInitial(int numInitial) {
		this.numInitial += numInitial;
	}

	public int getNumOffered() {
		return numOffered;
	}

	public void setNumOffered(int numOffered) {
		this.numOffered = numOffered;
	}
	
	public void incNumOffered(int numOffered) {
		this.numOffered += numOffered;
	}

	public int getNumAccepted() {
		return numAccepted;
	}

	public void setNumAccepted(int numAccepted) {
		this.numAccepted = numAccepted;
	}
	
	public void incNumAccepted(int numAccepted) {
		this.numAccepted += numAccepted;
	}

	public int getNumAssigned() {
		return numAssigned;
	}

	public void setNumAssigned(int numAssigned) {
		this.numAssigned = numAssigned;
	}
	
	public void incNumAssigned(int numAssigned) {
		this.numAssigned += numAssigned;
	}

	public int getNumExecuted() {
		return numExecuted;
	}

	public void setNumExecuted(int nunExecuted) {
		this.numExecuted = nunExecuted;
	}
	
	public void incNumExecuted(int nunExecuted) {
		this.numExecuted += nunExecuted;
	}

	public int getNumRejected() {
		return numRejected;
	}

	public void setNumRejected(int numRejected) {
		this.numRejected = numRejected;
	}
	
	public void incNumRejected(int numRejected) {
		this.numRejected += numRejected;
	}

	public int getNumGeneratedFOs() {
		return numGeneratedFOs;
	}

	public void setNumGeneratedFOs(int numGeneratedFOs) {
		this.numGeneratedFOs = numGeneratedFOs;
	}
	
	public void incNumGeneratedFOs(int numGeneratedFOs)
	{
		this.numGeneratedFOs += numGeneratedFOs;
	}

	public int getNumSendFlexOffers() {
		return numSendFlexOffers;
	}

	public void setNumSendFlexOffers(int numSendFlexOffers) {
		this.numSendFlexOffers = numSendFlexOffers;
	}
	
	public void incNumSendFlexOffers(int numSendFlexOffers) {
		this.numSendFlexOffers += numSendFlexOffers;
	}

}
