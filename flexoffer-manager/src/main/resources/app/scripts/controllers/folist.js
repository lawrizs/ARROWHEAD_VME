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
'use strict';

/**
 * @ngdoc function
 * @name webappApp.controller:AboutCtrl
 * @description
 * # AboutCtrl
 * Controller of the webappApp
 */
angular.module('foaManApp')
  .controller('FoListCtrl', function ($scope, serviceFOA, $modal, $timeout) {
	  $scope.refreshData = function() {
		  $scope.flList = serviceFOA.FlexOffers.query().$promise.then(
				  function(result){
					  $scope.flList = result;
					  /* Time/energy axis synchronization */
					  $scope.syncTAchanged($scope.syncTA);
					  $scope.syncEAchanged($scope.syncEA);
					  /* Clear the selection */
					  $scope.flSelection = []; 
				  });
	  };
	  $scope.refreshData();

	  /* By default, no flex-offers is selected */
	  $scope.flSelection = []; 
	  
	  /* Time/energy axis synchronization */
	  $scope.syncTA = true;  
	  $scope.syncEA = true;	  
	  $scope.timeRange = null;
	  $scope.energyRange = null;
	  	  
	  $scope.syncTAchanged = function(syncTA) {
	       if (syncTA == true && $scope.flList.length>0) {
	    	   var dateCmp = function(a,b){ return Date.parse(a) > Date.parse(b); };
	    	   
	    	   var orderedEST = $scope.flList.map(function(f) { return f.startAfterTime;}).sort(dateCmp);
	    	   var orderedLET = $scope.flList.map(function(f) { return f.endBeforeTime;}).sort(dateCmp);
	    	   
	    	   $scope.timeRange = [new Date(orderedEST[0]), new Date(orderedLET[orderedLET.length-1])];
	       } else {
	    	   $scope.timeRange = null;  	   
	       }	       
	  };
	  
	  $scope.syncEAchanged = function(syncEA) {
	       if (syncEA == true && $scope.flList.length>0) {
	    	   $scope.energyRange = [
							Math.min.apply(1e6, $scope.flList.map(function(f) {
								return Math.min.apply(1e6, f.slices
										.map(function(s) {
											return s.energyConstraint.lower;
										}));
							})),
							Math.max.apply(-1e6, $scope.flList.map(function(f) {
								return Math.max.apply(-1e6, f.slices
										.map(function(s) {
											return s.energyConstraint.upper;
										}));
							})) ];
	       } else {
	    	   $scope.energyRange = null;  	   
	       }	       
	   };
	   
	   /* Compute minutes between two dates - use in GUI */
	   $scope.dateDiff = function(date1, date2) {
			  var d1 = new Date(date1);
			  var d2 = new Date(date2);
			  return (d1-d2) / (1000 * 60);
	   }
	   $scope.toDate = function(date) {
		   return new Date(date);
	   }
	  
	  /* Modal flex-offer visualization/JSON output */
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
		
		/* Selection management */
		$scope.toggleSelection = function toggleSelection(flexoffer) {
		    var idx = $scope.flSelection.indexOf(flexoffer);
		    // is currently selected
		    if (idx > -1) {
		    	$scope.flSelection.splice(idx, 1);
		    }  else {
		      $scope.flSelection.push(flexoffer);
		    }
		};
		$scope.selectAll = function(sel) {
			$scope.flSelection.splice(0,$scope.flSelection.length);
			if (sel) {
				$scope.flList.map(function(f){$scope.flSelection.push(f);});				
			}
		}
		
	   /* For generating flex-offer schedules and acceptances */
	   $scope.generateAssignments = function(flist, add) {
		   for (var k=0; k<flist.length; k++)
			   {
			   	   var fo = flist[k];
				   var sch = new serviceFOA.FlexOfferSchedule({fid: fo.id});
				   
				   if (add) {
					   /* Generate random start time */
					   sch.startTime = new Date(
					   			new Date(fo.startAfterTime).getTime() + 
					   			Math.random() *  (new Date(fo.startBeforeTime).getTime() - new Date(fo.startAfterTime).getTime()));					   
					   sch.energyAmounts = []; 
					   for(var i=0; i<fo.slices.length; i++)
						   {
						   	sch.energyAmounts.push(fo.slices[i].energyConstraint.lower + Math.random() *
						   					(fo.slices[i].energyConstraint.upper - fo.slices[i].energyConstraint.lower));
						   }
					   sch.$save();
				   } else {
					   sch.$delete();
				   }
			   }
		   $timeout(function() {$scope.refreshData();}, 500);
		   
	   }
  }).controller('FoModalViewCtrl', function ($scope, $modalInstance, viewType, flexOffer) {

	  $scope.viewType = viewType;
	  $scope.flexOffer = flexOffer;
	  $scope.flexOfferJSON = angular.toJson($scope.flexOffer, true);
	  
	  $scope.close = function () {
	    $modalInstance.close();
	  };	  
	});;
