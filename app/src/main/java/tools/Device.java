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

    public String getAddress() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getName() {
        return ssid;
    }

    @Override
    public boolean equals(Object o) {
        final Device device = (Device) o;
        return device.toString().equals(toString());
    }

    @Override
    public String toString() {
        return ssid + " " + bssid;
    }
}
