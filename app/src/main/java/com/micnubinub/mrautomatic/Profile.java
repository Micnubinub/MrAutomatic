package com.micnubinub.mrautomatic;


import java.util.ArrayList;

import tools.Command;
import tools.Trigger;
import tools.Utility;

/**
 * Created by root on 20/08/14.
 */
public class Profile {
    private final String iD;
    private final String name;
    private final ArrayList<Command> commands;
    private final ArrayList<Trigger> triggers;
    private final ArrayList<Trigger> restrictions;
    private final ArrayList<Trigger> prohibitions;
    private final int priority;

    public Profile(String iD, String name, String triggers, String restrictions, String prohibitions, String commands, int priority) {
        this.iD = iD;
        this.priority = priority;
        this.name = name;

        this.commands = Utility.getCommands(commands);
        this.restrictions = Utility.getTriggers(restrictions);
        this.prohibitions = Utility.getTriggers(prohibitions);
        this.triggers = Utility.getTriggers(triggers);

        for (int i = 0; i < this.restrictions.size(); i++) {
            this.restrictions.get(i).setProfileID(iD);
        }

        for (int i = 0; i < this.triggers.size(); i++) {
            this.triggers.get(i).setProfileID(iD);
        }

        for (int i = 0; i < this.prohibitions.size(); i++) {
            this.prohibitions.get(i).setProfileID(iD);
        }

    }

    public String getName() {
        return name;
    }

    public String getiD() {
        return iD;
    }

    public ArrayList<Command> getCommands() {
        return commands;
    }

    public ArrayList<Trigger> getTriggers() {
        return triggers;
    }

    public ArrayList<Trigger> getRestrictions() {
        return restrictions;
    }

    public ArrayList<Trigger> getProhibitions() {
        return prohibitions;
    }

    public int getPriority() {
        return priority;
    }

    public String getID() {
        return iD;
    }

    @Override
    public String toString() {
        return String.format("%s. %s | %s | %s | %s | %s \n", iD, name, triggers.toString(), prohibitions.toString(), restrictions.toString(), commands.toString());
    }
}
