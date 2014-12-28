package tools;

/**
 * Created by root on 12/12/14.
 */
public class Device {
    private final String ssid;
    private String bssid;

    public Device(String ssid, String bssid) {
        this.bssid = bssid;
        this.ssid = ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getSsid() {
        return ssid;
    }

    @Override
    public String toString() {
        return ssid + " " + bssid;
    }
}
