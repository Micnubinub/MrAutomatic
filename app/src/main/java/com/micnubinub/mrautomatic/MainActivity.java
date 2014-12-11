package com.micnubinub.mrautomatic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

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
//                intent.putExtra(Utility.EDIT_PROFILE, profiles.get(position).getID());
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
        setContentView(R.layout.material_profile_manager_activity);
        ListView listView = (ListView) findViewById(R.id.profile_list);
        profiles = Utility.getListProfiles(this);
        adapter = new ProfileManagerAdapter(this, profiles);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(listener);

        findViewById(R.id.new_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditProfile.class);
                MainActivity.this.startActivity(intent);
            }
        });

        Intent intent = new Intent(this, ProfileService.class);
        startService(intent);

        Toast.makeText(this, Utility.getDay(System.currentTimeMillis()), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        profiles = Utility.getListProfiles(this);
        adapter.setProfiles(profiles);
        adapter.notifyDataSetChanged();
    }

}
