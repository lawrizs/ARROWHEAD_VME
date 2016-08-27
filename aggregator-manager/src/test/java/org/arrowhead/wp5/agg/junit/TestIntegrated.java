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



import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import org.arrowhead.wp5.agg.api.AggregationException;
import org.arrowhead.wp5.agg.api.ChangeRecOfFlexOffer;
import org.arrowhead.wp5.agg.api.FOAggParameters;
import org.arrowhead.wp5.agg.api.FOAggParameters.ConstraintAggregateType;
import org.arrowhead.wp5.agg.api.FOAggParameters.ConstraintType;
import org.arrowhead.wp5.agg.api.IAggregation;
import org.arrowhead.wp5.agg.impl.foaggregation.FOAggregation;
import org.arrowhead.wp5.agg.impl.foaggregation.FOContinuousQuery;
import org.arrowhead.wp5.core.entities.AggregatedFlexOffer;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferConstraint;
import org.junit.Before;


public class TestIntegrated  extends TestCase{
	FOAggregation foa = new FOAggregation();
//	private AggParamAbstracter fa2D;
	FOAggParameters p = null;
	//private List<FlexOffer>  fl; 

	@Override
	@Before
	public void setUp() throws Exception {
	}
	
	public void testRandomAddDelete() throws AggregationException
	{
		p = new FOAggParameters();		
		p.getConstraintPair().startAfterToleranceType = ConstraintType.acSet;
		p.getConstraintPair().startAfterTolerance = 1;
		p.getConstraintPair().startBeforeToleranceType = ConstraintType.acSet;
		p.getConstraintPair().startBeforeTolerance = 1;		
		p.getConstraintAggregate().valueMin = 5;
		p.getConstraintAggregate().valueMax = 10;
		p.getConstraintAggregate().aggConstraint = ConstraintAggregateType.acFlexOfferCount;		
		
		FOContinuousQuery q =  foa.queryCreate(p);
		q.aggIncStart();		
		//System.out.println("Incremental query processing activated!");
				
		boolean hasAgg = false;
		//System.out.println("Loading of workload stated!");
		//List<FlexOffer> r = new ArrayList<FlexOffer>();
		
//		this.fa2D = UtilsFoDummies.generate2DflexofferAbstracter();				
//		this.fl = UtilsFoDummies.generateUniform2DFlexOffers(1234, 10000, -1, 1, -1, 1, 0, 100);
		
		for(int i=0; i<5000; i++)
		{
			FlexOffer f = new FlexOffer();
			UtilsFoDummies.FlexOffer_SetDim1(f, i*0.01);
			//UtilsFoDummies.FlexOffer_SetWeight(f, 1);
			
			foa.foAdd(f);
			
			if (i % 2 == 0) 
			{
				foa.foDel(f);				
			}

			if (i % 1 == 0)
			{
				for(ChangeRecOfFlexOffer c : q.aggIncGetChanges())
				{
					hasAgg = true;
					assertTrue("Weight is not in the expected range", getWeight(c)>=p.getConstraintAggregate().valueMin &&
																	  getWeight(c)<=p.getConstraintAggregate().valueMax);
					//System.out.println("Flex-offer "+c.getFlexOffer().hashCode()+ " "+ c.getChangeType()+ " weight:"+ getWeight(c));
				}
				for(@SuppressWarnings("unused") ChangeRecOfFlexOffer n : q.nonAggFOsGetChanges())
				{	
					//System.out.println("Non-aggregated "+n.getFlexOffer().hashCode()+ " "+ n.getChangeType()+ " weight:"+ getWeight(n));
				}
				q.aggIncReset();
				//System.out.println("******* Commit ********");
			}			
		}

		for(ChangeRecOfFlexOffer c : q.aggIncGetChanges())
		{
			assertTrue("Weight is not in the expected range", getWeight(c)>=p.getConstraintAggregate().valueMin &&
															  getWeight(c)<=p.getConstraintAggregate().valueMax);

			System.out.println("Group Nr. "+c+": "+c.getChangeType().toString());
		}
		q.aggIncReset();
		
		assertTrue("No flex-offers were aggregated", hasAgg);
		
//		Random ran = new Random(12345);
//		for(int i=1; i< 15; i++)
//		{
//			if ((i % 1000) == 0)
//			{
//				for (FlexOffer fr: r)
//					foa.foDel(fr);
			
//				for(FlexOfferChange c : bi)
//				{
//					System.out.println("Group Nr. "+c+": "+c.changeType.toString());
//				}
//				bi.commitChanges();
//				break;
			//}
//			int ind = i % (r.size()-1); //;ran.nextInt(r.size()-1);
			//if ((i%2)==0)
//				foa.foAdd(r.get(ind));
		//	else foa.foDel(r.get(ind));
//		}			
		//int i = 0;
		
		//System.out.println("Sequential query processing started");
//		for(FlexOffer f : q.aggregateSequentially())
//		{
//			System.out.println("Group found:");
//		}
		
		//System.out.println("Done!");	
	}
	
	/*
	 * 2013-03-07  Tests the returning of non-aggregated flex-offers
	 */
	public void testReturnOfNonAggregatedFlexOffers() throws AggregationException
	{
		p = new FOAggParameters();		
		p.getConstraintPair().startAfterToleranceType = ConstraintType.acSet;
		p.getConstraintPair().startAfterTolerance = 1;
		p.getConstraintPair().startBeforeToleranceType = ConstraintType.acSet;
		p.getConstraintPair().startBeforeTolerance = 1;		
		p.getConstraintAggregate().valueMin = 0;
		p.getConstraintAggregate().valueMax = 15;
		p.getConstraintAggregate().aggConstraint = ConstraintAggregateType.acTotalEnMax;		
		
		IAggregation agg = new FOAggregation();
		Collection<FlexOffer> list = new ArrayList<FlexOffer>();
		
		for(int i=0; i<500; i++)
		{
			FlexOffer f = new FlexOffer();			
			UtilsFoDummies.FlexOffer_SetDim1(f, i*0.01);			
			if (f.getTotalEnergyConstraint() == null)
				f.setTotalEnergyConstraint(new FlexOfferConstraint(0,0));
			f.getTotalEnergyConstraint().setUpper(20); // Aggregation will fail as 20>15
			list.add(f);
		}
		
		int cnt1 = agg.Aggregate(p, list).size();
		assertEquals("Non-aggregated flex-offers were not added to the aggregation output!", 500, cnt1);
	}
	
	private int getWeight(ChangeRecOfFlexOffer c)
	{
		if (c.getFlexOffer() instanceof AggregatedFlexOffer)
			return ((AggregatedFlexOffer) c.getFlexOffer()).getSubFoMetas().size();
		else 
			return 1;
	}

	@SuppressWarnings("unused")
	private FOAggParameters setUpAggParameters()
	{
		return UtilsFoDummies.generateAggregationParameters(1,1, 100, 500);
	}
	
}
