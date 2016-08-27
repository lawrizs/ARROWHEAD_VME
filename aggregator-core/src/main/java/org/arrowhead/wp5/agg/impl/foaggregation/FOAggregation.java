package org.arrowhead.wp5.agg.impl.foaggregation;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.arrowhead.wp5.agg.api.AggregationException;
import org.arrowhead.wp5.agg.api.ChangeRecOfFlexOffer;
import org.arrowhead.wp5.agg.api.FOAggParameters;
import org.arrowhead.wp5.agg.api.FOAggParameters.ConstraintAggregate;
import org.arrowhead.wp5.agg.api.FOAggParameters.ConstraintPair;
import org.arrowhead.wp5.agg.api.FOAggParameters.ConstraintType;
import org.arrowhead.wp5.agg.api.IAggregation;
import org.arrowhead.wp5.agg.api.IFOAggregation;
import org.arrowhead.wp5.agg.api.IFOCQAggregation;
import org.arrowhead.wp5.agg.api.IFOContinuousQuery;
import org.arrowhead.wp5.agg.impl.common.AggParamAbstracter;
import org.arrowhead.wp5.agg.impl.common.FGridVector;
import org.arrowhead.wp5.agg.impl.common.FlexOfferDimValueDelegate;
import org.arrowhead.wp5.agg.impl.common.FlexOfferWeightDelegate;
import org.arrowhead.wp5.agg.impl.grouper.FSubgrpBuilderWgtBounded.BinPackingParams;
import org.arrowhead.wp5.agg.impl.staticAggregator.StaticFOadder;
import org.arrowhead.wp5.core.entities.AggregatedFlexOffer;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferConstraint;

/**
 * This class offers the complete flex-offer aggregation functionality.
 * This includes both the sequential and incremental aggregation approaches. 
 * It offers an implementations of IFOAggregation and IFOCQAggregation
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 * @see IFOAggregation
 * @see IFOCQAggregation
 *
 */

public class FOAggregation implements IAggregation {
	// All registered queries
	private ArrayList<FOContinuousQuery> queries = new ArrayList<FOContinuousQuery>();	

	public FOAggregation(){}
	
	private AggParamAbstracter buildDefaultFOAbstracter(FOAggParameters aggParams) throws AggregationException
	{
		if (aggParams == null)
			throw new AggregationException("Aggregation parameters are not set!");
		List<Double> qVectValues = new ArrayList<Double>();
		List<FlexOfferDimValueDelegate> dimValDl = new ArrayList<FlexOfferDimValueDelegate>(); 
		
		// Initialize pair-wise similarity search
		if (aggParams.getConstraintPair() == null)
			throw new AggregationException("Aggregation parameter \"constraintPair\" is not set!");
		
		ConstraintPair cp = aggParams.getConstraintPair();				
		if (cp.durationToleranceType == ConstraintType.acSet) {
			qVectValues.add(cp.durationTolerance);
			dimValDl.add(new FlexOfferDimValueDelegate() {
							@Override
							public double getValue(FlexOffer f) {
								return f.getDurationIntervals();
							}
						});
		}
		
		if (cp.startAfterToleranceType == ConstraintType.acSet) {
			qVectValues.add(cp.startAfterTolerance);
			dimValDl.add(new FlexOfferDimValueDelegate() {
							@Override
							public double getValue(FlexOffer f) {
								return f.getStartAfterInterval();
							}
						});
		}
		
		if (cp.startBeforeToleranceType == ConstraintType.acSet) {
			qVectValues.add(cp.startBeforeTolerance);
			dimValDl.add(new FlexOfferDimValueDelegate() {
							@Override
							public double getValue(FlexOffer f) {
								return f.getStartBeforeInterval();
							}
						});
		}

		if (cp.assignmentBeforeToleranceType == ConstraintType.acSet) {
			qVectValues.add(cp.assignmentBeforeTolerance);
			dimValDl.add(new FlexOfferDimValueDelegate() {
							@Override
							public double getValue(FlexOffer f) {
								return f.getAssignmentBeforeInterval();
							}
						});
		}
		
		if (cp.timesliceCountToleranceType == ConstraintType.acSet) {
			qVectValues.add(cp.timesliceCountTolerance);
			dimValDl.add(new FlexOfferDimValueDelegate() {
							@Override
							public double getValue(FlexOffer f) {
								return f.getSlices().length;
							}
						});
		}

		if (cp.timeFlexibilityToleranceType == ConstraintType.acSet) {
			qVectValues.add(cp.timeFlexibilityTolerance);
			dimValDl.add(new FlexOfferDimValueDelegate() {
							@Override
							public double getValue(FlexOffer f) {
								return (f.getStartBeforeInterval() - f.getStartAfterInterval());
							}
						});
		}
		
		if (cp.totalEnLowAmountToleranceType == ConstraintType.acSet) {
			qVectValues.add(cp.totalEnLowAmountTolerance);
			dimValDl.add(new FlexOfferDimValueDelegate() {
							@Override
							public double getValue(FlexOffer f) {
								if (f.getTotalEnergyConstraint()!=null)
									return f.getTotalEnergyConstraint().getLower();
								else 
									return f.getSumEnergyConstraints().getLower();
							}
						});
		}

		if (cp.totalEnHighAmountToleranceType == ConstraintType.acSet) {
			qVectValues.add(cp.totalEnHighAmountTolerance);
			dimValDl.add(new FlexOfferDimValueDelegate() {
							@Override
							public double getValue(FlexOffer f) {
								if (f.getTotalEnergyConstraint()!=null)
									return f.getTotalEnergyConstraint().getUpper();
								else 
									return f.getSumEnergyConstraints().getUpper();
							}
						});
		}
		
		if (cp.totalEnFlexibilityToleranceType == ConstraintType.acSet) {
			qVectValues.add(cp.totalEnFlexibilityTolerance);
			dimValDl.add(new FlexOfferDimValueDelegate() {
							@Override
							public double getValue(FlexOffer f) {
								if (f.getTotalEnergyConstraint()!=null)
									return f.getTotalEnergyConstraint().getUpper() - 
													f.getTotalEnergyConstraint().getLower();
								else {
									FlexOfferConstraint c = f.getSumEnergyConstraints();
									return c.getUpper() - c.getLower();
								}
							}
						});
		}		
		
		if (cp.minTariffToleranceType == ConstraintType.acSet) {
			qVectValues.add(cp.minTariffTolerance);
			dimValDl.add(new FlexOfferDimValueDelegate() {
							@Override
							public double getValue(FlexOffer f) {
								throw new UnsupportedOperationException();
								//return f.getFlexibilityPrice();
//								return (double) f.getSumTariffConstraints().getLower();
							}
						});
		}		
		
		if (cp.maxTariffToleranceType == ConstraintType.acSet) {
			qVectValues.add(cp.maxTariffTolerance);
			dimValDl.add(new FlexOfferDimValueDelegate() {
							@Override
							public double getValue(FlexOffer f) {
								throw new UnsupportedOperationException();
								//return f.getFlexibilityPrice();
//								return (double) f.getSumTariffConstraints().getUpper();
							}
						});
		}		
		
		if (cp.minTariffProfileToleranceType == ConstraintType.acSet) {
			qVectValues.add(cp.minTariffProfileTolerance);
			dimValDl.add(new FlexOfferDimValueDelegate() {
							@Override
							public double getValue(FlexOffer f) {
								return (double) f.getSumTariffConstraints().getLower();
							}
						});
		}		
		
		if (cp.maxTariffProfileToleranceType == ConstraintType.acSet) {
			qVectValues.add(cp.maxTariffProfileTolerance);
			dimValDl.add(new FlexOfferDimValueDelegate() {
							@Override
							public double getValue(FlexOffer f) {
								return (double) f.getSumTariffConstraints().getUpper();
							}
						});
		}		
		
		if (cp.costPerEnergyUnitLimitType == ConstraintType.acSet) {
			qVectValues.add(cp.costPerEnergyUnitLimitTolerance);
			dimValDl.add(new FlexOfferDimValueDelegate() {
							@Override
							public double getValue(FlexOffer f) {
								throw new UnsupportedOperationException();
								//return (double) f.getCostPerEnergyUnitLimit();
							}
						});
		}
		
		// 2016-08-16 new grouping parameters related to baseline handling
		if (cp.baselineAdvancingType == ConstraintType.acSet) {
			qVectValues.add(cp.baselineAdvancingTolerance);
			dimValDl.add(new FlexOfferDimValueDelegate() {
				@Override
				public double getValue(FlexOffer f) {
					return (double) f.getDefaultSchedule().getStartInterval() - f.getStartAfterInterval();
				}
			});			
		}
		
		if (cp.baselineRetardingType == ConstraintType.acSet) {
			qVectValues.add(cp.baselineRetardingTolerance);
			dimValDl.add(new FlexOfferDimValueDelegate() {
				@Override
				public double getValue(FlexOffer f) {
					return (double) f.getStartBeforeInterval() - f.getDefaultSchedule().getStartInterval();
				}
			});			
		}
		

		if ( dimValDl.size() == 0)
			throw new AggregationException("At least 1 similarity criteria (ConstraintPair) must be set!");

		// Initialize all utilities
		FGridVector qVector = new FGridVector(qVectValues.size());
		for(int i=0; i<qVector.getDimCount(); i++)
			qVector.getValues()[i] = qVectValues.get(i);
		
		// Setup weight delegate
		if (aggParams.getConstraintAggregate() == null)
			throw new AggregationException("Aggregation parameter \"constraintAggregate\" is not set!");

		ConstraintAggregate ca = aggParams.getConstraintAggregate();		
		FlexOfferWeightDelegate foWdlg = null;
		BinPackingParams binPackParams = new BinPackingParams();;
		double weightMin = 0, weightMax = -1;
		switch (ca.aggConstraint)
		{
			case acFlexOfferCount: foWdlg = new FlexOfferWeightDelegate() {
				
				@Override
				public double getWeight(FlexOffer f) {
					return 1;
				}
	
				@Override
				public double addTwoWeights(double weight1, double weight2) {
					return weight1 + weight2;		// For flex-offer count, this is the simple addition
				}
	
				@Override
				public double getZeroWeight() {
					return 0;			// When no flex-offers exist in the group, its weight is 0
				};				
			};
			weightMin = ca.valueMin;
			weightMax = ca.valueMax;
			binPackParams.setEagerBinPacking(false);
			binPackParams.setForceFillMaximally(true);
			binPackParams.setUpsizeAllowed(true);
			break;
			case acTotalEnMax: foWdlg = new FlexOfferWeightDelegate() {
				
				@Override
				public double getWeight(FlexOffer f) {
					return (f.getTotalEnergyConstraint() != null ?  
							f.getTotalEnergyConstraint().getUpper(): f.getSumEnergyConstraints().getUpper());
				}

				@Override
				public double addTwoWeights(double weight1, double weight2) {
					return weight1 + weight2;		// For energy, the simple addition works
				}

				@Override
				public double getZeroWeight() {
					return 0;			// When no flex-offers exist in the group, its weight is 0
				};				
			};
			weightMin = ca.valueMin;
			weightMax = ca.valueMax;
			binPackParams.setEagerBinPacking(true);
			binPackParams.setForceFillMaximally(true);
			binPackParams.setUpsizeAllowed(true);
			break;
			case acTotalEnMin: foWdlg = new FlexOfferWeightDelegate() {
				@Override
				public double getWeight(FlexOffer f) {
					return (f.getTotalEnergyConstraint() != null ?  
							f.getTotalEnergyConstraint().getLower(): f.getSumEnergyConstraints().getLower());
				
				}
				@Override
				public double addTwoWeights(double weight1, double weight2) {
					return weight1 + weight2;		// For energy, the simple addition works;
				}
				@Override
				public double getZeroWeight() {
					return 0;			// When no flex-offers exist in the group, its weight is 0
				};				
			};
			weightMin = ca.valueMin;
			weightMax = ca.valueMax;
			binPackParams.setEagerBinPacking(true);
			binPackParams.setForceFillMaximally(true);
			binPackParams.setUpsizeAllowed(true);			
			break;
			case acTimeFlexibility:
				foWdlg = new FlexOfferWeightDelegate() {
					@Override
					public double getWeight(FlexOffer f) {
						// 0 - means no time flexibility
						// the lower the value, the larger is time flexibility 
						return -(f.getStartBeforeInterval() - f.getStartAfterInterval());					
					}
					@Override
					public double addTwoWeights(double weight1, double weight2) {
						// Max function is used to add two weights for time intervals
						return Math.max(weight1, weight2);		
					}
					@Override
					public double getZeroWeight() {
						return -Double.MAX_VALUE;        	// We start from this value for empty groups
					};				
				};
				weightMin = -ca.valueMax;
				weightMax = -ca.valueMin; // We inverts those values
				binPackParams.setEagerBinPacking(false);
				binPackParams.setForceFillMaximally(true);
				binPackParams.setUpsizeAllowed(true);
				break;
			case acEnProfileDuration:
				foWdlg = new FlexOfferWeightDelegate() {
					@Override
					public double getWeight(FlexOffer f) {
						// 0 - means no time flexibility
						// the lower the value, the larger is time flexibility 
						return f.getDurationIntervals();					
					}
					@Override
					public double addTwoWeights(double weight1, double weight2) {
						// Max function is used to add two weights for time intervals
						return Math.max(weight1, weight2);		
					}
					@Override
					public double getZeroWeight() {
						return 0;        	// We start from this value for empty groups
					};				
				};
				weightMin = ca.valueMin;
				weightMax = ca.valueMax; // We inverts those values
				binPackParams.setEagerBinPacking(false);
				binPackParams.setForceFillMaximally(false);
				binPackParams.setUpsizeAllowed(true);
				break;
		default:
//			throw new UnsupportedOperationException();
			//break;				
		}	

		// Initialize flex-offer abstracter
		AggParamAbstracter fa = new AggParamAbstracter(
				qVector, 
				dimValDl.toArray(new FlexOfferDimValueDelegate [0]),
				foWdlg,
				new StaticFOadder(aggParams.getPreferredProfileShape()));
		if (foWdlg != null)
		{
			fa.setWeightMinMaxValues(weightMin, weightMax);
			fa.setBinPackerParams(binPackParams);
		}

		return fa; 
	}
	
	
	@Override
	public FOContinuousQuery queryCreate(FOAggParameters aggParam) throws AggregationException {
		FOContinuousQuery fq = new FOContinuousQuery(buildDefaultFOAbstracter(aggParam));
		queries.add(fq);
		return fq;
	}
	
	// Method added for testing purposes
	public FOContinuousQuery queryCreate(AggParamAbstracter fpAbstracter) {
		FOContinuousQuery fq = new FOContinuousQuery(fpAbstracter);
		queries.add(fq);
		return fq;
	}

	@Override
	public boolean queryDrop(IFOContinuousQuery query) {
		return queries.remove(query);
	}

	@Override
	public void queryClear() {
		queries.clear();
	}

	@Override
	public void foClear() {
		for(FOContinuousQuery q : queries)
			q.foClear();
	}
	
	@Override
	public Collection<FlexOffer> foGetAll() {
		Set<FlexOffer> foSet = new HashSet<FlexOffer>();
		for(FOContinuousQuery q : queries)
			foSet.addAll(q.foGetMicroFOs());
		return foSet;
	}

	@Override
	public void foAdd(FlexOffer fo) {
		List<FlexOffer> fl = new ArrayList<FlexOffer>();
		fl.add(fo);
		for(FOContinuousQuery q : queries)
			q.foAdd(fl);
	}

	@Override
	public void foAdd(List<FlexOffer> fl) {
		for(FOContinuousQuery q : queries)
			q.foAdd(fl);
	}

	@Override
	public void foDel(FlexOffer fo) {
		List<FlexOffer> fl = new ArrayList<FlexOffer>();
		fl.add(fo);
		for(FOContinuousQuery q : queries)
			q.foDel(fl);
	}

	@Override
	public void foDel(List<FlexOffer> fl) {
		for(FOContinuousQuery q : queries)
			q.foDel(fl);
	}

	@Override
	public Collection<AggregatedFlexOffer> Aggregate(FOAggParameters aggParam, Collection<FlexOffer> fl) throws AggregationException {
		FOContinuousQuery fq = new FOContinuousQuery(buildDefaultFOAbstracter(aggParam));
		fq.foAdd(fl);
		List<AggregatedFlexOffer> rList = new ArrayList<AggregatedFlexOffer>();
		for(ChangeRecOfFlexOffer cf : fq.aggIncGetAll())
			rList.add((AggregatedFlexOffer)cf.getFlexOffer());
		
		// Update of 2013-03-07 to add all unaggregated flex-offers to the result list
		for(ChangeRecOfFlexOffer cf: fq.nonAggFOsGetAll())
		{
			// Wrap FlexOffer into DiscreteAggregatedFlexOffer
			AggregatedFlexOffer foAgg;
			foAgg = fq.getFlexOfferAbstracter().getFoNto1adder().aggregateSingleFo(cf.getFlexOffer());
			rList.add(foAgg);
		}
		
		return rList;		
	}

	@Override
	public Collection<FlexOffer> Disaggregate(AggregatedFlexOffer aggFo)
			throws AggregationException {
		return StaticFOadder.Disaggregate(aggFo);
	}
}


