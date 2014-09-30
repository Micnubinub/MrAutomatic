package com.micnubinub.mrautomatic;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import tools.Tools;

/**
 * Created with IntelliJ IDEA.
 * User: Michael
 * Date: 12/3/13
 * Time: 6:34 PM
 * To change this template use File | Settings | File Templates.
 */

public class ProfileService extends Service {


    private static final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private static final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
// Get the BluetoothDevice object from the Intent
                devices.add((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
            }
        }
    };
    private static final Timer timer = new Timer(true);
    private static final ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
    private static final ArrayList<Profile> viableProfiles = new ArrayList<Profile>();
    private static List<ScanResult> wifiScanResults;
    private static Cursor cursor;
    private static WifiManager wifiManager;
    private final ProfileDBHelper profileDBHelper = new ProfileDBHelper(this);
    boolean trigger_found;
    int scan_interval, retries;
    int bluetooth_old_value, wifi_old_value, current_count, battery_count, wifi_value, bluetooth_value, autobrightness_value, brightness, haptic_feedback_value, gps_value, data_int, sleep_timeout, accountsync_value, airplane_mode_value;
    //Todo make a final array list of type Profile
    //Todo make a thread that checks everything
    //Todo make an array list of triggered profiles, then add them into a new array list
    //Todo sort that array list in order of priority, then execute the highest priority
    //Todo make a method that runs at the beginning that checks if the user has
    //Todo SystemClock.sleep(10000)
    //Todo
    //Todo
    //Todo
    int media_volume, notification_value, incoming_call_volume, alarm_volume;
    private SQLiteDatabase profiledb;
    private boolean newBluetothArray = false;
    private boolean newWifiArray = false;
    private boolean scan = true;

    private final Thread scanThread = new Thread(new Runnable() {


        @Override
        public void run() {
            while (scan) {
                timer.scheduleAtFixedRate(new Scanner(), 0, scan_interval);
            }

        }
    });

    private ArrayList<BluetoothDevice> findBluetoothDevices() {

        try {
            newBluetothArray = true;
            devices.clear();
        } catch (Exception e) {
        }
        try {
            adapter.startDiscovery();
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(receiver, filter); // Don't forget to unregister during onDestroy
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    adapter.cancelDiscovery();
                }
            }, 11500);
        } catch (Exception e) {
        }
        return devices;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
/*
        //Todo consider making the string "profile" an ID rather than a bssid
        Notification.Builder builder = new Notification.Builder(this)
                //   .setSmallIcon(R.drawable.service_running, 0)
                .setContentTitle("Notification:")
                .setContentText("Content");
        Notification notification = buildForJellyBean(builder);
        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        startForeground(startId, notification);
*/

        return 0;//Service.START_STICKY;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private Notification buildForJellyBean(Notification.Builder builder) {
        // for some reason Notification.PRIORITY_DEFAULT doesn't show the counter
        builder.setPriority(Notification.FLAG_ONGOING_EVENT);
        return builder.build();
    }

    public void getOldValues() {
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

        if (wifi_old_value == 0) {
            Log.e("Setting old values", "wifi was off");
            wifiManager.setWifiEnabled(false);
        } else
            wifiManager.setWifiEnabled(true);
    }

    private ArrayList<Profile> getProfiles() {
        ArrayList<Profile> profiles = new ArrayList<Profile>();
        profiledb = profileDBHelper.getReadableDatabase();

        cursor = profiledb.query(ProfileDBHelper.PROFILE_TABLE, new String[]{ProfileDBHelper.ID, ProfileDBHelper.PRIORITY, ProfileDBHelper.TRIGGER_DEVICE_TYPE, ProfileDBHelper.BSSID}, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                profiles.add(
                        new Profile(
                                cursor.getString(cursor.getColumnIndex(ProfileDBHelper.ID)),
                                cursor.getString(cursor.getColumnIndex(ProfileDBHelper.TRIGGER_DEVICE_TYPE)),
                                cursor.getString(cursor.getColumnIndex(ProfileDBHelper.BSSID)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.PRIORITY)))
                        )
                );
            } catch (Exception e) {
            }
        }

        Collections.sort(profiles, new Comparator<Profile>() {
            @Override
            public int compare(Profile lhs, Profile rhs) {
                return lhs.getTriggerType().compareToIgnoreCase(lhs.getID());
            }
        });

        try {
            cursor.close();
            profiledb.close();
        } catch (Exception e) {
        }

        return profiles;
    }

    private void getProfile(String profile_id) {


        profiledb = profileDBHelper.getReadableDatabase();
        setOldValues();
        final String[] need = new String[]{ProfileDBHelper.WIFI, ProfileDBHelper.TRIGGER_DEVICE_TYPE, ProfileDBHelper.DATA_VALUE, ProfileDBHelper.BLUETOOTH, ProfileDBHelper.PROFILE_NAME, ProfileDBHelper.ALARM, ProfileDBHelper.SOUND_MEDIA, ProfileDBHelper.SOUND_NOTIFICATION, ProfileDBHelper.SOUND_PHONE_CALL, ProfileDBHelper.BRIGHTNESS, ProfileDBHelper.BRIGHTNESS_MODE, ProfileDBHelper.BSSID};
        //cursor = profile db.query(true, ProfileDBHelper.PROFILE_TABLE, need, ProfileDBHelper.BSSID + "=" + ID, null, null, null, null, null);
        cursor = profiledb.query(ProfileDBHelper.PROFILE_TABLE, need, null, null, null, null, null);


        if (Integer.parseInt(profile_id) >= 0 && Integer.parseInt(profile_id) < cursor.getCount()) {
            cursor.moveToPosition(Integer.parseInt(profile_id));

            wifi_value = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.WIFI)));
            bluetooth_value = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.BLUETOOTH)));
            autobrightness_value = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.BRIGHTNESS_MODE)));
            brightness = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.BRIGHTNESS)));
            notification_value = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.SOUND_NOTIFICATION)));
            incoming_call_volume = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.SOUND_PHONE_CALL)));
            media_volume = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.SOUND_MEDIA)));
            alarm_volume = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.ALARM)));
            data_int = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.DATA_VALUE)));
            sleep_timeout = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.SCREEN_TIMEOUT)));
            haptic_feedback_value = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.HAPTIC_FEEDBACK)));
            accountsync_value = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.SYNC)));
            profiledb.close();
            cursor.close();
            setProfile();

        } else {
            Log.e("Made it to get profile", "but trigger wasn't found");
            cursor.close();
            profiledb.close();
        }
    }

    private void setProfile() {
        // Toast.makeText(this, "Setting profile :" + )

        Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, notification_value);
        Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_MUSIC, media_volume);
        Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_ALARM, alarm_volume);
        Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, incoming_call_volume);

        if (bluetooth_value != 1) {
            if (adapter.isEnabled())
                adapter.disable();
            else if (!adapter.isEnabled())
                adapter.enable();
        }

        wifiManager.setWifiEnabled(!(wifi_value == 0));

        if (autobrightness_value != 1) {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
        } else {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        }

        int time_out = 60;
        switch (sleep_timeout) {

            case 0:
                time_out = 15;
                break;
            case 1:
                time_out = 30;
                break;
            case 2:
                time_out = 60;
                break;
            case 3:
                time_out = 120;
                break;
            case 4:
                time_out = 300;
                break;
            case 5:
                time_out = 600;
                break;
            case 6:
                time_out = 1800;
                break;
        }

        time_out = time_out * 1000;
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, time_out);

/*
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        switch (data_int) {
            case 0:
                connectivityManager.setNetworkPreference(ConnectivityManager.TYPE_MOBILE);
                break;
            case 1:
                connectivityManager.startUsingNetworkFeature(connectivityManager.TYPE_MOBILE, "a")
                break;
            case 2:
                data_value = "2g";
                break;
            case 3:
                connectivityManager.stopUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "android.net.conn.CONNECTIVITY_CHANGE");
                break;
        }
*/

    }

    @Override
    public void onCreate() {
        super.onCreate();
      /*  wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        profiledb = profileDBHelper.getReadableDatabase();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        switch (Integer.parseInt(prefs.getString(Tools.SCAN_INTERVAL, "0"))) {

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

/*
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        int NOTIFICATION = R.string.app_name;

        Notification notification = new Notification(R.drawable.flightmode, "started",
                System.currentTimeMillis());

        // Display a notification about us starting.  We put an icon in the status bar.
        notificationManager.notify(NOTIFICATION, notification);

        scanThread.setDaemon(true);
        scanThread.start();
        */
    }


    private void checkBattery(final Profile profile) {

        final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        final Intent batteryStatus = this.registerReceiver(null, filter);

        final int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        final boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

        final int battery_level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

        if ((profile.getTrigger().equals(Tools.TRIGGER_BATTERY_CHARGING) && isCharging) || battery_level < Integer.parseInt(profile.getTrigger()))
            viableProfiles.add(profile);

    }

    private void location(String location) {
        //Todo check location

    }

    public void checkBluetooth(final Profile profile) {

        //Todo get Old values and set them soon after scan ** if there is no profile found

        if (adapter.isEnabled()) {
            findBluetoothDevices();

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    checkBluetoothDevices(profile);
                }
            }, 12000);
        } else {
            try {
                adapter.enable();
            } catch (Exception e) {
            }
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    checkBluetooth(profile);
                }
            }, 350);
        }
    }

    private void checkBluetoothDevices(final Profile profile) {

        for (BluetoothDevice device : devices) {
            if (device.getAddress().equals(profile.getTrigger())) {
                viableProfiles.add(profile);
            }
        }

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
        wifiScanResults = wifiManager.getScanResults();
        newWifiArray = true;

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                checkWifiDevices(profile);
            }
        }, 10000);
    }

    private void checkWifiDevices(final Profile profile) {
        for (ScanResult scanResult : wifiScanResults) {
            if (scanResult.BSSID.toString().equals(profile.getTrigger()))
                viableProfiles.add(profile);
        }
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

        try {
            viableProfiles.clear();
        } catch (Exception w) {
        }

        for (Profile profile : getProfiles()) {
            if (profile.getTriggerType().equals(Tools.TRIGGER_BATTERY)) {
                checkBattery(profile);
            } else if (profile.getTriggerType().equals(Tools.TRIGGER_WIFI)) {
                checkWifi(profile);
            } else if (profile.getTriggerType().equals(Tools.TRIGGER_BLUETOOTH)) {
                checkBluetooth(profile);
            } else if (profile.getTriggerType().equals(Tools.TRIGGER_LOCATION)) {
            }

            newWifiArray = false;
            newBluetothArray = false;
        }
    }

    private class Scanner extends TimerTask {

        @Override
        public void run() {
            Log.e("Chk", "Checking");
            checkProfiles();
            Log.e("Chk", "Done checking");
            Log.e("viable prof", viableProfiles.toString());

        }
    }

}