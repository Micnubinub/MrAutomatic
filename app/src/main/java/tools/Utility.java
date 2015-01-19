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

import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
    //public static final String AIRPLANE_SETTING = "AIRPLANE_SETTING";
    public static final String SILENT_MODE_SETTING = "SILENT_MODE_SETTING";
    public static final String NOTIFICATION_VOLUME_SETTING = "NOTIFICATION_VOLUME_SETTING";
    public static final String MEDIA_VOLUME_SETTING = "MEDIA_VOLUME_SETTING";
    public static final String RINGER_VOLUME_SETTING = "RINGER_VOLUME_SETTING";
    //public static final String ACCOUNT_SYNC_SETTING = "ACCOUNT_SYNC_SETTING";
    public static final String AUTO_ROTATION_SETTING = "AUTO_ROTATION_SETTING";
    public static final String SLEEP_TIMEOUT_SETTING = "SLEEP_TIMEOUT_SETTING";
    public static final String WALLPAPER_SETTING = "WALLPAPER_SETTING";
    public static final String RINGTONE_SETTING = "RINGTONE_SETTING";
    public static final String LAUNCH_APP_SETTING = "LAUNCH_APP_SETTING";
    public static final String MEDIA_CONTROL_SETTING = "MEDIA_CONTROL_SETTING";
    public static final String ALARM_VOLUME_SETTING = "ALARM_VOLUME_SETTING";

    public static final String SCAN_INTERVAL = "SCAN_INTERVAL";
    public static final String DURATION = "DURATION";

    //Profile editor
    public static final String EDIT_PROFILE = "EDIT_PROFILE";
    public static final String PROFILE_ID = "PROFILE_ID";
    //Todo setting for a toast when a profile is set
    public static final String TOAST_WHEN_PROFILE_SET = "TOAST_WHEN_PROFILE_SET";
    /*
     public static final String a = " ";
     public static final String a = " ";
     public static final String a = " ";
     public static final String a = " ";
     public static final String a = " ";
     public static final String a = " ";
     public static final String a = " ";
     public static final String a = " "; */

    public static ArrayList<App> apps;
    private static Calendar calendar = Calendar.getInstance();

    public static void getApps(Context context) {
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

    }

    public static ArrayList<Command> processCommands(String commands) {
        final ArrayList<Command> commandList = new ArrayList<Command>();
        final String[] commandArray = commands.split(",");
        for (String s : commandArray) {
            final String[] tmp = s.split(":");
            commandList.add(new Command(tmp[0], tmp[1]));
        }
        return commandList;
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

    public static int getMinutes(long date) {
        DateFormat formatter = new SimpleDateFormat("mm");
        calendar.setTimeInMillis(date);
        return Integer.parseInt(formatter.format(calendar.getTime()));
    }

    public static String getDay(long date) {
        DateFormat formatter = new SimpleDateFormat("EEEE");
        calendar.setTimeInMillis(date);
        return formatter.format(calendar.getTime());
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


    public static ArrayList<Profile> getProfiles(Context context) {
        final ArrayList<Profile> profiles = new ArrayList<Profile>();
        final ProfileDBHelper profileDBHelper = new ProfileDBHelper(context);
        final SQLiteDatabase profiledb = profileDBHelper.getReadableDatabase();
        final String[] need = new String[]{ProfileDBHelper.ID, ProfileDBHelper.PROFILE_NAME, ProfileDBHelper.TRIGGERS, ProfileDBHelper.COMMANDS, ProfileDBHelper.PROHIBITIONS, ProfileDBHelper.RESTRICTIONS, ProfileDBHelper.PRIORITY};
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
                                cursor.getString(cursor.getColumnIndex(ProfileDBHelper.TRIGGERS)),
                                cursor.getString(cursor.getColumnIndex(ProfileDBHelper.RESTRICTIONS)),
                                cursor.getString(cursor.getColumnIndex(ProfileDBHelper.PROHIBITIONS)),
                                cursor.getString(cursor.getColumnIndex(ProfileDBHelper.COMMANDS)),
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

    public static void setAutoBrightness(Context context, int value) {
        try {
            if (value < 0)
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            else
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setBrightness(Context context, int value) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setWifi(Context context, int value) {
        try {
            ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(value > 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setBluetooth(Context context, int value) {
        try {
            final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (value > 1) {
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

    public static void setAirplaneMode(Context context, int value) {
        try {
            //Todo airplane ** only up to 16
            Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, (value > 0) ? 0 : 1);
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state", !(value > 0));
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        try {
            Uri uri = Uri.parse(value);

            WallpaperManager wpm = WallpaperManager.getInstance(context);
            BitmapFactory.Options options = new BitmapFactory.Options();
            // set to false to prepare image for decoding
            //
            options.inJustDecodeBounds = false;
            wpm.setBitmap(BitmapFactory.decodeStream(new FileInputStream(uri.getPath()), null, options));
            //Todo check this, make sure it doesn't run out of memory
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setSilentMode(Context context, int value) {
        try {

            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            switch (value) {
                case 0:
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

                    break;
                case 1:
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    break;
                case 2:
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    break;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setSetRingtone(Context context, String value) {
        try {
            Settings.System.putString(context.getContentResolver(), Settings.System.RINGTONE, value);
//            AudioManager.setRingerMode(RINGER_MODE_NORMAL);
//            AudioManager.setRingerMode(RINGER_MODE_SILENT);
//            AudioManager.setRingerMode(RINGER_MODE_VIBRATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setVibrate(Context context, int value) {
        try {

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

    public static String getCommandName(String item) {
        if (item.equals(Utility.ALARM_VOLUME_SETTING)) {
            item = "Alarm Volume";
        } else if (item.equals(Utility.AUTO_ROTATION_SETTING)) {
            item = "Auto rotation";
        } else if (item.equals(Utility.BLUETOOTH_SETTING)) {
            item = "Bluetooth";
        } else if (item.equals(Utility.WALLPAPER_SETTING)) {
            item = "Wallpaper";
        } else if (item.equals(Utility.WIFI_SETTING)) {
            item = "Wifi";
        } else if (item.equals(Utility.MEDIA_VOLUME_SETTING)) {
            item = "Media volume";
        } else if (item.equals(Utility.LAUNCH_APP_SETTING)) {
            item = "Launch app";
        } else if (item.equals(Utility.DATA_SETTING)) {
            item = "Data";
        } else if (item.equals(Utility.BRIGHTNESS_SETTING)) {
            item = "Brightness";
        } else if (item.equals(Utility.RINGER_VOLUME_SETTING)) {
            item = "Ringtone volume";
        } else if (item.equals(Utility.MEDIA_CONTROL_SETTING)) {
            item = "Music control";
        } else if (item.equals(Utility.NOTIFICATION_VOLUME_SETTING)) {
            item = "Notification volume";
        } else if (item.equals(Utility.RINGTONE_SETTING)) {
            item = "Ringtone";
        } else if (item.equals(Utility.SILENT_MODE_SETTING)) {
            item = "Silent mode";
        } else if (item.equals(Utility.SLEEP_TIMEOUT_SETTING)) {
            item = "Screen timeout";
        }
        return item;
    }

    public static ArrayList<Command> getCommands(String commands) {
        final ArrayList<Command> commandList = new ArrayList<Command>();
        for (String s : commands.split(",")) {
            final String[] array = s.split(":", 2);
            commandList.add(new Command(array[0], array[1]));
        }
        return commandList;
    }

    public static ArrayList<Trigger> getTriggers(String triggers) {
        final ArrayList<Trigger> triggerList = new ArrayList<Trigger>();
        for (String s : triggers.split(",")) {
            final String[] array = s.split(":", 2);
            triggerList.add(new Trigger(array[0], array[1]));
        }
        return triggerList;
    }

    public static String getTriggerName(String item) {
        if (item.equals(Utility.TRIGGER_APP_LAUNCH)) {
            item = "App launch";
        } else if (item.equals(Utility.TRIGGER_BATTERY)) {
            item = "Battery";
        } else if (item.equals(Utility.TRIGGER_BATTERY_TEMPERATURE)) {
            item = "Battery temperature";
        } else if (item.equals(Utility.TRIGGER_BLUETOOTH)) {
            item = "Bluetooth device";
        } else if (item.equals(Utility.TRIGGER_NFC)) {
            item = "NFC";
        } else if (item.equals(Utility.TRIGGER_LOCATION)) {
            item = "Location";
        } else if (item.equals(Utility.TRIGGER_EARPHONE_JACK)) {
            item = "Headphone jack";
        } else if (item.equals(Utility.TRIGGER_DOCK)) {
            item = "Dock";
        } else if (item.equals(Utility.TRIGGER_TIME)) {
            item = "Time";
        } else if (item.equals(Utility.TRIGGER_WIFI)) {
            item = "Wifi evice";
        }
        return item;
    }

    public static class EarphoneJackReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
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
            if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED))
                Toast.makeText(context, "Charging", Toast.LENGTH_LONG).show();

            if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED))
                Toast.makeText(context, "Not Charging", Toast.LENGTH_LONG).show();

        }
    }

    public static class BluetoothStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED))
                Toast.makeText(context, "Bluetooth connection state changed", Toast.LENGTH_LONG).show();

        }
    }

    public static class DockReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_DOCK_EVENT))
                Toast.makeText(context, "Dock event", Toast.LENGTH_LONG).show();

        }
    }

    public static class SmsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Message received", Toast.LENGTH_LONG).show();

        }
    }

    public static class ScreenOnOffReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "onoff", Toast.LENGTH_LONG).show();

        }
    }

    public static class WifiStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Wifi state c", Toast.LENGTH_LONG).show();

        }
    }
}
