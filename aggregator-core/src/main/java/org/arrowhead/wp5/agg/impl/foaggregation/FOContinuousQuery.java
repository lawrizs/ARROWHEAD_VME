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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.arrowhead.wp5.agg.api.ChangeRecOfFlexOffer;
import org.arrowhead.wp5.agg.api.FOCQcallbackParams;
import org.arrowhead.wp5.agg.api.IFOCQcallback;
import org.arrowhead.wp5.agg.api.IFOContinuousQuery;
import org.arrowhead.wp5.agg.impl.common.AggParamAbstracter;
import org.arrowhead.wp5.agg.impl.common.ChangeRecOfGroup;
import org.arrowhead.wp5.agg.impl.common.IFlexOfferNto1Adder;
import org.arrowhead.wp5.agg.impl.common.IteratorAsIterable;
import org.arrowhead.wp5.agg.impl.common.IteratorEmpty;
import org.arrowhead.wp5.agg.impl.common.IteratorNested;
import org.arrowhead.wp5.agg.impl.common.IteratorNested.InnerGenerator;
import org.arrowhead.wp5.agg.impl.common.IteratorSingleElement;
import org.arrowhead.wp5.agg.impl.common.IteratorTransforming;
import org.arrowhead.wp5.agg.impl.grouper.FGridMapper;
import org.arrowhead.wp5.agg.impl.grouper.FGroupBuilderIncremental;
import org.arrowhead.wp5.agg.impl.grouper.FGroupOptimizer;
import org.arrowhead.wp5.agg.impl.grouper.FGroupOptimizer.OptimizationParams;
import org.arrowhead.wp5.agg.impl.grouper.FSubgrpBuilderWgtBounded;
import org.arrowhead.wp5.agg.impl.grouper.IFSubgrpBuilder;
import org.arrowhead.wp5.core.entities.AggregatedFlexOffer;
import org.arrowhead.wp5.core.entities.FlexOffer;



/**
 * It offers an implementation of IFOContinuousQuery
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 * @see IFOContinuousQuery
 *
 */
public class FOContinuousQuery implements IFOContinuousQuery {
	private boolean queryEnabled = true;
	private FGridMapper gMapper; // A micro flex-offer pool
	private IFlexOfferNto1Adder foNto1Adder;	// A macro flex-offer pool
	private AggParamAbstracter aggParAbstracter;
	private	FGroupOptimizer fgOptimizer;
	private FGroupBuilderIncremental grpBuilder;	
	private IFSubgrpBuilder subgrpBuilder;
	private List<FOChangeNotifier> foChangeNotifiers = null;

	protected FGridMapper getGMapper()
	{
		return this.gMapper;
	}
	
	protected AggParamAbstracter getFlexOfferAbstracter()
	{
		return this.aggParAbstracter;
	}

	protected FOContinuousQuery(AggParamAbstracter aggParAbstracter) {
		this.aggParAbstracter = aggParAbstracter;			
		this.gMapper = new FGridMapper(this.aggParAbstracter);
		if (aggParAbstracter.isWeightBoundEnabled())
			this.subgrpBuilder = new FSubgrpBuilderWgtBounded(this.aggParAbstracter, 
															  aggParAbstracter.getBinPackerParams());
		else 
			this.subgrpBuilder = null;
				
		// Initialize group optimizer
		this.fgOptimizer = new FGroupOptimizer(new OptimizationParams(), this.aggParAbstracter);
		
		this.grpBuilder = new FGroupBuilderIncremental(this.gMapper);
		//	Initialize macro flex-offer pool
		this.foNto1Adder = this.aggParAbstracter.getFoNto1adder(); 
		
		this.gMapper.setChangeTracker(this.grpBuilder);	
	}

	protected void foAdd(Collection<FlexOffer> fl) {
		if (!this.queryEnabled)
			return;
		gMapper.foAdd(fl);
		
		// Update the listeners
		if (this.foChangeNotifiers != null)
			for (FOChangeNotifier l : this.foChangeNotifiers)
				l.updateSums(fl);
	}

	protected void foDel(Collection<FlexOffer> fl) {
		if (!this.queryEnabled)
			return;
		gMapper.foDel(fl);
		
		// Update the listeners
		if (this.foChangeNotifiers != null)
			for (FOChangeNotifier l : this.foChangeNotifiers)
				l.updateSums(fl);
	}

	protected void foClear() {
		this.gMapper.foClear();
		if (this.subgrpBuilder != null)
			this.subgrpBuilder.foClear();
		this.foNto1Adder.foClear();
		// Update the listeners
		if (this.foChangeNotifiers!=null)
			for (FOChangeNotifier l : this.foChangeNotifiers)
				l.resetSums();		
	}
	
	private Iterator<ChangeRecOfFlexOffer> incUpdateAggregate(Iterator<ChangeRecOfGroup> gc)
	{
		return new IteratorTransforming<ChangeRecOfGroup, ChangeRecOfFlexOffer>(gc)
		{
			@Override
			public ChangeRecOfFlexOffer apply(ChangeRecOfGroup gc)
			{
				return foNto1Adder.incUpdateAggregate(gc);
			}
		};		
	}
	
	private Iterator<ChangeRecOfGroup> updateSubGroups(
			Iterator<ChangeRecOfGroup> changedSuperGroups) {
		if (this.subgrpBuilder == null)
			return changedSuperGroups;
		else
			return new IteratorNested<ChangeRecOfGroup, ChangeRecOfGroup>(
					changedSuperGroups,
					new IteratorNested.InnerGenerator<ChangeRecOfGroup, ChangeRecOfGroup>() {
						@Override
						public Iterator<ChangeRecOfGroup> generateItems(
								ChangeRecOfGroup changedSuperGroup) {
							return subgrpBuilder
									.updateSingleGroup(changedSuperGroup);
						}
					});
	}
	
	/**
	 * @return iterator for aggIncGetChanges
	 * @see IFOContinuousQuery#aggIncGetChanges
	 */
	public Iterator<ChangeRecOfFlexOffer> aggIncGetChangesIterator() {
		// Optimize super groups
		this.grpBuilder.optimizeChangedGroups(this.fgOptimizer);
		// Chain-return iterators				
		return this.incUpdateAggregate(
			       this.updateSubGroups(
			    		  this.grpBuilder.getIteratorOfChangesUnlocked()));	
	}
	
	/**
	 * @return iterator for aggIncGetAll
	 * @see IFOContinuousQuery#aggIncGetAll
	 */
	public Iterator<ChangeRecOfFlexOffer> aggIncGetAllIterator() {
		final HashMap<FlexOffer, ChangeRecOfFlexOffer> foCngFOs = new HashMap<FlexOffer, ChangeRecOfFlexOffer>();
		Iterator<ChangeRecOfFlexOffer> foCngIter = this.aggIncGetChangesIterator();
		while(foCngIter.hasNext())
		{
			ChangeRecOfFlexOffer foCng = foCngIter.next();
			foCngFOs.put(foCng.getFlexOffer(), foCng);
		}
		
		return new IteratorNested<AggregatedFlexOffer, ChangeRecOfFlexOffer>(
				this.foNto1Adder.getAllAggregates(), new InnerGenerator<AggregatedFlexOffer, ChangeRecOfFlexOffer>() {
					@Override
					public Iterator<ChangeRecOfFlexOffer> generateItems(
							AggregatedFlexOffer aggFo) {
						ChangeRecOfFlexOffer foCng = foCngFOs.get(aggFo);
						if (foCng != null)
							return new IteratorSingleElement<ChangeRecOfFlexOffer>(foCng);
						else 
							return new IteratorSingleElement<ChangeRecOfFlexOffer>(new ChangeRecOfFlexOffer(aggFo));
					}
				});
	}	
	
	/**
	 * @return iterator for nonAggFOsGetChanges
	 * @see IFOContinuousQuery#nonAggFOsGetChanges
	 */
	public Iterator<ChangeRecOfFlexOffer> nonAggFOsGetChangesIterator()
	{
		if (this.subgrpBuilder instanceof FSubgrpBuilderWgtBounded)
			return ((FSubgrpBuilderWgtBounded)this.subgrpBuilder).getNonAggregatedFOchanges();
		else 
			return new IteratorEmpty<ChangeRecOfFlexOffer>();
	}

	/**
	 * @return iterator for nonAggFOsGetAll
	 * @see IFOContinuousQuery#nonAggFOsGetAll
	 */
	public Iterator<ChangeRecOfFlexOffer> nonAggFOsGetAllIterator() {
		if (this.subgrpBuilder instanceof FSubgrpBuilderWgtBounded)
			return ((FSubgrpBuilderWgtBounded)this.subgrpBuilder).getNonAggregatedFoAll();
		else 
			return new IteratorEmpty<ChangeRecOfFlexOffer>();
	}
			
	@Override
	public void aggIncStart()
	{
		this.queryEnabled = true;
	}
	
	@Override
	public void aggIncStop()
	{
		this.queryEnabled = false;
	}
	
	@Override
	public void aggIncReset()
	{
		// Later, erase all group changes
		this.grpBuilder.resetChanges();
		if (this.subgrpBuilder != null)
			this.subgrpBuilder.resetChanges();
	}
	
	@Override
	public Iterable<ChangeRecOfFlexOffer> aggIncGetAll() {		
		return new IteratorAsIterable<ChangeRecOfFlexOffer>(this.aggIncGetAllIterator());
	}
		
	@Override
	public Iterable<ChangeRecOfFlexOffer> aggIncGetChanges()
	{
		return new IteratorAsIterable<ChangeRecOfFlexOffer>(this.aggIncGetChangesIterator());
	}

	@Override
	public Iterable<ChangeRecOfFlexOffer> nonAggFOsGetAll() {	
		return new IteratorAsIterable<ChangeRecOfFlexOffer>(this.nonAggFOsGetAllIterator());
	}
	
	@Override
	public Iterable<ChangeRecOfFlexOffer> nonAggFOsGetChanges()	
	{
		return new IteratorAsIterable<ChangeRecOfFlexOffer>(this.nonAggFOsGetChangesIterator());
	}
	
	@Override
	public Collection<FlexOffer> foGetMicroFOs() {
		List<FlexOffer> foList = new ArrayList<FlexOffer>();
		for(Iterable<FlexOffer> fgrp : this.gMapper)
			for(FlexOffer f : fgrp)
				foList.add(f);
		return foList;
	}
	
	@Override
	public void aggIncSubscribeToChanges(FOCQcallbackParams params, IFOCQcallback callback) {
		if (this.foChangeNotifiers == null)
			this.foChangeNotifiers = new ArrayList<FOChangeNotifier>();
		
		this.foChangeNotifiers.add(new FOChangeNotifier(this, params, callback));
	}


}
