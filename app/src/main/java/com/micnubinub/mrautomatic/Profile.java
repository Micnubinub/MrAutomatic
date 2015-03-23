package com.micnubinub.mrautomatic;


import java.util.ArrayList;

import tools.TriggerOrCommand;
import tools.Utility;

/**
 * Created by root on 20/08/14.
 */
public class Profile {
    private final String iD;
    private final String name;
    private final ArrayList<TriggerOrCommand> commands;
    private final int priority;

    public Profile(String iD, String name, String triggers, int priority) {
        this.iD = iD;
        this.priority = priority;
        this.name = name;

        this.commands = Utility.getTriggersAndCommands(triggers);

        for (int i = 0; i < this.commands.size(); i++) {
            this.commands.get(i).setProfileID(iD);
        }

    }

    public String getName() {
        return name;
    }

    public String getiD() {
        return iD;
    }

    public ArrayList<TriggerOrCommand> getTriggersOrCommands() {
        return commands;
    }

    public int getPriority() {
        return priority;
    }

    public String getID() {
        return iD;
    }

    @Override
    public String toString() {
        return String.format("%s. %s | %s | %s | %s | %s \n", iD, name, commands.toString());
    }
}
