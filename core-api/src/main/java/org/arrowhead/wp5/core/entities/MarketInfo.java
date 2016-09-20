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
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="marketInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class MarketInfo implements Serializable {
	private static final long serialVersionUID = 7820978382893009508L;
	private String marketName;
	private String area;
	private long interval;
	private Date nextPeriod;
	
	public MarketInfo() {
		this.marketName = "";
		this.area = "";
		this.interval = 0;
		this.nextPeriod = new Date();
	}
	
	public MarketInfo(String marketName, String area, long interval, Date nextPeriod) {
		this.marketName = marketName;
		this.area = area;
		this.interval = interval;
		this.nextPeriod = nextPeriod;
	}
	
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public long getInterval() {
		return interval;
	}
	public void setInterval(long interval) {
		this.interval = interval;
	}
	public Date getNextPeriod() {
		return nextPeriod;
	}
	public void setNextPeriod(Date nextPeriod) {
		this.nextPeriod = nextPeriod;
	}

	public String getMarketname() {
		return marketName;
	}

	public void setMarketname(String marketName) {
		this.marketName = marketName;
	}
}
