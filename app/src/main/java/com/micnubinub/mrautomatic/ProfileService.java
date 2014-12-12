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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import tools.Utility;

/**
 * Created with IntelliJ IDEA.
 * User: Michael
 * Date: 12/3/13
 * Time: 6:34 PM
 * To change this template use File | Settings | File Templates.
 */

public class ProfileService extends Service {
//TODO FRIDAY :

    //Todo Onpreference changed listener
    //Todo private static scanInterval
    //Todo dont run service if>>
    //Todo           numProfiles =0
    //Todo       allProfiles can be handled with broadcasts
    //Todo make sure you have old values saved before a scan, and reset them after, before setting the profile
    //Todo private int triggers triggered,reset on CheckProfile scan, use for combos

    //Todo get profiles on each scan
    //Todo have all the listed broadcast receivers work in parallel
    //Todo have a method getAdapters(), which happens after you've gotten the profiles, gets needed adapters, nullifies others
    //Todo group scans, so that scans happen once per respective adapter>>
    //Todo        checkWifiProfiles(){ profiles.for > if profile.getType().equals("wifi")....}
    //Todo just save trigger as is, check if its a bssid by checking if it has 3 :s

    //android.intent.action.PACKAGE_ADDED

    //Todo at the end unregister all the receivers in on destroy
    private static final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
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
    private static Cursor cursor;
    private static WifiManager wifiManager;
    private static ArrayList<Profile> profiles;
    //Todo Sort array by combo number
    private static SQLiteDatabase profiledb;
    //Todo init and make sure you unregister receivers
    private static ScanListener bluetoothScanListener, wifiScanLListener;
    private final ProfileDBHelper profileDBHelper = new ProfileDBHelper(this);
    boolean trigger_found;
    int scan_interval, retries;
    int bluetooth_old_value, wifi_old_value;

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

    public static ArrayList<Profile> getProfiles() {
        return profiles;
    }

    private void findBluetoothDevices() {

        devices = new ArrayList<Device>();

        try {
            adapter.startDiscovery();
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(bluetoothReceiver, filter); // Don't forget to unregister during onDestroy
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    unregisterReceiver(bluetoothReceiver);
                    adapter.cancelDiscovery();
                    bluetoothScanListener.onScanComplete(devices);
                }
            }, 11500);
        } catch (Exception e) {
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Todo consider making the string "profile" an ID rather than a bssid
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
        //Todo check this
        if (adapter.isEnabled())
            bluetooth_old_value = 1;
        else
            bluetooth_old_value = 0;


        if (wifiManager.isWifiEnabled())
            wifi_old_value = 1;
        else
            wifi_old_value = 0;
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

    private ArrayList<Profile> refreshProfiles() {
        profiles = new ArrayList<Profile>();
        profiledb = profileDBHelper.getReadableDatabase();

        cursor = profiledb.query(ProfileDBHelper.PROFILE_TABLE, new String[]{ProfileDBHelper.ID, ProfileDBHelper.PRIORITY, ProfileDBHelper.PROFILE_NAME, ProfileDBHelper.TRIGGERS, ProfileDBHelper.COMMANDS}, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                profiles.add(
                        new Profile(
                                cursor.getString(cursor.getColumnIndex(ProfileDBHelper.ID)),
                                cursor.getString(cursor.getColumnIndex(ProfileDBHelper.PROFILE_NAME)),
                                cursor.getString(cursor.getColumnIndex(ProfileDBHelper.TRIGGERS)),
                                cursor.getString(cursor.getColumnIndex(ProfileDBHelper.COMMANDS)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.PRIORITY)))));
            } catch (Exception e) {
            }
        }
        try {
            cursor.close();
            profiledb.close();
        } catch (Exception e) {
        }

        return profiles;
    }

    private void getProfile(String profile_id) {
        setOldValues();


        if (Integer.parseInt(profile_id) >= 0 && Integer.parseInt(profile_id) < cursor.getCount()) {
            cursor.moveToPosition(Integer.parseInt(profile_id));
            profiledb.close();
            cursor.close();

        } else {
            Log.e("Made it to get profile", "but trigger wasn't found");
            cursor.close();
            profiledb.close();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
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

    private void getAdapters() {
        //Todo may be a waste of time
        boolean getBluetoothAdapter, getWifiAdapter;

        for (Profile profile : profiles) {
            if (profile.getTriggers().contains(Utility.TRIGGER_WIFI_BSSID) || profile.getTriggers().contains(Utility.TRIGGER_WIFI_SSID))
                getWifiAdapter = true;

            if (profile.getTriggers().contains(Utility.TRIGGER_BLUETOOTH_BSSID) || profile.getTriggers().contains(Utility.TRIGGER_BLUETOOTH_SSID))
                getBluetoothAdapter = true;

        }
    }

    private void checkBattery(final Profile profile) {

        final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        final Intent batteryStatus = this.registerReceiver(null, filter);

        final int battery_level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);


    }


    private void location(String location) {
        //Todo check location

    }

    public void checkBluetooth(final Profile profile) {

        //Todo get Old values and set them soon after scan ** if there is no profile found

        if (adapter.isEnabled()) {
            findBluetoothDevices();

        } else {
            try {
                adapter.enable();
            } catch (Exception e) {
            }
        }
    }

    private void checkBluetoothDevices(final Profile profile) {
//Todo, scan and use make sure listeners are init

    }

    public void checkWifi(final Profile profile) {
//make s accessible to the inner class

        if (wifiManager.isWifiEnabled()) {
            checkWifiDevices(profile);
        } else {
            wifiManager.setWifiEnabled(true);
            new Timer().schedule(new TimerTask() {
                public void run() {
                    checkWifi(profile);
                }
            }, 200);
        }

        wifiManager.startScan();
        //Todo  wifiScanResults = wifiManager.getScanResults(); >> use broadcast bluetoothReceiver
        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiReceiver, filter);
    }

    private void checkWifiDevices(final Profile profile) {

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

    private void checkProfiles() {
//Todo


    }

    //Todo use this interface to for wifi and bluetooth
    interface ScanListener {
        void onScanComplete(ArrayList<Device> devices);
    }

    public static class BootUpReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
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
//            if (isServiceRunning) {
//                try {
//                    if (loadAd) {
//                        bannerPopup.loadFullScreenAd();
//                        scheduleNext(context, false);
//                        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Utils.TOAST_BEFORE_BOOL, true))
//                            Toast.makeText(context, "Showing Ad in 10 secs", Toast.LENGTH_LONG).show();
//                        return;
//                    } else {
//                        bannerPopup.showFullScreenAd();
//                        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Utils.LOOP_SCHEDULE, false))
            scheduleNext(context, true);
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }

        }
    }


}