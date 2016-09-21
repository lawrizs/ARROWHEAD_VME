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

angular.module('foaManApp').controller('StatusCtrl',
		function($scope, $location, $timeout, $route, serviceFOA) {
	
			$scope.obj = serviceFOA.Objective.get();			
			$scope.simpleFOs = serviceFOA.SimpleFOs.query();
			$scope.aggFOs = serviceFOA.AggFOs.query();
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
			   /* fillColor : {
                    linearGradient : {
                        x1: 0,
                        y1: 0,
                        x2: 0,
                        y2: 1
                    },
                    stops : [
                        [0, Highcharts.getOptions().colors[0]],
                        [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                    ]
                 }*/
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
			},
			 {
				id : 4,
				name : "Scheduled Energy",
			    color: '#FF0000',
				series : serviceFOA.EnergyScheduled.get(),
				marker : {
                    enabled : true,
                    radius : 4
				} 
			}];
		
			
			$scope.updateObjective = function() {
				var upd = new serviceFOA.Objective($scope.obj);				
				upd.$save().then(function(res) { $route.reload(); });	
			}

			$scope.generateAssignments = function() {
				var asg = new serviceFOA.TriggerRandomAssignments();
				asg.$save().then(function(res) { $route.reload();  });		
				
			}
			/*[{"id":0,"state":"Initial","offeredById":"","acceptanceBeforeTime":"1970-01-01T01:00:00.000+0100","assignmentBeforeDurationSeconds":0,"assignmentBeforeTime":"1970-01-01T01:00:00.000+0100","creationTime":"1970-01-01T01:00:00.000+0100","durationSeconds":60,"endAfterTime":"1970-01-01T01:01:00.000+0100","endBeforeTime":"1970-01-01T01:01:00.000+0100","numSecondsPerInterval":60,"startAfterTime":"1970-01-01T01:00:00.000+0100","startBeforeTime":"1970-01-01T01:00:00.000+0100","slices":[{"durationSeconds":60,"costPerEnergyUnitLimit":1.0,"energyConstraint":{"lower":0.0,"upper":10.0},"tariffConstraint":{"lower":0.0,"upper":10.0}}],"flexOfferSchedule":null},
			 {"id":0,"state":"Initial","offeredById":"","acceptanceBeforeTime":"1970-01-01T01:01:00.000+0100","assignmentBeforeDurationSeconds":0,"assignmentBeforeTime":"1970-01-01T01:00:00.000+0100","creationTime":"1970-01-01T01:00:00.000+0100","durationSeconds":60,"endAfterTime":"1970-01-01T02:01:00.000+0100","endBeforeTime":"1970-01-01T02:30:00.000+0100","numSecondsPerInterval":60,"startAfterTime":"1970-01-01T01:15:00.000+0100","startBeforeTime":"1970-01-01T01:30:00.000+0100","slices":[{"durationSeconds":60,"costPerEnergyUnitLimit":1.0,"energyConstraint":{"lower":5.0,"upper":50.0},"tariffConstraint":{"lower":10.0,"upper":80.0}}],"flexOfferSchedule":null}]; */
		});
