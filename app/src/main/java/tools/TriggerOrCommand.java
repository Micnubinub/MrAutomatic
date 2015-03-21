package tools;

/**
 * Created by root on 10/12/14.
 */
public class TriggerOrCommand {
    private final Type type;
    private final String category;
    private String value, profileID, displayString;

    public TriggerOrCommand(Type type, String category, String value) {
        this.type = type;
        this.category = category;
        this.value = value;
    }

    public String getDisplayString() {
        return displayString;
    }

    public void setDisplayString(String displayString) {
        this.displayString = displayString;
    }

    public Type getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getProfileID() {
        return profileID;
    }

    public void setProfileID(String profileID) {
        this.profileID = profileID;
    }

    @Override
    public String toString() {
        return category + ":" + value;
    }

    public enum Type {
        TRIGGER, COMMAND, RESTRICTIONS, PROHIBITION
    }

}
