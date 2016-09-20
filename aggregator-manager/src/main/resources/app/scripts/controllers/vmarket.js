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

angular.module('foaManApp').controller('VMarketCtrl',
		function($scope, $location, $timeout, serviceFOA) {
			$scope.marketInfo = serviceFOA.MarketInfo.get();
			$scope.marketCommitments = serviceFOA.MarketCommitments.query();			
			$scope.generatedBids = [];
			$scope.updateTimeSeries = function() {
				$scope.timeSeries = [ 
					{
						id : 2,
						name : "Maximum Energy",
						type: 'area',
						series : serviceFOA.EnergyMaximum.get(),
						fillColor: 'rgba(100,190,69, 0.7)',
						marker : {
			                    enabled : true,
			                    radius : 2
			            }
					},	                      
				                      {
					id : 1,
					name : "Minimum Energy",
					series : serviceFOA.EnergyMinimum.get(),
					type: 'area',
					fillColor: 'rgba(100,190,69, 0.7)',
					marker : {
	                    enabled : true,
	                    radius : 2
					}			  
				},
				 {
					id : 3,
					name : "Baseline Energy (Default schedule)",
				    color: '#FFA500',
					series : serviceFOA.EnergyBaseline.get(),
					marker : {
	                    enabled : true,
	                    radius : 2
					}
				}];
				var bNr = 0;
				for(var i = 0; i < $scope.generatedBids.length; i++) {
					
					var ts =function(mf) {
						/*new serviceFOA.GenerateBidSchedule(mf).$save(function(ts) {					
							$scope.timeSeries.push(
							 {
								id : $scope.timeSeries.length + 1,
								name : mf.id + " schedule",
							    color: '#AA2244',
								series : ts,
								marker : {
				                    enabled : true,
				                    radius : 4
								}			
							})});*/
					};
					
					ts($scope.generatedBids[i].bidFlexOffer);
				};
			};

			$scope.updateTimeSeries();
			$scope.bidStartTime = new Date();
	
			
			var mins = $scope.bidStartTime.getMinutes();
			var quarterHours = Math.round(mins/15);
			if (quarterHours == 4)
			{
				$scope.bidStartTime.setHours($scope.bidStartTime.getHours()+1);
			}
			var rounded = (quarterHours*15)%60;
			$scope.bidStartTime.setMinutes(rounded);		
			$scope.bidStartTime.setSeconds(0);	
			$scope.bidStartTime.setMilliseconds(0);
			$scope.bidStartTime.setMinutes($scope.bidStartTime.getMinutes() + 15);
			
			$scope.bidEndTime = new Date($scope.bidStartTime);
			$scope.bidEndTime.setMinutes($scope.bidEndTime.getMinutes() + 15);
			
			$scope.generateBid = function(bidStartTime, bidEndTime) {
				var bids = serviceFOA.GenerateBidV2.get({bidStartTime: bidStartTime, bidEndTime: bidEndTime}, function(mbids) {
					$scope.generatedBids = mbids == null ? [] : [mbids] ;
					$scope.updateTimeSeries();
				});
				
			};
			
			$scope.sendBids = function(mbids) {
				var sBid1 = new serviceFOA.SendMarketBid(mbids[0]).$save(function(res) {
						alert("Bid successfully submitted to the Market!");
				}, function(err) {
						alert("Error submitting the bid to the Market!");
				});				
			};
		});
