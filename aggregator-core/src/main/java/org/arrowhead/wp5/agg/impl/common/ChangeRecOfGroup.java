package org.arrowhead.wp5.agg.impl.common;

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
import java.util.List;

import org.arrowhead.wp5.agg.api.ChangeRecOfObject;
import org.arrowhead.wp5.agg.api.ChangeType;
import org.arrowhead.wp5.agg.api.IChangeRecFactory;
import org.arrowhead.wp5.core.entities.FlexOffer;

/**
 * This class represent a change record of a flex-offer group and sets of flex-offers that 
 * were added or removed to/from the group
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 *
 */
public class ChangeRecOfGroup extends ChangeRecOfObject
{	
	public static IChangeRecFactory<FGroup, ChangeRecOfGroup> getFactory() {
		return new IChangeRecFactory<FGroup, ChangeRecOfGroup>() {			
			@Override
			public ChangeRecOfGroup getNewChRecInstance(FGroup g) {
				return new ChangeRecOfGroup(g);
			}
		};
	}
	
	public ChangeRecOfGroup(FGroup g)
	{
		this.setGroup(g);
	}
	
	// List of added/removed flex-offers, in case of upsized and downsized
	private List<FlexOffer> foAdded = null;
	private List<FlexOffer> foRemoved = null;
	
	public void foWasAdded(FlexOffer f) {
		if (this.getChangeType() == ChangeType.ctUpsized || 
				this.getChangeType() == ChangeType.ctDownsized || 
				this.getChangeType() == ChangeType.ctModified) {
			if (this.foRemoved!=null && this.foRemoved.contains(f))
				foRemoved.remove(f);
			else 
			{
				if (this.foAdded == null)
					this.foAdded = new ArrayList<FlexOffer>();
				this.foAdded.add(f);
			}
		}
	}
	
	public void foWasRemoved(FlexOffer f) {
		if (this.getChangeType() == ChangeType.ctUpsized || 
				this.getChangeType() == ChangeType.ctDownsized || 
				this.getChangeType() == ChangeType.ctModified) {
			if (this.foAdded!=null && this.foAdded.contains(f))
				foAdded.remove(f);
			else 
			{
				if (this.foRemoved == null)
					this.foRemoved = new ArrayList<FlexOffer>();
				this.foRemoved.add(f);
			}
		}
	}
	
	public FGroup getGroup()
	{
		return this.getChangedObjectAs();
	}
	
	public void setGroup(FGroup grp)
	{
		this.setChangedObject(grp);
	}
	
	// When a group is upsized, downsized, or modified there return added/removed flex-offers
	public List<FlexOffer> getAddedFlexOffers()
	{
		return this.foAdded;
	}
	
	public List<FlexOffer> getRemovedFlexOffers()
	{
		return this.foRemoved;
	}
}