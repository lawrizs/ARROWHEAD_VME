package org.arrowhead.wp5.agg.optim;

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


import java.util.Date;

import org.arrowhead.wp5.agg.impl.billing.MarketCommitment;
import org.arrowhead.wp5.agg.impl.billing.MarketContract;
import org.arrowhead.wp5.core.entities.BidV2;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferConstraint;
import org.arrowhead.wp5.core.entities.FlexOfferSchedule;
import org.arrowhead.wp5.core.entities.FlexOfferSlice;
import org.arrowhead.wp5.core.entities.TimeSeries;


public class AggregatorOptimization {
	private FlexOfferPortfolio fp;
	
	public AggregatorOptimization(FlexOfferPortfolio fp) {
		this.fp = fp;
	}
	
	
	public void optimizePortfolio() throws Exception {		
		this.fp.setSchedulesToBaseline();
		
		/* No market commitments, no optimization is needed*/
		if (this.fp.getMarketCommitments() == null || this.fp.getMarketCommitments().isEmpty()) {
			return;
		}
		
		// STEP 1: Solve time scheduling problem (black-box) first 
		
		// Initialize the time flexibility scheduling
		TimeFlexOptimizer tsOpt = new TimeFlexOptimizer(this.fp);		
		tsOpt.optimizeTimeFlex();
		
		/* Solve the swarm ops problem */
		AmountFlexOptimizer apOpt = new AmountFlexOptimizer(this.fp);
		apOpt.optimizeAmounts();
			
	}
	
	public BidV2 generate_maketV2_bid(long timeFrom, long timeTo) throws Exception {
		// BidV2 bid = new BidV2();
		FlexOffer bidFo = new FlexOffer();
		bidFo.setCreationTime(new Date());
		bidFo.setAcceptanceBeforeInterval(timeFrom);
		bidFo.setAssignmentBeforeInterval(timeFrom);
		bidFo.setStartAfterInterval(timeFrom);
		bidFo.setStartBeforeInterval(timeFrom);
		
		FlexOfferSlice [] slices = new FlexOfferSlice[(int) (timeTo - timeFrom + 1)];
		double[] bidQuant = new double[slices.length];
		bidFo.setSlices(slices);
		
		for(int i=0; i < slices.length; i++) {
			FlexOfferSlice s = new FlexOfferSlice();
			s.setDuration(1);
			s.setEnergyConstraint(new FlexOfferConstraint(-1e6, 1e6));
			s.setCostPerEnergyUnitLimit(0);
			slices[i] = s;
			bidQuant[i] = 1e6;
		}
		BidV2 bid = new BidV2();
		bid.setIs_seller_bid(true);
		bid.setBidFlexOffer(bidFo);
		bid.setWinPrices(new double[slices.length]);
		bid.setWinQuantities(bidQuant);
		MarketContract contract = new MarketContract();
		contract.setImbalanceFee(1e6); /* Imbalance fees are significant */
		
		
		MarketCommitment mc = new MarketCommitment();
		mc.setWinning_bid(bid);
		mc.setContract(contract);
		
		/* Find the reference schedule and price */
		this.optimizePortfolio();		
		double baseCost = this.fp.computePortfolioCost();
		TimeSeries ref_schedule = this.fp.getScheduledEnergy();		
			
		/* Find an up-regulation schedule and cost */					
		this.fp.getMarketCommitments().add(mc);
		try {
			this.optimizePortfolio();			
			
		} finally {
			this.fp.getMarketCommitments().remove(mc);
		}		
		
		double upCost = this.fp.computePortfolioCost();
		TimeSeries up_schedule = this.fp.getScheduledEnergy();
		
		/* Find a down-regulation schedule and cost */
		for(int i=0; i < slices.length; i++) {
			bidQuant[i] = -1e6;			
		}
		bid.setWinQuantities(bidQuant);
		
		/* Find an down-regulation schedule and cost */					
		this.fp.getMarketCommitments().add(mc);
		try {
			this.optimizePortfolio();			
			
		} finally {
			this.fp.getMarketCommitments().remove(mc);
		}		
		
		double downCost = this.fp.computePortfolioCost();
		TimeSeries down_schedule = this.fp.getScheduledEnergy();
				
		/* Setup the actual bid */
		FlexOfferSchedule ref_sch = new FlexOfferSchedule(bidFo);
		ref_sch.setStartTime(bidFo.getStartAfterTime());
		
		for(int i=0; i < slices.length; i++) {
			long tid = ref_sch.getStartInterval() + i;
			
			double ref_val = ref_schedule.getValue(tid);
			double min_val = down_schedule.getValue(tid);
			double max_val = up_schedule.getValue(tid);
			
			ref_sch.getEnergyAmounts()[i] = Math.min(Math.max(ref_val, min_val), max_val);						
			bidFo.getSlice(i).getEnergyConstraint().setLower(min_val);
			bidFo.getSlice(i).getEnergyConstraint().setUpper(max_val);
			bidFo.getSlice(i).setCostPerEnergyUnitLimit(Math.max(
					Math.abs(ref_val - min_val)>1e-6 ?  (downCost - baseCost) / (ref_val - min_val) : 0,
					Math.abs(max_val - ref_val)>1e-6 ?  (upCost - baseCost) / (max_val - ref_val) : 0 ) / slices.length);
		}
		bidFo.setDefaultSchedule(ref_sch);
		bidFo.setFlexOfferSchedule(null);
		
		// Re-create the bid
		bid = new BidV2();
		bid.setBidFlexOffer(bidFo);
		bid.setIs_seller_bid(true);
		
		bid.setSchedule_ref(ref_schedule);
		bid.setSchedule_up(up_schedule);
		bid.setSchedule_down(down_schedule);
		
		return bid;
	}
}
