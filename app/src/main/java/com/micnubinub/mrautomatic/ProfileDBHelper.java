package com.micnubinub.mrautomatic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mike on 26/08/13.
 */
public class ProfileDBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "Profile.db";
    public static final int DB_V = 1;
    public static final String PROFILE_TABLE = "Profiletable";
    public static final String PROFILE_NAME = "Profilename";
    public static final String ID = "_id";
    public static final String BSSID = "Bssid";
    public static final String DATA_VALUE = "Data";
    public static final String ALARM = "Alarm";
    public static final String SYNC = "Sync";
    public static final String AIRPLANE_MODE = "Airplane";
    public static final String BLUETOOTH = "Bluetooth";
    public static final String GPS = "Gps";
    public static final String HAPTIC_FEEDBACK = "Haptic";
    public static final String SOUND_MEDIA = "Media";
    public static final String SOUND_PHONE_CALL = "Ringer";
    public static final String SOUND_NOTIFICATION = "Notification";
    public static final String WIFI = "Wifi";
    public static final String TRIGGER_DEVICE_TYPE = "Device_type";
    public static final String SCREEN_TIMEOUT = "Timeout";
    public static final String PRIORITY = "Priority";
    public static final String BRIGHTNESS = "Brightness";
    public static final String BRIGHTNESS_MODE = "Brightness_mode";


    public ProfileDBHelper(Context context) {
        super(context, DB_NAME, null, DB_V);
    }

    @Override
    public void onCreate(SQLiteDatabase profiledb) {
        String sqlStatement = "create table " + PROFILE_TABLE
                + " (" + ID + " integer primary key autoincrement not null,"
                + PROFILE_NAME + " string not null,"
                + SCREEN_TIMEOUT + " string not null,"
                + SOUND_MEDIA + " string,"
                + SYNC + " string not null,"
                + BSSID + " string not null,"
                + SOUND_NOTIFICATION + " string,"
                + SOUND_PHONE_CALL + " string,"
                + WIFI + " string,"
                + TRIGGER_DEVICE_TYPE + " string not null,"
                + DATA_VALUE + " string,"
                + PRIORITY + " string,"
                + ALARM + " string,"
                + AIRPLANE_MODE + " string,"
                + BLUETOOTH + " string,"
                + BRIGHTNESS + " string,"
                + BRIGHTNESS_MODE + " string,"
                + GPS + " string,"
                + HAPTIC_FEEDBACK + " string"
                + ");";
        profiledb.execSQL(sqlStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {


    }
}
