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
    //3 sections triggers, restrictions

    //Triggers work at an OR basis, if any one of them is triggered, it will check through the restriction, if those
    //are met, the profile will be set
    //* there must be a minimum of one trigger
    //Todo ad ID (from db) to Profile.class and make add that to Trigger.class too, so when checking the triggers you can track the profile easily

    //Restrictions work on an and basis, they must ALL be satisfied, or else the profile won't be set

    //Prohibited must NOT be triggered, or else the profile won't be set

    //Todo refine these definitions, and come up with better words


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
        setContentView(R.layout.profile_manager_activity);
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
