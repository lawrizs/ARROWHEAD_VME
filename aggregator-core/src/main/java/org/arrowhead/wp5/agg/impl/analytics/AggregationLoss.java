package org.arrowhead.wp5.agg.impl.analytics;

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


/**
 * The class defines losses associated with flex-offer aggregation
 * 
 * @author Laurynas
 *
 */
public class AggregationLoss {
	/**
	 * Is a number that quantifies an energy flexibilities lost during aggregation. 
	 * It is a difference between total energy flexibilities expressed by sets of flex-offers before 
	 * and after aggregation.
	 */
	private double energyFlexibilityAbsoluteLoss;
	/**
	 * Is a number that quantifies a time flexibility lost during aggregation. 
	 * It is a difference between total time flexibilities expressed by sets of flex-offers before 
	 * and after aggregation.
	 */
	private double timeFlexibilityAbsoluteLoss;
	
	/**
	 * Is a number that quantifies a relative time flexibilities lost during aggregation. 
	 * It is a difference between time flexibilities expressed for a single flex-offer before 
	 * and after aggregation.
	 */
	private double timeFlexibilityPerFOLoss;	
	
	/**
	 * Is a number that quantifies a difference between extreme lower energy amounts that can be fixed 
	 * (scheduled) at some time interval before and after aggregation. 
	 */	
	private double lowerPeakReductionPotentialExtremeLoss;
	/**
	 * Is a number that quantifies a difference between extreme upper energy amounts that can be fixed 
	 * (scheduled) at some time interval before and after aggregation. 
	 */	
	private double upperPeakReductionPotentialExtremeLoss;
	
	/**
	 * Loss of the potential to yield minimum energy values in scheduling. It is expressed in average lower energy loss per 1 time stamp
	 */
	private double lowerPeakReductionPotentialPerTSLoss;
	
	/**
	 * Loss of the potential to yield maximum energy values in scheduling. It is expressed in average upper energy loss per 1 time stamp
	 */
	private double upperPeakReductionPotentialPerTSLoss;

	public double getEnergyFlexibilityAbsoluteLoss() {
		return energyFlexibilityAbsoluteLoss;
	}

	public double getLowerPeakReductionPotentialExtremeLoss() {
		return lowerPeakReductionPotentialExtremeLoss;
	}

	public double getLowerPeakReductionPotentialPerTSLoss() {
		return lowerPeakReductionPotentialPerTSLoss;
	}

	public double getTimeFlexibilityAbsoluteLoss() {
		return timeFlexibilityAbsoluteLoss;
	}

	public double getTimeFlexibilityPerFOLoss() {
		return timeFlexibilityPerFOLoss;
	}

	public double getUpperPeakReductionPotentialExtremeLoss() {
		return upperPeakReductionPotentialExtremeLoss;
	}

	public double getUpperPeakReductionPotentialPerTSLoss() {
		return upperPeakReductionPotentialPerTSLoss;
	}

	public void setEnergyFlexibilityAbsoluteLoss(
			double energyFlexibilityAbsoluteLoss) {
		this.energyFlexibilityAbsoluteLoss = energyFlexibilityAbsoluteLoss;
	}

	public void setLowerPeakReductionPotentialExtremeLoss(
			double lowerPeakReductionPotentialExtremeLoss) {
		this.lowerPeakReductionPotentialExtremeLoss = lowerPeakReductionPotentialExtremeLoss;
	}

	public void setLowerPeakReductionPotentialPerTSLoss(
			double lowerPeakReductionPotentialPerTSLoss) {
		this.lowerPeakReductionPotentialPerTSLoss = lowerPeakReductionPotentialPerTSLoss;
	}

	public void setTimeFlexibilityAbsoluteLoss(double timeFlexibilityAbsoluteLoss) {
		this.timeFlexibilityAbsoluteLoss = timeFlexibilityAbsoluteLoss;
	}

	public void setTimeFlexibilityPerFOLoss(double timeFlexibilityPerFOLoss) {
		this.timeFlexibilityPerFOLoss = timeFlexibilityPerFOLoss;
	}

	public void setUpperPeakReductionPotentialExtremeLoss(
			double upperPeakReductionPotentialExtremeLoss) {
		this.upperPeakReductionPotentialExtremeLoss = upperPeakReductionPotentialExtremeLoss;
	}

	public void setUpperPeakReductionPotentialPerTSLoss(
			double upperPeakReductionPotentialPerTSLoss) {
		this.upperPeakReductionPotentialPerTSLoss = upperPeakReductionPotentialPerTSLoss;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AggregationLoss [energyFlexibilityAbsoluteLoss=");
		builder.append(energyFlexibilityAbsoluteLoss);
		builder.append("(Wh)");
		builder.append(", timeFlexibilityAbsoluteLoss=");
		builder.append(timeFlexibilityAbsoluteLoss);
		builder.append("(time stamps)");
		builder.append(", timeFlexibilityPerFOLoss=");
		builder.append(timeFlexibilityPerFOLoss);
		builder.append("(time stamps/flex-offer)");
		builder.append(", lowerPeakReductionPotentialExtremeLoss=");
		builder.append(lowerPeakReductionPotentialExtremeLoss);
		builder.append("(Wh)");
		builder.append(", upperPeakReductionPotentialExtremeLoss=");
		builder.append(upperPeakReductionPotentialExtremeLoss);
		builder.append("(Wh)");
		builder.append(", lowerPeakReductionPotentialPerTSLoss=");
		builder.append(lowerPeakReductionPotentialPerTSLoss);
		builder.append("(Wh/time stamp)");
		builder.append(", upperPeakReductionPotentialPerTSLoss=");
		builder.append(upperPeakReductionPotentialPerTSLoss);
		builder.append("(Wh/time stamp)");
		builder.append("]");
		return builder.toString();
	}
}
