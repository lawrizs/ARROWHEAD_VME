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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import org.arrowhead.wp5.agg.impl.Aggregator;
import org.arrowhead.wp5.agg.impl.billing.AggregatorBillFactory;
import org.arrowhead.wp5.agg.impl.billing.AggregatorContract;
import org.arrowhead.wp5.agg.impl.billing.MarketCommitment;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferSchedule;
import org.arrowhead.wp5.core.entities.FlexOfferSlice;
import org.arrowhead.wp5.core.entities.TimeSeries;
import org.arrowhead.wp5.core.entities.TimeSeriesType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class that represents a portfolio of flex-offers to be optimized
 * 
 * @author Laurynas
 *
 */
public class FlexOfferPortfolio {
	private Aggregator agg;
	final static Logger logger = LoggerFactory.getLogger(FlexOfferPortfolio.class);
	
	/* FlexOffer expenses */	
	private List<FlexOffer> fos;
	private List<AggregatorContract> fo_contracts;
	/* Fixed expences that do not depend on FO schedules */
	private double fixedExpences = 0;
	
	/* FlexOffer gains */
	private List<MarketCommitment> mk_commit;
	
	public FlexOfferPortfolio(Aggregator agg, Collection<? extends FlexOffer> fos, Collection<MarketCommitment> mk_commit, double fixedCosts) {
		this.agg = agg;
		this.fos = new ArrayList<FlexOffer>();
		this.fo_contracts = new ArrayList<AggregatorContract>();
		this.fixedExpences = fixedCosts;
		
		/* Flex-offer expences */
		for(FlexOffer f : fos) {			
			
			// Check the schedules
			if (f.getDefaultSchedule()==null){
				f.setDefaultSchedule(new FlexOfferSchedule(f));
			}
			
			if (f.getFlexOfferSchedule() == null) {
				f.setFlexOfferSchedule(f.getDefaultSchedule().clone());
			}

			// Make sure the schedules are truely initialized
			if (f.getFlexOfferSchedule() == null) {
				f.setFlexOfferSchedule(new FlexOfferSchedule(f));
			}			
			
			// Check the slice durations
			boolean has_incompact_durs = false;
			
			
			for (int j=0; j< f.getSlices().length; j++) {
				if (f.getSlice(j).getDuration() != 1) {
					has_incompact_durs = true;
					break;
				}					
			}
			
			if (has_incompact_durs) {
				// Rework the profile to be compatible
				List<FlexOfferSlice> new_prof = new ArrayList<FlexOfferSlice>();
				double[] new_sch = new double[f.getDurationIntervals()];
				double[] new_defsch = new double[f.getDurationIntervals()];
						
				for (int l=0; l < f.getSlices().length; l++){
					for(int m = 0; m < f.getSlice(l).getDuration(); m++) {
						long dur = f.getSlice(l).getDuration();
						FlexOfferSlice fs = f.getSlices()[l];
						FlexOfferSlice s = new FlexOfferSlice(1, fs.getCostPerEnergyUnitLimit(), fs.getEnergyLower() / dur, fs.getEnergyUpper() / dur);
						
						new_prof.add(s);
						new_defsch[new_prof.size()-1] = f.getDefaultSchedule().getEnergyAmount(l) / dur;
						new_sch[new_prof.size()-1] = f.getFlexOfferSchedule().getEnergyAmount(l) / dur;
					}
				}
				f.setSlices(new_prof.toArray(new FlexOfferSlice [] {}));
				f.getDefaultSchedule().setEnergyAmounts(new_defsch);
				f.getFlexOfferSchedule().setEnergyAmounts(new_sch);
				
				logger.warn("Flex-offer has been resampled to have the slices of the duration of 1");
			}
					
			this.fos.add(f);
			this.fo_contracts.add(this.agg.getFlexOfferContract(f));
		}
		/* Flex-offer gains */
		this.mk_commit = new ArrayList<MarketCommitment>(mk_commit);
	}	
	
	public Aggregator getAggregator() {
		return this.agg;
	}
	
	public List<FlexOffer> getFlexOffers() {
		return this.fos;
	}
	
	
	public List<AggregatorContract> getFlexOfferContracts() {
		return this.fo_contracts;
	}
	
	public List<MarketCommitment> getMarketCommitments() {
		return this.mk_commit;
	}
	

	/* Get a time series of baseline energy */
	public TimeSeries getBaselineEnergy() throws Exception {
		TimeSeries ts = new TimeSeries(new double[] {});
		
		for(FlexOffer f : fos) {
			if (f.getDefaultSchedule() != null) {
				TimeSeries ft = new TimeSeries(f, TimeSeriesType.tstBaselineEnergy);				
				ts._extend(ft)._plus(ft);
			}
		}
		return ts;				
	}
		
	/* Get a time series of scheduled energy */
	public TimeSeries getScheduledEnergy() throws Exception {
		TimeSeries ts = new TimeSeries(new double[] {});
		
		for(FlexOffer f : fos) {
			if (f.getFlexOfferSchedule() != null) {
				TimeSeries ft = new TimeSeries(f, TimeSeriesType.tstScheduledEnergy);				
				ts._extend(ft)._plus(ft);
			}
		}
		return ts;		
	}
	
	/* Set all schedules equal to baseline */
	public void setSchedulesToBaseline() throws Exception {
		for(FlexOffer f : fos) {
			f.setFlexOfferSchedule(f.getDefaultSchedule().clone());
		}
	}
	
	
	public long getMinTid() {
		long value = Integer.MAX_VALUE;
		for(int i=0; i < this.fos.size(); i++) {
			if (this.fos.get(i).getFlexOfferSchedule() == null) continue;
			
			value = Math.min(value, fos.get(i).getFlexOfferSchedule().getStartInterval());
		}
		return value;
	}
	
	public long getMaxTid() {
		long value = -Integer.MAX_VALUE;
		for(int i=0; i < this.fos.size(); i++) {
			if (this.fos.get(i).getFlexOfferSchedule() == null) continue;
			
			value = Math.max(value, fos.get(i).getFlexOfferSchedule().getStartInterval() + fos.get(i).getFlexOfferSchedule().getEnergyAmounts().length - 1);
		}
		return value;
	}
		
	
	/* Compute direct flex-offer schedule expences */
	public double computeFlexOfferScheduleExpences() {
		double value = 0;
		for(int i=0; i < this.fos.size(); i++) {
			value += AggregatorBillFactory.getFlexOfferSchedulingCost(this.fo_contracts.get(i), this.fos.get(i));			
		}
		return value;
	}
	
	/* Compute market gains */
	public double computeMarketGains() {
		double value=0; 
		
		for (MarketCommitment mc : this.mk_commit) {
			for (int k=0; k<mc.getWinning_bid().getNumSlices(); k++) {
			  	value -= mc.getWinning_bid().getWinPrices()[k];
			}
		}
		
		return value; 
	}
	
	/* Compute market imbalance costs */
	public double computeMarketImbalanceCosts() {
		double value = 0;
		
		for (MarketCommitment mc : this.mk_commit) {
			for (int k=0; k<mc.getWinning_bid().getNumSlices(); k++) {
				long tid = mc.getWinning_bid().getBidFlexOffer().getStartAfterInterval() + k;				
				
				double energyAtTid = 0;
				
				for(int i=0; i<this.fos.size(); i++){
					FlexOfferSchedule sch = this.fos.get(i).getFlexOfferSchedule();
					
					if (sch == null) continue;
										
					if (tid < sch.getStartInterval()) { continue; }
					if (tid > sch.getStartInterval() + sch.getEnergyAmounts().length - 1) { continue; }
						
					energyAtTid += sch.getEnergyAmount((int) (tid - sch.getStartInterval()));
				}
				
				value += Math.abs(mc.getWinning_bid().getWinQuantities()[k] - energyAtTid) * mc.getContract().getImbalanceFee();
			}
		}
		return value;		
	}
	
	/* Compute the schedule cost */
	public double computePortfolioCost(){
		double value = this.fixedExpences;
		
		/* Flex-offer expenses */
		value += this.computeFlexOfferScheduleExpences();
		
		/* Market gains */
		for (MarketCommitment mc : this.mk_commit) {
			for (int k=0; k<mc.getWinning_bid().getNumSlices(); k++) {
				long tid = mc.getWinning_bid().getBidFlexOffer().getStartAfterInterval() + k;				
				
			  	value -= mc.getWinning_bid().getWinPrices()[k];
			
				/* Accont for imbalances */
				double energyAtTid = 0;
			
				for(int i=0; i<this.fos.size(); i++){
					FlexOfferSchedule sch = this.fos.get(i).getFlexOfferSchedule();
					
					if (sch == null) continue;
										
					if (tid < sch.getStartInterval()) { continue; }
					if (tid > sch.getStartInterval() + sch.getEnergyAmounts().length - 1) { continue; }
						
					energyAtTid += sch.getEnergyAmount((int) (tid - sch.getStartInterval()));
					
				}
			
				/* Account for imbalances */
				value += Math.abs(mc.getWinning_bid().getWinQuantities()[k] - energyAtTid) * mc.getContract().getImbalanceFee();
			}
		}
		
		return value;
	}

	/* Compute the scheudule imbalances*/
	static Hashtable<Long, Double> tid_tbl = new Hashtable<Long, Double>();
	
	private void fillTidTableWithScheduleValues() {
		tid_tbl.clear();
		for (int i=0; i < this.fos.size(); i++) {
			FlexOffer f = this.fos.get(i);
			FlexOfferSchedule sch = f.getFlexOfferSchedule();
			
			if (sch == null) continue;
			
			for(int j=0; j<sch.getEnergyAmounts().length; j++) {
				long tid = sch.getStartInterval() + j;
								
				if (!tid_tbl.containsKey(tid))  { 
					tid_tbl.put(tid, sch.getEnergyAmount(j)); 
				} else {
					tid_tbl.replace(tid, sch.getEnergyAmount(j) + tid_tbl.get(tid));	
				}				
			}						
		}	
	}
	
	public double computeEnergyImbalance() {
		this.fillTidTableWithScheduleValues();
		
		double imbalance = 0;
		for(Double value : tid_tbl.values()) {
			imbalance += Math.abs(value);
		}
		
		return imbalance; 
	}
	
	public double computeTotalEnergy() {
		double totalEnergy = 0;
		for (int i=0; i < this.fos.size(); i++) {
			FlexOffer f = this.fos.get(i);
			FlexOfferSchedule sch = f.getFlexOfferSchedule();
			
			if (sch == null) continue;
			
			for(int j=0; j<sch.getEnergyAmounts().length; j++) {
				totalEnergy += sch.getEnergyAmount(j);
			}
		}
		
		return totalEnergy;
	}
	
	 /**
	  * Compute the total energy at specific tid 
	  * @param tid
	  * @return
	  */
	public double computeTotalEnergy(long tid) {
		double totalEnergy = 0;
		for (int i=0; i < this.fos.size(); i++) {
			FlexOffer f = this.fos.get(i);
			FlexOfferSchedule sch = f.getFlexOfferSchedule();
			
			if (sch == null) continue;
			
			for(int j=0; j<sch.getEnergyAmounts().length; j++) {
				long t = sch.getStartInterval() + j;
				
				if (t == tid) {
					totalEnergy += sch.getEnergyAmount(j);
				}
			}			
		}
		
		return totalEnergy;
	}

	public double getFixedExpences() {
		return fixedExpences;
	}

	public void setFixedExpences(double fixedExpences) {
		this.fixedExpences = fixedExpences;
	}
	
}
