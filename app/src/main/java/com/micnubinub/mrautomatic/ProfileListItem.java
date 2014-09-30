package com.micnubinub.mrautomatic;


/**
 * Created by root on 20/08/14.
 */
public class ProfileListItem {
    private final String iD;
    private final String name;
    private final String triggerType;
    private final String trigger;

    public ProfileListItem(String iD, String name, String triggerType, String trigger) {
        this.iD = iD;
        this.trigger = trigger;
        this.name = name;
        this.triggerType = triggerType;
    }


    public String getName() {
        return name;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public String getTrigger() {
        return trigger;
    }

    public String getID() {
        return iD;
    }
}
