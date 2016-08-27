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


import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferSlice;

public class AmountFlexOptimizer {
    private FlexOfferPortfolio fp = null;
    
    public AmountFlexOptimizer(FlexOfferPortfolio fp) {
    	this.fp = fp;
    }    
    
    private double getEnergyValue(int fid, int sliceId) {
    	return this.fp.getFlexOffers().get(fid).getFlexOfferSchedule().getEnergyAmount(sliceId);
    }
    
    private void setEnergyValue(int fid, int sliceId, double value) {
    	this.fp.getFlexOffers().get(fid).getFlexOfferSchedule().getEnergyAmounts()[sliceId] = value;
    }
    
    private double getTotalEnergyValue(int flexOfferId) {
    	FlexOffer f = this.fp.getFlexOffers().get(flexOfferId);
    	double value = 0;
    	
    	for(int i=0; i<f.getSlices().length; i++) {
    		value += f.getFlexOfferSchedule().getEnergyAmount(i);
    	}
    	
    	return value;
    }

    
    private void fixEnergyValue(int fid, int sliceId) {
    	double value = getEnergyValue(fid, sliceId);
    	FlexOffer f = this.fp.getFlexOffers().get(fid);
    	FlexOfferSlice fc = f.getSlice(sliceId);
    	
    	// Fix based on total energy constraints 
    	if (f.getTotalEnergyConstraint() != null) {
	    	double totalEnerg = this.getTotalEnergyValue(fid);
	    	
	    	if (totalEnerg < f.getTotalEnergyConstraint().getLower()) {
	    		this.setEnergyValue(fid, sliceId, this.getEnergyValue(fid, sliceId) + f.getTotalEnergyConstraint().getLower() - totalEnerg);
	    	}
	    	
	    	if (totalEnerg > f.getTotalEnergyConstraint().getUpper()) {
	    		this.setEnergyValue(fid, sliceId, this.getEnergyValue(fid, sliceId) + f.getTotalEnergyConstraint().getUpper() - totalEnerg);
	    	}

    	}
    	
    	// Fix based on min/max energy constraints    	
    	if (value > fc.getEnergyUpper()) {
    		this.setEnergyValue(fid, sliceId, fc.getEnergyUpper());
    	}
    	
    	if (value < fc.getEnergyLower()) {
    		this.setEnergyValue(fid, sliceId, fc.getEnergyLower());
    	}    	
    }
    
	private boolean isValidSolution() {
		for(int fid = 0; fid < this.fp.getFlexOffers().size(); fid++) {
			FlexOffer f = this.fp.getFlexOffers().get(fid);

			if (f.getTotalEnergyConstraint() == null) {
				continue;
			}
			
			double totalValue = this.getTotalEnergyValue(fid);
			
			if (totalValue < f.getTotalEnergyConstraint().getLower()) {
				return false;
			}
			
			if (totalValue > f.getTotalEnergyConstraint().getUpper()) {
				return false;
			}
		}
		return true;
	}
    
    protected void optimizeAmount(int fid, int sliceId) {
    	double step = 1e3;
    	double value = this.getEnergyValue(fid, sliceId);
    	double fitness = this.fp.computePortfolioCost();
        boolean forward = true;
        
    	// Optimize a specific energy value
    	while (step > 1e-4) {
    	
    		while(true) {
	    		double newVal = value + (forward ? 1.0 : -1.0) * step;
	    		this.setEnergyValue(fid, sliceId, newVal);
	    		this.fixEnergyValue(fid, sliceId); // Try to fix the amount value
	    		double newfitness = this.fp.computePortfolioCost();
	    		
	    		if (newfitness < fitness) {
	    			value = this.getEnergyValue(fid, sliceId);
	    			fitness = newfitness;
	    		} else {
	    			forward = !forward;
	    			break;
	    		}
    		}    		
    		
    		step /= 2;
    	}
    }
    
    // Optimize amounts, return true if it is a valid solution
    public boolean optimizeAmounts() {
    	int numIter = 10;
    	
	    for(int i=0; i < numIter; i++) {
	    	
	    	for(int fid = 0; fid < this.fp.getFlexOffers().size(); fid++) {
	    		FlexOffer f = this.fp.getFlexOffers().get(fid);
	    		
	    		for(int sliceId = 0; sliceId < f.getSlices().length; sliceId++) {
	    			this.optimizeAmount(fid, sliceId);
	    		}
	    	}
	    	
	    	if (this.isValidSolution()){ break; }
    	}
	    
	    return this.isValidSolution();
    }
}
