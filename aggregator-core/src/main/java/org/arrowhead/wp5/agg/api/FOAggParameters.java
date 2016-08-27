package org.arrowhead.wp5.agg.api;

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


import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A class that specifies all parameters of the flex-offer aggregation. 
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 *
 */
@XmlRootElement
public final class FOAggParameters  implements Serializable {	
	private static final long serialVersionUID = 1L;
	
	private ProfileShape preferredProfileShape;
	private ConstraintPair constraintPair;
	private ConstraintAggregate constraintAggregate;
	
	public FOAggParameters()
	{
	  this.setPreferredProfileShape(ProfileShape.psAlignStart);
	  this.setConstraintAggregate(new ConstraintAggregate());
	  this.setConstraintPair(new ConstraintPair());
	}
	/**
	 * This parameter controls how flex-offer energy profiles are added (aggregated) 
	 * 	
	 * @param preferredProfileShape
	 */
	public void setPreferredProfileShape(ProfileShape preferredProfileShape) {
		this.preferredProfileShape = preferredProfileShape;
	}

	public ProfileShape getPreferredProfileShape() {
		return preferredProfileShape;
	}
	
	/**
	 *  This parameter controls, which flex-offers are allowed to be aggregated together.
	 * @param constraintPair
	 */
	public void setConstraintPair(ConstraintPair constraintPair) {
		this.constraintPair = constraintPair;
	}

	public ConstraintPair getConstraintPair() {
		return constraintPair;
	}

	/**
	 *  This parameter set constraints on a "shape" of aggregated flex-offers. Note, when this parameter is set,
	 *  some of micro flex-offer might not be aggregated into macro flex-offers.
	 *   
	 * @param constraintAggregate
	 */
	public void setConstraintAggregate(ConstraintAggregate constraintAggregate) {
		this.constraintAggregate = constraintAggregate;
	}

	public ConstraintAggregate getConstraintAggregate() {
		return constraintAggregate;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FOAggParameters [preferredProfileShape=");
		builder.append(preferredProfileShape);
		builder.append(", constraintPair=");
		builder.append(constraintPair.toString());
		builder.append(", constraintAggregate=");
		builder.append(constraintAggregate.toString());
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Defines how flex-offer energy profiles are added (aggregated) 
	 */
	public enum ProfileShape {
		/**
		 * Profiles of flex-offers will always be aligned to the left ("startAfter") during 
		 * their addition (aggregation).
		 */
		psAlignStart, 
		/**
		 * Profiles of flex-offers will be aligned based on the baseline profile
		 */
		psAlignBaseline,
		/**
		 * Profiles of flex-offers will always be aligned to the right ("startBefore") during 
		 * their addition (aggregation).
		 */
		psAlignEnds,
		/**
		 * Profiles of flex-offers will always be aligned arbitrary to prevent high peaks in upper and lower energy bounds 
		 * in the aggregated flex-offer while still trying to preserve time flexibility
		 */		
		psUniformTimeFlex,
		/**
		 * Profiles of flex-offers will always be aligned arbitrary to prevent high peaks in upper and lower energy bounds 
		 * in the aggregated flex-offer while using all available time flexibility. Resulting macro flex-offers will not
		 * have time flexibility available ("startAfter" = "startBefore"). 
		 */		
		psFlat,
		
		/**
		 * These are one of 5 balance alignment modes, described in the TKDE paper (TODO: give a ref.) 
		 */
		psDynamicSimulatedAnnealing, psZeroExhaustiveSearch, psExhaustiveSearch, psExhaustiveGreedy, psSimpleGreedy
	}
	
	/**
	 * Defines if a certain aggregation (sub-) parameter is enforced of not.
	 * 
	 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
	 *
	 */
	public enum ConstraintType {
		acNotSet,
		acSet
	}	
	
	/**
	 * Defines a sub-parameters that controls, which flex-offers are allowed to be aggregated together.
	 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
	 *
	 */
	public final static class ConstraintPair implements Serializable {
		private static final long serialVersionUID = 1L;
		
		/**
		 *	f1 and f2 are aggregated if a 
		 *  difference between their profile lengths (interval count) does not exceed this parameter
		 */
		public double durationTolerance;
		public ConstraintType durationToleranceType = ConstraintType.acNotSet;

		/**
		 *	f1 and f2 are aggregated if a 
		 *  difference between their "startAfter" times does not exceed this parameter
		 */
		public double startAfterTolerance;
		public ConstraintType startAfterToleranceType = ConstraintType.acNotSet;
		
		/**
		 *	f1 and f2 are aggregated if a 
		 *  difference between their "startBefore" times does not exceed this parameter
		 */		
		public double startBeforeTolerance;
		public ConstraintType startBeforeToleranceType = ConstraintType.acNotSet;
		/**
		 *	f1 and f2 are aggregated if a 
		 *  difference between their "assignmentBeforeTime" times does not exceed this parameter
		 */		
		public double assignmentBeforeTolerance;
		public ConstraintType assignmentBeforeToleranceType = ConstraintType.acNotSet;
		/**
		 *	f1 and f2 are aggregated if a 
		 *  difference between their energy interval counts does not exceed this parameter
		 */		
		public double timesliceCountTolerance;
		public ConstraintType timesliceCountToleranceType = ConstraintType.acNotSet;;
		/**
		 *	f1 and f2 are aggregated if a 
		 *  difference between their time flexibilities ("startBefore" - "startAfter") does not 
		 *  exceed this parameter
		 */		
		public double timeFlexibilityTolerance;
		public ConstraintType timeFlexibilityToleranceType = ConstraintType.acNotSet;
		/**
		 *	f1 and f2 are aggregated if a 
		 *  difference between overall energy constraint lower bounds 
		 *  (or sums of lower energy bounds - if overall energy constraint is not specified) does not 
		 *  exceed this parameter
		 */		
		public double totalEnLowAmountTolerance;
		public ConstraintType totalEnLowAmountToleranceType = ConstraintType.acNotSet;
		/**
		 *	f1 and f2 are aggregated if a difference between overall energy constraint upper bounds 
		 *  (or sums of upper energy bounds - if overall energy constraint is not specified) 
		 *  does not exceed this parameter
		 */				
		public double totalEnHighAmountTolerance;
		public ConstraintType totalEnHighAmountToleranceType = ConstraintType.acNotSet;
		/**
		 *	f1 and f2 are aggregated if a difference between their overall energy flexibilities 
		 *  ("overallEnergy.upper - overallEnergy.upper" or sum of "energy.upper - energy.lower") does not
		 *   exceed this parameter
		 */		
		public double totalEnFlexibilityTolerance;
		public ConstraintType totalEnFlexibilityToleranceType = ConstraintType.acNotSet;
		
		/**
		 *	f1 and f2 are aggregated if a difference between their "flexibilityPrice" does not exceed 
		 *  this parameter
		 */
		public double minTariffTolerance;
		public ConstraintType minTariffToleranceType = ConstraintType.acNotSet;
		/**
		 *	f1 and f2 are aggregated if a difference between their "flexibilityPrice" does not exceed 
		 *  this parameter
		 */		
		public double maxTariffTolerance;
		public ConstraintType maxTariffToleranceType = ConstraintType.acNotSet;
		
		/**
		 *	f1 and f2 are aggregated if a difference between their "minTariffProfile" does not exceed 
		 *  this parameter
		 */
		public double minTariffProfileTolerance;
		public ConstraintType minTariffProfileToleranceType = ConstraintType.acNotSet;
		/**
		 *	f1 and f2 are aggregated if a difference between their "maxTariffProfile" does not exceed 
		 *  this parameter
		 */		
		public double maxTariffProfileTolerance;
		public ConstraintType maxTariffProfileToleranceType = ConstraintType.acNotSet;
		
		/**
		 * This aggregation parameter applies on the costPerEnergyUnitLimit.
		 */
		public double costPerEnergyUnitLimitTolerance;
		public ConstraintType costPerEnergyUnitLimitType = ConstraintType.acNotSet;

		/**
		 * f1 and f2 are aggregated if f1 and f2 can be "equally" advanced within their time flexibility range
		 * with respect to the baseline schedule
		 */
		public double baselineAdvancingTolerance;
		public ConstraintType baselineAdvancingType = ConstraintType.acNotSet;
		
		/**
		 * f1 and f2 are aggregated if f1 and f2 can be "equally" retarded within their time flexibility range
		 * with respect to the baseline schedule
		 */
		public double baselineRetardingTolerance;
		public ConstraintType baselineRetardingType = ConstraintType.acNotSet;
	
		
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ConstraintPair [durationTolerance=");
			builder.append(durationTolerance);
			builder.append(", durationToleranceType=");
			builder.append(durationToleranceType);
			builder.append(", startAfterTolerance=");
			builder.append(startAfterTolerance);
			builder.append(", startAfterToleranceType=");
			builder.append(startAfterToleranceType);
			builder.append(", startBeforeTolerance=");
			builder.append(startBeforeTolerance);
			builder.append(", startBeforeToleranceType=");
			builder.append(startBeforeToleranceType);
			builder.append(", assignmentBeforeTolerance=");
			builder.append(assignmentBeforeTolerance);
			builder.append(", assignmentBeforeToleranceType=");
			builder.append(assignmentBeforeToleranceType);
			builder.append(", timesliceCountTolerance=");
			builder.append(timesliceCountTolerance);
			builder.append(", timesliceCountToleranceType=");
			builder.append(timesliceCountToleranceType);
			builder.append(", timeFlexibilityTolerance=");
			builder.append(timeFlexibilityTolerance);
			builder.append(", timeFlexibilityToleranceType=");
			builder.append(timeFlexibilityToleranceType);
			builder.append(", totalEnLowAmountTolerance=");
			builder.append(totalEnLowAmountTolerance);
			builder.append(", totalEnLowAmountToleranceType=");
			builder.append(totalEnLowAmountToleranceType);
			builder.append(", totalEnHighAmountTolerance=");
			builder.append(totalEnHighAmountTolerance);
			builder.append(", totalEnHighAmountToleranceType=");
			builder.append(totalEnHighAmountToleranceType);
			builder.append(", totalEnFlexibilityTolerance=");
			builder.append(totalEnFlexibilityTolerance);
			builder.append(", totalEnFlexibilityToleranceType=");
			builder.append(totalEnFlexibilityToleranceType);
			builder.append(", minTariffTolerance=");
			builder.append(minTariffTolerance);
			builder.append(", minTariffToleranceType=");
			builder.append(minTariffToleranceType);
			builder.append(", maxTariffTolerance=");
			builder.append(maxTariffTolerance);
			builder.append(", maxTariffToleranceType=");
			builder.append(maxTariffToleranceType);
			builder.append(", costPerEnergyUnitLimitType=");
			builder.append(costPerEnergyUnitLimitType);
			builder.append("]");
			return builder.toString();
		}
		
	
	}

	/**
	 * Defines possible variants of the aggregate constraints 
	 * 
	 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
	 *
	 */
	public enum ConstraintAggregateType {
		/**
		 * The aggregate constraint is disabled - all micro flex-offers are aggregated into macro flex-offers
		 */
		acNotSet,
		/**
		 * The total number of flex-offers in a group is bounded. 
		 */
		acFlexOfferCount,
		/**
		 * The total minimum (lower) amount of energy (overall or sum of energy per interval) offered by all flex-offers in a group 
		 * (and the resulting macro flex-offer) is bounded.
		 */
		acTotalEnMin,
		/**
		 * The total maximum (higher) amount of energy (overall or sum of energy per interval) offered by all flex-offers in a group 
		 * (and the resulting macro flex-offer) is bounded.
		 */		
		acTotalEnMax,
		/**
		 * Time flexibility ("startBefore - startAfter") in a group (and the resulting macro flex-offer) is bounded.
		 * This parameter can be used to request macro flex-offers of a bounded time flexibility.
		 */
		acTimeFlexibility,
		/**
		 * Length of the energy profile in a group (and the resulting macro flex-offer) 
		 * is bounded. This parameter can be used to request macro flex-offers of a bounded energy profile length.
		 */
		acEnProfileDuration // Not documented in D3.1. - allow constraining the profile's duration 
	}
	
	/**
	 * Defines the aggregate constraint 
	 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
	 */	
	public final static class ConstraintAggregate implements Serializable {
		private static final long serialVersionUID = 1L;
		
		public ConstraintAggregateType aggConstraint = ConstraintAggregateType.acNotSet;
		/**
		 * Lower bound of the aggregate constraint  
		 */
		public double valueMin;
		/**
		 * Upper bound of the aggregate constraint  
		 */		
		public double valueMax;
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ConstraintAggregate [aggConstraint=");
			builder.append(aggConstraint);
			builder.append(", valueMin=");
			builder.append(valueMin);
			builder.append(", valueMax=");
			builder.append(valueMax);
			builder.append("]");
			return builder.toString();
		}
	}
}
