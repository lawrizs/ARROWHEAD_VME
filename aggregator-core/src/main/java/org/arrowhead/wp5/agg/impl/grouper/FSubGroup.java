package org.arrowhead.wp5.agg.impl.grouper;

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
import java.util.Iterator;
import java.util.List;

import org.arrowhead.wp5.agg.impl.common.FGroup;
import org.arrowhead.wp5.core.entities.FlexOffer;

/**
 * A sub-group is a partition of a super-group. It is a set of similar flex-offers that collectively 
 * satisfy a weight bound criteria, e.g., their total time flexibility is in a given range. 
 *  
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 *
 */
public class FSubGroup extends FGroup {
	private static int SUBGROUP_AUTO_COUNTER = 0;
	private int gID;
	private List<FlexOffer> fol = new ArrayList<FlexOffer>();
	
	public FSubGroup()
	{
		this.gID = SUBGROUP_AUTO_COUNTER++;
	}
	
	public List<FlexOffer> getFlexOfferList()
	{
		return this.fol;
	}
	
	public void addFlexOffers(Iterable<FlexOffer> fl)
	{
		for(FlexOffer f : fl)
			fol.add(f);
	}
	
	public void addFlexOffer(FlexOffer f)
	{
		this.fol.add(f);
	}
	
	public void removeFlexOffer(FlexOffer f)
	{
		this.fol.remove(f);
	}
	
	public void clearFlexOffers()
	{
		this.fol.clear();
	}

	@Override
	public Iterator<FlexOffer> iterator() {
		return fol.iterator();
	}

	@Override
	public int getGId() {
		return this.gID;	// Pseudo groupId
	}		
}	

// Bin packed groups
class FSubGroupList extends ArrayList<FSubGroup>
{	
	private static final long serialVersionUID = 1L;
	
	private FGroup superGroup = null;

	
	public FSubGroupList(FGroup superGroup)
	{
		this.superGroup = superGroup;
	}
	
	public FGroup getSuperGroup() {
		return superGroup;
	}

	// Important to keep track of flex-offers, not added to subgroups
	public Iterable<FlexOffer> getNonBinPackedFlexOffers() {
		List<FlexOffer> nFos = new ArrayList<FlexOffer>();
		for (FlexOffer f : this.superGroup)
		{
			boolean found = false;
			for(FSubGroup g: this)
				if (g.getFlexOfferList().contains(f))
				{
					found = true;
					break;
				}
			if (!found)
				nFos.add(f);
		}		
		return nFos;
	}	

}
