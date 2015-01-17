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

    //Todo Onpreference changed listener
    //Todo private static scanInterval
    //Todo dont run service if>> numProfiles =0 or if allProfiles can be handled with broadcasts
    //Todo make sure you have old values saved before a scan, and reset them after, before setting the profile
    //Todo make an arraylist of triggered profiles, from that group, check which ones satisfy the ristrictions, then from those check prohibitions...
    //Todo then when done sort the fully triggered profiles by priority and set the first
    //Todo setting for a toast when a profile is set
    //Todo just save trigger as is, check if its a bssid by checking if it has 3 :s
    //Todo set up the time alarms at boot
    //android.intent.action.PACKAGE_ADDED

    //Todo at the end unregister all the receivers in on destroy

    //TODO Scans :
    //Todo private int triggers triggered,reset on CheckProfile scan, use for combos
    //Todo get profiles on each scan
    //Todo group scans, so that scans happen once per respective adapter>>
    //Todo        checkWifiProfiles(){ profiles.for > if profile.getType().equals("wifi")....}

    //Todo group by type (use sort)
    //Todo might actually have to make a list of type device while scanning,to check for restrictions and prohibitions later on, instead of doing another scan, set old values then
    //Todo if trigger is triggered add them into the viable list of triggers
    //Todo use this array, to check for restrictions and prohibitions
    //Todo revert to old settings if no triggers are triggered, >> more complicated than initially looks
    //Todo fix the explanation in use-ssid, and use it

    //Todo IMPORTANT
    /**
     * final IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
     * final Utility.EarphoneJackReceiver receiver = new Utility.EarphoneJackReceiver();
     * context.registerReceiver(receiver, receiverFilter);
     * <p/>
     * * do for all broadcasts with "Intent.FLAG_RECEIVER_REGISTERED_ONLY"
     */
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
    private static ArrayList<Profile> profiles;
    //Todo init and make sure you unregister receivers
    private static ScanListener bluetoothScanListener, wifiScanLListener;
    private final ProfileDBHelper profileDBHelper = new ProfileDBHelper(this);
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
        bluetooth_old_value = adapter.isEnabled() ? 1 : 0;
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

    private void checkBattery(final Profile profile) {

        final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        final Intent batteryStatus = this.registerReceiver(null, filter);

        final int battery_level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);


    }


    private void location(String location) {
        //Todo check location

    }

    public void checkBluetooth(final Profile profile) {
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