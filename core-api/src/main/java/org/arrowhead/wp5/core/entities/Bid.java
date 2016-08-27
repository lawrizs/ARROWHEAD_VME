package org.arrowhead.wp5.core.entities;

/*-
 * #%L
 * ARROWHEAD::WP5::Core Data Structures
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

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="marketBid")
@XmlAccessorType(XmlAccessType.FIELD)
public class Bid implements Serializable, Comparable<Bid> {
	private static final long serialVersionUID = -1493128027690766604L;
	long price;
	long quantity;
	String owner = null;
	String id;
	boolean isUp;
	double winPrice;
	double winQuantity;
	
	public double getWinPrice() {
		return winPrice;
	}

	public void setWinPrice(double winPrice) {
		this.winPrice = winPrice;
	}

	public double getWinQuantity() {
		return winQuantity;
	}

	public void setWinQuantity(double winQuantity) {
		this.winQuantity = winQuantity;
	}

	public void incrWinQuantity(double winQuantity) {
		this.winQuantity += winQuantity;
	}

	public Bid() {
	}
	
	public Bid(long price, long quantity, boolean isUp, String owner, String id) {
		this.price = price; // DKK 
		this.quantity = quantity;
		this.isUp = isUp;
		this.owner = owner;
		this.id = id;
	}
	
	public boolean isUp() {
		return isUp;
	}
	public void setUp(boolean isUp) {
		this.isUp = isUp;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getPrice() {
		return price;
	}
	
	public void setPrice(long price) {
		this.price = price;
	}
	
	public long getQuantity() {
		return quantity;
	}
	
	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}
	
	@Override
	public String toString() {
		return "Bid " + price + ",- " + quantity + "kWh - Owner: " + owner + " id: " + id;
	}

	@Override
	public int compareTo(Bid o) {
		if (this.price < o.price)
			return -1;
		if (this.price > o.price) 
			return 1;
		return 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
	    if (obj == this) return true;
	    if (!(obj instanceof Bid))return false;
	    Bid other = (Bid)obj;
	    if (this.price != other.price)
	    	return false;
	    if (this.quantity != other.quantity)
	    	return false;
	    if (this.isUp != other.isUp)
	    	return false;
	    if (this.owner != other.owner) {
	    	if (this.owner != null && !this.owner.equals(other.owner))
	    		return false;
	    }
	    if (this.id != other.id) {
	    	if (this.id != null && !this.id.equals(other.id))
	    		return false;
	    }
		return true;
	}
}
