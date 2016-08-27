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


import javax.xml.bind.annotation.XmlRootElement;


/**
 * This class defines the contract between the DER and Aggregator.
 *  
 * @author Laurynas
 *
 */
@XmlRootElement
public class AggregatorContract {
	/**
	 * Aggregator agrees to pay a fixed monthly amount Prev (reward) to DER owner (Prosumer) if Prosumer is flexible, 
	 * issue flexoffers, and consume/produce energy according to the Aggregator’s schedules. This amount Prev is agreed prior 
	 * the contracted period and is invariant to actual flexibility offered by Prosumer.
	 */
	private double fixedReward = 0;
	
	/**
	 * Aggregator agrees to pay a certain amount to Prosumer for each issued flexoffer, if the corresponding flexoffer schedule is followed. 
	 * The price for flexoffer is determined as follows using time flexibility and amount flexibility prices Ptf and Paf:
	 */
	private double timeFlexReward = 0;
	private double energyFlexReward = 0;
	
	/**
	 * Aggregator agrees to pay to Prosumer a certain amount that depends on (1) if the schedule flexoffer's schedule differs from the base load (default schedule) 
	 * specified in a Prosumer flexoffer, and (2) how much Aggregator’s schedule differs from the base load (default schedule) - this amount is computed based on two 
	 * “delta” components: Pdt and Pda, where Pdt is the start-time deviation price, Pda is the amount deviation price. 
	 */
	private double schedulingFixedReward = 0;
	private double schedulingStartTimeReward = 0;
	private double schedulingEnergyReward = 0;

	public double getFixedReward() {
		return fixedReward;
	}
	public void setFixedReward(double fixedReward) {
		this.fixedReward = fixedReward;
	}
	public double getTimeFlexReward() {
		return timeFlexReward;
	}
	public void setTimeFlexReward(double timeFlexReward) {
		this.timeFlexReward = timeFlexReward;
	}
	public double getEnergyFlexReward() {
		return energyFlexReward;
	}
	public void setEnergyFlexReward(double energyFlexReward) {
		this.energyFlexReward = energyFlexReward;
	}
	public double getSchedulingFixedReward() {
		return schedulingFixedReward;
	}
	public void setSchedulingFixedReward(double schedulingFixedReward) {
		this.schedulingFixedReward = schedulingFixedReward;
	}
	public double getSchedulingStartTimeReward() {
		return schedulingStartTimeReward;
	}
	public void setSchedulingStartTimeReward(double schedulingStartTimeReward) {
		this.schedulingStartTimeReward = schedulingStartTimeReward;
	}
	public double getSchedulingEnergyReward() {
		return schedulingEnergyReward;
	}
	public void setSchedulingEnergyReward(double schedulingEnergyReward) {
		this.schedulingEnergyReward = schedulingEnergyReward;
	}	
}
