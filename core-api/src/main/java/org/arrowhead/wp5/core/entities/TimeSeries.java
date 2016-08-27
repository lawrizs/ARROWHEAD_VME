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
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


/**
 * Utility class for representing time series  
 * 
 * @author Laurynas
 *
 */
@XmlRootElement
public class TimeSeries implements Serializable {
	private static final long serialVersionUID = 5996734100363832480L;
	/**
	 * The interval to which this time series is aligned 
	 */
	private long startInterval=0;
	/**
	 * The value returned for intervals not explicitly set by the user.
	 */
	private double defaultValue = 0;

	/**
	 * The time series data 
	 */
	private double[] data;
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(startInterval);
		if(data!=null){
			sb.append("[");
			boolean b=false;
			for (double d : data) {
				if(b){
					sb.append("|");
				} else {
					b=true;
				}
				sb.append((double)Math.round(d*10)/10);
			}
			sb.append("]");
		}
		return sb.toString();
	}
	
	public String toRange() {
		return "["+getStartInterval()+"-"+getEndInterval()+")";
	}
	private NumberFormat nf = NumberFormat.getNumberInstance();

	public String toCSV() {
		nf.setMaximumFractionDigits(1);
		StringBuilder sb = new StringBuilder();
		sb.append(startInterval);
		if(data!=null){
			sb.append("\t");
			boolean b=false;
			for (double d : data) {
				if(b){
					sb.append("\t");
				} else {
					b=true;
				}
				sb.append(nf.format(d));
			}
		}
		return sb.toString();
	}
	
	public TimeSeries() {
		super();
	}
	
	public TimeSeries(TimeSeries ts) {
		setStartInterval(ts.getStartInterval());
		setData(ts.getData().clone()); 
	}

	public TimeSeries(double[] vals) {
		this(0,vals);
	}

	public TimeSeries(long begin, double [] vals) {
		setData(vals);
		setStartInterval(begin);
	}
	
	/* Build a time series from a flexoffer*/
	public TimeSeries(FlexOffer fo, TimeSeriesType type) {
	
		switch (type) {
		case tstScheduledEnergy: {		
			double [] values = new double[(int) (fo.getDurationIntervals())];
			int t = 0;			
			/* Compute scheduled energy */
			for (int sid = 0; sid < fo.getSlices().length; sid++) {
				FlexOfferSlice s = fo.getSlice(sid);
				double sv = fo.getFlexOfferSchedule().getEnergyAmount(sid); 
				
				for(int tt = 0; tt < s.getDuration(); tt++) {
					values[t++]  = sv / s.getDuration();
				}
			}
			
			/* Set start time */			
			this.setStartInterval(fo.getFlexOfferSchedule().getStartInterval());
			/* Set data values */
			this.setData(values);
		
			break;
		}
		case tstBaselineEnergy: {		
			double [] values = new double[(int) (fo.getDurationIntervals())];
			int t = 0;			
			/* Compute scheduled energy */
			for (int sid = 0; sid < fo.getSlices().length; sid++) {
				FlexOfferSlice s = fo.getSlice(sid);
				double sv = fo.getDefaultSchedule().getEnergyAmount(sid); 
				
				for(int tt = 0; tt < s.getDuration(); tt++) {
					values[t++]  = sv / s.getDuration();
				}
			}
			
			/* Set start time */			
			this.setStartInterval(fo.getDefaultSchedule().getStartInterval());
			/* Set data values */
			this.setData(values);
		
			break;
		}		
		case tstMinEnergy:
		case tstMaxEnergy: {
			double[] baseTs = new double[(int) (fo.getEndBeforeInterval() - fo.getStartAfterInterval())];
			
			this.setStartInterval(fo.getStartAfterInterval());
			this.setData(baseTs);
			
			for(int i=0; i< baseTs.length; i++) {
				baseTs[i] = 0; // type == TimeSeriesType.tstMinEnergy ? Double.MAX_VALUE : -Double.MAX_VALUE;
			}
			
			double[] windowTs = new double[fo.getDurationIntervals()];
			int t = 0;
			
			for (int sid = 0; sid < fo.getSlices().length; sid++) {
				FlexOfferSlice s = fo.getSlice(sid);
				
				for(int tt = 0; tt < s.getDuration(); tt++) {
					windowTs[t++]  = type == TimeSeriesType.tstMinEnergy ? s.getEnergyConstraint().getLower() : s.getEnergyConstraint().getUpper();
				}
			}
			
			/* Perform minimization */
			for(int s = 0; s< fo.getStartBeforeInterval() - fo.getStartAfterInterval() + 1; s++) {
				for(int i = 0; i<windowTs.length; i++) {
					if ((type == TimeSeriesType.tstMinEnergy && (windowTs[i] < baseTs[(int)(s+i)])) ||
						(type == TimeSeriesType.tstMaxEnergy && (windowTs[i] > baseTs[(int)(s+i)]))) {
						baseTs[s+i] = windowTs[i];
					}
				}
			}			
	
			break;
		}	
		default:
		}
	}
	
	public TimeSeries(long begin,List<Double> l) {
		double[] da = new double[l.size()];
		for (int i = 0; i < da.length; i++) {
			da[i]=l.get(i);
		}
		setData(da);
		setStartInterval(begin);
	}

	public TimeSeries(long begin, int count, double val) {
		setStartInterval(begin);
		setData(new double[count]);
		for(int t=0; t < count; t++) {
			getData()[t] = val;
		}
	}
	
	public boolean isEmpty() {
		return data == null || data.length == 0;
	}
	//
	// Value methods
	//

	public double getValue(long interval) {
		if((interval-getStartInterval() >= 0 && interval-getStartInterval() < getData().length)) {
			return getData()[(int) (interval-getStartInterval())];
		} else {
			return defaultValue;
		}
	}

	public double setValue(int interval, double value) {
		assert(interval-getStartInterval() >= 0 && interval-getStartInterval() < getData().length);
		return getData()[(int)(interval-getStartInterval())] = value;
	}
	
	public void setValue(double value) {
		for(int i=0; i<getData().length; i++)
			getData()[i] = value;
	}

	//
	// Time methods
	//

	public int countIntervals() {
		return (getData()!=null ? getData().length : 0);
	}

	public TimeSeries shift(long interval) {
		assert(getStartInterval() + interval >= 0);
		TimeSeries ret = new TimeSeries(this);
		ret._shift(interval);
		return ret;
	}
	public TimeSeries _shift(long interval) {
		assert(getStartInterval() + interval >= 0);
		setStartInterval(getStartInterval() + interval);
		return this;
	}

	public TimeSeries trim() {
		long s;
		for(s = 0; s < getData().length; s++) {
			if(getData()[(int)s] != 0) break;
		}
		s = getStartInterval()+s;
		
		long e;
		for(e = getData().length; e > 0; e--) {
			if(getData()[(int)e-1] != 0) break;
		}
		e = getStartInterval()+e;

		TimeSeries ret;
		if(s < e) {
			ret = new TimeSeries(s, (int) (e-s),0);
			ret._copy(this);
		}
		else
			ret = this;

		return ret;
	}

	public TimeSeries update(TimeSeries arg) {
		return extend(arg)._copy(arg);
	}
	
	public TimeSeries _update(TimeSeries arg) {
		return this._extend(arg)._copy(arg);
	}
	
	public TimeSeries extend(TimeSeries arg) {
		if(arg == null || arg.isEmpty()) { 
			return new TimeSeries(this);
		}

		long s = this.isEmpty() ? arg.getStartInterval() : Math.min(getStartInterval(), arg.getStartInterval());
		long e = this.isEmpty() ? arg.getEndInterval() : Math.max(getEndInterval(), arg.getEndInterval());

		if(e-s == countIntervals()) { // No extension (argument is included into this time-series)
			return new TimeSeries(this);
		}

		TimeSeries ret = new TimeSeries(s, (int) (e-s), 0.0);
		ret._copy(this); // This time series will always be fully present in the result time series

		return ret;
	}

	public TimeSeries _extend(TimeSeries arg) {
		TimeSeries ret = extend(arg);
		this.setData(ret.getData()); // Replace data array
		this.setStartInterval(ret.getStartInterval());
		return this;
	}

	public TimeSeries reduce(TimeSeries arg) {
		long s = Math.max(getStartInterval(), arg.getStartInterval());
		long e = Math.min(getStartInterval()+getData().length, arg.getStartInterval()+arg.getData().length);

		if(arg.getStartInterval() <= getStartInterval()) { // Starts before this
			s = arg.getStartInterval()+arg.getData().length; // Reduce from the beginning
			e = getStartInterval()+getData().length; // Not changed
		}
		else if(arg.getStartInterval()+arg.getData().length >= getStartInterval()+getData().length) { // Ends after this
			s = getStartInterval(); // Not changed
			e = arg.getStartInterval(); // Reduce from the end
		}
		else { // Reduce in the middle (splits this time series into two time series
			return this; 
		}

		if(s >= e) { // Empty result
			return new TimeSeries(getStartInterval(), 1, 0);
		}

		TimeSeries ret = new TimeSeries(s, (int)(e-s), 0.0);
		ret._copy(this);

		return ret;
	}

	public TimeSeries intersect(TimeSeries arg) {
		long s = Math.max(getStartInterval(), arg.getStartInterval());
		long e = Math.min(getStartInterval()+getData().length, arg.getStartInterval()+arg.getData().length);

		if(s >= e) return null;

		TimeSeries ret = new TimeSeries(s, (int)(e-s), 0.0);
		ret._copy(this);

		return ret;
	}

	public TimeSeries subSeries(long s,long e) {
		if(s >= e) return null;

		TimeSeries ret = new TimeSeries(s, (int)(e-s), 0.0);
		ret._copy(this);

		return ret;
	}

	public TimeSeries subSeries(TimeSeries ts) {
		return subSeries(ts.getStartInterval(),ts.getEndInterval());
	}

	//
	// Scalars
	//

	public double integral() {
		double sum = 0;
		for(int t=0; t < getData().length; t++) {
			sum += getData()[t];
		}
		return sum;
	}

	public double max() {
		double res = Double.NEGATIVE_INFINITY;
		for(int t=0; t < getData().length; t++) {
			if(getData()[t] < res) continue;
			res = getData()[t];
		}
		return res;
	}

	public double min() {
		double res = Double.POSITIVE_INFINITY;
		for(int t=0; t < getData().length; t++) {
			if(getData()[t] > res) continue;
			res = getData()[t];
		}
		return res;
	}

	//
	// Unary
	//

	public TimeSeries abs() {
		TimeSeries ret = new TimeSeries(this);
		for(int t=0; t < ret.getData().length; t++) {
			if(ret.getData()[t] < 0) ret.getData()[t] = -ret.getData()[t];
		}
		return ret;
	}

	public TimeSeries neg() {
		TimeSeries ret = new TimeSeries(this);
		for(int t=0; t < ret.getData().length; t++) {
			ret.getData()[t] = -ret.getData()[t];
		}
		return ret;
	}
	
	public double minimum() {
		double result = Double.MAX_VALUE;
		for (double d : data) {
			if(d<result) {
				d=result;
			}
		}
		return result;
	}

	public TimeSeries mul(double arg) {
		TimeSeries ret = new TimeSeries(this);
		for(int t=0; t < ret.getData().length; t++) {
			ret.getData()[t] *= arg;
		}
		return ret;
	}

	public TimeSeries div(double arg) {
		assert(arg != 0);
		TimeSeries ret = new TimeSeries(this);
		for(int t=0; t < ret.getData().length; t++) {
			ret.getData()[t] /= arg;
		}
		return ret;
	}

	public TimeSeries plus(double arg) {
		TimeSeries ret = new TimeSeries(this);
		for(int t=0; t < ret.getData().length; t++) {
			ret.getData()[t] += arg;
		}
		return ret;
	}

	public TimeSeries minus(double arg) {
		TimeSeries ret = new TimeSeries(this);
		for(int t=0; t < ret.getData().length; t++) {
			ret.getData()[t] -= arg;
		}
		return ret;
	}

	//
	// Binary arithmetics
	//

	public TimeSeries copy(TimeSeries arg) {
		TimeSeries ret = new TimeSeries(this);
		ret._copy(arg);
		return ret;
	}
	public TimeSeries _copy(TimeSeries arg) {
		if(arg==null) return this;
		for(int t=0; t < getData().length; t++) {
			int tt = (int) ((getStartInterval() + t) - arg.getStartInterval());
			if(tt < 0 || tt >= arg.getData().length) continue;
			getData()[t] = arg.getData()[tt];
		}
		return this;
	}

	public TimeSeries mul(TimeSeries arg) {
		TimeSeries ret = new TimeSeries(this);
		ret._mul(arg);
		return ret;
	}
	public TimeSeries _mul(TimeSeries arg) {
		for(int t=0; t < getData().length; t++) {
			int tt = (int) ((getStartInterval() + t) - arg.getStartInterval());
			if(tt < 0 || tt >= arg.getData().length) continue;
			getData()[t] *= arg.getData()[tt];
		}
		return this;
	}

	public TimeSeries minus(TimeSeries arg) {
		TimeSeries ret = new TimeSeries(this);
		ret._minus(arg);
		return ret;
	}
	public TimeSeries _minus(TimeSeries arg) {
		for(int t=0; t < getData().length; t++) {
			int tt = (int) ((getStartInterval() + t) - arg.getStartInterval());
			if(tt < 0 || tt >= arg.getData().length) continue;
			getData()[t] -= arg.getData()[tt];
		}
		return this;
	}

	public TimeSeries plus(TimeSeries arg) {
		TimeSeries ret = new TimeSeries(this);
		ret._plus(arg);
		return ret;
	}
	

	public TimeSeries _plus(TimeSeries arg) {
		for(int t=0; t < getData().length; t++) {
			int tt = (int) ((getStartInterval() + t) - arg.getStartInterval());
			if(tt < 0 || tt >= arg.getData().length) continue;
			getData()[t] += arg.getData()[tt];
		}
		return this;
	}

	public TimeSeries imbalance_change(TimeSeries fo_balance) {
		// How much the argument changes this imbalance
		// = |this| - |this + fo_balance|
		return this.abs().minus( ( this.plus(fo_balance) ).abs() );
	}

	/**
	 * @return the end of the time series (excluding this interval)
	 */
	@XmlTransient
	public long getEndInterval() {
		return getStartInterval()+(getData() != null ? getData().length: 0);
	}
	
	@XmlAttribute
	public Date getEndTime() {
		return FlexOffer.toAbsoluteTime(this.getEndInterval());
	}

	public void setStartInterval(long startInterval) {
		this.startInterval = startInterval;
	}

	@XmlTransient
	public long getStartInterval() {
		return startInterval;
	}
	
	@XmlAttribute
	public Date getStartTime() {
		return FlexOffer.toAbsoluteTime(this.getStartInterval());
	}

	public void setData(double[] data) {
		this.data = data;
	}

	public final double[] getData() {
		return data;
	}

	public long[] getIntervals() {
		long[] result = new long[countIntervals()];
		for(int i=0; i<data.length; i++) {
			result[i] = startInterval+i;
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(data);
		result = (int) (prime * result + startInterval);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimeSeries other = (TimeSeries) obj;
		if (!Arrays.equals(data, other.data))
			return false;
		if (startInterval != other.startInterval)
			return false;
		return true;
	}

	public double getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(double defaultValue) {
		this.defaultValue = defaultValue;
	}

	public TimeSeries _minimize(double min) {
		for (int i = 0; i < data.length; i++) {
			if(data[i]<min)data[i]=min;
		}
		return this;
	}

}
