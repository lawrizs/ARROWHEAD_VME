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


import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.arrowhead.wp5.agg.impl.Aggregator;
import org.arrowhead.wp5.agg.impl.billing.AggregatorBill;
import org.arrowhead.wp5.agg.impl.billing.AggregatorContract;
import org.arrowhead.wp5.aggmanager.impl.resources.entities.CustomerStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/billing")
public class BillingResource {
	final static Logger logger = LoggerFactory.getLogger(BillingResource.class);	
	private Aggregator agg;
	
	public BillingResource(Aggregator agg) {
		this.agg = agg;
	}
	
	
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<CustomerStat> getActiveCustomers() {
		List<CustomerStat> cList = new ArrayList<CustomerStat>();
		
		for(String cId : this.agg.getActiveCustomers())
		{
			CustomerStat cStat = new CustomerStat();
			cStat.setCustomerId(cId);
			cStat.setNumFlexOffers(this.agg.getActiveCustomerFlexOffers(cId).size());
			cStat.setTotalRewardForFlexibility(this.agg.getCustomerBill(cId).getRewardTotal());
			cList.add(cStat);
		}
		
		return cList;
	}
	
	@GET
	@Path("/defaultContract")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public AggregatorContract getDefaultContract() {
		return this.agg.getDefaultContract();
	}	
	
	@POST
	@Path("/defaultContract")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public void setDefaultContract(AggregatorContract contract) {
		this.agg.setDefaultContract(contract);
	}	
	

	@Path("/contract/{cId}")
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public AggregatorContract getContract(@PathParam("cId") String customerId) {
		return this.agg.getContract(customerId);
	}
		
	
	@Path("/bill/{cId}")
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public AggregatorBill getBill(@PathParam("cId") String customerId) {
		return this.agg.getCustomerBill(customerId);
	}	
}
