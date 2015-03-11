package tools;

/**
 * Created by Michael on 3/11/2015.
 */
public class Enums {

//    android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED
//    android.bluetooth.adapter.action.DISCOVERY_FINISHED
//    android.bluetooth.adapter.action.STATE_CHANGED
//    android.bluetooth.device.action.ACL_CONNECTED
//    android.bluetooth.device.action.ACL_DISCONNECTED
//    android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED
//    android.bluetooth.device.action.BOND_STATE_CHANGED
//    android.bluetooth.device.action.FOUND
//    android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED
//    android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED
//    android.intent.action.ACTION_POWER_CONNECTED
//    android.intent.action.ACTION_POWER_DISCONNECTED
//    android.intent.action.AIRPLANE_MODE
//    android.intent.action.BATTERY_CHANGED
//    android.intent.action.BATTERY_LOW
//    android.intent.action.BOOT_COMPLETED
//    android.intent.action.DATA_SMS_RECEIVED
//    android.intent.action.DOCK_EVENT
//    android.intent.action.HEADSET_PLUG
//    android.intent.action.NEW_OUTGOING_CALL
//    android.intent.action.PACKAGE_ADDED
//    android.intent.action.PACKAGE_INSTALL
//    android.intent.action.PHONE_STATE
//    android.intent.action.SCREEN_OFF
//    android.intent.action.SCREEN_ON
//    android.media.RINGER_MODE_CHANGED
//    android.net.nsd.STATE_CHANGED
//    android.net.wifi.STATE_CHANGE
//    android.net.wifi.WIFI_STATE_CHANGED

    public enum Device {
        CONNECTED, DISCONNECTED
    }

    public enum Battery {
        CHARGER_CONNECTED, CHARGER_DISCONNECTED, ABOVE_PERCENT, BELOW_PERCENT, IN_RANGE_PERCENT, OUT_OF_RANGE_PERCENT, IN_RANGE_TEMP, OUT_OF_RANGE_TEMP, BELOW_TEMPERATURE, ABOVE_TEMPERATURE
    }

    public enum Location {
        IN_LOCATION, OUT_OF_LOCATION
    }

    public enum Switch {
        ON, OFF
    }

//    public enum Switch {
//        ON, OFF
//    }
//
//    public enum Switch {
//        ON, OFF
//    }
//
//    public enum Switch {
//        ON, OFF
//    }
//
//    public enum Switch {
//        ON, OFF
//    }
//
//    public enum Switch {
//        ON, OFF
//    }
//
//    public enum Switch {
//        ON, OFF
//    }


}
