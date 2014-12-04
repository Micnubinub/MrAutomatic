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
public class Tools {
    public static final String SCHEDULED_RECORDING = "SCHEDULED_RECORDING";
    public static final String SCHEDULED_COMMAND = "SCHEDULED_COMMAND";
    public static final String WAKE_UP = "WAKE_UP";
    public static final String WIFI_OFF = "WIFI_OFF";
    public static final String EDIT_PROFILE = "EDIT_PROFILE";
    public static final String TRIGGER_BATTERY = "BATTERY";
    public static final String TRIGGER_BLUETOOTH = "BLUETOOTH";
    public static final String TRIGGER_WIFI = "WIFI";
    public static final String TRIGGER_LOCATION = "LOCATION";
    public static final String CURRENT_PROFILE = "CURRENT_PROFILE";
    public static final String TRIGGER_TIME = "TIME";
    public static final String TRIGGER_NFC = "NFC";
    public static final String SCAN_INTERVAL = "SCAN_INTERVAL";
    public static final String NEXT_SCAN = "NEXT_SCAN";
    public static final String TRIGGER_BATTERY_CHARGING = "TRIGGER_BATTERY_CHARGING";
    /*

     public static final String;

     public static final String;
     public static final String;
     public static final String;
     public static final String;
     public static final String;
     public static final String;
     public static final String;
     public static final String;
     public static final String; */

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
        final String[] need = new String[]{ProfileDBHelper.ID, ProfileDBHelper.PROFILE_NAME, ProfileDBHelper.TRIGGER_DEVICE_TYPE, ProfileDBHelper.BSSID};
        final Cursor cursor = profiledb.query(ProfileDBHelper.PROFILE_TABLE, need, null, null, null, null, null);
        try {
            cursor.moveToPosition(0);
        } catch (Exception e) {
        }
        while (!cursor.isAfterLast()) {
            try {
                profiles.add(
                        new ProfileListItem(
                                cursor.getString(cursor.getColumnIndex(ProfileDBHelper.ID)),
                                cursor.getString(cursor.getColumnIndex(ProfileDBHelper.PROFILE_NAME)),
                                cursor.getString(cursor.getColumnIndex(ProfileDBHelper.TRIGGER_DEVICE_TYPE)),
                                cursor.getString(cursor.getColumnIndex(ProfileDBHelper.BSSID))
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
