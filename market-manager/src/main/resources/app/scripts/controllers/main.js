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

/**
 * @ngdoc function
 * @name webappApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the webappApp
 */
angular.module('marketManApp')
  .controller('MainCtrl', function ($scope, $location, $timeout, serviceMarket) {
	  	JSONEditor.defaults.options.no_additional_properties = true;
	  	$scope.id =  serviceMarket.Id.get();
	  	$scope.info =  serviceMarket.Info.get();
	  	
		serviceMarket.Supply.query()
		  .$promise.then(function(supply) {
		      $scope.supply = supply;
		  });	
		serviceMarket.Demand.query()
		  .$promise.then(function(demand) {
		      $scope.demand = demand;
		  });	
//	  	$scope.stats = serviceMarket.Stats.get();

//  }).controller('MainCtrl_AggParams', function ($scope, serviceMarket, schemas) {
//	   $scope.aggParams = serviceMarket.AggParams.get();
//	   $scope.aggParamsSchema =  schemas.aggParamsSchema;
//	    
//	    $scope.onAggParamsSave = function(event) {
//	    	var newAggPars = new serviceMarket.AggParams($scope.editor.getValue());
//	    	newAggPars.$save();
//	    };	    
//  })
//  .controller('MainCtrl_XmppParams', function ($scope, serviceMarket, schemas) {
//	  $scope.xmppParams = serviceMarket.XmppParams.get()
//	  $scope.xmppParamsSchema = schemas.xmppParamsSchema;
//	 	    
//	  $scope.onXmppParamsSave = function(event) {
//	    	var newXmppPars = new serviceMarket.XmppParams($scope.editor.getValue());
//	    	newXmppPars.$save();
//	  };
//  })
//  .controller('MainCtrl_AnalyticsParams', function ($scope, serviceMarket, schemas) {
//	  $scope.analyticsParams = serviceMarket.AnalyticsParams.get()
//	  $scope.analyticsParamsSchema = schemas.analyticsParamsSchema;
//	 	    
//	  $scope.onAnalyticsParamsSave = function(event) {
//	    	var newAnalyticsPars = new serviceMarket.AnalyticsParams($scope.editor.getValue());
//	    	newAnalyticsPars.$save();
//	  };
  });