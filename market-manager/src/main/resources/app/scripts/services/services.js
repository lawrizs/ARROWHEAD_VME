/*-
 * #%L
 * ARROWHEAD::WP5::Market Manager
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
angular.module('marketManApp')
.factory('serviceMarket', function ($resource) {
	var factory = {};

	factory.rootUrl = "http://localhost:9996/";
	factory.consoleUrl = 'ws://localhost:9996/console';
	
	factory.Id = $resource(factory.rootUrl + 'api/market/id');
	factory.Info = $resource(factory.rootUrl + 'api/market/info');
	factory.Connection = $resource(factory.rootUrl + 'api/market/connection');
	factory.Supply = $resource(factory.rootUrl + 'api/market/supply');
	factory.Demand = $resource(factory.rootUrl + 'api/market/demand');
	factory.SupplyUp = $resource(factory.rootUrl + 'api/market/supplyUp');
	factory.DemandUp = $resource(factory.rootUrl + 'api/market/demandUp');
	factory.SupplyDown = $resource(factory.rootUrl + 'api/market/supplyDown');
	factory.DemandDown = $resource(factory.rootUrl + 'api/market/demandDown');

	factory.WinSupplyUp = $resource(factory.rootUrl + 'api/market/winSupplyUp');
	factory.WinDemandUp = $resource(factory.rootUrl + 'api/market/winDemandUp');
	factory.WinSupplyDown = $resource(factory.rootUrl + 'api/market/winSupplyDown');
	factory.WinDemandDown = $resource(factory.rootUrl + 'api/market/winDemandDown');
	
	factory.Clear = $resource(factory.rootUrl + 'api/market/clear');

	return factory;
});
