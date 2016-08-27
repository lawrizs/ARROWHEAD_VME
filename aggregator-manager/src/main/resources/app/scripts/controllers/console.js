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

angular.module('foaManApp')
  .controller('ConsoleCtrl', ['$scope', 'serviceFOA', function ($scope, serviceFOA) {	    	    
	    $scope.isConsoleHidden = true;
        $scope.wasErrorPrinted = false;
       
        var connect = function (){            	
        		if ($scope.ws !== undefined && $scope.ws.readyState !== 3)
        			{ return; }
        		
			    $scope.ws = new WebSocket(serviceFOA.consoleUrl, ['consoleRawProtocol']);
			    // ws.binaryType = 'arraybuffer';
			    
			    $scope.ws.onopen = function () {
		            console.log('The console socket opened successfully!');
		            
		            $scope.wasErrorPrinted = false;	            
		            $scope.$broadcast('terminal-output', {
			            output: true,
			            text: ['Welcome to the AggregatorManager console.',
			                   '',
			                   'Please type "help();" for a list of commands.'],
			            breakLine: false
			        });
			        $scope.$apply();		            
		        };
		
		        $scope.ws.onerror = function (error) {
		        	  console.log('The console Error ' + error);
		        	  
		        	  if ($scope.wasErrorPrinted == false)
		        		  {
		        		  	  $scope.wasErrorPrinted = true; 
				        	  $scope.$broadcast('terminal-output', {
						            output: true,
						            text: ['No connection to the console!'],
						            breakLine: false
						        });
						      $scope.$apply();
		        		  };
		        	  
		        	  setTimeout(connect(), 1000);			          
		        };
		
		        $scope.ws.onmessage = function (e) {		        	
			          var f = new FileReader();			          
			          f.onload = function(e) {			        	  
			              $scope.$broadcast('terminal-output', {
			                  output: true,
			                  text: e.target.result.split('\n').filter(function(s){ return s.length > 0; }),
			                  breakLine: false
			              });
			              $scope.$apply();
			          };			          
			          f.readAsText(e.data);
		        };        
		
		        $scope.ws.onclose = function () {
		            console.log('The console socket has closed! Reconnecting');
		            
		            setTimeout(connect(), 1000);
		        };
        };
            
        connect();
        
        $scope.$on('terminal-input', function (e, consoleInput) {
	        var cmd = consoleInput[0];
	        
	        if (cmd.command !== null)
 	        	{
					if ($scope.ws !== null && $scope.ws.readyState === 1) {
						$scope.ws.send(cmd.command);
					} else {
						setTimeout(connect(), 1000);
					}
					console.log(cmd);
				}
	    });

  }])
  .config(['terminalConfigurationProvider', function (terminalConfigurationProvider) {
	  terminalConfigurationProvider.config('default').allowTypingWriteDisplaying = false;
	  terminalConfigurationProvider.config('default').promptConfiguration = { end: '>', user: 'FOA User', separator: '', path: '\\' };
}]);
