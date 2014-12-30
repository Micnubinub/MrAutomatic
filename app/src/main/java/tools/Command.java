package tools;

/**
 * Created by root on 10/12/14.
 */
public class Command {
    private final String type;
    private String value;

    public Command(String type, String value) {
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

    @Override
    public String toString() {
        return type + ":" + value;
    }
}
