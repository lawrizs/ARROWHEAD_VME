package org.arrowhead.wp5.agg.api;

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


/**
 * This class represent a change record of an (general) object 
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 *
 */
public class ChangeRecOfObject {
	private Object changedObject = null;
	private ChangeType changeType = ChangeType.ctNone;
	
	public void setChangeType(ChangeType changeType) {
		this.changeType = changeType;
	}
	
	public ChangeType getChangeType() {
		return changeType;
	}
	
	public void setChangedObject(Object changedObject) {
		this.changedObject = changedObject;
	}
	
	public Object getChangedObject() {
		return changedObject;
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T getChangedObjectAs ()
	{
		return (T) this.changedObject ;
	}

	/* 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((changeType == null) ? 0 : changeType.hashCode());
		result = prime * result
				+ ((changedObject == null) ? 0 : changedObject.hashCode());
		return result;
	}

	/* 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChangeRecOfObject other = (ChangeRecOfObject) obj;
		if (changeType != other.changeType)
			return false;
		if (changedObject == null) {
			if (other.changedObject != null)
				return false;
		} else if (!changedObject.equals(other.changedObject))
			return false;
		return true;
	}
	
}
