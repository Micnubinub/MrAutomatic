package tools;

/**
 * Created by root on 10/12/14.
 */
public class Trigger {
    private final String type;
    private String value;
    private String profile_id;

    public Trigger(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getProfileID() {
        return profile_id;
    }

    public void setProfileID(String profile_id) {
        this.profile_id = profile_id;
    }

    @Override
    public String toString() {
        return type + ":" + value;
    }
}
