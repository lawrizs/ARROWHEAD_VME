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


angular.module('marketManApp')
	.controller('BidsCtrl', function ($scope, $location, $timeout, serviceMarket) {

		var colorMap  = {'Supply Up':'#FF0000', 'Demand Up': '#0000FF', 'Supply Down':'#FF0000', 'Demand Down':'#0000FF'};
		$scope.colorFunction = function() {
		    return function(d, i) {
		    	return colorMap[d.key]
		    };
		}
		
		$scope.clear = function () {
			serviceMarket.Clear.query()
				.$promise.then(function(f){})
		}
		
		$scope.upBidss = []
		$scope.downBidss = []

		serviceMarket.SupplyUp.query()
		  .$promise.then(function(supply) {
			  var a = $scope.upBidss
//		      supply.sort(function(a,b){return a.price-b.price})
		      var array = $.map(supply, function(value, index) {
		    	    return [{"x":value.quantity, "y":value.price/1000, "size":1}];
		    	});
		      
//		      if (array.length >0)
//		    	  array.unshift([0,0])
		      
		      console.log(array)
		      
		      a.push(
		          {"key": "Supply Up",
		        	  "values":array
		          })
		      
		      $scope.upBidss = a
		  });

		serviceMarket.SupplyDown.query()
		  .$promise.then(function(supply) {
			  var a = $scope.downBidss
		      supply.sort(function(a,b){return a.price-b.price})
		      var array = $.map(supply, function(value, index) {
		    	    return [{"x":value.quantity, "y":value.price/1000, "size":1}];
		    	});
		      
//		      if (array.length >0)
//		    	  array.unshift([0,0])
		      
		      console.log(array)
		      
		      a.push(
		          {"key": "Supply Down",
		        	  "values":array
		          })
		      
		      $scope.downBidss = a
		  });

		serviceMarket.DemandUp.query()
		  .$promise.then(function(supply) {
			  var a = $scope.upBidss
		      supply.sort(function(a,b){return a.price-b.price})
		      var array = $.map(supply, function(value, index) {
		    	    return [{"x":value.quantity, "y":value.price/1000, "size":1}];
		    	});
		      
//		      if (array.length >0)
//		    	  array.unshift([0,0])
		      
		      console.log(array)
		      
		      a.push(
		          {"key": "Demand Up",
		        	  "values":array
		          })
		      
		      $scope.upBidss = a
		  });

		serviceMarket.DemandDown.query()
		  .$promise.then(function(supply) {
			  var a = $scope.downBidss
		      supply.sort(function(a,b){return a.price-b.price})
		      var array = $.map(supply, function(value, index) {
		    	    return [{"x":value.quantity, "y":value.price/1000, "size":1}];
		    	});
		      
//		      if (array.length >0)
//		    	  array.unshift([0,0])
		      
		      console.log(array)
		      
		      a.push(
		          {"key": "Demand Down",
		        	  "values":array
		          })
		      
		      $scope.downBidss = a
		  });
  });
