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

angular.module('foaManApp').controller('BillingCtrl',
		function($scope, $location, serviceFOA, $modal) {
			$scope.customers = serviceFOA.Customers.query();
			$scope.showBill = function(customerId) {
				 var modalInstance = $modal.open({
				      templateUrl: 'billModalTemplate.html',
				      controller: 'BillingCtrl_BillModal',
				      size: 'lg',
				      resolve: {
				    	customerId : function() { return customerId; }
				      }
				    });		    
				 modalInstance.result.then(function () {});	 
			}
		}
)
.controller('BillingCtrl_DefContract', function ($scope, serviceFOA, schemas, $route) {
  	   JSONEditor.defaults.options.no_additional_properties = true;
	   $scope.defaultContract = serviceFOA.DefaultContract.get();
	   $scope.defaultContractSchema =  schemas.contractSchema;
	    
	   $scope.onDefaultContractSave = function(event) {
	    	var newDefContract = new serviceFOA.DefaultContract($scope.editor.getValue());
	    	newDefContract.$save().then(function(thing){
	    		$route.reload();
	        });
	   };	    
 })
 .controller('BillingCtrl_BillModal', function ($scope, $modalInstance, customerId, serviceFOA) {
	 $scope.bill = serviceFOA.CustomerBill.get({cid: customerId});
	 
	 $scope.close = function () {
		    $modalInstance.dismiss('cancel');
	 };  	   	    
 }) ;
