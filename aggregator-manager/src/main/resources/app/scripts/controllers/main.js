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

/**
 * @ngdoc function
 * @name webappApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the webappApp
 */
angular.module('foaManApp')
  .controller('MainCtrl', function ($scope, $location, $timeout, serviceFOA) {
	  	JSONEditor.defaults.options.no_additional_properties = true;
	  	$scope.id =  serviceFOA.Id.get();
	  	$scope.stats = serviceFOA.Stats.get();

  }).controller('MainCtrl_AggParams', function ($scope, serviceFOA, schemas) {
	   $scope.aggParams = serviceFOA.AggParams.get();
	   $scope.aggParamsSchema =  schemas.aggParamsSchema;
	    
	    $scope.onAggParamsSave = function(event) {
	    	var newAggPars = new serviceFOA.AggParams($scope.editor.getValue());
	    	newAggPars.$save();
	    };	    
  })
  .controller('MainCtrl_XmppParams', function ($scope, serviceFOA, schemas) {
	  $scope.xmppParams = serviceFOA.XmppParams.get()
	  $scope.xmppParamsSchema = schemas.xmppParamsSchema;
	 	    
	  $scope.onXmppParamsSave = function(event) {
	    	var newXmppPars = new serviceFOA.XmppParams($scope.editor.getValue());
	    	newXmppPars.$save();
	  };
  })
  .controller('MainCtrl_AnalyticsParams', function ($scope, serviceFOA, schemas) {
	  $scope.analyticsParams = serviceFOA.AnalyticsParams.get()
	  $scope.analyticsParamsSchema = schemas.analyticsParamsSchema;
	 	    
	  $scope.onAnalyticsParamsSave = function(event) {
	    	var newAnalyticsPars = new serviceFOA.AnalyticsParams($scope.editor.getValue());
	    	newAnalyticsPars.$save();
	  };
  });