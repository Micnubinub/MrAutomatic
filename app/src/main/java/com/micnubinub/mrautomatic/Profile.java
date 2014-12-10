package com.micnubinub.mrautomatic;


import java.util.ArrayList;

import tools.Command;
import tools.Trigger;

/**
 * Created by root on 20/08/14.
 */
public class Profile {

    private final String iD;
    private final ArrayList<Command> commands;
    private final ArrayList<Trigger> triggers;
    private final int priority;

    public Profile(String iD, ArrayList<Command> commands, ArrayList<Trigger> triggers, int priority) {
        this.triggers = triggers;
        this.iD = iD;
        this.commands = commands;
        this.priority = priority;
    }


    public String getID() {
        return iD;
    }


    public ArrayList<Command> getCommands() {
        return commands;
    }

    public ArrayList<Trigger> getTriggers() {
        return triggers;
    }

    public int getPriority() {
        return priority;
    }
}
