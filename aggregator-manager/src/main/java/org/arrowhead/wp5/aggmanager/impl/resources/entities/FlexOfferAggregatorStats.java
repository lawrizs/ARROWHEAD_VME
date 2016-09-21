package org.arrowhead.wp5.aggmanager.impl.resources.entities;

/*-
 * #%L
 * ARROWHEAD::WP5::Aggregator Manager
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
public class FlexOfferAggregatorStats implements Serializable {
	private static final long serialVersionUID = 6866750839232069565L;
	/* A number of generated flex-offers */
	int numSimpleFOs = 0;	
	/* A number of flex-offers send to a FlexOfferAgent consumer (aggregator) */
	int numAggFOs = 0;
	/* A number of flex-offers in different states */
	int numInitial = 0;
	int numOffered = 0;
	int numAccepted = 0;
	int numAssigned = 0;
	int numExecuted = 0;
	int	numRejected = 0;
	
	private double flexOfferScheduleExpences;
	private double marketGains;
	private double marketImbalanceCosts;
	private double fixedCosts;
	private double portfolioCost;

	public double getFlexOfferScheduleExpences() {
		return flexOfferScheduleExpences;
	}

	public double getMarketGains() {
		return marketGains;
	}

	public double getMarketImbalanceCosts() {
		return marketImbalanceCosts;
	}

	public int getNumAccepted() {
		return numAccepted;
	}

	public int getNumAggFOs() {
		return numAggFOs;
	}
	
	public int getNumAssigned() {
		return numAssigned;
	}

	public int getNumExecuted() {
		return numExecuted;
	}

	public int getNumInitial() {
		return numInitial;
	}
	
	public int getNumOffered() {
		return numOffered;
	}

	public int getNumRejected() {
		return numRejected;
	}

	public int getNumSimpleFOs() {
		return numSimpleFOs;
	}
	
	public double getPortfolioCost() {
		return portfolioCost;
	}

	public void incNumAccepted(int numAccepted) {
		this.numAccepted += numAccepted;
	}

	public void incNumAggFOs(int numSendFlexOffers) {
		this.numAggFOs += numSendFlexOffers;
	}
	
	public void incNumAssigned(int numAssigned) {
		this.numAssigned += numAssigned;
	}

	public void incNumExecuted(int nunExecuted) {
		this.numExecuted += nunExecuted;
	}

	public void incNumInitial(int numInitial) {
		this.numInitial += numInitial;
	}
	
	public void incNumOffered(int numOffered) {
		this.numOffered += numOffered;
	}

	public void incNumRejected(int numRejected) {
		this.numRejected += numRejected;
	}

	public void incNumSimpleFOs(int numGeneratedFOs)
	{
		this.numSimpleFOs += numGeneratedFOs;
	}
	
	public void setFlexOfferScheduleExpences(double flexOfferExpences) {
		this.flexOfferScheduleExpences = flexOfferExpences;		
	}

	public void setMarketGains(double marketGains) {
		this.marketGains = marketGains;
	}

	public void setMarketImbalanceCosts(double marketImbalanceCosts) {
		this.marketImbalanceCosts = marketImbalanceCosts;		
	}
	
	public void setNumAccepted(int numAccepted) {
		this.numAccepted = numAccepted;
	}

	public void setNumAggFOs(int numSendFlexOffers) {
		this.numAggFOs = numSendFlexOffers;
	}

	public void setNumAssigned(int numAssigned) {
		this.numAssigned = numAssigned;
	}

	public void setNumExecuted(int nunExecuted) {
		this.numExecuted = nunExecuted;
	}

	public void setNumInitial(int numInitial) {
		this.numInitial = numInitial;
	}

	public void setNumOffered(int numOffered) {
		this.numOffered = numOffered;
	}


	public void setNumRejected(int numRejected) {
		this.numRejected = numRejected;
	}

	public void setNumSimpleFOs(int numGeneratedFOs) {
		this.numSimpleFOs = numGeneratedFOs;
	}

	public void setPortfolioCost(double portfolioCost) {
		this.portfolioCost = portfolioCost;
	}

	public void setPortfolioTotalCost(double portfolioCost) {
		this.portfolioCost = portfolioCost;		
	}

	public double getFixedCosts() {
		return fixedCosts;
	}

	public void setFixedCosts(double fixedCosts) {
		this.fixedCosts = fixedCosts;
	}

	
	
}
