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
import android.media.AudioManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    /**
     * Profile service
     * <p/>
     */
    //Todo make a list of all the use full broadcast receivers and triggers and make a class full of enums to be used to proceessss>>continue adding some
    //Todo extract all the scanning code in wifiListAdapter and bTListAdapter
    //Todo then when done sort the fully triggered profiles by priority and set the first
    //Todo        checkWifiProfiles(){ profiles.for > if profile.getCategory().equals("wifi")....}
    //Todo if trigger is triggered add them into the viable list of triggers, use this array, to check for restrictions and prohibitions
    //Todo revert to old settings if no triggers are triggered, >> more complicated than initially looks
    //Todo fix the explanation in use-ssid, and use it
    //Todo location item = long, lat, rad;
    //Todo if a time trigger is used make it so that the service schedules the next scan to be after the time triggers duration has passed


    private static final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                bluetoothDevices.add(new Device(device.getName(), device.getAddress()));
            }
        }
    };

    private static final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                for (ScanResult scanResult : wifiManager.getScanResults()) {
                    wifiDevices.add(new Device(scanResult.SSID, scanResult.BSSID));
                }
                wifiScanLListener.onScanComplete(wifiDevices);
            }
        }
    };
    private static final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private static final LocationListener locationListener = new MyLocationListener();
    private static final ArrayList<TriggerOrCommand> viable = new ArrayList<>();
    private static AlarmManager alarmManager;
    private static LocationManager locationManager;
    private static WifiManager wifiManager;
    private static AudioManager audioManager;
    private static ArrayList<Device> bluetoothDevices = new ArrayList<Device>(), wifiDevices = new ArrayList<Device>();
    private static PendingIntent alarmIntent;
    private static ArrayList<Profile> profiles;
    private static boolean continueCheckingTOC;
    private static int scan_interval;
    private static boolean wifiOrBluetoothComplete, toastWhenProfileSet;

    private static ScanListener bluetoothScanListener = new ScanListener() {
        @Override
        public void onScanComplete(ArrayList<Device> devices) {
            if (wifiOrBluetoothComplete)
                continueScan();
            else
                wifiOrBluetoothComplete = true;
        }
    };

    private static ScanListener wifiScanLListener = new ScanListener() {
        @Override
        public void onScanComplete(ArrayList<Device> devices) {
            if (wifiOrBluetoothComplete)
                continueScan();
            else
                wifiOrBluetoothComplete = true;
        }
    };

    private static int bluetooth_old_value, wifi_old_value;
    private static Context context;

    public static void scheduleNext(Context context) {
        if (context == null) {
            Log.e("can't ScheduleNext", "context =- null");
            return;
        }
        final long sched = System.currentTimeMillis() + scan_interval;
        Log.e("scheduling: ", String.valueOf(sched));
        final Intent intent = new Intent(context, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC, sched, alarmIntent);
        } else
            alarmManager.set(AlarmManager.RTC, sched, alarmIntent);
    }

    private static void continueScan() {
        Log.e("continue", "scan");
        /**TODO **IMPORTANT
         * getTriggers() if triggered add to viable
         * getRestrictions() if ALL within bounds keep, else remove from viable
         * getProhibitions() if ALL not within bounds keep, else remove
         * sort by priority
         * sort by scope
         * =====================================================================
         * scanForLoop()
         * bool continueCheckingTrigger ^^ >> use this to short circuit the check >> paste in every check
         *
         *
         *
         */
        checkTriggers();

        setProfile();
        completeScan();
    }

    private static void checkTriggers() {
        Log.e("check", "triggers");
        final ArrayList<TriggerOrCommand> triggers = getTriggers();
        for (TriggerOrCommand trigger : triggers) {
            if (trigger.getType() == TriggerOrCommand.Type.COMMAND)
                continue;
            checkBattery(trigger);
            checkBatteryTemperature(trigger);
            checkBluetooth(trigger);
            checkWifi(trigger);
            checkDock(trigger);
            checkHeadEarphoneJack(trigger);
            checkLocation(trigger);
            checkTime(trigger);
            Log.e("checking... :", trigger.toString());
        }
    }

    private static ArrayList<TriggerOrCommand> getTriggers() {
        final ArrayList<TriggerOrCommand> triggerOrCommands = new ArrayList<>();
        if (profiles == null || profiles.size() < 1)
            return triggerOrCommands;

        for (Profile profile : profiles) {
            final ArrayList<TriggerOrCommand> arrayList = profile.getTriggersOrCommands();
            for (TriggerOrCommand triggerOrCommand : arrayList) {
                if (triggerOrCommand.getType() == TriggerOrCommand.Type.TRIGGER)
                    triggerOrCommands.add(triggerOrCommand);
            }
        }
        return triggerOrCommands;
    }

    private static ArrayList<TriggerOrCommand> getRestrictions() {
        final ArrayList<TriggerOrCommand> triggerOrCommands = new ArrayList<>();

        if (profiles == null || profiles.size() < 1)
            return triggerOrCommands;

        for (Profile profile : profiles) {
            final ArrayList<TriggerOrCommand> arrayList = profile.getTriggersOrCommands();
            for (TriggerOrCommand triggerOrCommand : arrayList) {
                if (triggerOrCommand.getType() == TriggerOrCommand.Type.RESTRICTIONS)
                    triggerOrCommands.add(triggerOrCommand);
            }
        }
        return triggerOrCommands;
    }

    private static ArrayList<TriggerOrCommand> getProhibitions() {
        final ArrayList<TriggerOrCommand> triggerOrCommands = new ArrayList<>();
        if (profiles == null || profiles.size() < 1)
            return triggerOrCommands;

        for (Profile profile : profiles) {
            final ArrayList<TriggerOrCommand> arrayList = profile.getTriggersOrCommands();
            for (TriggerOrCommand triggerOrCommand : arrayList) {
                if (triggerOrCommand.getType() == TriggerOrCommand.Type.PROHIBITION)
                    triggerOrCommands.add(triggerOrCommand);
            }
        }
        return triggerOrCommands;
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

    private static void removeFromViable(TriggerOrCommand tOC) {
        if ((tOC != null) && (viable.contains(tOC)))
            viable.remove(tOC);
    }

    private static void addToViable(TriggerOrCommand tOC) {
        if ((tOC != null) && !(viable.contains(tOC)))
            viable.add(tOC);
    }

    private static void checkBatteryTemperature(final TriggerOrCommand triggerOrCommand) {
        //Todo fill in
        if (!(triggerOrCommand.getCategory().equals(Utility.TRIGGER_BATTERY_TEMPERATURE)))
            return;

        final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        final Intent batteryStatus = context.registerReceiver(null, filter);
        final float battery_temp = Math.round(batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f);
        switch (triggerOrCommand.getType()) {
            case RESTRICTIONS:
                try {
                    if (Integer.parseInt(triggerOrCommand.getValue()) < battery_temp)
                        removeFromViable(triggerOrCommand);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case TRIGGER:
                try {
                    if (Integer.parseInt(triggerOrCommand.getValue()) >= battery_temp)
                        addToViable(triggerOrCommand);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PROHIBITION:
                try {
                    if (Integer.parseInt(triggerOrCommand.getValue()) >= battery_temp)
                        removeFromViable(triggerOrCommand);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private static void checkBluetooth(final TriggerOrCommand triggerOrCommand) {
        //Todo fill in
        if (!(triggerOrCommand.getCategory().equals(Utility.TRIGGER_BLUETOOTH)))
            return;

        switch (triggerOrCommand.getType()) {
            case RESTRICTIONS:
                if (!isBluetoothDeviceAvailable(triggerOrCommand.getValue()))
                    removeFromViable(triggerOrCommand);
                break;
            case TRIGGER:
                if (isBluetoothDeviceAvailable(triggerOrCommand.getValue()))
                    addToViable(triggerOrCommand);
                break;
            case PROHIBITION:
                if (isBluetoothDeviceAvailable(triggerOrCommand.getValue()))
                    removeFromViable(triggerOrCommand);
                break;
        }
    }

    private static boolean isBluetoothDeviceAvailable(String device) {
        for (Device bluetoothDevice : bluetoothDevices) {
            if (bluetoothDevice.getName().equals(device) || bluetoothDevice.getAddress().equals(device))
                return true;
        }
        return false;
    }

    private static boolean iWifiDeviceAvailable(String device) {
        for (Device wifiDevice : wifiDevices) {
            if (wifiDevice.getName().equals(device) || wifiDevice.getAddress().equals(device))
                return true;
        }
        return false;
    }

    private static void checkWifi(final TriggerOrCommand triggerOrCommand) {
        //Todo fill in
        if (!(triggerOrCommand.getCategory().equals(Utility.TRIGGER_WIFI)))
            return;

        switch (triggerOrCommand.getType()) {
            case RESTRICTIONS:
                if (!iWifiDeviceAvailable(triggerOrCommand.getValue()))
                    removeFromViable(triggerOrCommand);
                break;
            case TRIGGER:
                if (iWifiDeviceAvailable(triggerOrCommand.getValue()))
                    addToViable(triggerOrCommand);
                break;
            case PROHIBITION:
                if (iWifiDeviceAvailable(triggerOrCommand.getValue()))
                    removeFromViable(triggerOrCommand);
                break;
        }
    }

    private static void checkHeadEarphoneJack(final TriggerOrCommand triggerOrCommand) {
        //Todo fill in
        if (!(triggerOrCommand.getCategory().equals(Utility.TRIGGER_EARPHONE_JACK)))
            return;

        switch (triggerOrCommand.getType()) {
            case RESTRICTIONS:
                try {
                    if (!audioManager.isWiredHeadsetOn())
                        addToViable(triggerOrCommand);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TRIGGER:
                try {
                    if (audioManager.isWiredHeadsetOn())
                        addToViable(triggerOrCommand);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PROHIBITION:
                try {
                    if (audioManager.isWiredHeadsetOn())
                        removeFromViable(triggerOrCommand);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private static void checkDock(final TriggerOrCommand triggerOrCommand) {
        //Todo fill in
        if (!(triggerOrCommand.getCategory().equals(Utility.TRIGGER_DOCK)))
            return;
//        IntentFilter ifilter = new IntentFilter(Intent.ACTION_DOCK_EVENT);
//        Intent dockStatus = context.registerReceiver(null, ifilter);
//
//        int dockState = .getIntExtra(EXTRA_DOCK_STATE, -1);
//        boolean isDocked = dockState != Intent.EXTRA_DOCK_STATE_UNDOCKED;
//        switch (triggerOrCommand.getType()) {
//            case RESTRICTIONS:
//
//                break;
//            case TRIGGER:
//
//                addToViable(triggerOrCommand);
//                break;
//            case PROHIBITION:
//
//                break;
//        }
    }

    private static void checkTime(final TriggerOrCommand triggerOrCommand) {
        //Todo fill in
        if (!(triggerOrCommand.getCategory().equals(Utility.TRIGGER_TIME)))
            return;

//        switch (triggerOrCommand.getType()) {
//            case RESTRICTIONS:
//
//                break;
//            case TRIGGER:
//
//                break;
//            case PROHIBITION:
//
//                break;
//        }
    }

    private static void checkLocation(final TriggerOrCommand triggerOrCommand) {
        //Todo fill in
        if (!(triggerOrCommand.getCategory().equals(Utility.TRIGGER_LOCATION)))
            return;

//        switch (triggerOrCommand.getType()) {
//            case RESTRICTIONS:
//
//                break;
//            case TRIGGER:
//
//                break;
//            case PROHIBITION:
//
//                break;
//        }
    }

    private static void checkBattery(final TriggerOrCommand triggerOrCommand) {
        if (context == null || !(triggerOrCommand.getCategory().equals(Utility.TRIGGER_BATTERY)))
            return;
        final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        //Todo ---- register this, and toast it to see if it works well or not, if so make it a broadcast receiver
        //Todo viable.add(...), remove()

        final Intent batteryStatus = context.registerReceiver(null, filter);
        final int battery_level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

        switch (triggerOrCommand.getType()) {
            case RESTRICTIONS:
                try {
                    if (!(Integer.parseInt(triggerOrCommand.getValue()) > battery_level))
                        addToViable(triggerOrCommand);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TRIGGER:
                try {
                    if (Integer.parseInt(triggerOrCommand.getValue()) > battery_level)
                        addToViable(triggerOrCommand);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PROHIBITION:
                try {
                    if (Integer.parseInt(triggerOrCommand.getValue()) > battery_level)
                        removeFromViable(triggerOrCommand);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }


    private static void checkProfiles() {
        Log.e("check", "profiles");
        getOldValues();
        if (context == null)
            return;

        if (bluetoothDevices == null)
            bluetoothDevices = new ArrayList<Device>();
        else
            bluetoothDevices.clear();

        if (wifiDevices == null)
            wifiDevices = new ArrayList<Device>();
        else
            wifiDevices.clear();

        wifiOrBluetoothComplete = false;

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
                        bluetoothScanListener.onScanComplete(bluetoothDevices);
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


    private static void checkIfShouldRunService() {
        //Todo dont run service if>> numProfiles =0 or if allProfiles can be handled with broadcasts
        if (context == null)
            return;
        for (int i = 0; i < profiles.size(); i++) {
            final Profile profile = profiles.get(i);
//            for (int j = 0; j < profile.getTriggers().size(); i++) {
//                final TriggerOrCommand trigger = profile.getTriggers().get(j);
//                final String type = trigger.getCategory();
//                //Todo ---- might be more
//                if (type.equals(Utility.TRIGGER_BLUETOOTH) || type.equals(Utility.TRIGGER_BATTERY) || type.equals(Utility.TRIGGER_WIFI) || type.equals(Utility.TRIGGER_LOCATION)) {
//                    return;
//                }
//            }
        }
        context.stopService(new Intent(context, ProfileService.class));
    }

    private static void setProfile() {
        if (viable.size() < 1) {
            setOldValues();
            return;
        }
        final ArrayList<Profile> viableProfiles = getViableProfiles();
        profiles.clear();
        sortViableProfiles(viableProfiles);

        //Todo fill in
        final Profile profile = viableProfiles.get(0);
        Log.e("Setting Profile: ", profile.getID() + ". " + profile.getName());

        final ArrayList<TriggerOrCommand> commands = Utility.getCommands(profile.getTriggersOrCommands());

        for (TriggerOrCommand command : commands) {
            setCommand(command);
        }
    }

    private static void sortViableProfiles(ArrayList<Profile> profiles) {
        Collections.sort(profiles, new Comparator<Profile>() {
            @Override
            public int compare(Profile profile, Profile profile2) {
                return profile.getPriority() - profile2.getPriority();
            }
        });
//Todo consider sorting with radius
    }

//    private static int getRadiusOrPriority(String type){
//        * Location
//                * Wifi
//                * Bluetooth
//                * Battery
//                * NFC
//    }

    public static ArrayList<Profile> getViableProfiles() {
        final ArrayList<Profile> profiles1 = new ArrayList<>();
        for (Profile profile : profiles) {
            final boolean isProhibPass = Utility.getProhibitions(profile.getTriggersOrCommands()).size() == getViableProhibitionsUsingProfile(profile.getID()).size();
            final boolean isRestrPass = Utility.getRestrictions(profile.getTriggersOrCommands()).size() == getViableRestrictionsUsingProfile(profile.getID()).size();

            if (isProhibPass && isRestrPass)
                profiles1.add(profile);
        }

        return profiles1;
    }

    public static ArrayList<TriggerOrCommand> getViableRestrictionsUsingProfile(String profileID) {
        final ArrayList<TriggerOrCommand> triggerOrCommands = new ArrayList<>();
        for (TriggerOrCommand triggerOrCommand : viable) {
            if ((triggerOrCommand.getType() == TriggerOrCommand.Type.RESTRICTIONS) && (profileID.equals(triggerOrCommand.getProfileID())))
                triggerOrCommands.add(triggerOrCommand);
        }
        return triggerOrCommands;
    }

    public static ArrayList<TriggerOrCommand> getViableProhibitionsUsingProfile(String profileID) {
        final ArrayList<TriggerOrCommand> triggerOrCommands = new ArrayList<>();
        for (TriggerOrCommand triggerOrCommand : viable) {
            if ((triggerOrCommand.getType() == TriggerOrCommand.Type.PROHIBITION) && (profileID.equals(triggerOrCommand.getProfileID())))
                triggerOrCommands.add(triggerOrCommand);
        }
        return triggerOrCommands;
    }

    private static void completeScan() {
        wifiOrBluetoothComplete = false;
        //Todo get profiles on each scan, schedule next
        if (context == null)
            return;
        Toast.makeText(context, "Viable : " + viable.toString(), Toast.LENGTH_LONG).show();
        Log.e("Viable : ", viable.toString());
        scheduleNext(context);
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

    private static void startScan() {
        Log.e("start", "scan");
        //TODO STARTSCAN()
        viable.clear();
        profiles = Utility.getProfiles(context);
        //Todo consider saving a string oldProfile and using it to check if all the profiles are
        checkProfiles();
    }

    private static void setCommand(TriggerOrCommand command) {
        if (context == null || !(command.getType() == TriggerOrCommand.Type.COMMAND))
            return;

        final String val = command.getValue();
        if (val == null)
            return;

        try {
            if (command.getCategory().equals("WIFI_SETTING")) {
                Utility.setWifi(context, val.equals("1"));

            } else if (command.getCategory().equals("BLUETOOTH_SETTING")) {
                Utility.setBluetooth(context, val.equals("1"));

            } else if (command.getCategory().equals("DATA_SETTING")) {
                //Todo

            } else if (command.getCategory().equals("BRIGHTNESS_SETTING")) {
                if (val.equals("-1")) {
                    Utility.setAutoBrightness(context, true);
                } else {
                    Utility.setBrightness(context, Integer.parseInt(val));
                }
            } else if (command.getCategory().equals("SILENT_MODE_SETTING")) {
                Utility.setSilentMode(context, val.equals("1"));
            } else if (command.getCategory().equals("NOTIFICATION_VOLUME_SETTING")) {
                Utility.setNotificationVolume(context, Integer.parseInt(val));
            } else if (command.getCategory().equals("MEDIA_VOLUME_SETTING")) {
                Utility.setMediaVolume(context, Integer.parseInt(val));
            } else if (command.getCategory().equals("RINGER_VOLUME_SETTING")) {
                Utility.setRingerVolume(context, Integer.parseInt(val));
            } else if (command.getCategory().equals("AUTO_ROTATION_SETTING")) {
                Utility.setAutoRotation(context, val.equals("1"));
            } else if (command.getCategory().equals("SLEEP_TIMEOUT_SETTING")) {
                Utility.setScreenTimeout(context, Integer.parseInt(val));
            } else if (command.getCategory().equals("WALLPAPER_SETTING")) {
                Utility.setWallpaper(context, val);
            } else if (command.getCategory().equals("RINGTONE_SETTING")) {
                Utility.setSetRingtone(context, val);
            } else if (command.getCategory().equals("LAUNCH_APP_SETTING")) {
                Utility.launchApp(context, val);
            } else if (command.getCategory().equals("MEDIA_CONTROL_SETTING")) {
                Utility.controlMedia(context, val);
            } else if (command.getCategory().equals("ALARM_VOLUME_SETTING")) {
                Utility.setAlarmVolume(context, Integer.parseInt(val));
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        toastWhenProfileSet = prefs.getBoolean(Utility.PREF_TOAST_WHEN_PROFILE_SET, true);
        scan_interval = Utility.getScanIntervalFromInt(prefs.getInt(Utility.PREF_SCAN_INTERVAL, 2));
        startScan();
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
            startScan();
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