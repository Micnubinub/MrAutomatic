package tools;

/**
 * Created by root on 25/08/14.
 */
public class WirelessDevice {
    private final String name;
    private final String bssid;

    public WirelessDevice(String name, String bssid) {
        super();
        this.name = name;
        this.bssid = bssid;
    }

    public String getName() {
        return name;
    }

    public String getBssid() {
        return bssid;
    }

}
