package org.arrowhead.wp5.agg.impl.billing;

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


import org.arrowhead.wp5.core.entities.BidV2;

/**
 * This represents the commitments which Aggregator enters to, after a bid is won. 
 * 
 * @author Laurynas
 *
 */
public class MarketCommitment {
	private long location=0; /* Location tag */
	private BidV2 winning_bid;
	private MarketContract contract;
	
	public long getLocation() {
		return location;
	}
	public void setLocation(long location) {
		this.location = location;
	}
	
	public BidV2 getWinning_bid() {
		return winning_bid;
	}
	public void setWinning_bid(BidV2 winning_bid) {
		this.winning_bid = winning_bid;
	}
	
	public MarketContract getContract() {
		return contract;
	}
	public void setContract(MarketContract contract) {
		this.contract = contract;
	}
}
