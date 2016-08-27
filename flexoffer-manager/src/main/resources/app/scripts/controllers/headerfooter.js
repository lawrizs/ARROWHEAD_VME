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

angular.module('foaManApp')
  .controller('HeaderCtrl', function ($scope, $location, $timeout, serviceFOA) {
	    $scope.isActive = function (viewLocation) { 
	        return viewLocation === $location.path();
	    };
	    
	    $scope.connect = function(connect) {
	    	var c = new serviceFOA.Connection();
	    	c.value = connect;	    	
	    	c.$save();
	    };
	    
	    $scope.isConnected = { value: false };
	    (function tick() {
	    	serviceFOA.Connection.get().$promise.then(
	    	   function(val) {
	    		$scope.isConnected = val;
	    	}, function(error){
	    		$scope.isConnected = null;
	    	 	});
	        $timeout(tick, 2000);
	    })();
		
		/*
		 *	Arrowhead Framework
		 */
//		$scope.connectArrowhead = function(connect) {
//	    	var ca = new serviceFOA.ConnectionArrowhead();
//	    	ca.value = connect;
//	    	ca.$save();
//	    };
//	    
//	    $scope.isConnectedArrowhead = { value: false };
//	    (function tick() {
//	    	serviceFOA.ConnectionArrowhead.get().$promise.then(
//	    	   function(val) {
//	    		$scope.isConnectedArrowhead = val;
//	    	}, function(error){
//	    		$scope.isConnectedArrowhead = null;
//	    	 	});
//	        $timeout(tick, 2000);
//	    })();
  });
