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
'use strict';

/* Services */
angular.module('foaManApp')
.factory('serviceFOA', function ($resource) {
	var factory = {};

	factory.rootUrl = "http://localhost:9998/";
	factory.consoleUrl = 'ws://localhost:9998/console';
	
	factory.Connection = $resource(factory.rootUrl + 'api/manager/connection');
	factory.Id = $resource(factory.rootUrl + 'api/manager/id');
	factory.AggParams = $resource(factory.rootUrl + 'api/manager/aggparams');
	factory.XmppParams = $resource(factory.rootUrl + 'api/manager/xmppparams');
	factory.AnalyticsParams = $resource(factory.rootUrl + 'api/analytics/settings');
	factory.Stats = $resource(factory.rootUrl + 'api/manager/stats');	
	factory.SimpleFOs = $resource(factory.rootUrl + 'api/manager/flexoffers/:fid');
	factory.AggFOs = $resource(factory.rootUrl + 'api/manager/aggflexoffers/:fid');
	factory.EnergyMinimum = $resource(factory.rootUrl + 'api/energy/minimum');
	factory.EnergyMaximum = $resource(factory.rootUrl + 'api/energy/maximum');
	factory.EnergyScheduled = $resource(factory.rootUrl + 'api/energy/scheduled');
	factory.EnergyBaseline = $resource(factory.rootUrl + 'api/energy/baseline');
	factory.AnalyticsQuery = $resource(factory.rootUrl + 'api/analytics/query', {},  {   'save':   {method:'POST', timeout : 100000 }  });
	factory.Customers = $resource(factory.rootUrl + 'api/billing');
	factory.DefaultContract = $resource(factory.rootUrl + 'api/billing/defaultContract');
	factory.CustomerBill = $resource(factory.rootUrl + 'api/billing/bill/:cid');
	factory.MarketInfo = $resource(factory.rootUrl + 'api/vmarket');
	factory.MarketCommitments = $resource(factory.rootUrl + 'api/vmarket/commitments');
	factory.GenerateBidFlexOffer = $resource(factory.rootUrl + 'api/vmarket/generateBidFo');
	/* factory.GenerateBidSchedule = $resource(factory.rootUrl + 'api/vmarket/bidSchedule'); */
	/* factory.SendMarketBids = $resource(factory.rootUrl + 'api/vmarket/sendMarketBids'); */
	factory.TriggerRandomAssignments = $resource(factory.rootUrl + 'api/schedule', {},  {   'save':   {method:'POST'}  });
	
	return factory;
});