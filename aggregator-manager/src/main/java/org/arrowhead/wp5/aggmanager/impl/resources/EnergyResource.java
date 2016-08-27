package org.arrowhead.wp5.aggmanager.impl.resources;

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


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.arrowhead.wp5.agg.impl.Aggregator;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.TimeSeries;
import org.arrowhead.wp5.core.entities.TimeSeriesType;

@Path("/energy")
public class EnergyResource {
	private Aggregator agg;
	
	public EnergyResource(Aggregator agg) {
		this.agg = agg;
	}
	
	@GET
	@Path("/scheduled")	
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public TimeSeries getScheduledEnergy() throws Exception {
		return this.agg.getScheduledEnergy();
	}
	
	@GET
	@Path("/baseline")	
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public TimeSeries getBaselineEnergy() throws Exception {
		return this.agg.getBaselineEnergy();
	}
	
	@GET
	@Path("/minimum")	
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public TimeSeries getMinimumEnergy() throws Exception {
		TimeSeries ts = new TimeSeries(new double[] {});
		
		for(FlexOffer f : this.agg.getAllSimpleFlexOffers()) {
			TimeSeries ft = new TimeSeries(f, TimeSeriesType.tstMinEnergy);				
			ts._extend(ft)._plus(ft);
		}
		return ts;		
	}
	
	@GET
	@Path("/maximum")	
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public TimeSeries getMaximumEnergy() throws Exception {
		TimeSeries ts = new TimeSeries(new double[] {});
		
		for(FlexOffer f : this.agg.getAllSimpleFlexOffers()) {
			TimeSeries ft = new TimeSeries(f, TimeSeriesType.tstMaxEnergy);				
			ts._extend(ft)._plus(ft);
		}
		return ts;		
	}

}
