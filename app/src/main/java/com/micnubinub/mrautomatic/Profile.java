package com.micnubinub.mrautomatic;


import java.util.ArrayList;

/**
 * Created by root on 20/08/14.
 */
public class Profile {
    private final String iD;
    private final String name;
    private final ArrayList<String> commands;
    private final ArrayList<String> triggers;
    private final int priority;

    public Profile(String iD, String name, String trigger, String command, int priority) {
        this.iD = iD;
        this.priority = priority;
        this.name = name;

        this.triggers = new ArrayList<>();
        for (String s : trigger.split(",")) {
            triggers.add(s);
        }

        this.commands = new ArrayList<>();
        for (String s : command.split(",")) {
            commands.add(s);
        }

    }


    public String getName() {
        return name;
    }

    public String getiD() {
        return iD;
    }


    public int getPriority() {
        return priority;
    }

    public ArrayList<String> getCommands() {
        return commands;
    }

    public ArrayList<String> getTriggers() {
        return triggers;
    }

    public String getID() {
        return iD;
    }
}
