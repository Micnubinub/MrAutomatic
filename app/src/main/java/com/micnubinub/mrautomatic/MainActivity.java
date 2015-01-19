package com.micnubinub.mrautomatic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import adapters.ProfileManagerAdapter;
import tools.Utility;

/**
 * Created by root on 21/08/14.
 */
public class MainActivity extends Activity {

    private final AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            try {
                Intent intent = new Intent(MainActivity.this, EditProfile.class);
                intent.putExtra(Utility.EDIT_PROFILE, profiles.get(position).getID());
                MainActivity.this.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    ProfileManagerAdapter adapter;
    private ArrayList<Profile> profiles;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_manager_activity);
        ListView listView = (ListView) findViewById(R.id.profile_list);
        profiles = Utility.getProfiles(this);
        adapter = new ProfileManagerAdapter(this, profiles);
        Log.e("profiles", profiles.toString());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(listener);

        findViewById(R.id.new_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(MainActivity.this, EditProfile.class);
                MainActivity.this.startActivity(intent);
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
