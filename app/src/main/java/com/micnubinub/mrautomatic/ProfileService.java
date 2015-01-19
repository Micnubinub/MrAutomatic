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
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import tools.Device;
import tools.Trigger;
import tools.Utility;

/**
 * Created with IntelliJ IDEA.
 * User: Michael
 * Date: 12/3/13
 * Time: 6:34 PM
 * To change this template use File | Settings | File Templates.
 */


public class ProfileService extends Service {
    //Todo make sure you have old values saved before a scan, and reset them after, before setting the profile
    //Todo make an arraylist of triggered profiles, from that group, check which ones satisfy the ristrictions, then from those check prohibitions...
    //Todo then when done sort the fully triggered profiles by priority and set the first
    //Todo set up the time alarms at boot
    //Todo group scans, so that scans happen once per respective adapter>>
    //Todo        checkWifiProfiles(){ profiles.for > if profile.getType().equals("wifi")....}
    //Todo group by type (use sort)
    //Todo might actually have to make a list of type device while scanning,to check for restrictions and prohibitions later on, instead of doing another scan, set old values then
    //Todo if trigger is triggered add them into the viable list of triggers, use this array, to check for restrictions and prohibitions
    //Todo revert to old settings if no triggers are triggered, >> more complicated than initially looks
    //Todo fix the explanation in use-ssid, and use it

    private static final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                devices.add(new Device(device.getName(), device.getAddress()));
            }
        }
    };


    private static final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                final ArrayList<Device> devices = new ArrayList<Device>();
                for (ScanResult scanResult : wifiManager.getScanResults()) {
                    devices.add(new Device(scanResult.SSID, scanResult.BSSID));
                }
                wifiScanLListener.onScanComplete(devices);
            }
        }
    };
    private static final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private static ArrayList<Device> devices = new ArrayList<Device>();
    private static PendingIntent alarmIntent;
    private static AlarmManager alarmManager;
    private static WifiManager wifiManager;
    private static ArrayList<Profile> profiles, viableProfiles = new ArrayList<>();
    private static int scan_interval, retries;
    //Todo use this in the scan listeners, to check if they other is done, so you can....
    private static boolean wifi_or_bluetooth_complete, toastWhenProfileSet;
    private static ScanListener bluetoothScanListener = new ScanListener() {
        @Override
        public void onScanComplete(ArrayList<Device> devices) {
            final ArrayList<Trigger> triggers = new ArrayList<>();
            for (int i = 0; i < profiles.size(); i++) {
                final ArrayList<Trigger> triggers1 = profiles.get(i).getTriggers();
                for (int j = 0; j < triggers1.size(); j++) {
                    triggers.add(triggers1.get(j));
                }
            }

            for (int i = 0; i < triggers.size(); i++) {
                final Trigger trigger = triggers.get(i);
                for (int j = 0; j < devices.size(); j++) {
                    final Device device = devices.get(j);
                    if (device.getBssid().equals(trigger.getValue()) || device.getSsid().equals(trigger.getValue()))
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
            final ArrayList<Trigger> triggers = new ArrayList<>();
            for (int i = 0; i < profiles.size(); i++) {
                final ArrayList<Trigger> triggers1 = profiles.get(i).getTriggers();
                for (int j = 0; j < triggers1.size(); j++) {
                    triggers.add(triggers1.get(j));
                }
            }

            for (int i = 0; i < triggers.size(); i++) {
                final Trigger trigger = triggers.get(i);
                for (int j = 0; j < devices.size(); j++) {
                    final Device device = devices.get(j);
                    if (device.getBssid().equals(trigger.getValue()) || device.getSsid().equals(trigger.getValue()))
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
    private final ProfileDBHelper profileDBHelper = new ProfileDBHelper(this);

    public static void scheduleNext(Context context, boolean load) {

        try {
            // Toast.makeText(context, "LOAD_AD sched" + String.valueOf(loadAd), Toast.LENGTH_LONG).show();
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (load) {

                Intent i = new Intent(context, AlarmReceiver.class);
                if (false) {
                    alarmManager.cancel(alarmIntent);
                    return;
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                        alarmManager.setExact(AlarmManager.RTC, (System.currentTimeMillis() + (mins * 60000)) - 10000, PendingIntent.getBroadcast(context, 0, i, 0));
                    }
//                    else
//                        alarmManager.set(AlarmManager.RTC, (System.currentTimeMillis() + (mins * 60000)) - 10000, PendingIntent.getBroadcast(context, 0, i, 0));
                }
            } else {
                final Intent intent = new Intent(context, AlarmReceiver.class);
                alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC, System.currentTimeMillis() + 10000, alarmIntent);
                } else
                    alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 10000, alarmIntent);
            }


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

    private static boolean checkBattery(final Trigger profile) {
        if (context == null)
            return false;
        final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        //Todo register this, and toast it to see if it works well or not, if so make it a broadcast receiver
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
        for (int i = 0; i < viableProfiles.size(); i++) {
            final Profile profile = viableProfiles.get(i);
            //Todo check restrictions and prohib here
        }
    }

    private static void checkProfiles() {
        //Todo paste this in every static method that relies on Context
        if (context == null)
            return;

        //Todo lots* of work to do here

        if (!adapter.isEnabled()) {
            adapter.enable();
            devices = new ArrayList<Device>();
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
        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiReceiver, filter);

    }

    private static void checkIfShouldRunService() {
        //Todo dont run service if>> numProfiles =0 or if allProfiles can be handled with broadcasts
        if (context == null)
            return;
        for (int i = 0; i < profiles.size(); i++) {
            final Profile profile = profiles.get(i);
            for (int j = 0; j < profile.getTriggers().size(); i++) {
                final Trigger trigger = profile.getTriggers().get(j);
                final String type = trigger.getType();
                //Todo --- might be more
                if (type.equals(Utility.TRIGGER_BLUETOOTH) || type.equals(Utility.TRIGGER_BATTERY) || type.equals(Utility.TRIGGER_WIFI) || type.equals(Utility.TRIGGER_LOCATION)) {
                    return;
                }
            }
        }

        context.stopService(new Intent(context, ProfileService.class));
    }

    private static void completeScan() {
        wifi_or_bluetooth_complete = false;
        viableProfiles.clear();
        //Todo get profiles on each scan, schedule next

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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private Notification buildForJellyBean(Notification.Builder builder) {
        // for some reason Notification.PRIORITY_DEFAULT doesn't show the counter
        builder.setPriority(Notification.FLAG_ONGOING_EVENT);
        return builder.build();
    }

    public void getOldValues() {
        //Todo add more
        if (adapter != null)
            bluetooth_old_value = adapter.isEnabled() ? 1 : 0;
        if (wifiManager != null)
            wifi_old_value = wifiManager.isWifiEnabled() ? 1 : 0;

    }

    public void setOldValues() {
        if (bluetooth_old_value != 1) {
            if (adapter.isEnabled()) {
                adapter.disable();
            }
        } else {
            adapter.enable();
        }
        wifiManager.setWifiEnabled(wifi_old_value > 0);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
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

    private void location(String location) {
        //Todo check location

    }

    public void checkBluetooth(final Profile profile) {

    }

    private void checkBluetoothDevices(final Profile profile) {
        //Todo, scan and use make sure listeners are init

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

    //Todo use this interface to for wifi and bluetooth
    interface ScanListener {
        void onScanComplete(ArrayList<Device> devices);
    }

    public static class BootUpReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
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
            scheduleNext(context, true);

        }
    }

}