package org.arrowhead.wp5.core.entities;

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


import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class describes a flex-offer in the discrete time.      
 */
/**
 * @author Laurynas
 *
 */
@XmlRootElement(name="flexOffer")
@XmlAccessorType(XmlAccessType.FIELD)
public class FlexOffer implements Serializable, Cloneable {
    private static final Logger logger = LoggerFactory.getLogger(FlexOffer.class);
    
	/* Specified the relation between the intervals and minutes */
	public static final int numSecondsPerInterval()
	{
		return 15*60;	/* 1 flex-offer interval corresponds to 15*60 seconds/15 minutes */
	}
	
	/* Convert discrete FlexOffer time to abosolute time */
	public static final Date toAbsoluteTime(long foTime)
	{
		long timeInMillisSinceEpoch = foTime * (numSecondsPerInterval() * 1000);
		return new Date(timeInMillisSinceEpoch);
	}
	
	/* Convert abosolute time to discrete FlexOffer time */
	public static final long toFlexOfferTime(Date time)
	{
		long timeInMillisSinceEpoch = time.getTime();		
		return timeInMillisSinceEpoch / (numSecondsPerInterval() * 1000);
	}
	
	private static final long serialVersionUID = -6141402215673014415L;
		
	/**
	 *  ID of flex-offer. 
	 *  
	 *  A clarification of this ID is:
	 *  The flex-offer owner/sender sets this ID to a value it prefers. When a flex-offer is sent to 
	 *  a receiver, the receiver creates a local flex-offer copy, returning an ID of its local copy.
	 *  
	 *  If the sender wants to update/delete the flex-offer on the receiver, it must use the receiver's local ID.
	 *  It the receiver wants to update/delete the flex-offer on the sender, it must use the sender's local ID.  
	 * 
	 *  @author Laurynas
	 */
	@XmlAttribute
	private int id = 0;

	@XmlAttribute
	private FlexOfferState state = FlexOfferState.Initial;
	
	@XmlAttribute
	private String stateReason=null;
	
	@XmlTransient
	private long creationInterval;

	// negotiation time constrains
	@XmlTransient
	private long acceptanceBeforeInterval;

	// start time constrains
	@XmlTransient
	private long assignmentBeforeInterval; // Not set (no constraint)
	
	// Assign this interval number before the scheduled start time of the flex-offer 
	@XmlTransient
	private long assignmentBeforeDurationIntervals = 0; // Not set (no constraint)
	
	/**
	 * Derived sum of duration from all consecutive energy intervals. It is
	 * based on discrete time intervals.
	 */
	private transient int duration = Integer.MIN_VALUE;
	
	// energy intervals
	@XmlElement
	private FlexOfferSlice[] slices = new FlexOfferSlice[0];

	@XmlAttribute
	private String offeredById = ""; // Is not associated with any legal entity

	@XmlTransient
	private long startAfterInterval;

	@XmlTransient
	private long startBeforeInterval;

	// if null then no total constraint
	@XmlElement
	private FlexOfferConstraint totalEnergyConstraint = null;
	
	@XmlElement(nillable = true)
	private FlexOfferSchedule flexOfferSchedule = null;
	
	/* 2015-08-05 Added the support for the default schedule, aka "baseline" */
	@XmlElement(nillable = true)
	private FlexOfferSchedule defaultSchedule = null;

	/**
	 * Initializes an empty discrete flex-offer. 
	 */
	public FlexOffer() {}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FlexOffer other = (FlexOffer) obj;
		if (acceptanceBeforeInterval != other.acceptanceBeforeInterval)
			return false;
		if (assignmentBeforeDurationIntervals != other.assignmentBeforeDurationIntervals)
			return false;
		if (assignmentBeforeInterval != other.assignmentBeforeInterval)
			return false;
		if (creationInterval != other.creationInterval)
			return false;
		if (flexOfferSchedule == null) {
			if (other.flexOfferSchedule != null)
				return false;
		} else if (!flexOfferSchedule.equals(other.flexOfferSchedule))
			return false;
		if (defaultSchedule == null) {
			if (other.defaultSchedule != null)
				return false;
		} else if (!defaultSchedule.equals(other.defaultSchedule))
			return false;		
		if (id != other.id)
			return false;
		if (offeredById == null) {
			if (other.offeredById != null)
				return false;
		} else if (!offeredById.equals(other.offeredById))
			return false;
		if (!Arrays.equals(slices, other.slices))
			return false;
		if (startAfterInterval != other.startAfterInterval)
			return false;
		if (startBeforeInterval != other.startBeforeInterval)
			return false;
		if (state != other.state)
			return false;
		if (stateReason == null) {
			if (other.stateReason != null)
				return false;
		} else if (!stateReason.equals(other.stateReason))
			return false;
		if (totalEnergyConstraint == null) {
			if (other.totalEnergyConstraint != null)
				return false;
		} else if (!totalEnergyConstraint.equals(other.totalEnergyConstraint))
			return false;
		return true;
	}
	
	@Override
	public Object clone() {
        try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	     

	public long getAcceptanceBeforeInterval() {
		return acceptanceBeforeInterval;
	}

	public void setAcceptanceBeforeInterval(long acceptanceBeforeInterval) {
		this.acceptanceBeforeInterval = acceptanceBeforeInterval;
	}

	@XmlAttribute
	public Date getAcceptanceBeforeTime() {
		return FlexOffer.toAbsoluteTime(this.acceptanceBeforeInterval);
	}

	public void setAcceptanceBeforeTime(Date time) {
		this.acceptanceBeforeInterval = FlexOffer.toFlexOfferTime(time);
	}
	/**
	 * @return 	assign before duration 
	 */
	public long getAssignmentBeforeDurationIntervals() {
		return assignmentBeforeDurationIntervals;
	}
		
	public void setAssignmentBeforeDurationIntervals(long assignmentBeforeDurationIntervals) {
		this.assignmentBeforeDurationIntervals = assignmentBeforeDurationIntervals;
	}

	@XmlAttribute
	public long getAssignmentBeforeDurationSeconds() {
		return FlexOffer.numSecondsPerInterval() * this.assignmentBeforeDurationIntervals; 
	}

	public void setAssignmentBeforeDurationSeconds(int seconds) {
		this.assignmentBeforeDurationIntervals = seconds / FlexOffer.numSecondsPerInterval();
	}

	/**
	 * @return 	assign before intervals 
	 */
	public long getAssignmentBeforeInterval() {
		return assignmentBeforeInterval;
	}
	
	public void setAssignmentBeforeInterval(long assignmentBeforeInterval) {
		this.assignmentBeforeInterval = assignmentBeforeInterval;
	}

	@XmlAttribute
	public Date getAssignmentBeforeTime() {
		return FlexOffer.toAbsoluteTime(this.assignmentBeforeInterval);
	}

	public void setAssignmentBeforeTime(Date time) {
		this.assignmentBeforeInterval = FlexOffer.toFlexOfferTime(time);
	}
	

	public long getCreationInterval() {
		return creationInterval;
	}
	
	public void setCreationInterval(long creationInterval) {
		this.creationInterval = creationInterval;
	}
	
	@XmlAttribute
	public Date getCreationTime() {
		return FlexOffer.toAbsoluteTime(this.creationInterval);
	}

	public void setCreationTime(Date time) {
		this.creationInterval = FlexOffer.toFlexOfferTime(time);
	}
	
	/**
	 * @return duration in number of intervals
	 * Laurynas: I still think that getDuration is a better name. Note, that a duration can be higher than an interval count.
	 */
	public int getDurationIntervals() {
		if (duration == Integer.MIN_VALUE) {
			duration = 0;
			for (FlexOfferSlice slice : slices) {
				duration += slice.getDuration();
			}
		}
		return duration;
	}


	@XmlAttribute
	public int getDurationSeconds() {
		return FlexOffer.numSecondsPerInterval() * getDurationIntervals();
	}
	
	public long getEndAfterInterval() {
		return getStartAfterInterval() + getDurationIntervals();
	}
	
	@XmlAttribute
	public Date getEndAfterTime() {
		return FlexOffer.toAbsoluteTime(getEndAfterInterval());
	}
	
	public long getEndBeforeInterval() {
		return getStartBeforeInterval() + getDurationIntervals();
	}


	@XmlAttribute
	public Date getEndBeforeTime() {
		return FlexOffer.toAbsoluteTime(getEndBeforeInterval());
	}
	
	public FlexOfferSchedule getFlexOfferSchedule() {
		return flexOfferSchedule;
	}

	public int getId() {
		return id;
	}


	/**
	 *  Results the global ID of the legal entity role that issued this flex-offer 
	 * @return the offeredById
	 */
	public String getOfferedById() {
		return offeredById;
	}
			
	/**
	 * @param i the index of the energy interval
	 * @return the i-th energy interval
	 */
	public FlexOfferSlice getSlice(int i) {
		return slices[i];
	}

	/**
	 * Returns the energy intervals.
	 * 
	 * @return the energy intervals
	 */
	public FlexOfferSlice[] getSlices() {
		return slices;
	}
	
	/**
	 * Get time interval duration in seconds. This is a system-wide constant
	 */
	@XmlAttribute
	public int getNumSecondsPerInterval(){
		return FlexOffer.numSecondsPerInterval();
	}

	
	public long getStartAfterInterval() {
		return this.startAfterInterval;
	}
	
	@XmlAttribute
	public Date getStartAfterTime() {
		return FlexOffer.toAbsoluteTime(this.startAfterInterval);
	}
	
	public void setStartAfterTime(Date time) {
		this.startAfterInterval = FlexOffer.toFlexOfferTime(time);
	}
	
	public long getStartBeforeInterval() {
		return this.startBeforeInterval;
	}
	
	@XmlAttribute
	public Date getStartBeforeTime() {
		return FlexOffer.toAbsoluteTime(this.startBeforeInterval);
	}

	public void setStartBeforeTime(Date time) {
		this.startBeforeInterval = FlexOffer.toFlexOfferTime(time);
	}

	public FlexOfferState getState() {
		return state;
	}
	
	public String getStateReason() {
		return stateReason;
	}

	/**
	 * This method computes the totalEnergyConstraint of the FlexOffer.
	 * 
	 * @return EnergyConstraint
	 */
	// If total energy constraints are not specified, we simply compute them if
	// needed
	public FlexOfferConstraint getSumEnergyConstraints() {
		double totalLower = 0, totalUpper = 0;
		for (FlexOfferSlice i : this.slices) {
			totalUpper += i.getEnergyUpper();
			totalLower += i.getEnergyLower();
		}
		return new FlexOfferConstraint(totalLower, totalUpper);
	}

	/**
	 * This method computes the totalTariffConstraint (price) of the FlexOffer profile.
	 * 
	 * @return tariffConstraint of type EnergyConstraint
	 */
	public FlexOfferConstraint getSumTariffConstraints(){
		double totalLower = 0, totalUpper = 0;
		for (FlexOfferSlice i : this.slices) {
			if (i.getTariffConstraint() != null)
			{
				totalUpper += i.getTariffConstraint().getUpper();
				totalLower += i.getTariffConstraint().getLower();
			}
		}
		return new FlexOfferConstraint(totalLower, totalUpper);
	
	}

	/**
	 * Returns the total energy constraint.
	 * 
	 * @return the total energy constraint
	 */
	public FlexOfferConstraint getTotalEnergyConstraint() {
		return totalEnergyConstraint;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ (int) (acceptanceBeforeInterval ^ (acceptanceBeforeInterval >>> 32));
		result = prime
				* result
				+ (int) (assignmentBeforeDurationIntervals ^ (assignmentBeforeDurationIntervals >>> 32));
		result = prime
				* result
				+ (int) (assignmentBeforeInterval ^ (assignmentBeforeInterval >>> 32));
		result = prime * result
				+ (int) (creationInterval ^ (creationInterval >>> 32));
		result = prime
				* result
				+ ((flexOfferSchedule == null) ? 0 : flexOfferSchedule
						.hashCode());
		result = prime
				* result
				+ ((defaultSchedule == null) ? 0 : defaultSchedule.hashCode());		
		result = prime * result + id;
		result = prime * result
				+ ((offeredById == null) ? 0 : offeredById.hashCode());
		result = prime * result + Arrays.hashCode(slices);
		result = prime * result
				+ (int) (startAfterInterval ^ (startAfterInterval >>> 32));
		result = prime * result
				+ (int) (startBeforeInterval ^ (startBeforeInterval >>> 32));
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result
				+ ((stateReason == null) ? 0 : stateReason.hashCode());
		result = prime
				* result
				+ ((totalEnergyConstraint == null) ? 0 : totalEnergyConstraint
						.hashCode());
		return result;
	}

	
	public boolean isConsumption() {
		boolean isCons = false;
		boolean isProd = false;
		
		for(FlexOfferSlice slice : this.slices)
		{
			isCons |= slice.isConsumption();
			isProd |= slice.isProduction();
		}
		
		/* Is a flex-offer is neither consumption or production, it is consumption*/
		if (!isCons && !isProd)		
			return true; 
		
		return isCons;
	}

	/**
	 * @TODO Need to implement. Check constraints, etc. 
	 * @return
	 */
    public boolean isCorrect() {
        if (state == null) {
            logger.info("state is null.");
            return false;
        }
        if (offeredById == null || "".equals(offeredById)) {
            logger.info("offeredById is null or empty.");
            return false;
        }
        if (slices == null) {
            logger.info("slices is null.");
            return false;
        }
        if (defaultSchedule != null && !defaultSchedule.isCorrect(this)) {
            logger.info("defaultSchedule is not null but incorrect");
            return false;
        }
        if (flexOfferSchedule != null && !flexOfferSchedule.isCorrect(this)) {
            logger.info("flexOfferSchedule is not null but incorrect");
            return false;
        }
        if (startAfterInterval > startBeforeInterval) {
            logger.info("startAfterInterval is after startBeforeInterval");
            return false;
        }
        
        if (acceptanceBeforeInterval > this.getEndAfterInterval()) {
            logger.info("acceptanceBeforeInterval is after endBeforeInterval");
            return false;
        }
        if (assignmentBeforeInterval > this.getEndAfterInterval()) {
            logger.info("assignmentBeforeInterval is after endBeforeInterval");
            return false;
        }
       /* if (assignmentBeforeDurationIntervals < startBeforeInterval) {
            logger.info("assignmentBeforeDurationIntervals is after startBeforeInterval");
            return false;
        }*/

//        for (FlexOfferSlice s : slices) {
//            check totalEnergyConstraint
//        }
        return true;
    }

	public boolean isProduction() {
		boolean isProd = false;		
		
		for(FlexOfferSlice slice : this.slices)
			isProd |= slice.isProduction();
		
		return isProd;
	}
	
	/**
	 * Removes the total energy constraints.
	 */
	public void removeTotalEnergyConstraint() {
		totalEnergyConstraint = null;
	}
	
	public void setFlexOfferSchedule(FlexOfferSchedule flexOfferSchedule) {
		this.flexOfferSchedule = flexOfferSchedule;
		this.state = FlexOfferState.Assigned;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Set the global ID of the legal entity role that issues this flex-offer
	 * 
	 * @param offeredById the offeredById to set
	 */
	public void setOfferedById(String offeredById) {
		this.offeredById = offeredById;
	}

	public void setSlices(FlexOfferSlice[] slices) {
		this.slices = slices;
	}

	public void setStartAfterInterval(long startAfterInterval) {
		this.startAfterInterval = startAfterInterval;
	}

	public void setStartBeforeInterval(long startBeforeInterval) {
		this.startBeforeInterval = startBeforeInterval;
	}

	public void setState(FlexOfferState state) {
		this.state = state;		
	}

	public void setStateReason(String stateReason) {
		this.stateReason = stateReason;
	}

	/**
	 * @param totalEnergyConstraint
	 *            the totalEnergyConstraint to set
	 */
	public void setTotalEnergyConstraint(FlexOfferConstraint totalEnergyConstraint) {
		this.totalEnergyConstraint = totalEnergyConstraint;
	}
	
	public FlexOfferSchedule getDefaultSchedule() {
		return defaultSchedule;
	}

	public void setDefaultSchedule(FlexOfferSchedule defaultSchedule) {
		this.defaultSchedule = defaultSchedule;
	}


	/**
	 * Laurynas: Please use a more meaningful name.
	 * 
	 * @param timeInterval
	 * @return true if timepoint is within offered flexibility
	 */
	public boolean spansOverInterval(long timeInterval) {
		return (timeInterval >= getStartAfterInterval() && timeInterval < getEndBeforeInterval());
	}

	
	public String toJsonString(){return null;}
	
	public String toXmlString(){return null;}	
	

//	@Override
//	public String toString() {
//		StringBuilder result = new StringBuilder();
//
//		String newLine = System.getProperty("line.separator");
//		final String tab = "\t";
//
//		int currentTime = 0;
//
//		if (getSlices() != null) {
//			for (int i = 0; i < getSlices().length; i++) {
//				result.append("FO" + tab + currentTime + tab);
//				if (getProductType().isProduction()) {
//					result.append("Prod");
//					result.append(energyIntervals[i]);
//					result.append(tab);
//				}
//				else if (getProductType().isConsumption()) {
//					result.append("Cons");
//					result.append(energyIntervals[i]);
//					result.append(tab);
//				} else {
//					result.append("not Prod or Cons");
//					result.append(energyIntervals[i]);
//					result.append(tab);
//				}
//
//				result.append(energyIntervals[i].getDuration() + tab);
//				result.append(energyIntervals[i].getCostPerEnergyUnitLimit() + tab);
//				if (totalEnergyConstraint == null) {
//					result.append("-" + tab + "-" + tab);
//				} else if (getProductType().isProduction()) {
//					result.append(totalEnergyConstraint.getEnergyLower() + tab);
//					result.append(totalEnergyConstraint.getEnergyUpper() + tab);
//				} else if (getProductType().isConsumption()) {
//					result.append(-totalEnergyConstraint.getEnergyUpper() + tab);
//					result.append(-totalEnergyConstraint.getEnergyLower() + tab);
//				}
//				result.append(this.startAfterInterval + tab);
//				result.append(this.startBeforeInterval + tab);
//				result.append(this.getAssignmentBeforeInterval() + tab);
//				result.append(this.getDuration() + newLine);
//
//				currentTime += energyIntervals[i].getDuration();
//			}
//		} else {
//			result.append(" no intervals defined");
//		}		
//		
//		return result.toString();
//	}
//
//	public String toCompactString() {
//		StringBuilder result = new StringBuilder();
//
//		String newLine = System.getProperty("line.separator");
//		final String tab = "\t";
//		
//		result.append("FO-info " + tab);
//		if (getProductType().isProduction()) {
//			result.append("P" + tab);
//		}
//		else if (getProductType().isConsumption()) {
//			result.append("C" + tab);
//		} 
//		if (flexOffer != null) {
//			result.append(flexOffer.getId() + tab);
//		}
//		else {
//			result.append("-1" + tab);
//		}
//		result.append(startAfterInterval + tab);
//		result.append(startBeforeInterval + tab);
//		result.append(this.getDuration() + tab);
//		
//		if (totalEnergyConstraint == null) {
//			result.append("-" + tab + "-" + tab);
//		} else {
//			result.append(totalEnergyConstraint.getEnergyLower() + tab);
//			result.append(totalEnergyConstraint.getEnergyUpper() + tab);
//		} 
//		result.append(newLine);
//
//		int currentTime = 0;
//
//		if (getDiscreteEnergyIntervals() != null) {
//			for (int i = 0; i < getDiscreteEnergyIntervals().length; i++) {
//				
//				result.append("FO-slice" + tab);
//
//				result.append(i + tab);
//				result.append(currentTime + tab);
//				result.append(energyIntervals[i].getDuration() + tab);
//				result.append(energyIntervals[i].getCostPerEnergyUnitLimit() + tab);
//				result.append(energyIntervals[i].getEnergyLower() + tab);
//				result.append(energyIntervals[i].getEnergyUpper() + tab);
//				result.append(newLine);
//
//				currentTime += energyIntervals[i].getDuration();
//			}
//		} 
//		
//		return result.toString();
//	}

}
