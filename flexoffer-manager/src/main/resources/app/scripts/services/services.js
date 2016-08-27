/*-
 * #%L
 * ARROWHEAD::WP5::FlexOffer Manager
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

	factory.rootUrl = '/';
	factory.consoleUrl = 'ws://'+location.hostname+':9997/console';
	
	factory.DerList = $resource(factory.rootUrl + 'api/ders');
	factory.FOAstats = $resource(factory.rootUrl + 'api/foa/stats');
	factory.Connection = $resource(factory.rootUrl + 'api/foa/connection');
	factory.ConnectionDetails = $resource(factory.rootUrl + 'api/foa/connectiondetails');
	factory.Mode = $resource(factory.rootUrl + 'api/foa/mode');
	factory.FlexOffers = $resource(factory.rootUrl + 'api/foa/flexoffers/:fid');
	factory.FlexOfferSchedule = $resource(factory.rootUrl + 'api/foa/flexoffers/:fid/schedule', {fid: '@fid'});
	
	/*
	 *	Arrowhead Framework
	 */
//	factory.ConnectionArrowhead = $resource(factory.rootUrl + 'api/foa/connectionArrowhead');
//	factory.FlexOfferAggregatorProducers = $resource(factory.rootUrl + 'api/foa/lookupServiceProducers');
//	factory.FlexOfferAgentLocalProducers = $resource(factory.rootUrl + 'api/foa/localServiceProducers');
	
	return factory;
});