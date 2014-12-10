package tools;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.provider.Settings;

import com.micnubinub.mrautomatic.ProfileDBHelper;
import com.micnubinub.mrautomatic.ProfileListItem;

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
    public static final String TRIGGER_BATTERY_PERCENTAGE = "BATTERY_PERCENTAGE";
    public static final String TRIGGER_BATTERY_CHARGING = "BATTERY_CHARGING";
    public static final String TRIGGER_BLUETOOTH_BSSID = "BLUETOOTH_BSSID";
    public static final String TRIGGER_BLUETOOTH_SSID = "BLUETOOTH_SSID";
    public static final String TRIGGER_WIFI_BSSID = "WIFI_BSSID";
    public static final String TRIGGER_WIFI_SSID = "WIFI_SSID";
    public static final String TRIGGER_APP_LAUNCH = "TRIGGER_APP_LAUNCH";
    public static final String TRIGGER_LOCATION = "LOCATION";
    public static final String TRIGGER_TIME = "TIME";
    public static final String TRIGGER_NFC = "NFC";
    public static final String SCAN_INTERVAL = "SCAN_INTERVAL";

    public static final String WIFI_SETTING = "WIFI_SETTING";
    public static final String BLUETOOTH_SETTING = "BLUETOOTH_SETTING";
    public static final String DATA_SETTING = "DATA_SETTING";
    public static final String BRIGHTNESS_SETTING = "BRIGHTNESS_SETTING";
    public static final String BRIGHTNESS_AUTO_SETTING = "BRIGHTNESS_AUTO_SETTING";
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
    public static final String START_MUSIC_SETTING = "START_MUSIC_SETTING";
    public static final String ALARM_VOLUME_SETTING = "ALARM_VOLUME_SETTING";

    /*

     public static final String a = " ";
     public static final String a = " ";
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
            //Todo packages and figure out if you should add bitmap to the App class
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


    public static ArrayList<ProfileListItem> getListProfiles(Context context) {
        final ArrayList<ProfileListItem> profiles = new ArrayList<ProfileListItem>();
        final ProfileDBHelper profileDBHelper = new ProfileDBHelper(context);
        final SQLiteDatabase profiledb = profileDBHelper.getReadableDatabase();
        final String[] need = new String[]{ProfileDBHelper.ID, ProfileDBHelper.PROFILE_NAME, ProfileDBHelper.TRIGGERS, ProfileDBHelper.COMMANDS, ProfileDBHelper.PRIORITY};
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
                        new ProfileListItem(
                                cursor.getString(cursor.getColumnIndex(ProfileDBHelper.ID)),
                                cursor.getString(cursor.getColumnIndex(ProfileDBHelper.PROFILE_NAME)),
                                cursor.getString(cursor.getColumnIndex(ProfileDBHelper.TRIGGERS)),
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

        Collections.sort(profiles, new Comparator<ProfileListItem>() {
            //Todo not working right

            @Override
            public int compare(ProfileListItem lhs, ProfileListItem rhs) {
                return lhs.getName().compareToIgnoreCase(lhs.getName());
            }
        });
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
            //Todo airplane
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setSilentMode(Context context, int value) {
        try {
            //Todo silent mode
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setAccountSync(Context context, int value) {
        try {
            //Todo account sync
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playMusic(Context context) {
        try {//Todo play music
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void setWallpaper(Context context, String value) {
        try {
            Uri uri = Uri.parse(value);
            //Todo when saving uri.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setSetRingtone(Context context, String value) {
        try {
            Uri uri = Uri.parse(value);
            //Todo when saving uri.toString();
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

    public static void launchApp(Context context, String value) {
        try {
            getPackageManager(context).getLaunchIntentForPackage(value);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
