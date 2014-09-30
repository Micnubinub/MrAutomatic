package com.micnubinub.mrautomatic;


/**
 * Created by root on 20/08/14.
 */
public class Profile {

    private final String iD;
    private final String triggerType;
    private final String trigger;
    private final int priority;

    public Profile(String iD, String triggerType, String trigger, int priority) {
        this.trigger = trigger;
        this.iD = iD;
        this.triggerType = triggerType;
        this.priority = priority;
    }

    public String getTrigger() {
        return trigger;
    }

    public String getID() {
        return iD;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public int getPriority() {
        return priority;
    }
}
