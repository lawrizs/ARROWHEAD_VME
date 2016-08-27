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



import java.util.List;

import junit.framework.TestCase;

import org.arrowhead.wp5.agg.impl.common.AggParamAbstracter;
import org.arrowhead.wp5.agg.impl.common.FGridRectangle;
import org.arrowhead.wp5.agg.impl.common.FGroup;
import org.arrowhead.wp5.agg.impl.grouper.FGridCellPopulated;
import org.arrowhead.wp5.agg.impl.grouper.FGridMapper;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.junit.Before;
import org.junit.Test;


public class TestFGridMapper extends TestCase {
	private static final double AllowedDoubleError = 1E-5;
	@SuppressWarnings("unused")
	private double totalWeight = 0;
	private AggParamAbstracter fa2D;
	private FGridMapper gm;
	private List<FlexOffer>  fl; 
	
	@Override
	@Before
	public void setUp() throws Exception {		
		this.fa2D = UtilsFoDummies.generate2DflexofferAbstracter();				
		this.gm = new FGridMapper(fa2D);
		this.fl = UtilsFoDummies.generateUniform2DFlexOffers(1234, 10000, -1, 1, -1, 1, 0, 100);
		
		// Total weight
		this.totalWeight = 0;
		for(FlexOffer f : fl) 
			this.totalWeight += fa2D.getWeightDlg().getWeight(f);
	}

	@Test
	public void testCellCountsOnInserts()
	{	
		gm.foClear();
		// Add dummy flex-offer so that dimensions will be aligned to point (0,0)
		gm.foAdd(UtilsFoDummies.generateUniform2DFlexOffers(1234, 1, 0, 0, 0, 0, 0, 0));
		gm.foAdd(fl);
		
		int cntCells = 0;
		int cntFo = 0;
		for(FGridCellPopulated c : gm)
		{
			cntCells ++;
			for(@SuppressWarnings("unused") FlexOffer f : c)
				cntFo ++;
			List<FGroup> l = c.getNeighbours();
			assertEquals(3, l.size());
		}		
		assertEquals(4, cntCells);	
		assertEquals(fl.size() + 1, cntFo);
	}
	
	@Test
	public void testCellCountsOnModify()
	{	
		gm.foClear();
		FlexOfferModStats stats = UtilsFoDummies.generateRandomModifications(this.fa2D, this.fl, gm, this.fl.size() * 1, 0.7);
		
		int cntFo = 0;
		for(FGridCellPopulated c : gm)
		{
			for(@SuppressWarnings("unused") FlexOffer f : c)
				cntFo ++;
		}		
		assertEquals(stats.addedFOS.size() - stats.removedFOS.size(), cntFo);	
	}
	
	@Test
	public void testWeightBalance()
	{	
		gm.foClear();		
		FlexOfferModStats stats = UtilsFoDummies.generateRandomModifications(this.fa2D, this.fl, gm, this.fl.size() * 1, 0.7);
		
		double weightCells = 0; 
		for(FGridCellPopulated c : gm)
		{
			double weightFOs = 0;
			for(FlexOffer f : c)					
				weightFOs += this.fa2D.getWeightDlg().getWeight(f);
			assertEquals(weightFOs, c.getGroupWeight(fa2D), AllowedDoubleError);	
			weightCells += c.getGroupWeight(fa2D);
		}
		assertEquals(weightCells, stats.addedWeight - stats.removedWeight, AllowedDoubleError);		
	}
	
	@Test
	public void testMBRs()
	{
		gm.foClear();		
		UtilsFoDummies.generateRandomModifications(this.fa2D, this.fl, gm, this.fl.size() * 1, 0.7);

		for(FGridCellPopulated c : gm)
		{
			FGridRectangle r = new FGridRectangle(2);
			r.clearRectangle();
			for(FlexOffer f : c)
				r.extendMBR(this.fa2D.getFlexOfferVector(f));
	
			FGridRectangle r2 = c.getMBR(this.fa2D);
			
			for(int i=0; i<2; i++)
			{
				assertEquals(r.getVecHigh().getValues()[i], r2.getVecHigh().getValues()[i], AllowedDoubleError);
				assertEquals(r.getVecLow().getValues()[i], r2.getVecLow().getValues()[i], AllowedDoubleError);
			}
		}	
	}
	
	public static void main(String[] args) {
	        junit.textui.TestRunner.run(TestFGridMapper.class);
	}

}
