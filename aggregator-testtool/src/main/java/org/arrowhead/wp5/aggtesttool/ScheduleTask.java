package org.arrowhead.wp5.aggtesttool;

import java.util.TimerTask;

import org.arrowhead.wp5.core.entities.FlexOffer;

public abstract class ScheduleTask extends TimerTask {
    FlexOffer flexOffer;

    public ScheduleTask(FlexOffer fo) {
        this.flexOffer = fo;
    }

    public FlexOffer getFlexOffer() {
        return flexOffer;
    }
}
