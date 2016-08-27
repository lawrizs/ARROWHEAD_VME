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
	.controller('ClearingCtrl', function ($scope, $location, $timeout, serviceMarket) {

		var colorMap  = {'Supply Up':'#FF0000', 'Demand Up': '#0000FF', 'Supply Down':'#FF0000', 'Demand Down':'#0000FF'};
		$scope.colorFunction = function() {
		    return function(d, i) {
		    	return colorMap[d.key]
		    };
		}
		
		$scope.clear = function () {
			console.log("clear")
			serviceMarket.Clear.query()
				.$promise.then(function(f){})
		}
		
		$scope.upBids = []
		$scope.downBids = []

		serviceMarket.SupplyUp.query()
		  .$promise.then(function(supply) {
			  var a = $scope.upBids
		      $scope.supplyUp = supply;
		      console.log($scope.supplyUp)
		      var total = 0
		      supply.sort(function(a,b){return a.price-b.price})
		      var array = $.map(supply, function(value, index) {
		    	  total += value.quantity
		    	    return [[total, value.price/1000]];
		    	});
		      
		      if (array.length >0)
		    	  array.unshift([0,0])
		      
		      console.log(array)
		      
		      a.push(
		          {"key": "Supply Up",
		        	  "values":array
		          })
		      
		      $scope.upBids = a
		  });
		serviceMarket.DemandUp.query()
		  .$promise.then(function(demand) {
			  var a = $scope.upBids
		      $scope.DemandUp = demand;
		      console.log($scope.DemandUp)
		      var total = 0
		      demand.sort(function(a,b){return b.price-a.price})
		      var array = $.map(demand, function(value, index) {
		    	  total += value.quantity
		    	    return [[total, value.price/1000]];
		    	});
		      
		      if (array.length >0)
		    	  array.unshift([0,array[0][1]])
//		      console.log(array)
//		      array.reverse()
		      
		      a.push(
		          {"key": "Demand Up",
		        	  "values":array
		          })
		      
		      $scope.upBids = a
		  });
		serviceMarket.SupplyDown.query()
		  .$promise.then(function(supply) {
			  var a = $scope.downBids
		      $scope.supplyDown = supply;
		      console.log($scope.supplyDown)
		      var total = 0
		      supply.sort(function(a,b){return a.price-b.price})
		      var array = $.map(supply, function(value, index) {
		    	  total += value.quantity
		    	    return [[total, value.price/1000]];
		    	});
		      
		      if (array.length >0)
		    	  array.unshift([0,0])
		      
		      console.log(array)

		      a.push(
		          {"key": "Supply Down",
		        	  "values":array
		          })
		    	  
		      $scope.downBids = a
		  });
		serviceMarket.DemandDown.query()
		  .$promise.then(function(demand) {
			  var a = $scope.downBids
		      $scope.DemandDown = demand;
		      console.log($scope.DemandDown)
		      var total = 0
		      demand.sort(function(a,b){return b.price-a.price})
		      var array = $.map(demand, function(value, index) {
		    	  total += value.quantity
		    	    return [[total, value.price/1000]];
		    	});
		      
		      if (array.length >0)
		    	  array.unshift([0,array[0][1]])
//		      console.log(array)
//		      array.reverse()

		      a.push(
		          {"key": "Demand Down",
		        	  "values":array
		          })

		      $scope.downBids = a
		  });

//		$scope.exampleData = [
//			{"key":"Group 0",
//				"values":[
//				          {"x":23,"y":36},
//				      ]
//			},
//			{"key":"Group 1",
//				"values":[
//				          {"x":12,"y":28}
//			          ]
//			},
//			{"key":"Group 2",
//				"values":[
//				          {"x":48,"y":23}
//			          ]
//			}
//		];
//		$scope.exampleData2 = [
//           {
//        	   "key": "Series 1",
//        	   "values": [[0,8],[25,33],[50,33] ]
//           },
//           {
//        	   "key": "Series 2",
//        	   "values": [[25,33],[25,50] ]
//           }
//        ]
  });
