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

/** Aggregator's bill to be generated for a DER owner **/
@XmlRootElement
public class AggregatorBill {
	/* Aggregator's bill records */
	
	private String customerId = ""; /* customer Id */

	/* Number sent flexoffers */
	private int numFlexOffers = 0;
	
	/* Total time flexibility */
	private long totalTimeFlex = 0;
	
	/* Total amount flexibility */
	private double totalEnergyFlex = 0;
	
	/* Number of custom (differing from a baseline) schedule activations */
	private int numCustomScheduleActivations = 0;
	
	/* Total EST deviations from the default */
	private double totalStartTimeDeviations = 0;
	
	/* Total amount deviations from the default */
	private double totalEnergyDeviations = 0;
	
	/* Aggregator's monetary rewards */
	private double rewardTotal = 0; /* Total reward to a Prosumer */
	
	private double rewardFixed = 0; /* Fixed reward */
	
	private double rewardTotalTimeFlex = 0; /* Reward for time flexibility */
	
	private double rewardTotalEnergyFlex = 0; /* Reward for energy flexibility */
	
	private double rewardTotalSchedFixed = 0; /* Reward for scheduling a flexoffer */
	
	private double rewardTotalSchedEST = 0;   /* Reward for scheduling EST */
	
	private double rewardTotalSchedEnergy = 0; /* Reward for scheduling amount */
		 	
	public AggregatorBill() {		
	}
	

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public int getNumFlexOffers() {
		return numFlexOffers;
	}

	public void setNumFlexOffers(int numFlexOffers) {
		this.numFlexOffers = numFlexOffers;
	}

	public long getTotalTimeFlex() {
		return totalTimeFlex;
	}

	public void setTotalTimeFlex(long l) {
		this.totalTimeFlex = l;
	}

	public double getTotalEnergyFlex() {
		return totalEnergyFlex;
	}

	public void setTotalEnergyFlex(double totalEnergyFlex) {
		this.totalEnergyFlex = totalEnergyFlex;
	}

	public int getNumCustomScheduleActivations() {
		return numCustomScheduleActivations;
	}

	public void setNumCustomScheduleActivations(int numCustomScheduleActivations) {
		this.numCustomScheduleActivations = numCustomScheduleActivations;
	}

	public double getTotalStartTimeDeviations() {
		return totalStartTimeDeviations;
	}

	public void setTotalStartTimeDeviations(double totalStartTimeDeviations) {
		this.totalStartTimeDeviations = totalStartTimeDeviations;
	}

	public double getTotalEnergyDeviations() {
		return totalEnergyDeviations;
	}

	public void setTotalEnergyDeviations(double totalEnergyDeviations) {
		this.totalEnergyDeviations = totalEnergyDeviations;
	}

	public double getRewardTotal() {
		return rewardTotal;
	}

	public void setRewardTotal(double rewardTotal) {
		this.rewardTotal = rewardTotal;
	}

	public double getRewardFixed() {
		return rewardFixed;
	}

	public void setRewardFixed(double rewardFixed) {
		this.rewardFixed = rewardFixed;
	}

	public double getRewardTotalTimeFlex() {
		return rewardTotalTimeFlex;
	}

	public void setRewardTotalTimeFlex(double rewardTotalTimeFlex) {
		this.rewardTotalTimeFlex = rewardTotalTimeFlex;
	}

	public double getRewardTotalEnergyFlex() {
		return rewardTotalEnergyFlex;
	}

	public void setRewardTotalEnergyFlex(double rewardTotalEnergyFlex) {
		this.rewardTotalEnergyFlex = rewardTotalEnergyFlex;
	}

	public double getRewardTotalSchedFixed() {
		return rewardTotalSchedFixed;
	}

	public void setRewardTotalSchedFixed(double rewardTotalSchedFixed) {
		this.rewardTotalSchedFixed = rewardTotalSchedFixed;
	}

	public double getRewardTotalSchedEST() {
		return rewardTotalSchedEST;
	}

	public void setRewardTotalSchedEST(double rewardTotalSchedEST) {
		this.rewardTotalSchedEST = rewardTotalSchedEST;
	}

	public double getRewardTotalSchedEnergy() {
		return rewardTotalSchedEnergy;
	}

	public void setRewardTotalSchedEnergy(double rewardTotalSchedEnergy) {
		this.rewardTotalSchedEnergy = rewardTotalSchedEnergy;
	}	
	
}
