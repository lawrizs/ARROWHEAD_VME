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
angular
		.module('foaManApp')
		.directive(
				'aggVisual',
				function($compile, schemas) {
					return {
						restrict : 'E',
						require : 'ngModel',
						scope : {
							model : '=ngModel'
						},
						link : function($scope, element, attributes) {
							
							/* This traverses the elements and properties of an object, applying a function on each element */
							var traverseObj = function(obj, fn) {
								if (!fn(obj) && ((typeof obj === 'object')||(typeof obj === 'array'))) {
									angular.forEach(obj, function(value, key) {	traverseObj(value, fn);	});										
								}
							}
							
							/* Flex-offer support */							
							var isFO = function(o) {
								return  o != null &&
										o.hasOwnProperty('startAfterTime')
										&& o.hasOwnProperty('startBeforeTime')
										&& o.hasOwnProperty('slices');
							}
							
							var renderFOs = function(foList) {								
								$scope.foArray = [];
								
								for (var i = 0; i < foList.length; i++) {
										$scope.foArray.push(foList[i]);
								}

								var ngFoSet = angular.element("<div data-ng-fo-set flexoffers='foArray'></div>");
								element.append(ngFoSet);
								$compile(ngFoSet)($scope);																
							};
							
							/* Time series support */
							var isTS = function(o) {
								return (o != null) && (o.hasOwnProperty('series') || o.hasOwnProperty('data'));
							};
							
							var renderTSs = function(tsList) {
								var toRenderTS = function(o) {
										if (o.hasOwnProperty('series') && o.series.hasOwnProperty('data')) {
											var ro = $.extend(true, {}, o);
											// generate an array of random data
							                var data = [];
							                var starttime = new Date(ro.series.startTime).getTime();
							                var endtime = new Date(ro.series.endTime).getTime();
							                var step = (endtime - starttime) / (ro.series.data.length);						                    
							                
							                for(var i=0; i<ro.series.data.length; i++) {						                	
							                	data.push([starttime + step * i, ro.series.data[i]]);
							                }
							                ro.data = data;
							                delete ro.series;
							                return ro;
										};
										return null;
									};
								if (tsList.length > 0) {
									$scope.tsArray = tsList.map(function(o){ return toRenderTS(o); }).filter(function(o) { return o !== null; });
									var tsSet = angular.element("<div data-ng-ts-set-view timeseries='tsArray'></div>");
									element.append(tsSet);
									$compile(tsSet)($scope);
								}
							};
							
							var koContainer = document.createElement("div"),
							    koEditor = new JSONEditor(koContainer, {schema: schemas.supportedSchemas[0]}),
							    koValidator = new JSONEditor.Validator(koEditor, schemas.supportedSchemas[0]);
							
							/* Known object support */							
							var checkKnownObj = function(o) {
								// return false; // Too slow - disable for now.
								
								if (!(typeof o === 'object')) { return false; }; // Early return
								
								for(var i=0; i<schemas.analyticalSchemas.length; i++) {
									var results = koValidator._validateSchema(schemas.analyticalSchemas[i], o);									
									if (!results.length) { // No validation errors
										found = true;
										element.append(new JSONEditor(element[0], {schema: schemas.analyticalSchemas[i],
																				   startval: o, iconlib: "bootstrap3", theme: "bootstrap3" }));	
										return true;
									};
								}
															
								return false;
							}
							
							$scope.$watch("model",
									function(newValue, oldValue) {
												var m = newValue;
												
												element.empty();

												var FOs = [],
													TSs = [],
													hasKnownObj = false;
												
												traverseObj(m, function(o) {
													if (isFO(o)) { FOs.push(o);	return true; }
													if (isTS(o)) { TSs.push(o); return true; }
													if (checkKnownObj(o)) { hasKnownObj = true; return true;}
													return false;
												})
												
												if (m === null || (FOs.length == 0 && TSs.length == 0 && !hasKnownObj)) { 
													element.append(angular.element("<p>No visual object to show</p>"));
												} else {
													if (FOs.length > 0) { renderFOs(FOs); }
													if (TSs.length > 0) { renderTSs(TSs); }
												}
											}, true);
						}
					}
				});
