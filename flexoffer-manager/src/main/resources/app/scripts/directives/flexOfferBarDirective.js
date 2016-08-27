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
﻿'use strict';

angular.module('foaManApp')
.directive('ngBar', ['$window', 'd3Service', function($window, d3Service) {
    return {
        restrict: 'A',
        scope: { flexoffer: '=', 
        		 timerange: '=',
        		 energyrange: '=',
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
	                var margin = { top: 20, right: 5, bottom: 20, left: 60 };
	                var barPadding = 2;
	                var yAxisPadding = -5;
	
	                // Colours
	                var colSch = "#3914AF";
	                
	                var height = 400 - margin.top - margin.bottom;	            	
	                var width = dirWidth - margin.right - margin.left;
	                
	                
	                // Delete old data
	                svg.selectAll('*').remove();
	                
	                var canvas = svg.attr('width', width + margin.left + margin.right).attr('height', height + margin.top + margin.bottom).append('g').attr("transform", "translate(" + margin.left + "," + margin.top + ")");
	            	
	                if (flexoffer == null)
	                	{
	                		canvas.append("text")
		                    .attr("x", 0)             
		                    .attr("y", 0)
		                    .attr("text-anchor", "left")  
		                    .style("font-size", "18px") 
		                    .style("text-decoration", "underline")  
		                    .text("Flex-offer is undefined!");
	                    	return;
	                	}	                
	
	                var timeFrom = new Date(scope.timerange ? scope.timerange[0] : flexoffer.startAfterTime);
	                var timeTo   = new Date(scope.timerange ? scope.timerange[1] : flexoffer.endBeforeTime);
	                var energyFrom = scope.energyrange ? 
        					scope.energyrange[0] : 
        					d3.min(flexoffer.slices, function(s) { return s.energyConstraint.lower; });	                
	                var energyTo = scope.energyrange ? 
	                					scope.energyrange[1] : 
	                					d3.max(flexoffer.slices, function(s) { return s.energyConstraint.upper; });                					
	
	                // Scales
	                var xScale = d3.time.scale().domain([timeFrom, timeTo]).range([0, width]); 	
	                var yScale = d3.scale.linear().domain([energyFrom, energyTo]).rangeRound([height, 0]);
	
	                // Make svg Axis
	                var xAxis = d3.svg.axis().scale(xScale).orient("bottom").ticks(width / xTickDensity).innerTickSize(-height).tickPadding(10);
	                var yAxis = d3.svg.axis().scale(yScale).orient("left").innerTickSize(-width).tickPadding(10);
	
	                canvas.append("g")
	                	.attr("class", "foXaxis")
	                	.attr("transform", "translate(0," + height + ")")
	                	.call(xAxis);	
	                
	                canvas.append("g")
	                	.attr("class", "foYaxis")
	                	.attr('transform', 'translate(' + yAxisPadding + ',0)')
	                	.call(yAxis)
	                	.append("text")	                	
	                	.attr("y", -2)
	                	.attr("x", -10)	                	
	                	.style("text-anchor", "end")
	                	.text("kWh");
	                
	                // Add horizontal line, for X axis 
	                canvas
		                .append('line')
	                	.attr('class', 'foY0line')
	                	.attr('x1', xScale(timeFrom))
	                	.attr('x2', xScale(timeTo))
	                	.attr('y1', yScale(0))
	                	.attr('y2', yScale(0));
	                
	                /* Add time flexibility region */
	                var tStart = new Date(flexoffer.startAfterTime),
	                	tEnd = new Date(flexoffer.startBeforeTime),
	                	tLEnd = new Date(flexoffer.endBeforeTime),
	                	tH = 15;
	                
	                /* Add time flexibility area and lines/text */
	                canvas
		                .append('rect')
		                .attr('class', 'foTimeFlexibility')
		                .attr('x', xScale(tStart))
		                .attr('y', yScale(0)-tH)
		                .attr('height', tH)
		                .attr('width', xScale(tEnd) - xScale(tStart));
	                
	                canvas
		            	.append('line')
	                	.attr('class', 'foTimeFlexibility')
	                	.attr('x1', xScale(tStart))
	                	.attr('x2', xScale(tStart))
	                	.attr('y1', yScale(energyTo))
	                	.attr('y2', yScale(energyFrom));
	                
	            	canvas
	                  	.append("text")
	                  	.attr('class', 'foTimeFlexibility')
	                  	.attr("x", xScale(tStart))
	                	.attr("y", yScale(energyTo))		                	
	                	.style("text-anchor", "start")
	                	.text("EST:"+tStart.hhmm());
	            	
	            	canvas
		            	.append('line')
	                	.attr('class', 'foTimeFlexibility')
	                	.attr('x1', xScale(tEnd))
	                	.attr('x2', xScale(tEnd))
	                	.attr('y1', yScale(energyTo))
	                	.attr('y2', yScale(energyFrom));
                
	            	canvas
	                  	.append("text")
	                  	.attr('class', 'foTimeFlexibility')
	                  	.attr("x", xScale(tEnd))
	                	.attr("y", yScale(energyTo))		                	
	                	.style("text-anchor", "end")
	                	.text("LST:"+tEnd.hhmm());	      
	            	
	               	canvas
		            	.append('line')
	                	.attr('class', 'foTimeFlexibility')
	                	.attr('x1', xScale(tLEnd))
	                	.attr('x2', xScale(tLEnd))
	                	.attr('y1', yScale(energyTo))
	                	.attr('y2', yScale(energyFrom));
	            
	            	canvas
	                  	.append("text")
	                  	.attr('class', 'foTimeFlexibility')
	                  	.attr("x", xScale(tLEnd))
	                	.attr("y", yScale(energyTo))		                	
	                	.style("text-anchor", "end")
	                	.text("LET:"+tLEnd.hhmm());		            	
		                
	                // Make bars
	                var barSlices = [];
	                var tl =  flexoffer.flexOfferSchedule !== null ? 
	                		new Date(flexoffer.flexOfferSchedule.startTime) :  
	                		new Date(flexoffer.startAfterTime); 
	                		
	                for(var i=0; i< flexoffer.slices.length; i++)
	                	{
	                		var s = flexoffer.slices[i];
	                		var th = d3.time.second.offset(tl, s.durationSeconds);
	                		var sch = flexoffer.flexOfferSchedule !== null ? 
	                					flexoffer.flexOfferSchedule.energyAmounts[i] :
	                					null;
	                		
	                		barSlices.push({tl: tl,th: th, td: xScale(th)-xScale(tl), 
	                					    el: s.energyConstraint.lower, eh: s.energyConstraint.upper,
	                					    sch: sch});
	                		tl = th;
	                	}	                
	             
	                var sliceGroup = canvas
	                				.selectAll('.slice')
	                				.data(barSlices)
	                				.enter()
	                				.append('g')
	                				.attr('class', 'slice')
	                				.attr('transform', function (s) { return "translate(" + (xScale(s.tl)) + ",0)";});
	                
	                sliceGroup
	                	.append('rect')
	                	.attr('class', 'foAreaBase')
	                	.attr('width', function (s) { return s.td; })
	                	.attr('y', function (s) { return s.eh>=0 ? yScale(s.el) : yScale(0); })
	                    .attr('height', function (s) { return Math.max(0, s.eh>=0 ? 
	                    						yScale(Math.max(0, energyFrom)) - yScale(s.el) : 
	                    						yScale(s.eh)-yScale(0));});
	                
	                sliceGroup
	                	.append('rect')
	                	.attr('class', 'foAreaFlex')
	                	.attr('width', function (s) { return s.td; })
	                	.attr('y', function (s) { return yScale(s.eh); })
	                    .attr('height', function (s) { return yScale(s.el) - yScale(s.eh); });
	
	                // Make schedule line
	                if (flexoffer.flexOfferSchedule !== null) {
	                	var st = new Date(flexoffer.flexOfferSchedule.startTime);	                	
	                	sliceGroup
		                	.append('line')
		                	.attr('class', 'foSchedule')
		                	.attr('x1', 0)
		                	.attr('x2', function(s) { return s.td;})
		                	.attr('y1', function(s) { return yScale(s.sch); })
		                	.attr('y2', function(s) { return yScale(s.sch); })
		                	
		                canvas
			            	.append('line')
		                	.attr('class', 'foSchedule')
		                	.attr('x1', xScale(st))
		                	.attr('x2', xScale(st))
		                	.attr('y1', yScale(energyTo))
		                	.attr('y2', yScale(energyFrom));
	                	canvas
		                  	.append("text")
		                  	.attr('class', 'foSchedule')
		                  	.attr("x", xScale(st))
		                	.attr("y", yScale(energyTo)+15)		                	
		                	.style("text-anchor", "end")
		                	.text("ST:"+st.hhmm());
	                };
	            };
            });
    }}}]);