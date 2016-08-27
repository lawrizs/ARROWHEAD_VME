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
﻿'use strict';

angular
		.module('foaManApp')
		.directive(
				'ngFoSet',
				function($compile, $modal) {
					return {
						restrict : 'A',
						scope : {
							flexoffers : '=flexoffers'
						},
						templateUrl : "views/flexOfferSetView.html",
						link : function($scope) {

							if ($scope.flexoffers === null
									|| $scope.flexoffers.length === 0) {
								element.append("No flexoffers to vizualize");
								return;
							}

							/* Time, energy axis synchronization */
							var dateCmp = function(a, b) {
								return (new Date(a)) - (new Date(b));
							};
							
							
							$scope.viewMode = 0; /* View as Graph */ 

							$scope.syncTA = true; /* Sync time */
							$scope.syncEA = true; /* Sync energy */
							var recomputeRanges = function() {

								if ($scope.syncTA) {
									var orderedEST = $scope.flexoffers.map(
											function(f) {	return f.startAfterTime; }).sort(dateCmp);
									var orderedLET = $scope.flexoffers.map(
											function(f) {	return f.endBeforeTime;  }).sort(dateCmp);

									$scope.timeRange = [
											new Date(orderedEST[0]),
											new Date(orderedLET[orderedLET.length - 1]) ];
								} else {
									$scope.timeRange = null;
								}

								if ($scope.syncEA) {
									$scope.energyRange = [
											Math.min
													.apply(
															1e6,
															$scope.flexoffers
																	.map(function(
																			f) {
																		return Math.min
																				.apply(
																						1e6,
																						f.slices
																								.map(function(
																										s) {
																									return s.energyConstraint.lower;
																								}));
																	})),
											Math.max
													.apply(
															-1e6,
															$scope.flexoffers
																	.map(function(
																			f) {
																		return Math.max
																				.apply(
																						-1e6,
																						f.slices
																								.map(function(
																										s) {
																									return s.energyConstraint.upper;
																								}));
																	})) ];
								} else {
									$scope.energyRange = null;
								}

							};

							recomputeRanges();
							
							/* Compute minutes between two dates - use in GUI */							   
							$scope.dateDiff = function(date1, date2) {
								  var d1 = new Date(date1);
								  var d2 = new Date(date2);
								  return (d1-d2) / (1000 * 60);
							};
							
							$scope.toDate = function(date) {
								   return new Date(date);
							};

							$scope.$watchCollection('[syncTA,syncEA]',
									function() {
										recomputeRanges();
									});
							
							 $scope.showFO = function (size, flexOffer, viewType) {
								    var modalInstance = $modal.open({
								      templateUrl: 'foModalView.html',
								      controller: 'FoModalViewCtrl',
								      size: size,
								      resolve: {
								    	viewType : function() { return viewType; },
								        flexOffer: function () { return flexOffer; }
								      }
								    });		    
								    modalInstance.result.then(function () {});	     
								};

						}
					}
				})
.controller('FoModalViewCtrl', function ($scope, $modalInstance, viewType, flexOffer) {

	  $scope.viewType = viewType;
	  $scope.flexOffer = flexOffer;
	  $scope.flexOfferJSON = angular.toJson($scope.flexOffer, true);
	  
	  $scope.close = function () {
	    $modalInstance.close();
	  };	  
	});