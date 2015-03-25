package com.micnubinub.mrautomatic;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import tools.Utility;

/**
 * Created by Michael on 3/25/2015.
 */
public class Preferences extends Activity {
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

    }

    private void save() {
        editor = prefs.edit();
        editor.putString(Utility.PREF_PLAY_PREVIEW, );
        editor.putString(Utility.PREF_TOAST_WHEN_PROFILE_SET, );
        editor.putString(Utility.PREF_SCAN_INTERVAL, );
        editor.putString(Utility.PREF_DEFAULT_PROFILE, );

        editor.commit();
    }
}
