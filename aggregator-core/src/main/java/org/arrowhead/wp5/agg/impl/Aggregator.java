/**
 * 
 */
package org.arrowhead.wp5.agg.impl;

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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.annotation.XmlRootElement;

import org.arrowhead.wp5.agg.api.AggregationException;
import org.arrowhead.wp5.agg.api.FOAggParameters;
import org.arrowhead.wp5.agg.api.FOAggParameters.ConstraintType;
import org.arrowhead.wp5.agg.api.FOAggParameters.ProfileShape;
import org.arrowhead.wp5.agg.api.IFOAggregation;
import org.arrowhead.wp5.agg.impl.billing.AggregatorBill;
import org.arrowhead.wp5.agg.impl.billing.AggregatorBillFactory;
import org.arrowhead.wp5.agg.impl.billing.AggregatorContract;
import org.arrowhead.wp5.agg.impl.billing.MarketCommitment;
import org.arrowhead.wp5.agg.impl.billing.MarketContract;
import org.arrowhead.wp5.agg.impl.foaggregation.FOAggregation;
import org.arrowhead.wp5.agg.optim.AggregatorOptimization;
import org.arrowhead.wp5.agg.optim.FlexOfferPortfolio;
import org.arrowhead.wp5.agg.optim.OptimizationObjective;
import org.arrowhead.wp5.core.entities.AggregatedFlexOffer;
import org.arrowhead.wp5.core.entities.BidV2;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferException;
import org.arrowhead.wp5.core.entities.FlexOfferSchedule;
import org.arrowhead.wp5.core.entities.FlexOfferSlice;
import org.arrowhead.wp5.core.entities.FlexOfferState;
import org.arrowhead.wp5.core.entities.TimeSeries;
import org.arrowhead.wp5.core.entities.TimeSeriesType;
import org.arrowhead.wp5.core.interfaces.FlexOfferAggregatorProviderIf;
import org.arrowhead.wp5.core.interfaces.FlexOfferUpdateListener;
import org.arrowhead.wp5.core.wrappers.FlexOfferKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Laurynas
 * @TODO Utilize the incremental flex-offer aggregation, when market
 *       implementation is in place!
 */
@XmlRootElement
public class Aggregator implements FlexOfferAggregatorProviderIf {
	private final static AtomicInteger afoCounter = new AtomicInteger(0);

	private String id = "";

	final Logger logger = LoggerFactory.getLogger(Aggregator.class);

	private final IFOAggregation agg = new FOAggregation();

	/* Aggregated flex-offers and aggregation parameters */
	private Map<Integer, AggregatedFlexOffer> aggFlexOffers = new HashMap<Integer, AggregatedFlexOffer>();
	private Map<FlexOfferKey, FlexOffer> foaFlexOffers = new HashMap<FlexOfferKey, FlexOffer>();
	
	/* Market commitments*/
	private Collection<MarketCommitment> market_commitments = new ArrayList<MarketCommitment>();
	
	/* The default contract applicable to all DERs */
	private AggregatorContract defaultContract;
	private MarketContract defaultMarketContract;

	/* Aggregator's optimization target and parameters */	
	private OptimizationObjective objective = OptimizationObjective.objLowestCost;
	private FOAggParameters aggParameters = new FOAggParameters();	
	private FlexOfferUpdateListener foLst;

	/* All market bids */
	

	public Aggregator(String id, FlexOfferUpdateListener foLst) {
		this.id = id;
		
		/* Initialize the aggregator's objective */
		this.objective = OptimizationObjective.objLowestCost;
		
		/* Initialize the dafault contract */
		this.defaultContract = new AggregatorContract();
		this.defaultContract.setFixedReward(10.00 /* DKK */); /* 10DKK if the DER owner is flexible */
		this.defaultContract.setTimeFlexReward(0.1 /* DKK*/); /* 0.1DKK are payed per time flexibility */
		this.defaultContract.setEnergyFlexReward(0.1 /* DKK*/); /* 0.1DKK are payed per each kWh of energy flexibility */
		this.defaultContract.setSchedulingFixedReward(0.1 /* DKK */); /* 0.1DKK reward for scheduling differently than the default schedule */
		this.defaultContract.setSchedulingEnergyReward(0.2 /* DKK */); /* 0.2 DKK/1kWh reward for scheduling energy differently from the default schedule */
		this.defaultContract.setSchedulingStartTimeReward(0.2 /* DKK */); /* 0.2 DKK reward is paid for scheduling start-time differently from the default schedule */
		
		
		/* Intializess the market contract*/
		this.defaultMarketContract = new MarketContract();
		this.defaultMarketContract.setImbalanceFee(10 /* DKK */); /* 10 DKK is payed for each 1kWh deviation */ 
		
		
		this.aggParameters.setPreferredProfileShape(ProfileShape.psAlignBaseline);
		/* Set time flexibility tolerance */
		// this.aggParameters.getConstraintPair().timeFlexibilityTolerance = 0;
		// this.aggParameters.getConstraintPair().timeFlexibilityToleranceType = ConstraintType.acSet;
		this.aggParameters.getConstraintPair().baselineAdvancingTolerance = 4;
		this.aggParameters.getConstraintPair().baselineAdvancingType = ConstraintType.acSet;
		this.aggParameters.getConstraintPair().baselineRetardingTolerance = 4;
		this.aggParameters.getConstraintPair().baselineRetardingType = ConstraintType.acSet;
		this.aggParameters.getConstraintPair().startAfterTolerance = 4*12;
		this.aggParameters.getConstraintPair().startAfterToleranceType = ConstraintType.acSet;
		this.foLst = foLst;
		/* Set earliest start-time - these are not used now */
		// agg_params.getConstraintPair().startAfterTolerance = 1000;
		// agg_params.getConstraintPair().startAfterToleranceType =
		// ConstraintType.acSet;
		/**
		 * TODO this parameter was set randomly to fix exception, please check
		 * that it has correct value
		 */
		// this.aggParameters.getConstraintAggregate().aggConstraint =
		// ConstraintAggregateType.acTimeFlexibility;
		this.delayedPlanning();
	}
	

	/*
	 * ********************* Getters/ setters
	 * ***************************************
	 */
	
	public IFOAggregation getAggregator() {
		return this.agg;
	}

	public String getId() {
		return this.id;
	}
	
	

	public OptimizationObjective getObjective() {
		return objective;
	}


	public void setObjective(OptimizationObjective obj) {
		this.objective = obj;
		this.delayedPlanning();
	}
	

	public Collection<MarketCommitment> getMarket_commitments() {
		return market_commitments;
	}
	

	// public Map<Integer, AggregatorFOA> getFOAs() {
	// return this.aggFOAs;
	// }

	public FOAggParameters getAggParameters() {
		return aggParameters;
	}

	public void setAggParameters(FOAggParameters agg_params) {
		this.aggParameters = agg_params;
	}

	public void addSimpleFlexOffer(FlexOffer flexOffer) {
		
		/* Assign a default schedule (baseline) if not set */
		// TODO: remove "|| flexOffer.getDefaultSchedule().getEnergyAmounts() == null" when the XML bind error has been fixed.
		if (flexOffer.getDefaultSchedule() == null || flexOffer.getDefaultSchedule().getEnergyAmounts() == null) {
			FlexOfferSchedule ds = new FlexOfferSchedule();
			// By default, a user aims starting ASAP
			ds.setStartInterval((long)(flexOffer.getStartAfterInterval() + 0.0 * (flexOffer.getStartBeforeInterval() - flexOffer.getStartAfterInterval())));
			// By default, a user consumes average
			double [] amounts = new double[flexOffer.getSlices().length];			
			for(int i=0; i< flexOffer.getSlices().length; i++) {
				amounts[i] = flexOffer.getSlice(i).getEnergyLower() + 0.5 * (flexOffer.getSlice(i).getEnergyUpper() - 
																			 flexOffer.getSlice(i).getEnergyLower());
			}
			ds.setEnergyAmounts(amounts);
			flexOffer.setDefaultSchedule(ds);
		}

		// TODO: remove this when the XML bind error has been fixed.
		if (flexOffer.getFlexOfferSchedule() != null && flexOffer.getFlexOfferSchedule().getEnergyAmounts() == null) {
			flexOffer.setFlexOfferSchedule(null);
			flexOffer.setState(FlexOfferState.Initial);
		}
		
		/* By default, schedule flexoffers to follow default schedule */
			/*		if (flexOffer.getFlexOfferSchedule() == null) {
			flexOffer.setFlexOfferSchedule(new FlexOfferSchedule(flexOffer.getDefaultSchedule().getStartInterval(), 
															     flexOffer.getDefaultSchedule().getEnergyAmounts().clone()));		
		} */
		
		this.foaFlexOffers.put(new FlexOfferKey(flexOffer.getOfferedById(),
				Integer.toString(flexOffer.getId())), flexOffer);
		// this.aggregateFlexOffers();
		
		this.delayedPlanning();
	}

	public void deleteSimpleFlexOffer(FlexOffer flexOffer) {
		this.foaFlexOffers.remove(new FlexOfferKey(flexOffer.getOfferedById(),
				Integer.toString(flexOffer.getId())));
		this.delayedPlanning();
	}
	
	public FlexOffer getSimpleFlexOffer(FlexOfferKey key){
		return this.foaFlexOffers.get(key);
	}
	
	public Collection<FlexOffer> getSimpleFlexOffers() {
		return this.foaFlexOffers.values();
	}
	
	public void deleteSimpleFlexOffers() {
		this.foaFlexOffers.clear();
		this.delayedPlanning();
	}
	
	public void addMarketCommitment(MarketCommitment mc) {
		this.market_commitments.add(mc);
		this.delayedPlanning();
	}

	/**
	 * This method allows VM requesting a specific aggregated flexoffer
	 */
	@Override
	public FlexOffer getFlexOffer(int flexOfferId) throws FlexOfferException {
		return this.aggFlexOffers.get(flexOfferId);
	}

	/**
	 * This method allows VM requesting add aggregated flexoffers
	 */
	@Override
	public FlexOffer[] getFlexOffers() {
		return this.aggFlexOffers.values().toArray(new FlexOffer[] {});
	}

	/* Deleted flex-offers. For internal use only. */
	public void deleteFlexOffers() {
		this.aggFlexOffers.clear();
	}

	/**
	 * This method allows VM requesting aggregated flexoffer state
	 */
	@Override
	public FlexOfferState getFlexOfferState(int flexOfferId) {
		AggregatedFlexOffer fo = this.aggFlexOffers.get(flexOfferId);
		if (fo != null) {
			return fo.getState();
		}
		return FlexOfferState.Initial;
	}

	/**
	 * This method allows VM setting aggregated flexoffer state
	 */
	@Override
	public void setFlexOfferState(int flexOfferId,
			FlexOfferState flexOfferState, String stateReason)
			throws FlexOfferException {
		AggregatedFlexOffer fo = this.aggFlexOffers.get(flexOfferId);
		if (fo != null) {
			// TODO: add state checking logics
			fo.setState(flexOfferState);
			fo.setStateReason(stateReason);
		}
	}

	/**
	 * This method allows VM setting aggregated flexoffer schedule
	 */
	@Override
	public void createFlexOfferSchedule(int flexOfferId,
			FlexOfferSchedule flexOfferSchedule) throws FlexOfferException {
		AggregatedFlexOffer fo = this.aggFlexOffers.get(flexOfferId);
		if (fo != null && flexOfferSchedule != null) {
			if (flexOfferSchedule.isCorrect(fo)) {
				fo.setFlexOfferSchedule(flexOfferSchedule);

				/* Perform the disaggregation of the schedule, followed by execution */
				this.executeFlexOffers(this.disaggregateFlexOffer(fo));				
			} else
				throw new FlexOfferException(
						"Incorrect aggregated flex-offer schedule!");
		}
	}

	@Override
	public FlexOfferSchedule getFlexOfferSchedule(int flexOfferId) {
		AggregatedFlexOffer fo = this.aggFlexOffers.get(flexOfferId);
		if (fo != null)
			return fo.getFlexOfferSchedule();
		else
			return null;
	}

	@Override
	public void setFlexOfferSchedule(int flexOfferId,
			FlexOfferSchedule flexOfferSch) throws FlexOfferException {
		this.createFlexOfferSchedule(flexOfferId, flexOfferSch);
	}

	@Override
	public void deleteFlexOfferSchedule(int flexOfferId) throws FlexOfferException {
		AggregatedFlexOffer fo = this.aggFlexOffers.get(flexOfferId);
		if (fo != null) {
			fo.setFlexOfferSchedule(null);

			/* Perform the disaggregation of "no-schedule", followed by the execution */
			this.executeFlexOffers(this.disaggregateFlexOffer(fo));
		}
	}

	/*
	 * Get all simple flex-offers, submitted each FOA
	 */
	public Collection<FlexOffer> getAllSimpleFlexOffers() {
		return new ArrayList<FlexOffer>(foaFlexOffers.values());
	}

	/* An internal method for triggering flex-offer aggregation */
	public void aggregateFlexOffers() {
		this.aggFlexOffers.clear();
		try {
			Collection<AggregatedFlexOffer> aggList = agg.Aggregate(
					this.aggParameters, getAllSimpleFlexOffers());
			/* Let's give local IDs to new aggregated flex-offers */
			if (aggList != null) {
				for (AggregatedFlexOffer af : aggList) {
					af.setId(Aggregator.afoCounter.incrementAndGet());
					this.aggFlexOffers.put(af.getId(), af);
				}
			}
		} catch (AggregationException e) {
			logger.error("Error aggregating flex-offers. Error: ", e);
		}
	}

    ScheduledExecutorService execService =  Executors.newScheduledThreadPool(1);
    ScheduledFuture<?> execFuture = null;

	private double computeFixedCosts() {
		double value = 0;
		
		for (String customer : getActiveCustomers()) {
			AggregatorBill bill = this.getCustomerBill(customer);
			
			value += bill.getRewardFixed() + bill.getRewardTotalEnergyFlex() + bill.getRewardTotalTimeFlex(); //  + bill.getRewardTotalSchedFixed();			
		}
		return value;
	}
	
	public FlexOfferPortfolio getFlexOfferPortfolio() {
		return new FlexOfferPortfolio(this, aggFlexOffers.values(), this.market_commitments, this.computeFixedCosts());
	}
	
	public void delayedPlanning() {
		try {
			if (execFuture == null || execFuture.isDone()) {			
				execFuture = execService.schedule(new Runnable() {
					@Override
					public void run() {
						runPlanning();					
					}}, 1000, TimeUnit.MILLISECONDS);
			}
		} catch(Exception e) {
			
		}
	}	
	
	public void runPlanning() {
		/* Invalidate parts of flex-offer profiles that are positioned earlier than the current time */
		this.invalidateFlexOffers();
		
		/* Aggregate flex-offers */
		this.aggregateFlexOffers();				
				
		/* Run planning */		
		FlexOfferPortfolio fp = this.getFlexOfferPortfolio();
		AggregatorOptimization opt = new AggregatorOptimization(fp, this.objective);
		try {
			opt.optimizePortfolio();
		} catch (Exception e) {		
			logger.error("Planning error:", e);
		}		
		
		/* Disaggregate schedules */
		for(AggregatedFlexOffer f : aggFlexOffers.values()) {
			try {
				this.executeFlexOffers(this.disaggregateFlexOffer(f));
			} catch (FlexOfferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		
	}

	/**
	 *  Invalidate (parts of) flex-offer profiles that were supposed to be executed, i.e., are positioned earlier than the current time 
	 *  */	
	private void invalidateFlexOffers() {
		long currentTid = FlexOffer.toFlexOfferTime(new Date());
		
		for(FlexOffer f : this.getAllSimpleFlexOffers()) {
			FlexOfferSchedule sch = f.getFlexOfferSchedule();
			
			/* Two case, when : 1) schedule is availabe; 2) schedule is not available */
			if (sch != null) {
				
				/* Start-time bound has been crossed */				
				if (currentTid >= sch.getStartInterval()) {
					
					/* Stat-time treatment - lock-in the start-time */
					f.setStartAfterInterval(sch.getStartInterval());
					f.setStartBeforeInterval(sch.getStartInterval());
					
					/* Energy amount treatment - lock-in the energy amounts */
					long sliceTid = sch.getStartInterval();					
					for (int i=0; i < f.getSlices().length; i++) {
						FlexOfferSlice s = f.getSlice(i);
						
						if (currentTid >= sliceTid) {
							s.getEnergyConstraint().setLower(sch.getEnergyAmount(i));
							s.getEnergyConstraint().setUpper(sch.getEnergyAmount(i));
						}
						
						sliceTid += s.getDuration();
					}
				}				
			} else {
				
			
				/* Start time treatment */
				if (currentTid > f.getStartAfterInterval()) {
					
					/* Fix the EST */
					f.setStartAfterInterval(Math.min(currentTid, f.getStartBeforeInterval()));
				}
			}
		}		
	}


	/* Disaggregates a particular aggregated flexoffer schedule */
	private Collection<FlexOffer> disaggregateFlexOffer(AggregatedFlexOffer aggFo) {
		try {
			return agg.Disaggregate(aggFo);
		} catch (AggregationException e) {
			logger.error("Error disaggregating flex-offer. Error: ",
					e.getMessage());
		}
		return new ArrayList<FlexOffer>();
	}
	
	/* Notifies FOA about the new schedule*/
	public void executeFlexOffer(FlexOffer fo) throws FlexOfferException {		
		boolean hasSchedule = fo.getFlexOfferSchedule() != null;
		
		if (hasSchedule && !fo.getFlexOfferSchedule().isCorrect(fo)) {
			throw new FlexOfferException("Trying to execute an invalid flexoffer schedule!");
		};
		
		if (fo instanceof AggregatedFlexOffer) {
			try {
				Collection<FlexOffer> foc = this.agg.Disaggregate((AggregatedFlexOffer)fo);
				for(FlexOffer f : foc) {
					this.executeFlexOffer(f);
				}
			} catch (AggregationException e) {
				throw new FlexOfferException("Execution of flexoffer schedule failed due to the disaggregation error:"+e.getMessage());
			}
		} else {		
			fo.setState(hasSchedule ? FlexOfferState.Assigned : fo.getState());
			
			try {
				this.foLst.onFlexOfferScheduleUpdate(fo);
			} catch(Exception ex) {
					System.out.println("Error passing the schedule to a FOA: "+ex.getMessage());
			};
		}
	}
	
	public void executeFlexOffers(Collection<FlexOffer> fos) throws FlexOfferException {
		for(FlexOffer f : fos) {
			this.executeFlexOffer(f);
		}
	}
	
	/** Contract/customer management **/
	public Collection<String> getActiveCustomers() {
		HashSet<String> hs = new HashSet<String>();

		for (FlexOfferKey k : this.foaFlexOffers.keySet())
		{
			hs.add(k.getOwnId());
		}
		
		return hs;
	}
	
	public Collection<FlexOffer> getActiveCustomerFlexOffers(String customerId) {
		List<FlexOffer> foList = new ArrayList<>();
		for(Entry<FlexOfferKey, FlexOffer> kf : this.foaFlexOffers.entrySet()) {
			if (kf.getKey().getOwnId().equalsIgnoreCase(customerId)) {
				foList.add(kf.getValue());
			}
		}
		return foList;
	}
	
	public AggregatorContract getDefaultContract() {
		return this.defaultContract;
	}	

	public void setDefaultContract(AggregatorContract contract) {
		this.defaultContract = contract;		
	}
	
	public AggregatorContract getContract(String customerId) {
		return this.getDefaultContract();	/* Assume default contract */
	}
	
	public AggregatorContract getFlexOfferContract(FlexOffer f) {
		if (f instanceof AggregatedFlexOffer) {
			/* Compute aggregated contract */
			AggregatorContract ac = new AggregatorContract();
			
			int numSubFos = ((AggregatedFlexOffer)f).getSubFlexOffers().length;
			
			for (int i=0; i < numSubFos; i++) {
				AggregatorContract fc = getFlexOfferContract(((AggregatedFlexOffer)f).getSubFlexOffers()[i]);
				
				ac.setFixedReward(ac.getFixedReward() + fc.getFixedReward());
				ac.setSchedulingFixedReward(ac.getSchedulingFixedReward() + fc.getSchedulingFixedReward());
				ac.setTimeFlexReward(ac.getTimeFlexReward() + fc.getTimeFlexReward());				
				ac.setSchedulingStartTimeReward(ac.getSchedulingStartTimeReward() + fc.getSchedulingStartTimeReward());
				/* Conservativelly aggregate rawards */ 
				ac.setEnergyFlexReward(Math.max(ac.getEnergyFlexReward(), fc.getEnergyFlexReward()));				
				ac.setSchedulingEnergyReward(Math.max(ac.getSchedulingEnergyReward(), fc.getSchedulingEnergyReward()));				
			}
			
// 			ac.setTimeFlexReward(ac.getTimeFlexReward() * numSubFos);
//			ac.setEnergyFlexReward(ac.getEnergyFlexReward() * numSubFos);
//			ac.setSchedulingStartTimeReward(ac.getSchedulingStartTimeReward() * numSubFos);
//			ac.setSchedulingEnergyReward(ac.getSchedulingEnergyReward() * numSubFos);
			
			return ac;			
		} else { 
			return getContract(f.getOfferedById());
		}
	}
	
	public AggregatorBill getCustomerBill(String customerId) {
		return AggregatorBillFactory.generateBill(customerId,
												  this.getContract(customerId), 
												  this.getActiveCustomerFlexOffers(customerId));
	}
	
	/* Business-related methods */
	
	/**
	 * 
	 * Generates a deep-copy of all simple flexoffers, so that changes their attributes do not affect simple flexoffers 
	 * 
	 * */	
	public Collection<FlexOffer> cloneSimpleFlexOffers() {
		Collection<FlexOffer> col = new ArrayList<FlexOffer>();
		for(FlexOffer f : this.getSimpleFlexOffers()) {
			col.add((FlexOffer) f.clone());
		}
		return col;
	}


	public MarketContract getMarketContract() {
		// TODO Auto-generated method stub
		return this.defaultMarketContract;
	}


	public TimeSeries getScheduledEnergy() {
		TimeSeries ts = new TimeSeries(new double[] {});
		
		for(FlexOffer f : this.getAllSimpleFlexOffers()) {
			if (f.getFlexOfferSchedule() != null) {
				TimeSeries ft = new TimeSeries(f, TimeSeriesType.tstScheduledEnergy);				
				ts._extend(ft)._plus(ft);
			}
		}
		return ts;	
	}


	public TimeSeries getBaselineEnergy() {	
		TimeSeries ts = new TimeSeries(new double[] {});
	
		for(FlexOffer f : this.getAllSimpleFlexOffers()) {
			if (f.getDefaultSchedule() != null) {
				TimeSeries ft = new TimeSeries(f, TimeSeriesType.tstBaselineEnergy);				
				ts._extend(ft)._plus(ft);
			}
		}
		return ts;	
	}
	
	
	public BidV2 generate_maketV2_bid(long timeFrom, long timeTo) {
		FlexOfferPortfolio fp = new FlexOfferPortfolio(this, aggFlexOffers.values(), this.market_commitments, this.computeFixedCosts());
		/* Always optimize costs when generating bids */
		AggregatorOptimization opt = new AggregatorOptimization(fp, OptimizationObjective.objLowestCost); 
		try {
			BidV2 bid = opt.generate_maketV2_bid(timeFrom, timeTo);
			bid.setId(this.getId());
			bid.setOwner(this.getId());
			bid.setIs_seller_bid(true);
			return bid;
		} catch (Exception e) {		
			logger.error("Planning error:", e);
		}		
		return null;	
	}
}
