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


import java.util.Arrays;
import java.util.Random;

import org.arrowhead.wp5.core.entities.FlexOffer;

public class TimeFlexOptimizer {
	private AggregatorOptimization opt;
	private FlexOfferPortfolio fp;
	private Random r = new Random();
	
	/* Simulate Annealing parameters */
	private final double MAX_TEMP =           5000.0;
	private final double MIN_TEMP =           1.0;
	private final int 	 LIMIT = 50000;
	private final double BOLTZMANN_CONSTANT = 1.0;
	private final double TEMP_FACTOR =        0.5;
	
	private double timeFlexibility;
	
	public TimeFlexOptimizer(AggregatorOptimization opt){
		this.opt = opt;
		this.fp = opt.getFlexOfferPortfolio();
		this.timeFlexibility = this.computeTimeFlexibility(); 
	}
	
	private double computeTimeFlexibility() {
		double timeFlex = 0;
		for (FlexOffer f : this.fp.getFlexOffers()) {
			if (f.getStartBeforeInterval() - f.getStartAfterInterval() > timeFlex) {
				timeFlex = f.getStartBeforeInterval() - f.getStartAfterInterval();
			}
		}
		return timeFlex;
	}

	private long [] getCurrentState() {
		long [] state = new long[this.fp.getFlexOffers().size()];
		for(int i=0; i < state.length; i++) {
			state[i] = this.fp.getFlexOffers().get(i).getFlexOfferSchedule().getStartInterval();
		}
		return state;
	}
	
	private void setCurrentState(long [] state) {
		for(int i=0; i < state.length; i++) {
			this.fp.getFlexOffers().get(i).getFlexOfferSchedule().setStartInterval(state[i]);
		}
	}
	
	private long [] getNeighbourState(long [] state0, double t) {
		long [] state = new long[state0.length];
		for(int i = 0; i < state.length; i++){
			state[i] = (long) (state0[i] + r.nextGaussian() * t);

			// Check state validity
			if (this.fp.getFlexOffers().get(i).getStartAfterInterval() == 
				this.fp.getFlexOffers().get(i).getStartBeforeInterval()) {
				state[i] = this.fp.getFlexOffers().get(i).getStartAfterInterval();
				
				continue;
			}
			
			if (state[i] < this.fp.getFlexOffers().get(i).getStartAfterInterval()) {
				state[i] = this.fp.getFlexOffers().get(i).getStartBeforeInterval() -
						  (this.fp.getFlexOffers().get(i).getStartAfterInterval() - state[i]) %
						  (this.fp.getFlexOffers().get(i).getStartBeforeInterval() - 
						   this.fp.getFlexOffers().get(i).getStartAfterInterval());							
			}
			
			if (state[i] > this.fp.getFlexOffers().get(i).getStartBeforeInterval()) {
				state[i] = this.fp.getFlexOffers().get(i).getStartAfterInterval() +
						  (state[i] - this.fp.getFlexOffers().get(i).getStartBeforeInterval()) %
						  (this.fp.getFlexOffers().get(i).getStartBeforeInterval() - 
						   this.fp.getFlexOffers().get(i).getStartAfterInterval());				
			}
			
//			// Check state validity
//			if (state[i] < this.fp.getFlexOffers().get(i).getStartAfterInterval()) {
//				state[i] = this.fp.getFlexOffers().get(i).getStartAfterInterval();
//			}
//			
//			if (state[i] > this.fp.getFlexOffers().get(i).getStartBeforeInterval()) {
//				state[i] = this.fp.getFlexOffers().get(i).getStartBeforeInterval();
//			}
		}
		
		return state;
	}
	
	private double getFitnessValue(long [] state) {
		this.setCurrentState(state);
		return this.opt.getFitnessValue(); // .fp.computePortfolioCost();
	}
	
	private long [] simAnneal(FlexOfferPortfolio fp){
		double t; // Temperature
	
		long [] pCurrent = this.getCurrentState();
		long [] pDest = Arrays.copyOf(pCurrent, pCurrent.length);
		double e, e_new; // energy of current and new state

		e = getFitnessValue(pCurrent);
		t=2*this.timeFlexibility;  // MAX_TEMP;
		
		while( t >= MIN_TEMP) {
			if (e <= 1e-5) {
				break;
			}
			
			for (int i = 0; i < LIMIT; i++ ) { 
			
				pDest = getNeighbourState(pCurrent, t);
				
				e_new = this.getFitnessValue(pDest);
				
				// Check state acceptance
	  	        if ( Boltzmann(e, e_new, t) ) { // make pDest new current state?
	  	        	pCurrent = pDest;
			    }
			}

  	        // Reduce temperature
  	        t = t * TEMP_FACTOR;			
		}
		
		return pDest;
	}
	
	private boolean Boltzmann(double e1, double e2, double t) {
		 double x;
		 if ( e2 < e1 ) return true;
		 x = Math.exp( (-(e2 - e1)) / (BOLTZMANN_CONSTANT * t) );
		 if ( x <= 1.0 && r.nextDouble() < x ) return true;
		 if ( r.nextDouble() < (x - Math.floor(x)) ) // x > 1, so check decimal fraction of x
		      return true; 
		 return false;
	}

	/* This method implements Simulated Annealing for time flexibility optimization */
	public void optimizeTimeFlex() {			
		long [] startState = this.getCurrentState();
		double fitnessValue = this.opt.getFitnessValue(); // this.fp.computePortfolioCost();		
		this.setCurrentState(this.simAnneal(fp));	
		
		if (this.fp.computePortfolioCost() > fitnessValue) {
			this.setCurrentState(startState);
		}
	}

}
 