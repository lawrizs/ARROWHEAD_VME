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

angular.module('foaManApp').directive(
		'ngTsSetView',
		function($compile, $timeout) {
			return {
				restrict : 'A',
				scope : {
					timeseries : '=timeseries'
				},
				templateUrl : "views/timeseriesSetView.html",
				link : {
					pre : function($scope) {
						Highcharts.setOptions({                                            // This is for all plots, change Date axis to local timezone
				                global : {
				                    useUTC : false
				                }
				            });
						$scope.chartConfig = {
							options : {
								chart : {
									zoomType : 'x'
								},
								rangeSelector : {
									buttons : [{
						                    type : 'hour',
						                    count : 1,
						                    text : '1h'
						                }, {
						                    type : 'day',
						                    count : 1,
						                    text : '1 day'
						                }, {
						                    type : 'all',
						                    count : 1,
						                    text : 'All'
						                }],
						            selected : 1,
						            inputEnabled : true,
									enabled : true
								},
								yAxis: [{ opposite: true

								}],
								navigator : {
									enabled : true
								}
							},
							series : $scope.timeseries,							
							useHighStocks : true
						};
					},
					post: function($scope,element, attrs, controller) {
						$timeout(function() { $(window).resize(); }, 2000);
					}
				}
			}
		});