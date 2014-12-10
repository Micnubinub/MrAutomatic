package com.micnubinub.mrautomatic;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import adapters.BluetoothListAdapter;
import adapters.WifiListAdapter;
import tools.Utility;
import tools.WirelessDevice;
import view_classes.MaterialSeekBar;
import view_classes.MaterialSwitch;

/**
 * Created by root on 21/08/14.
 */
public class EditProfile extends Activity {
    private static final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
// Get the BluetoothDevice object from the Intent
                devices.add((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
            }
        }
    };
    private static final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private static ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
    private static Dialog dialog;

    private final ProfileDBHelper profileDBHelper = new ProfileDBHelper(this);
    private final View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.save:
                    save();
                    break;

                case R.id.cancel:
                    EditProfile.this.finish();
                    break;
                case R.id.trigger:
                    trigger().show();
                    break;
                case R.id.airplane_mode_click_view:
                    airplane_mode_click_view().show();
                    break;
                case R.id.bluetooth_click_view:
                    bluetooth_click_view().show();
                    break;

                case R.id.data_mode_click_view:
                    data_mode_click_view().show();
                    break;
                case R.id.gps_click_view:
                    gps_click_view().show();
                    break;
                case R.id.haptic_click_view:
                    haptic_click_view().show();
                    break;
                case R.id.screen_settings_click_view:
                    screen_settings_click_view().show();
                    break;
                case R.id.sound_click_view:
                    sound_click_view().show();
                    break;
                case R.id.silent_mode_click_view:
                    silent_mode_click_view().show();
                    break;
                case R.id.wifi_click_view:
                    wifi_click_view().show();
                    break;
                case R.id.sync_click_view:
                    sync_click_view().show();
                    break;


            }
        }
    };
    private final View.OnClickListener triggerDialogClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
            }

            switch (v.getId()) {
                case R.id.wifi_click_view:
                    wifiDevicePicker().show();
                    break;
                case R.id.bluetooth_click_view:
                    bluetoothDevicePicker().show();

                    break;
                case R.id.time_click_view:
                    timePicker().show();

                    break;
                case R.id.gps_click_view:
                    locationPicker().show();

                    break;
                case R.id.nfc_click_view:
                    nfcDevicePicker().show();

                    break;
                case R.id.battery_click_view:
                    batteryPiker().show();

                    break;
            }
        }
    };
    private final View.OnClickListener save_cancel = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.cancel:
                    bssid = "";
                    trigger_device_picked = false;
                    dialog.dismiss();
                    break;
                case R.id.save:
                    dialog.dismiss();
                    break;
            }
        }
    };
    int seekbar_battery_level, brightness_auto, brightness_auto_old_value, is_data_expanded, is_advanced_settings_expanded, battery_level, data_value, profile_image;
    int number_of_network_devices, wifi_value, bluetooth_value, autobrightness_value, haptic_feedback_value, gps_value, sync_value, airplane_mode_value;
    int brightness_value, ringer_phonecall_volume, ringer_old_value, alarm_old_value, alarm_volume, sleep_timeout;
    int wifi_old_value, bluetooth_old_value, brightness_old_value, media_volume, old_media_volume_value, notification_volume, old_notification_value, old_incoming_call_volume;
    private TextView wifi_description, bluetooth_description, gps_description, sound_description,
            sync_description, haptic_feedback_description, screen_timeout_description, silent_mode_description,
            data_description, airplane_mode_description, trigger_text_view;
    private EditText profile_name;
    private String ssid, bssid, trigger_type, update_profile, profile_name_text;
    private boolean update = false, trigger_device_picked = false;
    private Resources res;
    private WifiManager wifiManager;
    private ContentResolver contentResolver;
    private ContentValues content_values;
    private AudioManager audioManager;
    private List<ScanResult> wifi_scan_results;
    private Uri notification;
    private Cursor cursor;
    private SQLiteDatabase profiledb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.material_profile_manager_editor);


        init();
        getOldValues();

    }

    private void editProfile(String id) {
        update = true;
        getProfileValues(id);
        setViewValues();
    }

    private void setViewValues() {
        //Todo lots of work to be done here


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setOldValues();
        close();
    }

    @Override
    protected void onStop() {
        super.onStop();
        setOldValues();
        close();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void save() {

        setOldValues();

        profiledb = profileDBHelper.getWritableDatabase();
        if (!trigger_device_picked || bssid == "") {
            trigger().show();
            Toast.makeText(this, "You need a trigger device", Toast.LENGTH_LONG).show();
        } else {

            String wifi_value_string = String.valueOf(wifi_value);
            String bluetooth_value_string = String.valueOf(bluetooth_value);
            String gps_value_string = String.valueOf(gps_value);
            String auto_brightness_value_string = String.valueOf(autobrightness_value);
            String brightness_value_string = String.valueOf(brightness_value);
            String data_value_string = String.valueOf(data_value);
            String media_volume_string = String.valueOf(media_volume);
            String notification_value_string = String.valueOf(notification_volume);
            String ringer_phone_call_volume_string = String.valueOf(ringer_phonecall_volume);
            String sleep_timeout_string = String.valueOf(sleep_timeout);
            String haptic_feedback_value_string = String.valueOf(haptic_feedback_value);
            String airplane_mode_value_string = String.valueOf(airplane_mode_value);
            String sync_value_string = String.valueOf(sync_value);
            String profile_name_string = profile_name.getText().toString();

            if (profile_name_string.length() < 1 || profile_name_string == null)
                profile_name_string = "Untitled";

            content_values = new ContentValues();

            content_values.put(ProfileDBHelper.PROFILE_NAME, profile_name_string);
            content_values.put(ProfileDBHelper.PROFILE_NAME, profile_name_string);
            //Todo

            if (update) {
                try {
                    Log.e("write update", "passed");
                    profiledb.update(ProfileDBHelper.PROFILE_TABLE, content_values, ProfileDBHelper.ID + "=" + update_profile, null);
                } catch (Exception e) {
                    Log.e("write update", "failed");
                    e.printStackTrace();
                }
            } else {
                try {
                    profiledb.insert(ProfileDBHelper.PROFILE_TABLE, null, content_values);
                    Log.e("write insert", "passed");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("write insert", "failed");
                }
            }
            close();
            this.finish();
        }
    }

    public void getOldValues() {

        if (adapter.isEnabled())
            bluetooth_old_value = 1;
        else
            bluetooth_old_value = 0;

        if (wifiManager.isWifiEnabled())
            wifi_old_value = 1;
        else
            wifi_old_value = 0;


        try {
            brightness_auto_old_value = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Settings.SettingNotFoundException e) {
        }


        try {
            alarm_old_value = Settings.System.getInt(getContentResolver(), Settings.System.VOLUME_ALARM);
        } catch (Settings.SettingNotFoundException e) {
        }


        try {
            brightness_old_value = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
        }


        try {
            old_notification_value = Settings.System.getInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION);
        } catch (Settings.SettingNotFoundException e) {
        }

        try {
            old_incoming_call_volume = Settings.System.getInt(getContentResolver(), Settings.System.VOLUME_RING);
        } catch (Settings.SettingNotFoundException e) {
        }

        try {
            old_media_volume_value = Settings.System.getInt(getContentResolver(), Settings.System.VOLUME_MUSIC);

        } catch (Settings.SettingNotFoundException e) {
        }
    }

    public void cancel(View view) {
        finish();
    }

    public void setOldValues() {


        Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_MUSIC, old_media_volume_value);
        Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, old_notification_value);
        Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, old_incoming_call_volume);

        if (brightness_auto_old_value != 1) {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness_old_value);
        } else {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        }

        if (bluetooth_old_value == 1)
            if (!adapter.isEnabled())
                adapter.enable();
            else if (adapter.isEnabled())
                adapter.disable();

        if (wifi_old_value == 0)
            wifiManager.setWifiEnabled(false);
        else
            wifiManager.setWifiEnabled(true);


    }


    private void init() {
        //Todo view = findViewbiId(R.id.dddd)/....
        res = getResources();
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        trigger_text_view = (TextView) findViewById(R.id.trigger);
        trigger_text_view.setOnClickListener(listener);

        profile_name = (EditText) findViewById(R.id.profile_name);

        findViewById(R.id.airplane_mode_click_view).setOnClickListener(listener);
        findViewById(R.id.bluetooth_click_view).setOnClickListener(listener);
        findViewById(R.id.save).setOnClickListener(listener);
        findViewById(R.id.cancel).setOnClickListener(listener);
        findViewById(R.id.data_mode_click_view).setOnClickListener(listener);
        findViewById(R.id.gps_click_view).setOnClickListener(listener);
        findViewById(R.id.haptic_click_view).setOnClickListener(listener);
        findViewById(R.id.screen_settings_click_view).setOnClickListener(listener);
        findViewById(R.id.sound_click_view).setOnClickListener(listener);
        findViewById(R.id.silent_mode_click_view).setOnClickListener(listener);
        findViewById(R.id.wifi_click_view).setOnClickListener(listener);
        findViewById(R.id.sync_click_view).setOnClickListener(listener);
    }

    public void getProfileValues(String ID) {

        profiledb = profileDBHelper.getReadableDatabase();
        cursor = profiledb.query(ProfileDBHelper.PROFILE_TABLE, need, null, null, null, null, null);
        cursor.moveToPosition(Integer.parseInt(ID));

        update_profile = cursor.getString(cursor.getColumnIndex(ProfileDBHelper.ID));
        bssid = cursor.getString(cursor.getColumnIndex(ProfileDBHelper.BSSID));
        profile_name_text = cursor.getString(cursor.getColumnIndex(ProfileDBHelper.PROFILE_NAME));
        wifi_value = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.WIFI)));
        bluetooth_value = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.BLUETOOTH)));
        gps_value = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.GPS)));
        autobrightness_value = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.BRIGHTNESS_MODE)));
        brightness_value = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.BRIGHTNESS)));
        data_value = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.DATA_VALUE)));
        media_volume = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.SOUND_MEDIA)));
        notification_volume = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.SOUND_NOTIFICATION)));
        ringer_phonecall_volume = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.SOUND_PHONE_CALL)));
        sleep_timeout = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.SCREEN_TIMEOUT)));
        airplane_mode_value = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.AIRPLANE_MODE)));
        alarm_volume = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.ALARM)));
        trigger_type = cursor.getString(cursor.getColumnIndex(ProfileDBHelper.TRIGGER_DEVICE_TYPE));

        close();

    }

    private void close() {
        try {
            profiledb.close();
            cursor.close();
        } catch (Exception e) {
        }
    }

    private Dialog airplane_mode_click_view() {
        dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.material_profile_manager_airplane);

        final MaterialSwitch materialSwitch = (MaterialSwitch) dialog.findViewById(R.id.airplane_mode_switch);
        materialSwitch.setChecked(airplane_mode_value == 1 ? true : false);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                airplane_mode_value = materialSwitch.isChecked() ? 1 : 0;
            }
        });
        return dialog;
    }

    private Dialog bluetooth_click_view() {
        dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.material_profile_manager_bluetooth);


        final MaterialSwitch materialSwitch = (MaterialSwitch) dialog.findViewById(R.id.bluetooth_switch);
        materialSwitch.setChecked(bluetooth_value == 1 ? true : false);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                bluetooth_value = materialSwitch.isChecked() ? 1 : 0;
            }
        });
        return dialog;
    }

    private Dialog data_mode_click_view() {
        dialog = new Dialog(this, R.style.CustomDialog);


        return dialog;
    }

    private Dialog gps_click_view() {
        dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.material_profile_manager_gps);

        final MaterialSwitch materialSwitch = (MaterialSwitch) dialog.findViewById(R.id.gps_switch);
        materialSwitch.setChecked(gps_value == 1 ? true : false);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                gps_value = materialSwitch.isChecked() ? 1 : 0;
            }
        });
        return dialog;
    }

    private Dialog haptic_click_view() {
        dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.material_profile_manager_haptic_feedback);

        final MaterialSwitch materialSwitch = (MaterialSwitch) dialog.findViewById(R.id.haptic_switch);
        materialSwitch.setChecked(haptic_feedback_value == 1 ? true : false);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                haptic_feedback_value = materialSwitch.isChecked() ? 1 : 0;
            }
        });
        return dialog;
    }

    private Dialog screen_settings_click_view() {
        dialog = new Dialog(this, R.style.CustomDialog);

        dialog.setContentView(R.layout.material_profile_manager_screen_settings);
        final View brightnessBarLayout = dialog.findViewById(R.id.brightness_seekbar_layout);

        final MaterialSwitch materialSwitch = (MaterialSwitch) dialog.findViewById(R.id.auto_brightness_switch);
        materialSwitch.setChecked(autobrightness_value == 1 ? true : false);
        materialSwitch.setOnCheckedChangeListener(new MaterialSwitch.OnCheckedChangedListener() {
            @Override
            public void onCheckedChange(boolean isChecked) {
                if (!isChecked) {
                    brightnessBarLayout.setVisibility(View.VISIBLE);
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

                } else {
                    brightnessBarLayout.setVisibility(View.GONE);
                    try {
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                    } catch (Exception e) {
                    }
                }

                brightness_auto = isChecked ? 1 : 0;
            }
        });

        final MaterialSeekBar brightness = (MaterialSeekBar) dialog.findViewById(R.id.brightness_seekbar_level);
        brightness.setMax(255);
        try {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            brightness.setProgress(Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS));
        } catch (Settings.SettingNotFoundException e) {
        }


        brightness.setOnProgressChangedListener(new MaterialSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                brightness_value = progress;
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, progress);
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (brightness_auto_old_value != 1) {
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness_old_value);
                } else {
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                }
            }
        });

        return dialog;
    }

    private Dialog sound_click_view() {
        dialog = new Dialog(this, R.style.CustomDialog);

        dialog.setContentView(R.layout.material_profile_manager_sound_volumes);
        return dialog;
    }

    private Dialog silent_mode_click_view() {
        dialog = new Dialog(this, R.style.CustomDialog);

        dialog.setContentView(R.layout.material_profile_manager_silentmode);

        /*final MaterialSwitch materialSwitch = (MaterialSwitch) dialog.findViewById(R.id.wifi_switch);
        materialSwitch.setChecked(wifi_value == 1 ? true : false);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                wifi_value = materialSwitch.isChecked() ? 1 : 0;
            }
        });*/
        return dialog;
    }

    private Dialog wifi_click_view() {
        dialog = new Dialog(this, R.style.CustomDialog);

        dialog.setContentView(R.layout.material_profile_manager_wifi);

        final MaterialSwitch materialSwitch = (MaterialSwitch) dialog.findViewById(R.id.wifi_switch);
        materialSwitch.setChecked(wifi_value == 1 ? true : false);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                wifi_value = materialSwitch.isChecked() ? 1 : 0;
            }
        });
        return dialog;
    }

    private Dialog sync_click_view() {
        dialog = new Dialog(this, R.style.CustomDialog);

        dialog.setContentView(R.layout.material_profile_manager_sync);

        final MaterialSwitch materialSwitch = (MaterialSwitch) dialog.findViewById(R.id.sync_switch);
        materialSwitch.setChecked(sync_value == 1 ? true : false);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                sync_value = materialSwitch.isChecked() ? 1 : 0;
            }
        });
        return dialog;
    }

    private Dialog trigger() {
        dialog = new Dialog(this, R.style.CustomDialog);

        dialog.setContentView(R.layout.material_profile_manager_trigger_chooser);
        dialog.findViewById(R.id.wifi_click_view).setOnClickListener(triggerDialogClick);
        dialog.findViewById(R.id.bluetooth_click_view).setOnClickListener(triggerDialogClick);
        dialog.findViewById(R.id.time_click_view).setOnClickListener(triggerDialogClick);
        dialog.findViewById(R.id.gps_click_view).setOnClickListener(triggerDialogClick);
        dialog.findViewById(R.id.nfc_click_view).setOnClickListener(triggerDialogClick);
        dialog.findViewById(R.id.battery_click_view).setOnClickListener(triggerDialogClick);
        return dialog;
    }

    private Dialog wifiDevicePicker() {
        dialog = new Dialog(this, R.style.CustomDialog);

        dialog.setContentView(R.layout.material_profile_manager_wireless_scanner_bluetooth_wifi);

        ListView listView = (ListView) dialog.findViewById(R.id.list);
        final WifiListAdapter wifiListAdapter = new WifiListAdapter(this);
        listView.setAdapter(wifiListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                trigger_device_picked = true;
                trigger_type = Utility.TRIGGER_WIFI;
                bssid = ((WirelessDevice) wifiListAdapter.getItem(position)).getBssid();
                wifiListAdapter.setSelectedItem(position);
            }
        });

        dialog.findViewById(R.id.save_cancel).findViewById(R.id.cancel).setOnClickListener(save_cancel);
        dialog.findViewById(R.id.save_cancel).findViewById(R.id.save).setOnClickListener(save_cancel);
        return dialog;
    }

    private Dialog bluetoothDevicePicker() {
        dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.material_profile_manager_wireless_scanner_bluetooth_wifi);
        ListView listView = (ListView) dialog.findViewById(R.id.list);
        final BluetoothListAdapter bluetoothListAdapter = new BluetoothListAdapter(this);
        listView.setAdapter(bluetoothListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                trigger_device_picked = true;
                trigger_type = Utility.TRIGGER_BLUETOOTH;
                bssid = ((WirelessDevice) bluetoothListAdapter.getItem(position)).getBssid();
                bluetoothListAdapter.setSelectedItem(position);
            }
        });

        dialog.findViewById(R.id.save_cancel).findViewById(R.id.cancel).setOnClickListener(save_cancel);
        dialog.findViewById(R.id.save_cancel).findViewById(R.id.save).setOnClickListener(save_cancel);
        return dialog;
    }

    private Dialog timePicker() {
        dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.material_profile_manager_silentmode);

        return dialog;
    }

    private Dialog locationPicker() {
        dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.material_profile_manager_silentmode);

        return dialog;
    }

    private Dialog nfcDevicePicker() {
        dialog = new Dialog(this, R.style.CustomDialog);

        dialog.setContentView(R.layout.material_profile_manager_silentmode);
        return dialog;
    }

    private Dialog batteryPiker() {
        dialog = new Dialog(this, R.style.CustomDialog);

        dialog.setContentView(R.layout.material_profile_manager_battery);
        final View batteryBarLayout = dialog.findViewById(R.id.battery_seekbar_layout);
        final MaterialSeekBar battery_seekbar = (MaterialSeekBar) dialog.findViewById(R.id.battery_seekbar_level);
        final MaterialSwitch materialSwitch = (MaterialSwitch) dialog.findViewById(R.id.battery_charging_switch);
        final TextView percentage = (TextView) dialog.findViewById(R.id.battery_percentage);
        //Todo charging is -1
        battery_seekbar.setMax(100);
        battery_seekbar.setProgress(50);
        bssid = "50";
        percentage.setText("Battery level: " + bssid + "%");

        materialSwitch.setChecked(battery_level == -1 ? true : false);
        materialSwitch.setOnCheckedChangeListener(new MaterialSwitch.OnCheckedChangedListener() {
            @Override
            public void onCheckedChange(boolean isChecked) {
                if (!isChecked)
                    batteryBarLayout.setVisibility(View.VISIBLE);
                else
                    batteryBarLayout.setVisibility(View.GONE);
            }
        });

        battery_seekbar.setOnProgressChangedListener(new MaterialSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                bssid = String.valueOf(progress);
                percentage.setText("Battery level: " + bssid + "%");
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                trigger_type = Utility.TRIGGER_BATTERY;
                trigger_device_picked = true;
                if (materialSwitch.isChecked())
                    bssid = String.valueOf(-1);
            }
        });

        return dialog;
    }


}
