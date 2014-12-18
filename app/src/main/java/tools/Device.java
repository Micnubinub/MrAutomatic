package tools;

/**
 * Created by root on 12/12/14.
 */
public class Device {
    final String ssid;
    final String bssid;

    public Device(String ssid, String bssid) {
        this.bssid = bssid;
        this.ssid = ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public String getSsid() {
        return ssid;
    }
}
