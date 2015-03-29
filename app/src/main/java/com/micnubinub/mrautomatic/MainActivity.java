package com.micnubinub.mrautomatic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

import adapters.ProfileManagerAdapter;
import tools.Utility;

/**
 * Created by root on 21/08/14.
 */
public class MainActivity extends Activity {
    ProfileManagerAdapter adapter;
    private ArrayList<Profile> profiles;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_manager_activity);
        ListView listView = (ListView) findViewById(R.id.profile_list);
        profiles = Utility.getProfiles(this);
        adapter = new ProfileManagerAdapter(this, profiles);
        listView.setAdapter(adapter);

        findViewById(R.id.new_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(MainActivity.this, EditProfile.class);
                MainActivity.this.startActivity(intent);
            }
        });

        findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Preferences.class));
            }
        });

        final Intent intent = new Intent(this, ProfileService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        profiles = Utility.getProfiles(this);
        adapter.setProfiles(profiles);
        adapter.notifyDataSetChanged();
    }

}
