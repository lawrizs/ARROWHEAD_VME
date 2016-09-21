package org.arrowhead.wp5.agg.optim;

public enum OptimizationObjective {
	/**
	 * Aggregator aims for least cost 
	 */
 objLowestCost,
 
 /**
  * Aggregator aims for energy balance
  */
 objEnergyBalance,
 
 /**
  * Aggregator aims for low energy 
  */
 objEnergyMinFlat,
 
 /**
  * Aggregator aims for high energy
  *  
  */
 objEnergyMaxFlat,
 
 /**
  * Aggregator aims for low energy now
  *  */
 objEnergyMinNow,
 
 /**
  * Aggregator aims for high energy now
  *  */
 objEnergyMaxNow,
 
 /**
  * Manual scheduling
  */ 
 objManual
 
 
}
