package tools;

import android.app.WallpaperManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.KeyEvent;
import android.widget.Toast;

import com.micnubinub.mrautomatic.Profile;
import com.micnubinub.mrautomatic.ProfileDBHelper;
import com.micnubinub.mrautomatic.R;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tools.TriggerOrCommand.Type;
import view_classes.WeekDayChooser;

/**
 * Created by root on 9/07/14.
 */
public class Utility {
    public static final String CURRENT_PROFILE = "CURRENT_PROFILE";
    public static final String TRIGGER_BATTERY_TEMPERATURE = "BATTERY_TEMPERATURE";
    public static final String TRIGGER_BATTERY = "BATTERY_CHARGING";
    public static final String TRIGGER_BLUETOOTH = "BLUETOOTH";
    public static final String TRIGGER_WIFI = "WIFI";
    public static final String TRIGGER_APP_LAUNCH = "APP_LAUNCH";
    public static final String TRIGGER_LOCATION = "LOCATION";
    public static final String TRIGGER_TIME = "TIME";
    public static final String TRIGGER_DOCK = "DOCK";
    public static final String TRIGGER_EARPHONE_JACK = "EARPHONE_JACK";
    public static final String TRIGGER_NFC = "NFC";

    public static final String WIFI_SETTING = "WIFI_SETTING";
    public static final String BLUETOOTH_SETTING = "BLUETOOTH_SETTING";
    public static final String DATA_SETTING = "DATA_SETTING";
    public static final String BRIGHTNESS_SETTING = "BRIGHTNESS_SETTING";
    public static final String AIRPLANE_SETTING = "AIRPLANE_SETTING";
    public static final String SILENT_MODE_SETTING = "SILENT_MODE_SETTING";
    public static final String NOTIFICATION_VOLUME_SETTING = "NOTIFICATION_VOLUME_SETTING";
    public static final String MEDIA_VOLUME_SETTING = "MEDIA_VOLUME_SETTING";
    public static final String RINGER_VOLUME_SETTING = "RINGER_VOLUME_SETTING";
    public static final String ACCOUNT_SYNC_SETTING = "ACCOUNT_SYNC_SETTING";
    public static final String AUTO_ROTATION_SETTING = "AUTO_ROTATION_SETTING";
    public static final String SLEEP_TIMEOUT_SETTING = "SLEEP_TIMEOUT_SETTING";
    public static final String WALLPAPER_SETTING = "WALLPAPER_SETTING";
    public static final String RINGTONE_SETTING = "RINGTONE_SETTING";
    public static final String LAUNCH_APP_SETTING = "LAUNCH_APP_SETTING";
    public static final String MEDIA_CONTROL_SETTING = "MEDIA_CONTROL_SETTING";
    public static final String ALARM_VOLUME_SETTING = "ALARM_VOLUME_SETTING";

    //Profile editor
    public static final String EDIT_PROFILE = "EDIT_PROFILE";
    public static final String PREF_TOAST_WHEN_PROFILE_SET = "TOAST_WHEN_PROFILE_SET";
    public static final String PREF_PLAY_PREVIEW = "PREF_PLAY_PREVIEW";
    public static final String PREF_SCAN_INTERVAL = "PREF_SCAN_INTERVAL";
    public static final String PREF_DEFAULT_PROFILE = "PREF_DEFAULT_PROFILE";
    public static final String PREF_OVERRIDE_TIME_TRIGGER_DURATION = "PREF_OVERRIDE_TIME_TRIGGER_DURATION";

    /*
     public static final String a = " ";
     public static final String a = " ";
     public static final String a = " "; */

    public static ArrayList<App> apps;
    private static Calendar calendar = Calendar.getInstance();

    public static ArrayList<App> getApps(Context context) {
        final PackageManager manager = getPackageManager(context);
        final Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list = manager.queryIntentActivities(i, 0);
        try {
            apps.clear();
            apps = null;
        } catch (Exception e) {
        }

        apps = new ArrayList<App>(list.size());
        for (ResolveInfo info : list) {
            apps.add(new App(info.loadLabel(manager).toString(), info.activityInfo.packageName, info.loadIcon(manager)));
        }
        sort(apps);
        return apps;
    }

    public static long getNextDayOffWeek(WeekDayChooser.WeekDay dayOfWeek) {
        //TODO test
        final long now = System.currentTimeMillis();
        final int todayIndex = getDayIndex(getDay(now).toUpperCase());
        final int dayOfWeekIndex = getDayIndex(dayOfWeek);
        final int diffDays = (dayOfWeekIndex - todayIndex) % 7;
        return (diffDays * 86400000) - (getHours(now) * 3600000) - (getMinutes(now) * 60000) - (getSeconds(now) * 1000);
    }


    public static int getScanIntervalFromInt(int scan_interval) {
        int scans = 30000;
        switch (scan_interval) {
            case 0:
                scans = 30000;
                break;
            case 1:
                scans = 60000;
                break;
            case 2:
                scans = 120000;
                break;
            case 3:
                scans = 180000;
                break;
            case 4:
                scans = 300000;
                break;
            case 5:
                scans = 600000;
                break;
            case 6:
                scans = 900000;
                break;
        }
        return scans;
    }

    private static int getDayIndex(String weekDay) {
        WeekDayChooser.WeekDay[] weekDays = WeekDayChooser.WeekDay.values();
        for (int i = 0; i < weekDays.length; i++) {
            if (weekDay.equals(weekDays[i].toString()))
                return i;
        }
        return -1;
    }

    private static int getDayIndex(WeekDayChooser.WeekDay weekDay) {
        WeekDayChooser.WeekDay[] weekDays = WeekDayChooser.WeekDay.values();
        for (int i = 0; i < weekDays.length; i++) {
            if (weekDay == weekDays[i])
                return i;
        }
        return -1;
    }

    public static int getSeconds(long date) {
        DateFormat formatter = new SimpleDateFormat("ss");
        calendar.setTimeInMillis(date);
        return Integer.parseInt(formatter.format(calendar.getTime()));

    }

    public static int getHours(long date) {
        DateFormat formatter = new SimpleDateFormat("hh");
        calendar.setTimeInMillis(date);
        return Integer.parseInt(formatter.format(calendar.getTime()));
    }

    public static String getDay(long date) {
        final DateFormat formatter = new SimpleDateFormat("EEE");
        calendar.setTimeInMillis(date);
        return formatter.format(calendar.getTime());
    }

    public static String getMonth(long date) {
        final Calendar calendar = Calendar.getInstance();
        final DateFormat formatter = new SimpleDateFormat("MMM");
        calendar.setTimeInMillis(date);
        return formatter.format(calendar.getTime());
    }

    public static int getMinutes(long date) {
        DateFormat formatter = new SimpleDateFormat("mm");
        calendar.setTimeInMillis(date);
        return Integer.parseInt(formatter.format(calendar.getTime()));
    }

    public static PackageManager getPackageManager(Context context) {
        return context.getPackageManager();
    }

    public static void sort(ArrayList<App> list) {
        Collections.sort(list, new Comparator<App>() {
            @Override
            public int compare(App app, App app1) {
                return app.getName().compareToIgnoreCase(app1.getName());
            }
        });
    }

    public static String getAppName(String address) {
        if (apps == null)
            return "Unknown";
        else
            for (App app : apps) {
                if (app.getAddress().equals(address))
                    return app.getName();
            }
        return "Unknown";
    }

    public static ArrayList<TriggerOrCommand> getTriggers(ArrayList<TriggerOrCommand> list) {
        final ArrayList<TriggerOrCommand> trigs = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            final TriggerOrCommand command = list.get(i);
            if (command.getType().equals(Type.TRIGGER))
                trigs.add(command);
        }
        return trigs;
    }

    public static ArrayList<TriggerOrCommand> getRestrictions(ArrayList<TriggerOrCommand> list) {
        final ArrayList<TriggerOrCommand> trigs = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            final TriggerOrCommand command = list.get(i);
            if (command.getType().equals(Type.RESTRICTIONS))
                trigs.add(command);
        }
        return trigs;
    }

    public static ArrayList<TriggerOrCommand> getCommands(ArrayList<TriggerOrCommand> list) {
        final ArrayList<TriggerOrCommand> trigs = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            final TriggerOrCommand command = list.get(i);
            if (command.getType().equals(Type.COMMAND))
                trigs.add(command);
        }
        return trigs;
    }

    public static ArrayList<TriggerOrCommand> getProhibitions(ArrayList<TriggerOrCommand> list) {
        final ArrayList<TriggerOrCommand> trigs = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            final TriggerOrCommand command = list.get(i);
            if (command.getType().equals(Type.PROHIBITION))
                trigs.add(command);
        }
        return trigs;
    }


    public static ArrayList<Profile> getProfiles(Context context) {
        final ArrayList<Profile> profiles = new ArrayList<Profile>();
        final ProfileDBHelper profileDBHelper = new ProfileDBHelper(context);
        final SQLiteDatabase profiledb = profileDBHelper.getReadableDatabase();
        final String[] need = new String[]{ProfileDBHelper.ID, ProfileDBHelper.PROFILE_NAME, ProfileDBHelper.TRIGGERS_AND_COMMANDS, ProfileDBHelper.PRIORITY};
        final Cursor cursor = profiledb.query(ProfileDBHelper.PROFILE_TABLE, need, null, null, null, null, null);
        try {
            cursor.moveToPosition(0);
        } catch (Exception e) {
        }
        while (!cursor.isAfterLast()) {
            try {
                int priority;
                try {
                    priority = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.PRIORITY)));
                } catch (Exception e) {
                    priority = 3;
                }
                profiles.add(
                        new Profile(
                                cursor.getString(cursor.getColumnIndex(ProfileDBHelper.ID)),
                                cursor.getString(cursor.getColumnIndex(ProfileDBHelper.PROFILE_NAME)),
                                cursor.getString(cursor.getColumnIndex(ProfileDBHelper.TRIGGERS_AND_COMMANDS)),
                                priority
                        )
                );
            } catch (Exception e) {
            }
            cursor.moveToNext();
        }
        try {
            cursor.close();
            profiledb.close();
        } catch (Exception e) {
        }
        return profiles;
    }

    public static void setAutoRotation(Context context, boolean value) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, value ? 1 : 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void controlMedia(Context context, String value) {
        //Todo fill in
    }

    public static void setNotificationVolume(Context context, int value) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.VOLUME_NOTIFICATION, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setMediaVolume(Context context, int value) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.VOLUME_MUSIC, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setAlarmVolume(Context context, int value) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.VOLUME_ALARM, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setRingerVolume(Context context, int value) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.VOLUME_RING, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setAutoBrightness(Context context, boolean value) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, value ? Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC : Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setBrightness(Context context, int value) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setWifi(Context context, boolean value) {
        try {
            ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setBluetooth(Context context, boolean value) {
        try {
            final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (value) {
                if (!adapter.isEnabled())
                    adapter.enable();
            } else {
                if (adapter.isEnabled())
                    adapter.disable();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setScreenTimeout(Context context, int value) {
        //Todo check
        try {
            int time_out;
            switch (value) {
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
                default:
                    time_out = 60;
                    break;
            }
            time_out = time_out * 1000;
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, time_out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setData(Context context, int value) {
        try {/*
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * public static void setAirplaneMode(Context context, int value) {
     * try {
     * //Todo airplane ** only up to 16
     * Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, (value > 0) ? 0 : 1);
     * Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
     * intent.putExtra("state", !(value > 0));
     * context.sendBroadcast(intent);
     * } catch (Exception e) {
     * e.printStackTrace();
     * }
     * }
     */

    public static void playMusic(Context context) {
        try {
            final long eventTime = SystemClock.uptimeMillis();
            final Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
            final KeyEvent downEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
            downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
            context.sendOrderedBroadcast(downIntent, null);
            final Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
            final KeyEvent upEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
            upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
            context.sendOrderedBroadcast(upIntent, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void setWallpaper(Context context, String value) {
        //Todo fill in
        try {
            final Uri uri = Uri.parse(value);

            final WallpaperManager wpm = WallpaperManager.getInstance(context);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            // set to false to prepare image for decoding
            //
            options.inJustDecodeBounds = false;
            wpm.setBitmap(decodeFile(new File(uri.getPath())));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Bitmap decodeFile(File f) {
        //Todo check this, make sure it doesn't run out of memory
        Bitmap b = null;

        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (fis == null)
            return null;

        BitmapFactory.decodeStream(fis, null, o);
        try {
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

//      int scale = 1;
//        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
//            scale = (int) Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
//                    (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
//        }
//
//        //Decode with inSampleSize
//        BitmapFactory.Options o2 = new BitmapFactory.Options();
//        o2.inSampleSize = scale;
//        fis = new FileInputStream(f);
//        b = BitmapFactory.decodeStream(fis, null, o2);
//        fis.close();

        return b;
    }

    public static void setSilentMode(Context context, boolean value) {
        try {
            //Todo consider getting the old value and setting if value is false
            final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (value)
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            else
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setSetRingtone(Context context, String value) {
        //Todo fill in
        try {
            Settings.System.putString(context.getContentResolver(), Settings.System.RINGTONE, value);
//            AudioManager.setRingerMode(RINGER_MODE_NORMAL);
//            AudioManager.setRingerMode(RINGER_MODE_SILENT);
//            AudioManager.setRingerMode(RINGER_MODE_VIBRATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setVibrate(Context context, boolean value) {
        try {
            //Todo consider getting the old value and setting if value is false
            final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (value)
                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            else
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void launchApp(Context context, String packageName) {
        try {
            final PackageManager packageManager = context.getPackageManager();
            context.startActivity(packageManager.getLaunchIntentForPackage(packageName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static ArrayList<TriggerOrCommand> getTriggersAndCommands(String triggers) {
        final ArrayList<TriggerOrCommand> triggerList = new ArrayList<TriggerOrCommand>();
        for (String s : triggers.split(",")) {
            final String[] array = s.split(":", 3);
            triggerList.add(new TriggerOrCommand(getTypeFromString(array[0]), array[1], array[2]));
        }
        return triggerList;
    }

    public static Type getTypeFromString(String typeString) {
        Type type = Type.TRIGGER;

        if (typeString.equals(Type.COMMAND.toString()))
            type = Type.COMMAND;
        else if (typeString.equals(Type.PROHIBITION.toString()))
            type = Type.PROHIBITION;
        else if (typeString.equals(Type.RESTRICTIONS.toString()))
            type = Type.RESTRICTIONS;

        return type;
    }

    public static String getTriggerOrCommandName(String item) {
        if (item.equals(TRIGGER_APP_LAUNCH)) {
            item = "App launch";
        } else if (item.equals(TRIGGER_BATTERY)) {
            item = "Battery";
        } else if (item.equals(TRIGGER_BATTERY_TEMPERATURE)) {
            item = "Battery temperature";
        } else if (item.equals(TRIGGER_BLUETOOTH)) {
            item = "Bluetooth device";
        } else if (item.equals(TRIGGER_NFC)) {
            item = "NFC";
        } else if (item.equals(TRIGGER_LOCATION)) {
            item = "Location";
        } else if (item.equals(TRIGGER_EARPHONE_JACK)) {
            item = "Headphone jack";
        } else if (item.equals(TRIGGER_DOCK)) {
            item = "Dock";
        } else if (item.equals(TRIGGER_TIME)) {
            item = "Time";
        } else if (item.equals(TRIGGER_WIFI)) {
            item = "Wifi device";
        } else if (item.equals(ALARM_VOLUME_SETTING)) {
            item = "Alarm Volume";
        } else if (item.equals(AUTO_ROTATION_SETTING)) {
            item = "Auto rotation";
        } else if (item.equals(BLUETOOTH_SETTING)) {
            item = "Bluetooth";
        } else if (item.equals(WALLPAPER_SETTING)) {
            item = "Wallpaper";
        } else if (item.equals(WIFI_SETTING)) {
            item = "Wifi";
        } else if (item.equals(MEDIA_VOLUME_SETTING)) {
            item = "Media volume";
        } else if (item.equals(LAUNCH_APP_SETTING)) {
            item = "Launch app";
        } else if (item.equals(DATA_SETTING)) {
            item = "Data";
        } else if (item.equals(BRIGHTNESS_SETTING)) {
            item = "Brightness";
        } else if (item.equals(RINGER_VOLUME_SETTING)) {
            item = "Ringtone volume";
        } else if (item.equals(MEDIA_CONTROL_SETTING)) {
            item = "Music control";
        } else if (item.equals(NOTIFICATION_VOLUME_SETTING)) {
            item = "Notification volume";
        } else if (item.equals(RINGTONE_SETTING)) {
            item = "Ringtone";
        } else if (item.equals(SILENT_MODE_SETTING)) {
            item = "Silent mode";
        } else if (item.equals(SLEEP_TIMEOUT_SETTING)) {
            item = "Screen timeout";
        }
        return item;
    }

    public static int getIcon(String type) {
        int icon = R.drawable.info;

        if (type.equals(TRIGGER_BATTERY)) {
            icon = R.drawable.battery_percentage;
        } else if (type.equals(TRIGGER_APP_LAUNCH) || type.equals(LAUNCH_APP_SETTING)) {
            icon = R.drawable.app_launch;
        } else if (type.equals(TRIGGER_BATTERY_TEMPERATURE)) {
            icon = R.drawable.battery_temperature;
        } else if (type.equals(TRIGGER_LOCATION)) {
            icon = R.drawable.location;
        } else if (type.equals(TRIGGER_WIFI) || type.equals(WIFI_SETTING)) {
            icon = R.drawable.wifi;
        } else if (type.equals(TRIGGER_BLUETOOTH) || type.equals(BLUETOOTH_SETTING)) {
            icon = R.drawable.bluetooth;
        } else if (type.equals(TRIGGER_NFC)) {
            icon = R.drawable.nfc;
        } else if (type.equals(TRIGGER_DOCK)) {
            icon = R.drawable.dock;
        } else if (type.equals(TRIGGER_TIME) || type.equals(ALARM_VOLUME_SETTING)) {
            icon = R.drawable.time_trigger;
        } else if (type.equals(TRIGGER_EARPHONE_JACK) || type.equals(MEDIA_CONTROL_SETTING) || type.equals(MEDIA_VOLUME_SETTING)) {
            icon = R.drawable.headphone_jack;
        } else if (type.equals(BRIGHTNESS_SETTING)) {
            icon = R.drawable.brightness;
        } else if (type.equals(DATA_SETTING)) {
            icon = R.drawable.data_temp;
        } else if (type.equals(NOTIFICATION_VOLUME_SETTING)) {
            icon = R.drawable.notification_temp;
        } else if (type.equals(RINGTONE_SETTING)) {
            icon = R.drawable.ringer;
        } else if (type.equals(SILENT_MODE_SETTING)) {
            icon = R.drawable.silent;
        } else if (type.equals(SLEEP_TIMEOUT_SETTING)) {
            icon = R.drawable.time_out;
        } else if (type.equals(AUTO_ROTATION_SETTING)) {
            icon = R.drawable.rotation;
        } else if (type.equals(RINGER_VOLUME_SETTING)) {
            icon = R.drawable.ringer;
        }
        return icon;
    }

    public static class EarphoneJackReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO implement
            Toast.makeText(context, "jack", Toast.LENGTH_LONG).show();
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);

                switch (state) {
                    case 0:
                        // "Headset is unplugged");
                        break;
                    case 1:
                        // "Headset is plugged");
                        break;
                    default:
                        // "I have no idea what the headset state is");
                }

                Toast.makeText(context, "jack", Toast.LENGTH_LONG).show();
                return;
            }

            if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(intent.getAction()))
                Toast.makeText(context, "bluetooth", Toast.LENGTH_LONG).show();
        }
    }

    public static class BatteryPowerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO implement

            if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED))
                Toast.makeText(context, "Charging", Toast.LENGTH_LONG).show();

            if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED))
                Toast.makeText(context, "Not Charging", Toast.LENGTH_LONG).show();

        }
    }

    public static class BluetoothStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO implement
            if (intent.getAction().equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED))
                Toast.makeText(context, "Bluetooth connection state changed", Toast.LENGTH_LONG).show();

        }
    }

    public static class DockReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO implement
            if (intent.getAction().equals(Intent.ACTION_DOCK_EVENT))
                Toast.makeText(context, "Dock event", Toast.LENGTH_LONG).show();

        }
    }

    public static class SmsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO implement
            Toast.makeText(context, "Message received", Toast.LENGTH_LONG).show();

        }
    }

    public static class ScreenOnOffReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO implement
            Toast.makeText(context, "onoff", Toast.LENGTH_LONG).show();

        }
    }

    public static class WifiStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO implement
            Toast.makeText(context, "Wifi state c", Toast.LENGTH_LONG).show();

        }
    }
}
