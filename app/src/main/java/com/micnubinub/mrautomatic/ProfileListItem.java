package com.micnubinub.mrautomatic;


/**
 * Created by root on 20/08/14.
 */
public class ProfileListItem {
    private final String iD;
    private final String name;
    private final String command;
    private final String trigger;
    private final int priority;

    public ProfileListItem(String iD, String name, String trigger, String command, int priority) {
        this.iD = iD;
        this.trigger = trigger;
        this.name = name;
        this.command = command;
        this.priority = priority;
    }


    public String getName() {
        return name;
    }

    public String getiD() {
        return iD;
    }

    public String getCommand() {
        return command;
    }

    public int getPriority() {
        return priority;
    }

    public String getTrigger() {
        return trigger;
    }

    public String getID() {
        return iD;
    }
}
