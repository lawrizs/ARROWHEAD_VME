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

 /* Market V2 bid representation */
@XmlRootElement(name="marketBid")
@XmlAccessorType(XmlAccessType.FIELD)
public class BidV2 implements Serializable, Comparable<BidV2> {
	private static final long serialVersionUID = -1493128027690766604L;
	
	/* Basic bid info */
	private	String owner = null;
	private String id;	
	/* Specifies if it's a selling bid */
	private boolean is_seller_bid;
	/* In Market V2, bid is represented as a FO */	
	private FlexOffer bidFlexOffer;

	
	/* If the bid is winning, this has to be initialised */
	double [] winPrices;
	double [] winQuantities;
	
	
	public FlexOffer getBidFlexOffer() {
		return bidFlexOffer;
	}

	public void setBidFlexOffer(FlexOffer bidFlexOffer) {
		this.bidFlexOffer = bidFlexOffer;
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

	public boolean isIs_seller_bid() {
		return is_seller_bid;
	}

	public void setIs_seller_bid(boolean is_seller_bid) {
		this.is_seller_bid = is_seller_bid;
	}

	public double[] getWinPrices() {
		return winPrices;
	}

	public void setWinPrices(double[] winPrices) {
		this.winPrices = winPrices;
	}

	public double[] getWinQuantities() {
		return winQuantities;
	}

	public void setWinQuantities(double[] winQuantities) {
		this.winQuantities = winQuantities;
		/* Update an associated FlexOffer schedule */
		this.bidFlexOffer.setFlexOfferSchedule(new FlexOfferSchedule(this.bidFlexOffer.getStartAfterInterval(), winQuantities));
	}

	public BidV2() {
	}
	
	/** @return A reference energy schedule (qs_ref) 
	 * */
	public double [] getRefEnergy () {		
		if (this.bidFlexOffer.getDefaultSchedule() == null)
			return null;		
		double [] s = new double [this.bidFlexOffer.getSlices().length];		
		for (int i=0; i<this.bidFlexOffer.getSlices().length; i++){
			s[i] = this.bidFlexOffer.getDefaultSchedule().getEnergyAmount(i);
		}		
		return s;
	}
	
	/**
	 * 
	 * @return A minimum bin energy schedule (-qs_down)
	 * 
	 */
	public double [] getMinEnergy () {		
		double [] s = new double [this.bidFlexOffer.getSlices().length];		
		for (int i=0; i<this.bidFlexOffer.getSlices().length; i++){
			s[i] = this.bidFlexOffer.getSlice(i).getEnergyLower();
		}		
		return s;
	}
	
	/**
	 * 
	 * @return A maximum bid energy schedule (qs_up)
	 * 
	 */
	public double [] getMaxEnergy () {		
		double [] s = new double [this.bidFlexOffer.getSlices().length];		
		for (int i=0; i<this.bidFlexOffer.getSlices().length; i++){
			s[i] = this.bidFlexOffer.getSlice(i).getEnergyUpper();
		}		
		return s;
	}
	
	
	/**
	 * 
	 * @return A unit price schedule (ps_unit)
	 * 
	 */
	public double [] getUnitPrice () {		
		double [] s = new double [this.bidFlexOffer.getSlices().length];		
		for (int i=0; i<this.bidFlexOffer.getSlices().length; i++){
			s[i] = this.bidFlexOffer.getSlice(i).getCostPerEnergyUnitLimit();
		}		
		return s;
	}
	
	public double getAvgUnitPrice() {
		double [] s = this.getUnitPrice();
		double a = 0;
		
		for (int i=0; i<s.length; i++) {
			a += s[i];
		}		
		return a / s.length;
	}

	@Override
	public int compareTo(BidV2 arg0) {
		return this.getAvgUnitPrice() == arg0.getAvgUnitPrice() ? 0 :
			   this.getAvgUnitPrice() <  arg0.getAvgUnitPrice() ? -1 : 1;
	}

	public long getTimeFrom() {
		return this.getBidFlexOffer().getStartAfterInterval();
	}
	
	public long getTimeTo() {
		return this.getBidFlexOffer().getEndBeforeInterval();
	}
	
	public int getNumSlices() {
		return this.bidFlexOffer.getSlices().length;
	}
	
}
