package org.arrowhead.wp5.core.interfaces;

/*-
 * #%L
 * ARROWHEAD::WP5::Core Data Structures
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


import java.util.ArrayList;

import org.arrowhead.wp5.core.entities.ArrowheadException;

/**
 * This is an interface of the Arrowhead subsystem
 * 
 * @author IL0021B
 * @version 1.0
 * @created 19-Kov-2014 11:41:16
 */
public interface ArrowheadIf
{
	/**
	 * Register a service into the Arrowhead SD
	 * @param serviceProducer/serviceConsumer	// TODO
	 * @throws ArrowheadException
	 * @return true if sucessfully published | false if failed to publish 
	 */
	public Boolean publishService(String producerName) throws ArrowheadException;
	
	/**
	 * Unregister a service from the Arrowhead SD
	 * @param serviceProducer/serviceConsumer	// TODO
	 * @throws ArrowheadException
	 * @return true if sucessfully unpublished | false if failed to unpublish
	 */
	public Boolean unPublishService(String producerName) throws ArrowheadException;
	
	/**
	 * Lookup for producers of certain service type
	 * @param serviceType
	 * @throws ArrowheadException
	 * @return set of service producers (empty if none) 
	 */
	public ArrayList<?> lookupServiceProducers(String serviceType) throws ArrowheadException;
	
	/**
	 * Checks if a specific system is authorized in the Arrowhead Framework
	 * @param serviceId
	 * @throws ArrowheadException
	 * @return true if system is authorized | false if system is not authorized 
	 *
	public Boolean checkAuth() throws ArrowheadException;*/
	
	/**
	 * Checks if there is an active configuration for the system in the Orchestration
	 * @throws ArrowheadException
	 * @return active Orchestration configuration
	 *
	public String checkActiveConfiguration() throws ArrowheadException;*/
	
	/**
	 * Shutdown all service producers and service consumers of the Arrowhead Framework
	 * @throws ArrowheadException
	 * @return true if successfully destroyed | false if services producers and consumers were not destroyed
	 */
	public void shutdownArrowheadApp() throws ArrowheadException;
	
}//end ArrowheadSubsystem