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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.arrowhead.wp5.agg.api.ChangeRecOfObject;
import org.arrowhead.wp5.agg.api.ChangeType;
import org.arrowhead.wp5.agg.api.IChangeRecFactory;

/**
 *
 * A generic change tracker. It stores objects and their change flags. It allows updating flags 
 * when changes to an object occurs 
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 * @version 1.1
 * Change list (with respect to D3.3): 
 *  -  v1.1: bug-fix: the change state was set incorrectly.   
 *
 * @param <OT> type of an objects, e.g., FlexOffer, FGroup.
 * @param <CT> type of an objects's change recored, e. g., ChangeRecOfFlexOffer, ChangeRecOfGroup
 */
public class ChangeTracker <OT, CT extends ChangeRecOfObject> implements Iterable<CT> {
	// Change tracking functionality
	private LinkedHashMap<OT, CT> allChangeRecords = new LinkedHashMap<OT, CT>();
	private IChangeRecFactory<OT, CT> chRecFactory = null;
		
	public ChangeTracker(IChangeRecFactory<OT, CT> chRecFactory)
	{
		this.chRecFactory = chRecFactory;
	}	
	
	public void clearChanges()
	{
		this.allChangeRecords.clear();
	}	
	
	// Incrementally update the existing state
	public CT incUpdateChange(OT changedObject, ChangeType newCt)
	{	
		ChangeType oldCt = ChangeType.ctNone;
		CT changeRec = this.allChangeRecords.get(changedObject);				
		if (changeRec != null)
			oldCt = changeRec.getChangeType();
		else 
		{
			changeRec = this.chRecFactory.getNewChRecInstance(changedObject);
			changeRec.setChangedObject(changedObject); 
			// v1.1 change -  changeRec.setChangeType(newCt);
		}
		// v1.1 change - we set it all time, not only in condition
		changeRec.setChangeType(newCt);
			
		switch (newCt) {
		case ctAdded:
			if (oldCt == ChangeType.ctDeleted)
				changeRec.setChangeType(ChangeType.ctModified);
			break;
		case ctDeleted:
			if (oldCt == ChangeType.ctAdded)
				changeRec.setChangeType(ChangeType.ctNone);
			break;
		case ctDownsized:
			switch (oldCt) {
			case ctAdded:
				changeRec.setChangeType(ChangeType.ctAdded);
				break;
			case ctUpsized:
				changeRec.setChangeType(ChangeType.ctModified);
				break;
			default:
				break;
			}			
			break;
		case ctUpsized:
			switch (oldCt) {
			case ctAdded:
				changeRec.setChangeType(ChangeType.ctAdded); 
				break;
			case ctDownsized:
				changeRec.setChangeType(ChangeType.ctModified);
				break;
			default:
				break;
			}			
			break;
		case ctModified:
			switch(oldCt){
			case ctAdded:
				changeRec.setChangeType(ChangeType.ctAdded); 
				break;
			default:
				break;
			}
		default:
			break;			
		}
		// Store in a global change table
		if (changeRec.getChangeType() == ChangeType.ctNone)
			this.allChangeRecords.remove(changedObject);
		else
			this.allChangeRecords.put(changedObject, changeRec);
		
		return changeRec;
	}
	
	public void forgetChange(Object o)
	{
		this.allChangeRecords.remove(o);
	}
	
	public ChangeType getChange(Object o)
	{
		ChangeRecOfObject co = this.allChangeRecords.get(o);
		if (co == null)
			return ChangeType.ctNone;
		else 
			return co.getChangeType();
	}

	@Override
	public Iterator<CT> iterator() {
		return getAllChangesLocked().iterator();
	}
	
	// We lock the original change list so that changes could be made during the enumeration  
	public Collection<CT> getAllChangesLocked()	
	{
		return new ArrayList<CT>(this.allChangeRecords.values());
	}
	
	// Don't want to lock 
	public Collection<CT> getAllChangesUnlocked()	
	{
		return this.allChangeRecords.values();
	}

}
