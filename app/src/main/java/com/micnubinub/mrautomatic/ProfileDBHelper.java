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
    public static final String TRIGGERS = "TRIGGERS";
    public static final String COMMANDS = "COMMANDS";
    public static final String PRIORITY = "PRIORITY";


    public ProfileDBHelper(Context context) {
        super(context, DB_NAME, null, DB_V);
    }

    @Override
    public void onCreate(SQLiteDatabase profiledb) {
        //Todo prohibit
        String sqlStatement = "create table " + PROFILE_TABLE
                + " (" + ID + " integer primary key autoincrement not null,"
                + PROFILE_NAME + " string not null,"
                + COMMANDS + " string not null,"
                + PRIORITY + " string,"
                + TRIGGERS + " string"
                + ");";
        profiledb.execSQL(sqlStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {


    }
}
