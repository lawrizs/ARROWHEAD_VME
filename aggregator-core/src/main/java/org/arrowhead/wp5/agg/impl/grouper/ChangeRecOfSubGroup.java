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


import org.arrowhead.wp5.agg.api.IChangeRecFactory;
import org.arrowhead.wp5.agg.impl.common.ChangeRecOfGroup;

/**
 * This class represent a change record of flex-offer sub-group 
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 *
 */
public class ChangeRecOfSubGroup extends ChangeRecOfGroup
{	
	public static IChangeRecFactory<FSubGroup, ChangeRecOfSubGroup> getFactorySubGroup() {
		return new IChangeRecFactory<FSubGroup, ChangeRecOfSubGroup>() {			
			@Override
			public ChangeRecOfSubGroup getNewChRecInstance(FSubGroup g) {
				return new ChangeRecOfSubGroup(g);
			}
		};
	}
	
	public ChangeRecOfSubGroup(FSubGroup g)
	{
		super(g);
		this.setChangedObject(g);
	}
	
	@Override
	public FSubGroup getGroup()
	{
		return (FSubGroup) this.getChangedObjectAs();
	}
	
	public void setGroup(FSubGroup grp)
	{
		this.setChangedObject(grp);
	}
}