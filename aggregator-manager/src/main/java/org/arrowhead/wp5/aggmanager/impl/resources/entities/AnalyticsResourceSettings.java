package org.arrowhead.wp5.aggmanager.impl.resources.entities;

/*-
 * #%L
 * ARROWHEAD::WP5::Aggregator Manager
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


import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the connection settings needed for the analytics to work.
 *  
 * @author Laurynas
 *
 */
@XmlRootElement
public class AnalyticsResourceSettings {
	private String solveDB_URL = "jdbc:postgresql://192.168.56.102/postgres?user=laurynas&password=XXXXX";
	private String aggregator_URL = "http://192.168.56.1:9998/api/";
	
	public String getSolveDB_URL() {
		return solveDB_URL;
	}
	
	public void setSolveDB_URL(String solveDB_URL) {
		this.solveDB_URL = solveDB_URL;
	}
	
	public String getAggregator_URL() {
		return aggregator_URL;
	}
	
	public void setAggregator_URL(String aggregator_URL) {
		this.aggregator_URL = aggregator_URL;
	}
}
