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

import org.arrowhead.wp5.agg.api.AggregationException;
import org.arrowhead.wp5.agg.api.ChangeRecOfFlexOffer;
import org.arrowhead.wp5.agg.api.ChangeType;
import org.arrowhead.wp5.agg.api.FOAggParameters.ProfileShape;
import org.arrowhead.wp5.agg.impl.common.ChangeRecOfGroup;
import org.arrowhead.wp5.agg.impl.common.FGroup;
import org.arrowhead.wp5.agg.impl.staticAggregator.StaticFOadder;
import org.arrowhead.wp5.core.entities.AggregatedFlexOffer;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferSchedule;
import org.arrowhead.wp5.core.entities.FlexOfferSlice;
import org.junit.Before;
import org.junit.Test;


public class TestStaticFOadder  extends TestCase {
	private static final double AllowedDoubleError = 1E-5;
	private StaticFOadder sa;
	
	public TestStaticFOadder(){
		try {
			this.sa = new StaticFOadder(ProfileShape.psAlignStart);
		} catch (AggregationException e) {
			fail(e.getMessage());			
		}
	}
	
	@Override
	@Before
	public void setUp() throws Exception {
	}

	private double calcTotalLowerEnergyInProfile(FlexOffer f)
	{
		double sum = 0;
		for (FlexOfferSlice i : f.getSlices())
		{
			sum += i.getEnergyLower();
		}
		return sum;
	}
	
	private double calcTotalUpperEnergyInProfile(FlexOffer f)
	{
		double sum = 0;
		for (FlexOfferSlice i : f.getSlices())
		{
			sum += i.getEnergyUpper();
		}
		return sum;
	}
	
	private void checkConsistencyOfAggregate(FGroup subFoGrp, FlexOffer fAgg)
	{		
		// Check balances
		double val = Double.MAX_VALUE;
		for(FlexOffer f : subFoGrp)
			val = Math.min(val, f.getAcceptanceBeforeInterval());
		assertEquals(val, fAgg.getAcceptanceBeforeInterval(), AllowedDoubleError);
		
		val = Double.MAX_VALUE;;
		for(FlexOffer f : subFoGrp)
			val = Math.min(val, f.getAssignmentBeforeInterval());
		assertEquals(val, fAgg.getAssignmentBeforeInterval(), AllowedDoubleError);	
		
		// 2010-01-27 update
		val = -Double.MAX_VALUE;
		for(FlexOffer f : subFoGrp)
			val = Math.max(val, f.getAssignmentBeforeDurationIntervals());
		assertEquals(val, fAgg.getAssignmentBeforeDurationIntervals(), AllowedDoubleError);
//			
//		val = -Double.MAX_VALUE;
//		for(FlexOffer f : subFoGrp)
//			val = Math.max(val, f.getMinTariff());
//		assertEquals(val, fAgg.getMinTariff(), AllowedDoubleError);
		
		val = Double.MAX_VALUE;
		for(FlexOffer f : subFoGrp)
			val = Math.min(val, f.getStartAfterInterval());
		assertTrue(fAgg.getStartAfterInterval()>=val);
		
		val = -Double.MAX_VALUE;
		for(FlexOffer f : subFoGrp)
			val = Math.max(val, f.getStartBeforeInterval());
		assertTrue(fAgg.getStartBeforeInterval()<=val);
		
		// Don't check balance, only existance of total constraints
		for(FlexOffer f : subFoGrp)
			assertTrue(f.getTotalEnergyConstraint()==null ? fAgg.getTotalEnergyConstraint() == null :
															fAgg.getTotalEnergyConstraint() != null);
		// Check profiles
		val = 0;
		for(FlexOffer f : subFoGrp)
			val += calcTotalLowerEnergyInProfile(f);		
		assertEquals(val, calcTotalLowerEnergyInProfile(fAgg), AllowedDoubleError);

		val = 0;
		for(FlexOffer f : subFoGrp)
			val += calcTotalUpperEnergyInProfile(f);		
		assertEquals(val, calcTotalUpperEnergyInProfile(fAgg), AllowedDoubleError);		
	}
	
	private DummyFoGroup generateRealisticFOs(int count)
	{
		DummyFoGroup grp = new DummyFoGroup();
		
		if (count>0)
			grp.getFoList().add(
					UtilsFlexOffers.generateRealisticFlexOffer( 
												0, // creationTime 
												2, // timeFlexibility
												10, // priceForEnergyUnit
												1,  // energyFlexibility
												new int []    {1, 2, 1, 3, 1}, // enIntDurations 
												new double [] {1, 2, 3, 4, 5})); // enIntAmounts
		
		if (count>1)
			grp.getFoList().add(
					UtilsFlexOffers.generateRealisticFlexOffer( 
												1, // creationTime 
												2, // timeFlexibility
												10, // priceForEnergyUnit
												1,  // energyFlexibility
												new int []    {2, 2, 2, 2, 2}, // enIntDurations 
												new double [] {2, 2, 2, 2, 2})); // enIntAmounts
		
		
		if (count>2)
			grp.getFoList().add(
					UtilsFlexOffers.generateRealisticFlexOffer( 
												-5, // creationTime 
												10, // timeFlexibility
												12, // priceForEnergyUnit
												2,  // energyFlexibility
												new int []    {3, 1, 3, 3, 2}, // enIntDurations 
												new double [] {2, 2, 2, 2, 2})); // enIntAmounts
		

		if (count>3)
			grp.getFoList().add(
					UtilsFlexOffers.generateRealisticFlexOffer( 
												6, // creationTime 
												4, // timeFlexibility
												11.5, // priceForEnergyUnit
												2,  // energyFlexibility
												new int []    {2, 2, 1, 1, 5}, // enIntDurations 
												new double [] {2, 2, 2, 2, 2})); // enIntAmounts

		if (count>4)
			grp.getFoList().add(
					UtilsFlexOffers.generateRealisticFlexOffer( 
												-5, // creationTime 
												4, // timeFlexibility
												11.5, // priceForEnergyUnit
												2,  // energyFlexibility
												new int []    {1, 1, 1, 1, 1, 1, 1, 1, 1}, // enIntDurations 
												new double [] {3, 3, 3, 3, 3, 3, 3, 3, 3})); // enIntAmounts
		
		if (count > 5)
			fail("Generation of more than 5 flex-offer is not supported");
		
		return grp;
	}
	
	@Test
	public void test2FoInclusionManual() {
		DummyFoGroup gpr = generateRealisticFOs(2);
		ChangeRecOfGroup cg= new ChangeRecOfGroup(gpr);
		cg.setChangeType(ChangeType.ctAdded);
		ChangeRecOfFlexOffer ca = sa.incUpdateAggregate(cg);
		checkConsistencyOfAggregate(gpr, ca.getFlexOffer());
	}
	
	@Test
	public void testFoPowersetInclusionManual() {
		List<FGroup> grps = Utils.generateFGroupPowerSet(generateRealisticFOs(5));
		for (FGroup g : grps)		
		{
			ChangeRecOfGroup cg= new ChangeRecOfGroup(g);
			cg.setChangeType(ChangeType.ctAdded);
	 		ChangeRecOfFlexOffer ca = sa.incUpdateAggregate(cg);
	 		
	 		//System.out.println("Checking consistency of group: "+grps.indexOf(g));
	
			checkConsistencyOfAggregate(g, ca.getFlexOffer());
		}
	}

	@Test
	public void testFOexclusion() {
	 
		DummyFoGroup grp = generateRealisticFOs(5);
		// Safe one flex-offer for future use
		FlexOffer fo = grp.getFoList().get(4);
		grp.getFoList().remove(4);
		
		// First, aggregate as usual
		ChangeRecOfGroup cg= new ChangeRecOfGroup(grp);
		cg.setChangeType(ChangeType.ctAdded);
 		ChangeRecOfFlexOffer ca = sa.incUpdateAggregate(cg);
 		checkConsistencyOfAggregate(grp, ca.getFlexOffer());
		
 		// Try to remove and add some items
 		cg.setChangeType(ChangeType.ctModified);
 		cg.foWasRemoved(grp.getFoList().get(0));
 		grp.getFoList().remove(0);
 		cg.foWasAdded(fo);
 		grp.getFoList().add(fo); 		
 		ca = sa.incUpdateAggregate(cg);
 		 		
 		checkConsistencyOfAggregate(grp, ca.getFlexOffer());
	}
	
	@Test
	public void testAggregationDisaggregation() throws AggregationException
	{
		List<FGroup> grps = Utils.generateFGroupPowerSet(generateRealisticFOs(5));
		for (FGroup g : grps)		
		{	
			//if (grps.indexOf(g) != 5) continue;
			ChangeRecOfGroup cg= new ChangeRecOfGroup(g);
			cg.setChangeType(ChangeType.ctAdded);
	 		ChangeRecOfFlexOffer ca = sa.incUpdateAggregate(cg);
	 		AggregatedFlexOffer aggFo = (AggregatedFlexOffer)ca.getFlexOffer();
	 		
	 		// Generate a random fix for the aggregate
	 		FlexOfferSchedule fa = UtilsFlexOffers.getRandomFix(aggFo);
	 		assert(fa.isCorrect(ca.getFlexOffer()));
	 		
	 		double enBalance = UtilsFlexOffers.calcTotalAssignedEnergy(fa); 
	 		
	 		List<FlexOffer> fos = StaticFOadder.Disaggregate(aggFo);
	 		for(FlexOffer fo : fos)
	 		{
	 			if (!fo.getFlexOfferSchedule().isCorrect(fo))
	 			{
	 				System.out.println("Combination that failed: "+ grps.indexOf(g));
	 			}
	 			assertTrue(fo.getFlexOfferSchedule().isCorrect(fo));
	 			enBalance -= UtilsFlexOffers.calcTotalAssignedEnergy(fo.getFlexOfferSchedule()); 			
	 		}
	 		assertEquals(enBalance, 0, AllowedDoubleError);
		}
	}

}
