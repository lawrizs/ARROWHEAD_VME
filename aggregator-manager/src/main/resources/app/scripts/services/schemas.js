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

/* Supported JSON schemas */
angular.module('foaManApp').factory(
		'schemas',
		function() {
			var factory = {};

			factory.aggParamsSchema = {
				type : "object",
				title : "Aggregation Parameters",
				options : {
					"collapsed" : true
				},
				properties : {
					"preferredProfileShape" : {
						title : "FlexOffer Profile Shape",
						"$ref" : "#/definitions/psProfileShape"
					},
					"constraintAggregate" : {
						type : "object",
						properties : {
							"aggConstraint" : {
								"$ref" : "#/definitions/acStatus"
							},
							"valueMin" : {
								"type" : "integer"
							},
							"valueMax" : {
								"type" : "integer"
							}
						}
					},
					"constraintPair" : {
						type : "object",
						properties : {
							"durationTolerance" : {
								"type" : "integer",
								"propertyOrder" : 1
							},
							"durationToleranceType" : {
								"$ref" : "#/definitions/acStatus",
								"propertyOrder" : 2
							},
							"startAfterTolerance" : {
								"type" : "integer",
								"propertyOrder" : 3
							},
							"startAfterToleranceType" : {
								"$ref" : "#/definitions/acStatus",
								"propertyOrder" : 4
							},
							"startBeforeTolerance" : {
								"type" : "integer",
								"propertyOrder" : 5
							},
							"startBeforeToleranceType" : {
								"$ref" : "#/definitions/acStatus",
								"propertyOrder" : 6
							},
							"assignmentBeforeTolerance" : {
								"type" : "integer",
								"propertyOrder" : 7
							},
							"assignmentBeforeToleranceType" : {
								"$ref" : "#/definitions/acStatus",
								"propertyOrder" : 8
							},
							"timesliceCountTolerance" : {
								"type" : "integer",
								"propertyOrder" : 9
							},
							"timesliceCountToleranceType" : {
								"$ref" : "#/definitions/acStatus",
								"propertyOrder" : 10
							},
							"timeFlexibilityTolerance" : {
								"type" : "integer",
								"propertyOrder" : 11
							},
							"timeFlexibilityToleranceType" : {
								"$ref" : "#/definitions/acStatus",
								"propertyOrder" : 12
							},
							"totalEnLowAmountTolerance" : {
								"type" : "integer",
								"propertyOrder" : 13
							},
							"totalEnLowAmountToleranceType" : {
								"$ref" : "#/definitions/acStatus",
								"propertyOrder" : 14
							},
							"totalEnHighAmountTolerance" : {
								"type" : "integer",
								"propertyOrder" : 15
							},
							"totalEnHighAmountToleranceType" : {
								"$ref" : "#/definitions/acStatus",
								"propertyOrder" : 16
							},
							"totalEnFlexibilityTolerance" : {
								"type" : "integer",
								"propertyOrder" : 17
							},
							"totalEnFlexibilityToleranceType" : {
								"$ref" : "#/definitions/acStatus",
								"propertyOrder" : 18
							},
							"minTariffTolerance" : {
								"type" : "integer",
								"propertyOrder" : 19
							},
							"minTariffToleranceType" : {
								"$ref" : "#/definitions/acStatus",
								"propertyOrder" : 20
							},
							"maxTariffTolerance" : {
								"type" : "integer",
								"propertyOrder" : 21
							},
							"maxTariffToleranceType" : {
								"$ref" : "#/definitions/acStatus",
								"propertyOrder" : 22
							},
							"minTariffProfileTolerance" : {
								"type" : "integer",
								"propertyOrder" : 23
							},
							"minTariffProfileToleranceType" : {
								"$ref" : "#/definitions/acStatus",
								"propertyOrder" : 24
							},
							"maxTariffProfileTolerance" : {
								"type" : "integer",
								"propertyOrder" : 25
							},
							"maxTariffProfileToleranceType" : {
								"$ref" : "#/definitions/acStatus",
								"propertyOrder" : 26
							},
							"costPerEnergyUnitLimitTolerance" : {
								"type" : "integer",
								"propertyOrder" : 27
							},
							"costPerEnergyUnitLimitType" : {
								"$ref" : "#/definitions/acStatus",
								"propertyOrder" : 28
							}
						}
					}
				},
				definitions : {
					psProfileShape : {
						type : 'string',
						enum : [ "psAlignStart", "psAlignEnds",
								"psUniformTimeFlex", "psFlat" ],
						required : true
					},
					acStatus : {
						type : 'string',
						enum : [ "acNotSet", "acSet" ],
						required : true
					}
				}
			};
			
			factory.xmppParamsSchema = {
		  		type: "object",
		        title: "XMPP Server Parameters",
		        options: { "collapsed": true  },
		        properties: {
		        	"password": { type: "string", required : true },
		        	"resource": { type: "string", required : true },
		        	"username": { type: "string", required : true },
		        	"xmppServer": { type: "string", required : true }	  	
		        	}
		        };
			
			factory.analyticsParamsSchema = {
		  		type: "object",
		        title: "Analytics Engine Settings",
		        options: { "collapsed": true  },
		        properties: {
		        	"aggregator_URL": { type: "string", required : true },
		        	"solveDB_URL": { type: "string", required : true },	  	
		        	}
		        };
			
			factory.contractSchema = {
			  		type: "object",
			        title: "Aggregator Contract",
			        options: { "collapsed": false },
			        properties: {
			        	"energyFlexReward": { type: "number", required : true, description : "Reward (in DKK) for a single unit (kWh) of energy flexibility" },
			        	"fixedReward": { type: "number", required : true, description : "Fixed reward (in DKK) for issuing at least 1 flexoffer" },
			        	"schedulingEnergyReward": { type: "number", required : true, description : "Reward (in DKK) for a single unit deviation (kWh) in energy amount compared to the default schedule (baseline)" },
			        	"schedulingFixedReward": { type: "number", required : true, description : "Fixed reward paid for deviating a single time (1 flexoffer) from the default schedule (baseline)" },
			        	"schedulingStartTimeReward": { type: "number", required : true, description : "Reward (in DKK) for a single time unit deviation (15min) from the default schedule (baseline)" },
			        	"timeFlexReward": { type: "number", required : true, description : "Reward (in DKK) for a single unit of time flexibility"}
			        	}
			        };
			
			/* All supported schemas */
			factory.supportedSchemas = [factory.aggParamsSchema, factory.xmppParamsSchema, factory.analyticsParamsSchema ];
			
			/* Schemas to be used for analytical queries */
			factory.analyticalSchemas = [factory.aggParamsSchema];

			return factory;
		});