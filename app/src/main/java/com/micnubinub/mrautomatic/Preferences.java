package com.micnubinub.mrautomatic;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import tools.Utility;
import view_classes.MaterialSwitch;

/**
 * Created by Michael on 3/25/2015.
 */
public class Preferences extends Activity {
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor editor;
    private static MaterialSwitch toast, override, preview, only_receivers;
    private static View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.scan_intervals:
                    break;
                case R.id.default_profile:
                    break;
            }
        }
    };

    private static int scanIntervals;
    private static String profileID;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        findViewById(R.id.scan_intervals).setOnClickListener(listener);
        findViewById(R.id.default_profile).setOnClickListener(listener);

        toast = (MaterialSwitch) findViewById(R.id.toast_when_profile_set);
        override = (MaterialSwitch) findViewById(R.id.override_timer_duration);
        preview = (MaterialSwitch) findViewById(R.id.play_preview);
        only_receivers = (MaterialSwitch) findViewById(R.id.only_receivers);
        //Todo setChecked,inteveals =...
    }

    private void save() {
        editor = prefs.edit();
        editor.putBoolean(Utility.PREF_PLAY_PREVIEW, preview.isChecked());
        editor.putBoolean(Utility.PREF_TOAST_WHEN_PROFILE_SET, toast.isChecked());
        editor.putInt(Utility.PREF_SCAN_INTERVAL, scanIntervals);
        editor.putString(Utility.PREF_DEFAULT_PROFILE, profileID);
        editor.putBoolean(Utility.PREF_ONLY_USE_RECEIVERS, only_receivers.isChecked());
        editor.putBoolean(Utility.PREF_OVERRIDE_TIME_TRIGGER_DURATION, override.isChecked());

        editor.commit();
    }

    @Override
    protected void onDestroy() {
        save();
    }
}
