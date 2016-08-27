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
import java.util.List;

import junit.framework.TestCase;

import org.arrowhead.wp5.agg.api.ChangeRecOfFlexOffer;
import org.arrowhead.wp5.agg.impl.common.AggParamAbstracter;
import org.arrowhead.wp5.agg.impl.foaggregation.FOAggregation;
import org.arrowhead.wp5.agg.impl.foaggregation.FOContinuousQuery;
import org.arrowhead.wp5.core.entities.AggregatedFlexOffer;
import org.arrowhead.wp5.core.entities.AggregatedFlexOfferMetaData;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.junit.Before;

public class TestAC extends TestCase {
	private static final double AllowedDoubleError = 1E-5;
	private AggParamAbstracter fa2D;
	private FOAggregation ac;
	private FOContinuousQuery cq;
	private List<FlexOffer>  fl;
	private double totalWeight = 0;

	
	@Override
	@Before
	public void setUp() throws Exception {
		this.fa2D = UtilsFoDummies.generate2DflexofferAbstracter();
		this.ac = new FOAggregation();
				
		this.cq = ac.queryCreate(this.fa2D);
	
		// Generate flex-offers
		this.fl = UtilsFoDummies.generateUniform2DFlexOffers(1234, 10000, -1, 1, -1, 1, 0, 100);
		// Total weight
		this.totalWeight = 0;
		for(FlexOffer f : fl) 
			this.totalWeight += fa2D.getWeightDlg().getWeight(f);
	}

	@SuppressWarnings("unused")
	private void childsCheckExistance(AggregatedFlexOffer af)
	{
		if (af.getSubFoMetas() != null)
			for (AggregatedFlexOfferMetaData mf : af.getSubFoMetas())
				assertTrue(this.fl.contains(mf.getSubFlexOffer()));		
	}
	
	private void childsRemoveFromList(AggregatedFlexOffer af, List<FlexOffer> fl)
	{
		if (af.getSubFoMetas() != null)
			for (AggregatedFlexOfferMetaData mf : af.getSubFoMetas()) {
				assertTrue(fl.contains(mf.getSubFlexOffer()));
				fl.remove(mf.getSubFlexOffer());
			}
	}
	
	
	public void testSimpeFOadd()
	{	
		this.ac.foClear();
		this.cq.aggIncStart();		
		this.ac.foAdd(fl);
		
		double incWeight = 0;
		//List<FlexOffer> aggList = new ArrayList<FlexOffer>(this.fl);		
		for (ChangeRecOfFlexOffer f : this.cq.aggIncGetChanges())
		{
			// Check compare flex-offer lists
			//this.childsCheckExistance((DiscreteAggregatedFlexOffer) f.getFlexOffer());
			this.childsRemoveFromList((AggregatedFlexOffer)f.getFlexOffer(), fl);						
			// Check weight balance
			incWeight += this.fa2D.getWeightDlg().getWeight(f.getFlexOffer());
		}	
		assertEquals(0, fl.size());
		
		this.cq.aggIncReset();
		
		assertEquals(this.totalWeight, incWeight, AllowedDoubleError);
	}
	
	public void testSimpleFoModifications()
	{
		this.ac.foClear();
		this.cq.aggIncStart();		
			
		double incWeight = 0;
		
		List<FlexOffer> foAggregated = new ArrayList<FlexOffer>();
		
		for (int i= 0; i< 15; i++)
		{
			
			FlexOfferModStats stats = UtilsFoDummies.generateRandomModifications(this.fa2D, this.fl, this.ac, 1000, 0.5);
			incWeight += stats.addedWeight - stats.removedWeight;
			
			foAggregated.addAll(stats.addedFOS);
			foAggregated.removeAll(stats.removedFOS);
			
			List<FlexOffer> foAggregated2 = new ArrayList<FlexOffer>(foAggregated);
			double aggWeight = 0;
			for (ChangeRecOfFlexOffer f : this.cq.aggIncGetAll())
			{
				aggWeight += fa2D.getWeightDlg().getWeight(f.getFlexOffer());		
				
				// For checking if aggregated flex-offers contain all micro flex-offers
				childsRemoveFromList((AggregatedFlexOffer)f.getFlexOffer(), foAggregated2);
			}
			assertEquals(0, foAggregated2.size());
			
			this.cq.aggIncReset();
			

			assertEquals(incWeight, aggWeight, AllowedDoubleError);
		}		
	}
	
}
