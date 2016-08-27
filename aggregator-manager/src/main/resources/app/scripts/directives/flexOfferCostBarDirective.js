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

angular.module('foaManApp')
.directive('ngCostBar', ['$window', 'd3Service', function($window, d3Service) {
    return {
        restrict: 'A',
        scope: { flexoffer: '=', 
       	 	     timerange: '=',
        		 energyrange: '='
        	   },
        link: function (scope, element, attrs) {
        	d3Service.d3().then(function(d3) {
        		var xTickDensity = 75; 
        			
	            // Add the svg element to the dom
	            var svg = d3.select(element[0]).append('svg:svg');
	
	            // Browser onresize event
	            $window.onresize = function () {
	                scope.$apply();
	            };
	
	            // Watch for resize event
	            scope.$watch(function () { return $window.innerWidth + element[0].clientWidth; }, 
	            	function () { scope.render(scope.flexoffer); });
	
	            // Watch for newData event
	            scope.$watch('flexoffer', function () {   scope.render(scope.flexoffer); });
	            scope.$watch('timerange', function () {   scope.render(scope.flexoffer); });
	            scope.$watch('energyrange', function () { scope.render(scope.flexoffer); });
	            
	            Date.prototype.hhmm = function() {
	            	   var hh = this.getHours().toString();
	            	   var mm = this.getMinutes().toString();
	            	   return (hh[1]?hh:"0"+hh[0])+":"+(mm[1]?mm:"0"+mm[0]); 
	            	  };
	            	  
	            scope.render = function (flexoffer) {
		            var dirWidth = element[0].clientWidth != 0 ? element[0].clientWidth : $window.innerWidth;
		            
	                // Margin and paddings
	                var margin = { top: 20, right: 10, bottom: 20, left: 60 };
	                var barPadding = 2;
	                var yAxisPadding = -5;
	
	                // Colours
	                var colSch = "#3914AF";
	                
	                var height = 200 - margin.top - margin.bottom;	            	
	                var width = dirWidth - margin.right - margin.left;
	                
	                
	                // Delete old data
	                svg.selectAll('*').remove();
	                
	                var canvas = svg.attr('width', width + margin.left + margin.right).attr('height', height + margin.top + margin.bottom).append('g').attr("transform", "translate(" + margin.left + "," + margin.top + ")");
	            	
	                if (flexoffer == null || flexoffer.defaultSchedule == null)
	                	{
	                		canvas.append("text")
		                    .attr("x", 0)             
		                    .attr("y", 0)
		                    .attr("text-anchor", "left")  
		                    .style("font-size", "18px") 
		                    .style("text-decoration", "underline")  
		                    .text("Flex-offer or its default schedule is undefined!");
	                    	return;
	                	}	                
	                var sliceCount = flexoffer.slices.length;
	                
	                var timeFrom = new Date(scope.timerange ? scope.timerange[0] : flexoffer.startAfterTime);
	                var timeTo   = new Date(scope.timerange ? scope.timerange[1] : flexoffer.endBeforeTime);
	                
	                /* Compute min/max delta energy and price */
	                var minDEnergy=0, maxDEnergy=0, minCost=0, maxCost=0;
	                
	                for (var i=0; i<flexoffer.slices.length; i++) {
	                	var minDE = flexoffer.slices[i].energyConstraint.lower - flexoffer.defaultSchedule.energyAmounts[i];
	                	var maxDE = flexoffer.slices[i].energyConstraint.upper - flexoffer.defaultSchedule.energyAmounts[i];
	                	minDEnergy = Math.min(minDEnergy, minDE);
	                	maxDEnergy = Math.max(maxDEnergy, maxDE);
	                	minCost = 0; 
	                	maxCost = Math.max(maxCost,  Math.abs(maxDE) * flexoffer.slices[i].costPerEnergyUnitLimit);
	                	maxCost = Math.max(maxCost,  Math.abs(minDE) * flexoffer.slices[i].costPerEnergyUnitLimit);
	                }
	                
	                // Scales
	                var tScale = d3.time.scale().domain([timeFrom, timeTo]).range([0, width]);
	                var yScale = d3.scale.linear().domain([minCost, maxCost]).rangeRound([height-20, 0]);
	                
	                var xLeft  = tScale(new Date(flexoffer.startAfterTime));  
	                var xRight = tScale(new Date(flexoffer.endAfterTime));
	                var sWidth = (xRight - xLeft) / sliceCount; 
	 
	                var xScale = d3.scale.linear().domain([minDEnergy, maxDEnergy]).rangeRound([0, sWidth]);
	                  
	                	                
	                // Make svg Axis
	                var tAxis = d3.svg.axis().scale(tScale).orient("bottom").ticks(sliceCount).innerTickSize(-height).tickPadding(10);
	                var xAxis = d3.svg.axis().scale(xScale).orient("bottom").ticks(3).innerTickSize(-height - 15).tickPadding(10);
	                var yAxis = d3.svg.axis().scale(yScale).orient("left").innerTickSize(-width).tickPadding(10);
	                
	                canvas.append("g")
	                	.attr("class", "foTaxis")
	                	.attr("transform", "translate(0, " + height + ")")
	                	.call(tAxis);
	                
	                canvas.append("g")
                	.attr("class", "foYaxis")
                	.attr('transform', 'translate(' + yAxisPadding + ',0)')
                	.call(yAxis)
                	.append("text")	                	
                	.attr("y", -2)
                	.attr("x", -10)	                	
                	.style("text-anchor", "end")
                	.text("DKK");
	                //  &Delta;
                	                
	                
	                /* Add X axis*/
	                for (var i=0; i<flexoffer.slices.length; i++) {
	                	
	                	var sLeft = xLeft + sWidth * i;
	                	
		                canvas.append("g")
	                	.attr("class", "foXaxis")
	                	.attr("transform", "translate(".concat(sLeft).concat(",").concat(height - 25).concat(")"))
	                	.call(xAxis);	
		                
		            	var minDE = flexoffer.slices[i].energyConstraint.lower - flexoffer.defaultSchedule.energyAmounts[i];
	                	var maxDE = flexoffer.slices[i].energyConstraint.upper - flexoffer.defaultSchedule.energyAmounts[i];
	                	var minDECost = Math.abs(minDE) * flexoffer.slices[i].costPerEnergyUnitLimit;
	                	var maxDECost = Math.abs(maxDE) * flexoffer.slices[i].costPerEnergyUnitLimit;
		                
		                canvas.append('line')
	                	.attr('class', 'foCostFunction')
	                	.attr('x1', sLeft + xScale(minDE))
	                	.attr('y1', yScale(minDECost))
	                	.attr('x2', sLeft + xScale(0))
	                	.attr('y2', yScale(0));
		                
		                canvas.append('line')
	                	.attr('class', 'foCostFunction')
	                	.attr('x1', sLeft + xScale(0))
	                	.attr('y1', yScale(0))
	                	.attr('x2', sLeft + xScale(maxDE))
	                	.attr('y2', yScale(maxDECost));		                
	                }
	            }       
            });
    }}}]);