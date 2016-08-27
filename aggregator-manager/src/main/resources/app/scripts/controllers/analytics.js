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

angular.module('foaManApp').controller('AnalyticsCtrl',
		function($scope, $location, $timeout, serviceFOA, localStorageService) {
			$scope.query = "SELECT fo, fo->'flexOfferSchedule' FROM (SOLVESELECT fo IN (SELECT fo FROM get_agent_flexoffers) AS T WITH fosolver) AS s;";
			$scope.runningQuery = null;
			$scope.error = null;
			$scope.warnings = null;
						
			/* Let's persist these between browser refreshes */
			if (localStorageService.isSupported) {
				localStorageService.bind($scope, 'query');
				localStorageService.bind($scope, 'results');			
				localStorageService.bind($scope, 'error');
				localStorageService.bind($scope, 'warnings');
				localStorageService.bind($scope, 'history');
			};
			
			if (!$scope.results) {
				$scope.results = [];
			}
			
			if (!$scope.history) {
				$scope.history = [];
			}
			
			$scope.executeQuery = function(query, name) {
				var q = new serviceFOA.AnalyticsQuery({value:query});
				$scope.error = null;
				$scope.warnings = null;
				$scope.runningQuery = q.$save();
				$scope.runningQuery.query = query;
				$scope.runningQuery.name = name ? name : 'Query '+($scope.results.length + 1);
				
				// Save history
				var histItem = {name : name ? name: query, query: query };
				if ($scope.history.length===0 || ($scope.history[$scope.history.length - 1].name != histItem.name) 
											  || ($scope.history[$scope.history.length - 1].query != histItem.query)) {
					$scope.history.push(histItem);
				}
				
				$scope.runningQuery 
				   .then($scope.processResults)
				   .catch(function(req) { $scope.error = "Error executing the query:"+req; $scope.warnings = null; })
				   .finally(function()  { $scope.runningQuery = null;  });					
			}
			
			$scope.processResults = function(res) {
				/* Process error */
				$scope.error = res.error;
				$scope.warnings = res.warnings; 

				if (res.result && Array.isArray(res.result) && res.result.length > 0) {
					/* Deactive all tabs */
					angular.forEach($scope.results, function(o) { o.isActive = false; });
					
					/* Process the results */
					var res = {
							name: $scope.runningQuery.name,
							query: $scope.runningQuery.query,
							isActive: true,
							result: res.result,
							gridOptions: { data: res.result  }
					};
					$scope.results.push(res);
				}
			};
			
			$scope.stdQueries = [ {  name: 'Install Analytical Engine to SolveDB',
								    query: 'installAnalytics();'},
								  {  name: 'Agent Simple FlexOffers',
								    query: 'SELECT * FROM fo_get_simple'},
								  {  name: 'Agent Aggregated FlexOffers',
									query: 'SELECT * FROM fo_get_aggregated'},
								  {  name: 'Aggregate Simple FlexOffers (default parameters)',
									query: 'SELECT aggregate(fo) FROM fo_get_simple'},
								  {  name: 'Aggregate and Schedule',
									query: 'SELECT schedule(aggregate(fo)) FROM fo_get_simple'},
								  {  name: 'Aggregate, Schedule, and Execute',
									query: 'SELECT execute(schedule(aggregate(fo))) FROM fo_get_simple'},									
								  {  name: 'Aggregate, Schedule, and Disaggregate',
									query: 'SELECT disaggregate(schedule(aggregate(fo))) FROM fo_get_simple'},
								  {  name: 'Aggregate, Schedule, Disaggregate, and Execute',
									query: 'SELECT execute(disaggregate(schedule(aggregate(fo)))) FROM fo_get_simple'},									
								  {  name: 'Aggregate, Schedule, and Compare Schedules',
									query: 'SELECT fo_to_ts(schedule(aggregate(fo))) FROM fo_get_simple'},
								  {  name: 'Aggregate, Schedule, and Show Overall Schedule',
									query: 'SELECT ts_add(ts) FROM (SELECT fo_to_ts(schedule(aggregate(fo))) FROM fo_get_simple) AS T(ts)'}									
			                    ];
		});
