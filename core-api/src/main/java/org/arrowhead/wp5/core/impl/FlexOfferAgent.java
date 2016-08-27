package org.arrowhead.wp5.core.impl;

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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.arrowhead.wp5.core.entities.AbstractDER;
import org.arrowhead.wp5.core.entities.FlexOffer;
import org.arrowhead.wp5.core.entities.FlexOfferException;
import org.arrowhead.wp5.core.entities.FlexOfferSchedule;
import org.arrowhead.wp5.core.entities.FlexOfferState;
import org.arrowhead.wp5.core.interfaces.DERSubscriberIf;
import org.arrowhead.wp5.core.interfaces.FlexOfferAgentProviderIf;
import org.arrowhead.wp5.core.interfaces.FlexOfferUpdateListener;
import org.arrowhead.wp5.core.wrappers.FlexOfferKey;

/**
 * FlexOfferAgent is: - Producer/Provider of a FlexOfferAgent service instance -
 * Consumer/Subscriber of many instances of DER services
 * 
 * @author Laurynas
 *
 */
public class FlexOfferAgent implements FlexOfferAgentProviderIf,
		DERSubscriberIf {
	// FlexOfferAgent DER and flexOffer counter
	protected static final AtomicInteger derCounter = new AtomicInteger(0);
	protected static final AtomicInteger foCounter = new AtomicInteger(0);

	private String id;

	// Specifies the flex-offer agent mode
	private FlexOfferAgentMode mode = FlexOfferAgentMode.fmOfflinePassive;

	// A list of all DERs and DER flex-offers
	private Map<Integer, AbstractDER> aders;

	// Flex-offer agent local flex-offers, indexed by FOA IDs, and expecting
	// that each FO
	// has the same FOA ID.
	protected Map<FlexOfferKey, FlexOffer> foa_fos;

	private FlexOfferUpdateListener foLst;

	// The associated FOA subscriber (Aggregator) and its flex-offers
	// private FlexOfferAgentSubscriberIf foa_subscriber;

	/*
	 * This stores the mapping between the subscriber's flexOffer IDs and the
	 * FOA flexoffer IDs.
	 */
	// private Map<Integer, Integer> foa_subscriber_fo_ids;

	public FlexOfferAgent(String id, FlexOfferUpdateListener foLst) {
		this.aders = new HashMap<Integer, AbstractDER>();
		this.id = id;
		this.foa_fos = new HashMap<FlexOfferKey, FlexOffer>();
		this.foLst = foLst;
	}

	public String getId() {
		return id;
	}

	/**
	 * This is called to create a schedule for a given FOA flex-offer
	 */
	@Override
	public void createFlexOfferSchedule(int flexOfferId,
			FlexOfferSchedule flexOfferSchedule) throws FlexOfferException {

		FlexOffer fo = this.getFlexOffer(flexOfferId);

		if (fo != null) {
			if (flexOfferSchedule != null && flexOfferSchedule.isCorrect(fo)) {
				fo.setState(FlexOfferState.Assigned);
				fo.setFlexOfferSchedule(flexOfferSchedule);
				String derId = this.getDerIdFromFlexOffer(flexOfferId);
				AbstractDER ader = this.getAgentDer(Integer.parseInt(derId));
				ader.updateSchedule(fo);
			} else
				throw new FlexOfferException(
						"The flex-offer schedule is incorrect!");
		}
		this.foLst.onFlexOfferScheduleUpdate(fo);
	}

	@Override
	public void deleteFlexOfferSchedule(int flexOfferId)
			throws FlexOfferException {
		FlexOffer fo = this.getFlexOffer(flexOfferId);

		if (fo != null) {
			fo.setState(FlexOfferState.Accepted);
			fo.setFlexOfferSchedule(null);

			// /* Automatically re-route new schedules to DERs */
			// for (AbstractDER ader : this.aders.values()){
			// ader.updateDERschedule(flexOfferId);
			// }
		}
	}

	/* Returns a particular registered DER */
	public AbstractDER getAgentDer(int derid) {
		return this.aders.get(derid);
	}

	public String getDerIdFromFlexOffer(int flexOfferId) {
		for (FlexOfferKey foKey : foa_fos.keySet()) {
			if (Integer.parseInt(foKey.getId()) == flexOfferId) {
				return foKey.getOwnId();
			}
		}

		return null;
	}

	/* Returns all registered DERs */
	public ArrayList<AbstractDER> getAgentDers() {
		return new ArrayList<AbstractDER>(this.aders.values());
	}

	@Override
	public FlexOffer getFlexOffer(int flexOfferId) {
		for (FlexOfferKey foKey : foa_fos.keySet()) {
			if (Integer.parseInt(foKey.getId()) == flexOfferId) {
				return foa_fos.get(foKey);
			}
		}
		return null;
	}

	@Override
	public FlexOffer[] getFlexOffers() {
		return this.foa_fos.values().toArray(new FlexOffer[] {});
	}

	@Override
	public FlexOfferSchedule getFlexOfferSchedule(int flexOfferId) {
		FlexOffer fo = getFlexOffer(flexOfferId);
		if (fo != null)
			return fo.getFlexOfferSchedule();
		return null;
	}

	@Override
	public FlexOfferState getFlexOfferState(int flexOfferId) {
		FlexOffer fo = getFlexOffer(flexOfferId);
		if (fo != null)
			return fo.getState();
		return null;
	}

	// public FlexOfferAgentSubscriberIf getFoa_subscriber() {
	// return foa_subscriber;
	// }

	public FlexOfferAgentMode getMode() {
		return mode;
	}

	public void registerDer(AbstractDER der) {
		int id = derCounter.incrementAndGet();
		der.setId(Integer.toString(id)); /* ID in the FOA system */
		this.aders.put(id, der);
	}

	@Override
	public void setFlexOfferSchedule(int flexOfferId,
			FlexOfferSchedule flexOfferSch) throws FlexOfferException {
		this.createFlexOfferSchedule(flexOfferId, flexOfferSch);
	}

	@Override
	public void setFlexOfferState(int flexOfferId,
			FlexOfferState flexOfferState, String stateReason)
			throws FlexOfferException {
		FlexOffer fo = getFlexOffer(flexOfferId);
		if (fo != null) {
			// TODO: Implement state transition checking
			fo.setState(flexOfferState);
			fo.setStateReason(stateReason);
		}
	}

	public void setMode(FlexOfferAgentMode mode) {
		this.mode = mode;
	}

	@Override
	public int createFlexOffer(String ownerId, FlexOffer flexOffer)
			throws FlexOfferException {
		flexOffer.setId(foCounter.incrementAndGet());
		flexOffer.setOfferedById(id);
		this.foa_fos.put(
				new FlexOfferKey(ownerId, Integer.toString(flexOffer.getId())),
				flexOffer);
		this.foLst.onFlexOfferCreate(flexOffer);
		return flexOffer.getId();
	}

	@Override
	public FlexOffer getFlexOffer(String ownerId, int flexOfferId) {
		return this.foa_fos.get(new FlexOfferKey(ownerId, Integer
				.toString(flexOfferId)));
	}

	@Override
	public void setFlexOffer(String ownerId, int flexOfferId,
			FlexOffer flexOffer) throws FlexOfferException {
		flexOffer.setId(flexOfferId);
		flexOffer.setOfferedById(id);
		this.foa_fos.put(
				new FlexOfferKey(ownerId, Integer.toString(flexOffer.getId())),
				flexOffer);
		this.foLst.onFlexOfferCreate(flexOffer);
	}

	@Override
	public void deleteFlexOffer(String ownerId, int flexOfferId)
			throws FlexOfferException {
		// TODO Auto-generated method stub

	}

}
