package tools;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
        final PackageManager manager = packageManager(context);
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

    public static PackageManager packageManager(Context context) {
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

}
