package com.micnubinub.mrautomatic;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import tools.Device;
import tools.TriggerOrCommand;
import tools.Utility;

/**
 * Created with IntelliJ IDEA.
 * User: Michael
 * Date: 12/3/13
 * Time: 6:34 PM
 * To change this template use File | Settings | File Templates.
 */

public class ProfileService extends Service {
    //Todo make a list of all the use full broadcast receivers and triggers and make a class full of enums to be used to proceessss>>continue adding some
    //Todo extract all the scanning code in wifiListAdapter and bTListAdapter
    //Todo then when done sort the fully triggered profiles by priority and set the first
    //Todo        checkWifiProfiles(){ profiles.for > if profile.getCategory().equals("wifi")....}
    //Todo if trigger is triggered add them into the viable list of triggers, use this array, to check for restrictions and prohibitions
    //Todo revert to old settings if no triggers are triggered, >> more complicated than initially looks
    //Todo fix the explanation in use-ssid, and use it
    //Todo location item = long, lat, rad;

    private static final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                addDevice(new Device(device.getName(), device.getAddress()));
            }
        }
    };

    private static final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                final ArrayList<Device> devices = new ArrayList<Device>();
                for (ScanResult scanResult : wifiManager.getScanResults()) {
                    addDevice(new Device(scanResult.SSID, scanResult.BSSID));
                }
                wifiScanLListener.onScanComplete(devices);
            }
        }
    };
    private static final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private static final LocationListener locationListener = new MyLocationListener();
    private static ArrayList<Device> devices = new ArrayList<Device>();
    private static PendingIntent alarmIntent;
    private static AlarmManager alarmManager;
    private static LocationManager locationManager;
    private static WifiManager wifiManager;
    private static ArrayList<Profile> profiles, viableProfiles = new ArrayList<>();
    private static int scan_interval, retries;
    private static boolean wifi_or_bluetooth_complete, toastWhenProfileSet;
    private static ScanListener bluetoothScanListener = new ScanListener() {
        @Override
        public void onScanComplete(ArrayList<Device> devices) {
            final ArrayList<TriggerOrCommand> triggers = new ArrayList<>();
            for (int i = 0; i < profiles.size(); i++) {
                final ArrayList<TriggerOrCommand> triggers1 = profiles.get(i).getTriggers();
                for (int j = 0; j < triggers1.size(); j++) {
                    triggers.add(triggers1.get(j));
                }
            }

            for (int i = 0; i < triggers.size(); i++) {
                final TriggerOrCommand trigger = triggers.get(i);
                for (int j = 0; j < devices.size(); j++) {
                    final Device device = devices.get(j);
                    if (device.getAddress().equals(trigger.getValue()) || device.getName().equals(trigger.getValue()))
                        addProfileToViableList(trigger.getProfileID());
                }
            }
            if (wifi_or_bluetooth_complete)
                checkRestrictionsAndProhibitions();
            else
                wifi_or_bluetooth_complete = true;
        }
    };

    private static ScanListener wifiScanLListener = new ScanListener() {
        @Override
        public void onScanComplete(ArrayList<Device> devices) {
            final ArrayList<TriggerOrCommand> triggers = new ArrayList<>();
            for (int i = 0; i < profiles.size(); i++) {
                final ArrayList<TriggerOrCommand> triggers1 = profiles.get(i).getTriggers();
                for (int j = 0; j < triggers1.size(); j++) {
                    triggers.add(triggers1.get(j));
                }
            }

            for (int i = 0; i < triggers.size(); i++) {
                final TriggerOrCommand trigger = triggers.get(i);
                for (int j = 0; j < devices.size(); j++) {
                    final Device device = devices.get(j);
                    if (device.getAddress().equals(trigger.getValue()) || device.getName().equals(trigger.getValue()))
                        addProfileToViableList(trigger.getProfileID());
                }
            }

            if (wifi_or_bluetooth_complete)
                checkRestrictionsAndProhibitions();
            else
                wifi_or_bluetooth_complete = true;
        }
    };
    private static int bluetooth_old_value, wifi_old_value;
    private static Context context;

    public static void scheduleNext(Context context) {
        try {

            final Intent intent = new Intent(context, AlarmReceiver.class);
            alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC, System.currentTimeMillis() + scan_interval, alarmIntent);
            } else
                alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + scan_interval, alarmIntent);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void registerBroadcastReceivers(Context context) {
        //Todo register all necessary receivers
        /**
         * final IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
         * final Utility.EarphoneJackReceiver receiver = new Utility.EarphoneJackReceiver();
         * context.registerReceiver(receiver, receiverFilter);
         * <p/>
         * * do for all broadcasts with "Intent.FLAG_RECEIVER_REGISTERED_ONLY"
         */
    }

    private static boolean checkBattery(final TriggerOrCommand profile) {
        if (context == null)
            return false;
        final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        //Todo ---- register this, and toast it to see if it works well or not, if so make it a broadcast receiver
        final Intent batteryStatus = context.registerReceiver(null, filter);
        final int battery_level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        try {
            return (Integer.parseInt(profile.getValue()) < battery_level);
        } catch (Exception e) {
            Log.e("checkBattery :" + profile.getValue(), e.toString());
            return false;
        }
    }

    private static void checkRestrictionsAndProhibitions() {
        setOldValues();
        for (int i = 0; i < viableProfiles.size(); i++) {
            final Profile profile = viableProfiles.get(i);

            for (int j = 0; i < profile.getProhibitions().size(); j++) {
                final TriggerOrCommand prohib = profile.getProhibitions().get(j);
                if (isTriggerTriggered(prohib))
                    removeProfileFromViableList(prohib.getProfileID());
            }

            for (int j = 0; i < profile.getRestrictions().size(); j++) {
                final TriggerOrCommand restriction = profile.getRestrictions().get(j);
                if (!isTriggerTriggered(restriction))
                    removeProfileFromViableList(restriction.getProfileID());
            }
        }

        //Todo setProfile
        setProfile("");
    }

    private static boolean isTriggerTriggered(TriggerOrCommand trigger) {
        //Todo

        return false;
    }

    private static void checkProfiles() {
        getOldValues();
        if (context == null)
            return;

        if (devices == null)
            devices = new ArrayList<Device>();
        else
            devices.clear();

        if (!adapter.isEnabled()) {
            adapter.enable();

            try {
                adapter.startDiscovery();
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                context.registerReceiver(bluetoothReceiver, filter); // Don't forget to unregister during onDestroy
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        context.unregisterReceiver(bluetoothReceiver);
                        adapter.cancelDiscovery();
                        bluetoothScanListener.onScanComplete(devices);
                    }
                }, 11500);
            } catch (Exception e) {
            }

        } else {
            try {
                adapter.enable();
            } catch (Exception e) {
            }
        }

        if (!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);

        wifiManager.startScan();
        context.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    private static synchronized void addDevice(Device device) {
        devices.add(device);
    }

    private static void checkIfShouldRunService() {
        //Todo dont run service if>> numProfiles =0 or if allProfiles can be handled with broadcasts
        if (context == null)
            return;
        for (int i = 0; i < profiles.size(); i++) {
            final Profile profile = profiles.get(i);
            for (int j = 0; j < profile.getTriggers().size(); i++) {
                final TriggerOrCommand trigger = profile.getTriggers().get(j);
                final String type = trigger.getCategory();
                //Todo ---- might be more
                if (type.equals(Utility.TRIGGER_BLUETOOTH) || type.equals(Utility.TRIGGER_BATTERY) || type.equals(Utility.TRIGGER_WIFI) || type.equals(Utility.TRIGGER_LOCATION)) {
                    return;
                }
            }
        }
        context.stopService(new Intent(context, ProfileService.class));
    }

    private static void setProfile(String profileID) {
        Profile profile = null;
        loop:
        for (int i = 0; i < viableProfiles.size(); i++) {
            profile = viableProfiles.get(i);
            if (profile.getID().equals(profileID))
                break loop;
        }
        if (profile == null) {
            completeScan();
            return;
        } else {
            for (int i = 0; i < profile.getCommands().size(); i++) {
                final TriggerOrCommand command = profile.getCommands().get(i);
                //Todo set commands
            }
            completeScan();
        }
    }

    private static void completeScan() {
        wifi_or_bluetooth_complete = false;
        if (viableProfiles != null && viableProfiles.size() > 0)
            viableProfiles.clear();
        //Todo get profiles on each scan, schedule next

        if (context == null)
            return;

        scheduleNext(context);
        profiles = Utility.getProfiles(context);
    }

    private static void addProfileToViableList(String profileID) {
        for (int i = 0; i < profiles.size(); i++) {
            final Profile profile = profiles.get(i);
            if (profile.getID().equals(profileID)) {
                viableProfiles.add(profile);
                profiles.remove(profile);
            }
        }
    }

    private static void removeProfileFromViableList(String profileID) {
        for (int i = 0; i < viableProfiles.size(); i++) {
            final Profile profile = viableProfiles.get(i);
            if (profile.getID().equals(profileID)) {
                viableProfiles.remove(profile);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static Notification buildForJellyBean(Notification.Builder builder) {
        // for some reason Notification.PRIORITY_DEFAULT doesn't show the counter
        builder.setPriority(Notification.FLAG_ONGOING_EVENT);
        return builder.build();
    }

    public static void getOldValues() {
        if (adapter != null)
            bluetooth_old_value = adapter.isEnabled() ? 1 : 0;
        if (wifiManager != null)
            wifi_old_value = wifiManager.isWifiEnabled() ? 1 : 0;

    }

    public static void setOldValues() {
        if (bluetooth_old_value != 1) {
            if (adapter.isEnabled()) {
                adapter.disable();
            }
        } else {
            adapter.enable();
        }
        wifiManager.setWifiEnabled(wifi_old_value > 0);
    }

    private static void location(String location) {
        //Todo check location
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Notification.Builder builder = new Notification.Builder(this)
                // Todo  .setSmallIcon(R.drawable.service_running, 0)
                .setContentTitle("Notification:")
                .setOngoing(true)
                .setContentText("Content");
        Notification notification = buildForJellyBean(builder);
        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        startForeground(startId, notification);
        return Service.START_STICKY;//Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        toastWhenProfileSet = prefs.getBoolean(Utility.TOAST_WHEN_PROFILE_SET, true);
        switch (Integer.parseInt(prefs.getString(Utility.SCAN_INTERVAL, "0"))) {
            case 0:
                scan_interval = 30000;
                break;
            case 1:
                scan_interval = 60000;
                break;
            case 2:
                scan_interval = 120000;
                break;
            case 3:
                scan_interval = 180000;
                break;
            case 4:
                scan_interval = 300000;
                break;
            case 5:
                scan_interval = 600000;
                break;
            case 6:
                scan_interval = 900000;
                break;
        }
        //  final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //   final int NOTIFICATION = R.string.app_name;
        //final Notification notification = new Notification(R.drawable.save, "started", System.currentTimeMillis());
        // Display a notification about us starting.  We put an icon in the status bar.
        //notificationManager.notify(NOTIFICATION, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    private void checkLocation() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
    }

    interface ScanListener {
        void onScanComplete(ArrayList<Device> devices);
    }

    public static class BootUpReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Todo set up the time alarms at boot, bluetooth jack and all the other broadcast receivers that need to be activated from here

            final IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            final Utility.EarphoneJackReceiver receiver = new Utility.EarphoneJackReceiver();
            context.registerReceiver(receiver, receiverFilter);

            try {
//                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Utils.AUTO_START_BOOL, false)) {
                Intent myIntent = new Intent(context, ProfileService.class);
                context.startService(myIntent);
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            scheduleNext(context);
        }
    }

    private static class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            String longitude = "Longitude: " + loc.getLongitude();
            String latitude = "Latitude: " + loc.getLatitude();

            /**Todo
             boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
             // check if enabled and if not send user to the GSP settings
             // Better solution would be to display a dialog and suggesting to
             // go to the settings
             if (!enabled) {
             Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
             startActivity(intent);
             }

             float[] result = new float[1];
             Location.distanceBetween (startLat, startLng, endLat, endLng, result);

             if (result[0] < 5000) {
             // distance between first and second location is less than 5km
             }
             **/
            //Todo unregister when received, or make it so that its registered to receive the updated every scan_interval
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

}