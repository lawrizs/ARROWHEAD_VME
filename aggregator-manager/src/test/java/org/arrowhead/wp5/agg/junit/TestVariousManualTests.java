package org.arrowhead.wp5.agg.junit;

/*-
 * #%L
 * ARROWHEAD::WP5::Aggregator Manager
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



import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class TestVariousManualTests extends TestCase {

	@Override
	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void testDummy()
	{
		assertTrue(true);
	}

//	@Test
//	public void test1() throws AggregationException
//	{
//		FOAggregation foa = new FOAggregation();
//		FOAggParameters p = new FOAggParameters();
//		p.getConstraintPair().durationToleranceType = ConstraintType.acNotSet;
//		p.getConstraintPair().durationTolerance = 100;
//		p.getConstraintPair().startAfterToleranceType = ConstraintType.acSet;
//		p.getConstraintPair().startAfterTolerance = 1;
//		p.getConstraintPair().startBeforeToleranceType = ConstraintType.acNotSet;
//		p.getConstraintPair().startBeforeTolerance = 1;
//		p.getConstraintAggregate().valueMin = 10;
//		p.getConstraintAggregate().valueMax = 20;
//		p.getConstraintAggregate().aggConstraint = ConstraintAggregateType.acNotSet;
//		FOContinuousQuery q =  foa.queryCreate(p);
//		//System.out.println("Nr of dimensions:"+q.getNumOfDimensions());
//		q.aggIncStart();
//		System.out.println("Incremental query processing activated!");
//		System.out.println("Loading of workload stated!");
//		
//		for(int i=0; i<50000; i++)
//		{
//			DiscreteFlexOffer f = new DiscreteFlexOffer();
//			f.setStartAfterInterval((int) (i*0.001));
//			f.setStartBeforeInterval((int) (Math.random()*0.1));
//
//			foa.foAdd(f);
//			if (i % 2 == 0) 
//			{
//				foa.foDel(f);		
//			}
//
//			if ( i == 50000-1)
//			{
//				for(ChangeRecOfFlexOffer c : q.aggIncGetChanges())
//				{
//					System.out.println("Flex-offer "+c.getFlexOffer().hashCode()+ " "+ c.getChangeType()+ " weight:"+ c.getFlexOffer().getSumEnergyConstraints().getLower());
//				}
//				
//				for(ChangeRecOfFlexOffer n : q.nonAggFOsGetChanges())
//				{	
//					System.out.println("Non-aggregated "+n.getFlexOffer().hashCode()+ " "+ n.getChangeType()+ " weight:"+ n.getFlexOffer().getSumEnergyConstraints().getLower());
//				}
//				q.aggIncReset();
//				System.out.println("******* Commit ********");
//			}		
//		}
//		
//		System.out.println("Sequential query processing started");
//		System.out.println("All aggregated flex-offers");
//		for(ChangeRecOfFlexOffer c : q.aggIncGetAll())
//		{
//			System.out.println("Flex-offer "+c.getFlexOffer().hashCode()+ " "+ c.getChangeType()+ " weight:"+ c.getFlexOffer().getSumEnergyConstraints().getLower());
//		}
//		
//		System.out.println("All non-aggregated flex-offers");
//		for(ChangeRecOfFlexOffer n : q.nonAggFOsGetAll())
//		{	
//			System.out.println("Non-aggregated "+n.getFlexOffer().hashCode()+ " "+ n.getChangeType()+ " weight:"+ n.getFlexOffer().getSumEnergyConstraints().getLower());
//		}
//		
//		System.out.println("Done!");
//
//	}
}
