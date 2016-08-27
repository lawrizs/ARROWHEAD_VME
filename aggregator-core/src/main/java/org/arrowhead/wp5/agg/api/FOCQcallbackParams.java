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


import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A class that allows specifying a criterion for flex-offer change notification 
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 *
 */
@XmlRootElement
public final class FOCQcallbackParams  implements Serializable {
		private static final long serialVersionUID = 1L;
	    
		private int numFlexOffersThreshold = 0;
		private double lowerEnergyThreshold = 0;
		private double upperEnergyThreshold = 0;
		private double timeFlexThreshold = 0;
		/** 
		 * @return the numFlexOfferThreshold
		 */
		public int getNumFlexOffersThreshold() {
			return numFlexOffersThreshold;
		}
		/**
		 * Specifies a number of flex-offers that has to be added to a micro flex-offer 
		 * pool before a notification is generated. 
		 * 
		 * @param numFlexOffersThreshold the numFlexOffersThreshold to set
		 */
		public void setNumFlexOffersThreshold(int numFlexOffersThreshold) {
			this.numFlexOffersThreshold = numFlexOffersThreshold;
		}
		/**
		 * @return the lowerEnergyThreshold
		 */
		public double getLowerEnergyThreshold() {
			return lowerEnergyThreshold;
		}
		/**
		 * Specifies an amount of energy that has to be accumulated using lower energy bounds of flex-offers 
		 * before a notification is generated.
		 * 
		 * @param lowerEnergyThreshold the lowerEnergyThreshold to set
		 */
		public void setLowerEnergyThreshold(double lowerEnergyThreshold) {
			this.lowerEnergyThreshold = lowerEnergyThreshold;
		}
		/**
		 * @return the upperEnergyThreshold
		 */
		public double getUpperEnergyThreshold() {
			return upperEnergyThreshold;
		}
		/**
		 * Specifies an amount of energy that has to be accumulated using upper energy bounds of flex-offers 
		 * before a notification is generated.
		 * 
		 * @param upperEnergyThreshold the upperEnergyThreshold to set
		 */
		public void setUpperEnergyThreshold(double upperEnergyThreshold) {
			this.upperEnergyThreshold = upperEnergyThreshold;
		}
		/**
		 * @return the timeFlexThreshold
		 */
		public double getTimeFlexThreshold() {
			return timeFlexThreshold;
		}
		/**
		 * Specifies an total duration of time flexibility ("startBefore" - "startAfter") that has to be accumulated
		 * before a notification is generated.
		 * 
		 * @param timeFlexThreshold the timeFlexThreshold to set
		 */
		public void setTimeFlexThreshold(double timeFlexThreshold) {
			this.timeFlexThreshold = timeFlexThreshold;
		}	
}
