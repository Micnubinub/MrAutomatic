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
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tools.Command;
import tools.Trigger;
import tools.Utility;
import view_classes.MaterialCheckBox;
import view_classes.MaterialRadioButton;
import view_classes.MaterialRadioGroup;
import view_classes.MaterialSeekBar;
import view_classes.MaterialSwitch;

/**
 * Created by root on 21/08/14.
 */
public class EditProfile extends Activity {

    //Todo copy from>> toast with 4 ticks : triggers, ristrictions, prohibitions and commands
    //Todo ad info button at the far right of a scrollview MenuItem
    //Todo might wnd up removing the cards and makin the view flat
    //Todo preference to play preview/display preview when a value is set, e.g. brightness, volume

    private static final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
// Get the BluetoothDevice object from the Intent
                //devices.add((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
            }
        }
    };
    private static final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private static final ArrayList<String> availableCommands = new ArrayList<String>();
    private static final ArrayList<String> availableTriggers = new ArrayList<String>();
    private static final ArrayList<String> availableProhibitions = new ArrayList<String>();
    private static final ArrayList<String> availableRestrictions = new ArrayList<String>();
    private static final ArrayList<Command> addedCommands = new ArrayList<Command>();
    private static final ArrayList<Trigger> restrictionTriggers = new ArrayList<Trigger>();
    private static final ArrayList<Trigger> prohibitionTriggers = new ArrayList<Trigger>();
    private static final ArrayList<Trigger> normalTriggers = new ArrayList<Trigger>();
    private static LinearLayout prohibitionList, triggerList, restrictionList, commandList;
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
                    finish();
                    break;
            }
        }
    };
    private final View.OnClickListener tagClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String view = v.getTag().toString();
            //Todo split everything up >> if tag.beginswith: info, add, remove...

            if (view.equals("add_prohibitions")) {
                showProhibitionsDialog();
            } else if (view.equals("add_commands")) {
                showCommandsDialog();
            } else if (view.equals("add_restriction")) {
                showRestrictionsDialog();
            } else if (view.equals("add_triggers")) {
                showTriggersDialog();
            } else if (view.startsWith("info")) {
                showInfo(view);
            } else {
                checkLongString(view);
            }
        }
    };
    private final View.OnClickListener save_cancel = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.cancel:
                    //Todo check current dialog
                    dialog.dismiss();
                    break;
                case R.id.save:
                    //Todo dialog.Tag, dialog.getTag...
                    dialog.dismiss();
                    break;
            }
        }
    };
    int brightness_auto_old_value, ringer_old_value, alarm_old_value;
    int wifi_old_value, bluetooth_old_value, brightness_old_value, media_volume, old_media_volume_value, notification_volume, old_notification_value, old_incoming_call_volume;
    int profileId;
    private String currentDialog;
    private boolean edit = false;
    private EditText profile_name;
    private String ssid, bssid, trigger_type, update_profile, profile_name_text;
    private boolean trigger_device_picked = false;
    private Resources res;
    private WifiManager wifiManager;
    private ContentResolver contentResolver;
    private ContentValues content_values;
    private AudioManager audioManager;
    private List<ScanResult> wifi_scan_results;
    private Uri notification;
    private Cursor cursor;
    private SQLiteDatabase profiledb;

    private void checkLongString(String string) {
        try {
            final String[] split = string.split("_", 3);
            Toast.makeText(this, Arrays.toString(split), Toast.LENGTH_LONG).show();
            final String action = split[0];
            final String type = split[1];
            final String actor = split[2];

            if (action.toLowerCase().equals("add")) {
                if (type.contains("restri")) {
                    addRestrictionList(Utility.getTriggerName(actor), actor);
                } else if (type.contains("prohib")) {
                    addProhibitionList(Utility.getTriggerName(actor), actor);
                } else if (type.contains("trig")) {
                    addTriggerList(Utility.getTriggerName(actor), actor);
                } else {
                    addCommandList(Utility.getCommandName(actor), actor);
                }
                showEditorDialog(actor);

            } else {
                if (type.contains("restri")) {
                    removeRestriction(actor);
                } else if (type.contains("prohib")) {
                    removeProhibition(actor);
                } else if (type.contains("trig")) {
                    removeTrigger(actor);
                } else {
                    removeCommand(actor);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void showAlarmVolumeDialog() {
        //Todo make dialog
        final View view = View.inflate(EditProfile.this, R.layout., null);

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trigger trigger = new Trigger("Type", "value");
                normalTriggers.add(trigger);
                dialog.dismiss();
            }
        });


        showDialog(view);
    }

    private void showMediaVolumeDialog() {
        //Todo make dialog
        final View view = View.inflate(EditProfile.this, R.layout., null);
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trigger trigger = new Trigger("Type", "value");
                normalTriggers.add(trigger);
                dialog.dismiss();
            }
        });


        showDialog(view);
    }

    private void showNotificationVolumeDialog() {
        //Todo make dialog
        final View view = View.inflate(EditProfile.this, R.layout., null);
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trigger trigger = new Trigger("Type", "value");
                normalTriggers.add(trigger);
                dialog.dismiss();
            }
        });

        showDialog(view);
    }

    private void showRingtoneVolumeDialog() {
        //Todo make dialog
        final View view = View.inflate(EditProfile.this, R.layout., null);
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trigger trigger = new Trigger("Type", "value");
                normalTriggers.add(trigger);
                dialog.dismiss();
            }
        });


        showDialog(view);
    }

    private void showBrightnessDialog() {
        //Todo default, and adding
        final View view = View.inflate(EditProfile.this, R.layout.seekbar, null);

        ((TextView) view.findViewById(R.id.title)).setText("Brightness");
        final MaterialSeekBar materialSeekBar = (MaterialSeekBar) view.findViewById(R.id.material_seekbar);
        materialSeekBar.setMax(255);
        materialSeekBar.setProgress(50);

        final MaterialCheckBox materialCheckBox = (MaterialCheckBox) view.findViewById(R.id.material_checkbox);
        materialCheckBox.setVisibility(View.VISIBLE);
        materialCheckBox.setText("Auto-Brightness");
        materialCheckBox.setOnCheckedChangeListener(new MaterialCheckBox.OnCheckedChangedListener() {
            @Override
            public void onCheckedChange(MaterialCheckBox materialCheckBox, boolean isChecked) {
                materialSeekBar.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                //Todo remove the manual brightness if added, then add this
            }
        });

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trigger trigger = new Trigger(Utility.BRIGHTNESS_SETTING, String.valueOf(materialSeekBar.getProgress()));
                normalTriggers.add(trigger);
                dialog.dismiss();
            }
        });

        showDialog(view);
    }

    private void showBatteryDialog() {
        //Todo make dialog  battery remp, charge, percent

        //Todo default, and adding
        final View view = View.inflate(EditProfile.this, R.layout.seekbar, null);

        ((TextView) view.findViewById(R.id.title)).setText("Battery");
        final MaterialSeekBar materialSeekBar = (MaterialSeekBar) view.findViewById(R.id.material_seekbar);
        materialSeekBar.setMax(100);
        materialSeekBar.setProgress(50);

        final MaterialCheckBox materialCheckBox = (MaterialCheckBox) view.findViewById(R.id.material_checkbox);
        materialCheckBox.setVisibility(View.VISIBLE);
        materialCheckBox.setText("Charging");
        materialCheckBox.setOnCheckedChangeListener(new MaterialCheckBox.OnCheckedChangedListener() {
            @Override
            public void onCheckedChange(MaterialCheckBox materialCheckBox, boolean isChecked) {
                materialSeekBar.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                //Todo remove the battery percentage if added, then add this
            }
        });

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Trigger trigger = new Trigger(Utility.TRIGGER_BATTERY_PERCENTAGE, String.valueOf(materialSeekBar.getProgress()));
                normalTriggers.add(trigger);
                dialog.dismiss();
            }
        });

        showDialog(view);
    }

    private void showDataDialog() {
        //Todo make dialog
        final View view = View.inflate(EditProfile.this, R.layout.radio_group, null);

        ((TextView) view.findViewById(R.id.title)).setText("Data");
        final MaterialRadioGroup materialRadioGroup = (MaterialRadioGroup) view.findViewById(R.id.material_radio_group);

        final MaterialRadioButton button1 = new MaterialRadioButton(this);
        button1.setText("Off");
        final MaterialRadioButton button2 = new MaterialRadioButton(this);
        button2.setText("2G");
        final MaterialRadioButton button3 = new MaterialRadioButton(this);
        button3.setText("3G");
        final MaterialRadioButton button4 = new MaterialRadioButton(this);
        button4.setText("4G");

        materialRadioGroup.addView(button1);
        materialRadioGroup.addView(button2);
        materialRadioGroup.addView(button3);
        materialRadioGroup.addView(button4);

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trigger trigger = new Trigger(Utility.DATA_SETTING, String.valueOf(materialRadioGroup.getSelection()));
                normalTriggers.add(trigger);
                dialog.dismiss();
            }
        });

        showDialog(view);
    }

    private void showSleepTimeoutDialog() {
        //Todo make dialog
        final View view = View.inflate(EditProfile.this, R.layout.radio_group, null);

        ((TextView) view.findViewById(R.id.title)).setText("Data");
        final MaterialRadioGroup materialRadioGroup = (MaterialRadioGroup) view.findViewById(R.id.material_radio_group);

        final MaterialRadioButton button1 = new MaterialRadioButton(this);
        button1.setText("10 secs");
        final MaterialRadioButton button2 = new MaterialRadioButton(this);
        button2.setText("30 secs");
        final MaterialRadioButton button3 = new MaterialRadioButton(this);
        button3.setText("1 min");
        final MaterialRadioButton button4 = new MaterialRadioButton(this);
        button4.setText("2 min");
        final MaterialRadioButton button5 = new MaterialRadioButton(this);
        button1.setText("5 min");
        final MaterialRadioButton button6 = new MaterialRadioButton(this);
        button2.setText("10 min");
        final MaterialRadioButton button7 = new MaterialRadioButton(this);
        button3.setText("15 min");
        final MaterialRadioButton button8 = new MaterialRadioButton(this);
        button4.setText("30 min");

        materialRadioGroup.addView(button1);
        materialRadioGroup.addView(button2);
        materialRadioGroup.addView(button3);
        materialRadioGroup.addView(button5);
        materialRadioGroup.addView(button6);
        materialRadioGroup.addView(button7);
        materialRadioGroup.addView(button8);

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trigger trigger = new Trigger(Utility.DATA_SETTING, String.valueOf(materialRadioGroup.getSelection()));
                normalTriggers.add(trigger);
                dialog.dismiss();
            }
        });


        showDialog(view);
    }


    private void showMusicPlayerDialog() {
        //Todo make dialog
        final View view = View.inflate(EditProfile.this, R.layout.radio_group, null);

        ((TextView) view.findViewById(R.id.title)).setText("Media Control");
        final MaterialRadioGroup materialRadioGroup = (MaterialRadioGroup) view.findViewById(R.id.material_radio_group);

        final MaterialRadioButton button1 = new MaterialRadioButton(this);
        button1.setText("Play");
        final MaterialRadioButton button2 = new MaterialRadioButton(this);
        button2.setText("Pause");
        final MaterialRadioButton button3 = new MaterialRadioButton(this);
        button3.setText("Toggle");
        final MaterialRadioButton button4 = new MaterialRadioButton(this);
        button4.setText("Skip");
        final MaterialRadioButton button5 = new MaterialRadioButton(this);
        button4.setText("Previous");

        materialRadioGroup.addView(button1);
        materialRadioGroup.addView(button2);
        materialRadioGroup.addView(button3);
        materialRadioGroup.addView(button4);
        materialRadioGroup.addView(button5);

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trigger trigger = new Trigger(Utility.DATA_SETTING, String.valueOf(materialRadioGroup.getSelection()));
                normalTriggers.add(trigger);
                dialog.dismiss();
            }
        });

        showDialog(view);
    }


    private void showAutoRotationDialog() {
        //Todo setDefaults
        final View view = View.inflate(EditProfile.this, R.layout.switch_item, null);
        ((TextView) view.findViewById(R.id.title)).setText("Auto-rotation");
        final MaterialSwitch materialSwitch = (MaterialSwitch) view.findViewById(R.id.material_switch);
        materialSwitch.setText("Auto-rotation");
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trigger trigger = new Trigger(Utility.AUTO_ROTATION_SETTING, materialSwitch.isChecked() ? "1" : "0");
                normalTriggers.add(trigger);
                dialog.dismiss();
            }
        });


        showDialog(view);
    }


    private void showBluetoothDialog() {
        //Todo defaults, and reset value when clicked
        final View view = View.inflate(EditProfile.this, R.layout.switch_item, null);
        ((TextView) view.findViewById(R.id.title)).setText("Bluetooth");
        final MaterialSwitch materialSwitch = (MaterialSwitch) view.findViewById(R.id.material_switch);
        materialSwitch.setText("Bluetooth");
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trigger trigger = new Trigger(Utility.BLUETOOTH_SETTING, materialSwitch.isChecked() ? "1" : "0");
                normalTriggers.add(trigger);
                dialog.dismiss();
            }
        });

        showDialog(view);
    }

    private void showWifiDialog() {
        //Todo defaults, and reset value when clicked
        final View view = View.inflate(EditProfile.this, R.layout.switch_item, null);
        ((TextView) view.findViewById(R.id.title)).setText("Wifi");
        final MaterialSwitch materialSwitch = (MaterialSwitch) view.findViewById(R.id.material_switch);
        materialSwitch.setText("Wifi");
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trigger trigger = new Trigger(Utility.WIFI_SETTING, materialSwitch.isChecked() ? "1" : "0");
                normalTriggers.add(trigger);
                dialog.dismiss();
            }
        });

        showDialog(view);
    }


    private void showSilentModeDialog() {
        //Todo defaults, and reset value when clicked
        final View view = View.inflate(EditProfile.this, R.layout.switch_item, null);
        ((TextView) view.findViewById(R.id.title)).setText("Silent Mode");
        final MaterialSwitch materialSwitch = (MaterialSwitch) view.findViewById(R.id.material_switch);
        materialSwitch.setText("Silent Mode");
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trigger trigger = new Trigger(Utility.SILENT_MODE_SETTING, materialSwitch.isChecked() ? "1" : "0");
                normalTriggers.add(trigger);
                dialog.dismiss();
            }
        });

        showDialog(view);
    }

    private void showRingtoneDialog() {
        //Todo make dialog
        final View view = View.inflate(EditProfile.this, R.layout., null);
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trigger trigger = new Trigger("Type", "value");
                normalTriggers.add(trigger);
                dialog.dismiss();
            }
        });


        showDialog(view);
    }

    private void showWallPaperDialog() {
        //Todo make dialog
        final View view = View.inflate(EditProfile.this, R.layout., null);
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trigger trigger = new Trigger("Type", "value");
                normalTriggers.add(trigger);
                dialog.dismiss();
            }
        });

        showDialog(view);
    }

    private void showAppLauncherDialog() {
        //Todo make dialog
        final View view = View.inflate(EditProfile.this, R.layout., null);
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trigger trigger = new Trigger("Type", "value");
                normalTriggers.add(trigger);
                dialog.dismiss();
            }
        });

        showDialog(view);
    }

    private void showAppLaunchListenerDialog() {
        //Todo make dialog
        final View view = View.inflate(EditProfile.this, R.layout., null);
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trigger trigger = new Trigger("Type", "value");
                normalTriggers.add(trigger);
                dialog.dismiss();
            }
        });

        showDialog(view);
    }


    private void showNFCDialog() {
        //Todo make dialog
        final View view = View.inflate(EditProfile.this, R.layout., null);
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trigger trigger = new Trigger("Type", "value");
                normalTriggers.add(trigger);
                dialog.dismiss();
            }
        });

        showDialog(view);
    }


    private void showLocationDialog() {
        //Todo make dialog
        final View view = View.inflate(EditProfile.this, R.layout., null);
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trigger trigger = new Trigger("Type", "value");
                normalTriggers.add(trigger);
                dialog.dismiss();
            }
        });

        showDialog(view);
    }


    private void showDockDialog() {
        //Todo make dialog
        final View view = View.inflate(EditProfile.this, R.layout., null);
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trigger trigger = new Trigger("Type", "value");
                normalTriggers.add(trigger);
                dialog.dismiss();
            }
        });

        showDialog(view);
    }

    private void showHeadPhoneJackDialog() {
        //Todo make dialog
        final View view = View.inflate(EditProfile.this, R.layout., null);
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trigger trigger = new Trigger("Type", "value");
                normalTriggers.add(trigger);
                dialog.dismiss();
            }
        });

        showDialog(view);
    }

    private void showTimeDialog() {
        //Todo make dialog
        final View view = View.inflate(EditProfile.this, R.layout., null);
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trigger trigger = new Trigger("Type", "value");
                normalTriggers.add(trigger);
                dialog.dismiss();
            }
        });

        showDialog(view);
    }

    private void showEditorDialog(String command) {


        if (command.equals(Utility.ALARM_VOLUME_SETTING)) {
            showAlarmVolumeDialog();
        } else if (command.equals(Utility.AUTO_ROTATION_SETTING)) {
            showAutoRotationDialog();
        } else if (command.equals(Utility.BLUETOOTH_SETTING)) {
            showBluetoothDialog();
        } else if (command.equals(Utility.WALLPAPER_SETTING)) {
            showWallPaperDialog();
        } else if (command.equals(Utility.WIFI_SETTING)) {
            showWifiDialog();
        } else if (command.equals(Utility.BRIGHTNESS_AUTO_SETTING)) {
            showBrightnessDialog();
        } else if (command.equals(Utility.MEDIA_VOLUME_SETTING)) {
            showMediaVolumeDialog();
        } else if (command.equals(Utility.LAUNCH_APP_SETTING)) {
            showAppLauncherDialog();
        } else if (command.equals(Utility.DATA_SETTING)) {
            showDataDialog();
        } else if (command.equals(Utility.BRIGHTNESS_SETTING)) {
            showBrightnessDialog();
        } else if (command.equals(Utility.RINGER_VOLUME_SETTING)) {
            showRingtoneVolumeDialog();
        } else if (command.equals(Utility.START_MUSIC_SETTING)) {
            showMusicPlayerDialog();
        } else if (command.equals(Utility.NOTIFICATION_VOLUME_SETTING)) {
            showNotificationVolumeDialog();
        } else if (command.equals(Utility.RINGTONE_SETTING)) {
            showRingtoneDialog();
        } else if (command.equals(Utility.SILENT_MODE_SETTING)) {
            showSilentModeDialog();
        } else if (command.equals(Utility.SLEEP_TIMEOUT_SETTING)) {
            showSleepTimeoutDialog();
        } else if (command.equals(Utility.TRIGGER_APP_LAUNCH)) {
            showAppLaunchListenerDialog();
        } else if (command.equals(Utility.TRIGGER_BATTERY_CHARGING)) {
            showBatteryDialog();
        } else if (command.equals(Utility.TRIGGER_BATTERY_PERCENTAGE)) {
            showBatteryDialog();
        } else if (command.equals(Utility.TRIGGER_BATTERY_TEMPERATURE)) {
            showBatteryDialog();
        } else if (command.equals(Utility.TRIGGER_BLUETOOTH_BSSID)) {
            showBluetoothDialog();
        } else if (command.equals(Utility.TRIGGER_BLUETOOTH_SSID)) {
            showBluetoothDialog();
        } else if (command.equals(Utility.TRIGGER_NFC)) {
            showNFCDialog();
        } else if (command.equals(Utility.TRIGGER_LOCATION)) {
            showLocationDialog();
        } else if (command.equals(Utility.TRIGGER_EARPHONE_JACK)) {
            showHeadPhoneJackDialog();
        } else if (command.equals(Utility.TRIGGER_DOCK)) {
            showDockDialog();
        } else if (command.equals(Utility.TRIGGER_TIME)) {
            showTimeDialog();
        } else if (command.equals(Utility.TRIGGER_WIFI_SSID)) {
            showWifiDialog();
        } else if (command.equals(Utility.TRIGGER_WIFI_BSSID)) {
            showWifiDialog();
        }

    }

    private void showDialog(View contentView) {
        dialog.setContentView(contentView);
        dialog.show();
    }

    private void showInfo(String infoType) {
        if (infoType.contains("restric")) {
            //Todo restrictions help
            showHelpDialog("Restrictions", "restrictions help");
        } else if (infoType.contains("prohib")) {
            //Todo prohib help
            showHelpDialog("Prohibitions", "prohibitions help");
        } else if (infoType.contains("trig")) {
            //Todo trigger help
            showHelpDialog("Triggers", "triggers help");
        } else {
            //Todo commands help
            showHelpDialog("Commands", "commands help");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Bundle.getExtra edit/new >> if new fill in commands...
        setContentView(R.layout.profile_manager_editor);
        try {
            final String shouldEdit = savedInstanceState.getString(Utility.EDIT_PROFILE);
            edit = !(shouldEdit == null || shouldEdit.length() < 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (edit)
            profileId = savedInstanceState.getInt(Utility.PROFILE_ID);

        profile_name = (EditText) findViewById(R.id.profile_name);
        findViewById(R.id.cancel).setOnClickListener(listener);
        findViewById(R.id.save).setOnClickListener(listener);

        getLayouts();
        setInfoOnClickListeners();
        setAddItemOnClickListeners();

        fillInArrayLists();

//        init();
//        getOldValues();

    }

    private void getLayouts() {
        prohibitionList = (LinearLayout) findViewById(R.id.prohibitions).findViewById(R.id.content);
        ((TextView) (findViewById(R.id.prohibitions).findViewById(R.id.title))).setText("Prohibitions");

        triggerList = (LinearLayout) findViewById(R.id.triggers).findViewById(R.id.content);
        ((TextView) (findViewById(R.id.triggers).findViewById(R.id.title))).setText("Triggers");

        restrictionList = (LinearLayout) findViewById(R.id.restrictions).findViewById(R.id.content);
        ((TextView) (findViewById(R.id.restrictions).findViewById(R.id.title))).setText("Restrictions");

        commandList = (LinearLayout) findViewById(R.id.commands).findViewById(R.id.content);
        ((TextView) (findViewById(R.id.commands).findViewById(R.id.title))).setText("Commands");
    }

    private void setInfoOnClickListeners() {
        findViewById(R.id.prohibitions).findViewById(R.id.info).setOnClickListener(tagClickListener);
        findViewById(R.id.prohibitions).findViewById(R.id.info).setTag("info_prohibitions");

        findViewById(R.id.triggers).findViewById(R.id.info).setOnClickListener(tagClickListener);
        findViewById(R.id.triggers).findViewById(R.id.info).setTag("info_triggers");

        findViewById(R.id.restrictions).findViewById(R.id.info).setOnClickListener(tagClickListener);
        findViewById(R.id.restrictions).findViewById(R.id.info).setTag("info_restriction");

        findViewById(R.id.commands).findViewById(R.id.info).setOnClickListener(tagClickListener);
        findViewById(R.id.commands).findViewById(R.id.info).setTag("info_commands");
    }


    private void setAddItemOnClickListeners() {
        findViewById(R.id.prohibitions).findViewById(R.id.add_item).setOnClickListener(tagClickListener);
        findViewById(R.id.prohibitions).findViewById(R.id.add_item).setTag("add_prohibitions");

        findViewById(R.id.triggers).findViewById(R.id.add_item).setOnClickListener(tagClickListener);
        findViewById(R.id.triggers).findViewById(R.id.add_item).setTag("add_triggers");

        findViewById(R.id.restrictions).findViewById(R.id.add_item).setOnClickListener(tagClickListener);
        findViewById(R.id.restrictions).findViewById(R.id.add_item).setTag("add_restriction");

        findViewById(R.id.commands).findViewById(R.id.add_item).setOnClickListener(tagClickListener);
        findViewById(R.id.commands).findViewById(R.id.add_item).setTag("add_commands");
    }

    private void editProfile(String id) {

        getProfileValues(id);
        setViewValues();
    }

    private void setViewValues() {
        //Todo lots of work to be done here


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //setOldValues();
        close();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // setOldValues();
        close();
    }

    public void save() {
        //Todo for loop going through all the arrays building a string to be saved
        // setOldValues();

        profiledb = profileDBHelper.getWritableDatabase();
        if (!trigger_device_picked || bssid == "") {
            //trigger().show();
            Toast.makeText(this, "You need a trigger device", Toast.LENGTH_LONG).show();
        } else {

            String profile_name_string = profile_name.getText().toString();

            if (profile_name_string == null || profile_name_string.length() < 1)
                profile_name_string = "Untitled";

            content_values = new ContentValues();

            content_values.put(ProfileDBHelper.PROFILE_NAME, profile_name_string);
            content_values.put(ProfileDBHelper.PROFILE_NAME, profile_name_string);


            if (edit) {
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
            finish();
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            alarm_old_value = Settings.System.getInt(getContentResolver(), Settings.System.VOLUME_ALARM);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            brightness_old_value = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            old_notification_value = Settings.System.getInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            old_incoming_call_volume = Settings.System.getInt(getContentResolver(), Settings.System.VOLUME_RING);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            old_media_volume_value = Settings.System.getInt(getContentResolver(), Settings.System.VOLUME_MUSIC);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        res = getResources();
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    }

    public void getProfileValues(String ID) {
        profiledb = profileDBHelper.getReadableDatabase();
        // cursor = profiledb.query(ProfileDBHelper.PROFILE_TABLE, need, null, null, null, null, null);
        cursor.moveToPosition(Integer.parseInt(ID));

        update_profile = cursor.getString(cursor.getColumnIndex(ProfileDBHelper.ID));
        profile_name_text = cursor.getString(cursor.getColumnIndex(ProfileDBHelper.PROFILE_NAME));

        close();

    }

    private void close() {
        try {
            profiledb.close();
            cursor.close();
        } catch (Exception e) {
        }
    }

//    private Dialog wifiDevicePicker() {
//        dialog = new Dialog(this, R.style.CustomDialog);
//
//        dialog.setContentView(R.layout.material_profile_manager_wireless_scanner_bluetooth_wifi);
//
//        ListView listView = (ListView) dialog.findViewById(R.id.list);
//        final WifiListAdapter wifiListAdapter = new WifiListAdapter(this);
//        listView.setAdapter(wifiListAdapter);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                trigger_device_picked = true;
//               // trigger_type = Utility.TRIGGER_WIFI;
//                bssid = ((WirelessDevice) wifiListAdapter.getItem(position)).getBssid();
//                wifiListAdapter.setSelectedItem(position);
//            }
//        });
//
//        dialog.findViewById(R.id.save_cancel).findViewById(R.id.cancel).setOnClickListener(save_cancel);
//        dialog.findViewById(R.id.save_cancel).findViewById(R.id.save).setOnClickListener(save_cancel);
//        return dialog;
//    }
//
//    private Dialog bluetoothDevicePicker() {
//        dialog = new Dialog(this, R.style.CustomDialog);
//        dialog.setContentView(R.layout.material_profile_manager_wireless_scanner_bluetooth_wifi);
//        ListView listView = (ListView) dialog.findViewById(R.id.list);
//        final BluetoothListAdapter bluetoothListAdapter = new BluetoothListAdapter(this);
//        listView.setAdapter(bluetoothListAdapter);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                trigger_device_picked = true;
//                //trigger_type = Utility.TRIGGER_BLUETOOTH;
//                bssid = ((WirelessDevice) bluetoothListAdapter.getItem(position)).getBssid();
//                bluetoothListAdapter.setSelectedItem(position);
//            }
//        });
//
//        dialog.findViewById(R.id.save_cancel).findViewById(R.id.cancel).setOnClickListener(save_cancel);
//        dialog.findViewById(R.id.save_cancel).findViewById(R.id.save).setOnClickListener(save_cancel);
//        return dialog;
//    }


    private void showProhibitionsDialog() {
        dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.trigger_chooser_dialog);
        currentDialog = "prohibitions";
        dialog.findViewById(R.id.cancel).setOnClickListener(save_cancel);
        ((TextView) dialog.findViewById(R.id.title)).setText("Add prohibition");
        populateProhibitions((LinearLayout) dialog.findViewById(R.id.content));
        dialog.show();
    }

    private void populateProhibitions(LinearLayout layout) {
        try {
            layout.removeAllViews();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < availableProhibitions.size(); i++) {
            final String item = availableProhibitions.get(i);
            final TextView view = (TextView) View.inflate(this, R.layout.command_item, null);
            view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            view.setText(Utility.getTriggerName(item));
            view.setTag("add_prohibition_" + item);
            view.setOnClickListener(tagClickListener);
            layout.addView(view);
        }
        layout.invalidate();
    }


    private void showTriggersDialog() {
        dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.trigger_chooser_dialog);
        currentDialog = "triggers";
        dialog.findViewById(R.id.cancel).setOnClickListener(save_cancel);
        ((TextView) dialog.findViewById(R.id.title)).setText("Add trigger");
        populateTriggers((LinearLayout) dialog.findViewById(R.id.content));
        dialog.show();
    }

    private void populateTriggers(LinearLayout layout) {
        try {
            layout.removeAllViews();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < availableTriggers.size(); i++) {
            final String item = availableTriggers.get(i);
            final TextView view = (TextView) View.inflate(this, R.layout.command_item, null);
            view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            view.setText(Utility.getTriggerName(item));
            view.setTag("add_trigger_" + item);
            view.setOnClickListener(tagClickListener);
            layout.addView(view);
        }
        layout.invalidate();
    }

    private void showRestrictionsDialog() {
        dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.trigger_chooser_dialog);
        currentDialog = "restrictions";
        dialog.findViewById(R.id.cancel).setOnClickListener(save_cancel);
        ((TextView) dialog.findViewById(R.id.title)).setText("Add restriction");
        populateRestrictions((LinearLayout) dialog.findViewById(R.id.content));
        dialog.show();
    }

    private void populateRestrictions(LinearLayout layout) {
        try {
            layout.removeAllViews();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < availableRestrictions.size(); i++) {
            final String item = availableRestrictions.get(i);
            final TextView view = (TextView) View.inflate(this, R.layout.command_item, null);
            view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            view.setText(Utility.getTriggerName(item));
            view.setTag("add_restriction_" + item);
            view.setOnClickListener(tagClickListener);
            layout.addView(view);
        }
        layout.invalidate();
    }

    private void showCommandsDialog() {
        dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.trigger_chooser_dialog);
        currentDialog = "commands";
        dialog.findViewById(R.id.cancel).setOnClickListener(save_cancel);
        ((TextView) dialog.findViewById(R.id.title)).setText("Add command");
        populateCommands((LinearLayout) dialog.findViewById(R.id.content));
        dialog.show();
    }

    private void populateCommands(LinearLayout layout) {
        try {
            layout.removeAllViews();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < availableCommands.size(); i++) {
            final String item = availableCommands.get(i);
            final TextView view = (TextView) View.inflate(this, R.layout.command_item, null);
            view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            view.setText(Utility.getCommandName(item));
            view.setTag("add_command_" + item);
            view.setOnClickListener(tagClickListener);
            layout.addView(view);
        }
        layout.invalidate();
    }

    private void showCopyFromChooserDialog() {
        final Dialog dialog = new Dialog(this, R.style.CustomDialog);
        //Todo change create xml for this
        currentDialog = "copy_from";
        dialog.setContentView(R.layout.list_view);
        View view = dialog.findViewById(R.id.a);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });

        dialog.show();
    }

    private void showHelpDialog(String titleText, String help) {
        dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.help_dialog);
        final TextView title = (TextView) dialog.findViewById(R.id.title);
        final TextView text = (TextView) dialog.findViewById(R.id.text);

        title.setText(titleText);
        text.setText(help);

        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private View getView(String viewName, String type, String viewCommand) {
        final View view = View.inflate(EditProfile.this, R.layout.command_list_item, null);
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        //Todo fix this
        view.findViewById(R.id.text).setTag("open_" + type + viewCommand);
        view.findViewById(R.id.text).setOnClickListener(tagClickListener);
        //Todo fix this
        view.findViewById(R.id.remove).setTag("remove_" + type + viewCommand);
        view.findViewById(R.id.remove).setOnClickListener(tagClickListener);
        view.setTag(viewCommand);

        ((TextView) view.findViewById(R.id.text)).setText(viewName);

        return view;
    }

    private void removeView(LinearLayout layout, String name) {
        Toast.makeText(this, String.format("lChildCo, name : %d, %s", layout.getChildCount(), name), Toast.LENGTH_LONG).show();

        for (int i = 0; i < layout.getChildCount(); i++) {
            final View view = layout.getChildAt(i);
            Toast.makeText(this, "checking view tag :" + view.getTag().toString(), Toast.LENGTH_LONG).show();
            if (view.getTag().toString().contains(name)) {
                layout.removeView(view);
                layout.invalidate();
                return;
            }
        }
    }

    private void addProhibitionList(String name, String command) {
        prohibitionList.addView(getView(name, "prohibition_", command), 0);
        availableProhibitions.remove(command);
    }

    private void addTriggerList(String name, String command) {
        triggerList.addView(getView(name, "trigger_", command), 0);
        availableTriggers.remove(command);

    }

    private void addRestrictionList(String name, String command) {
        restrictionList.addView(getView(name, "restriction_", command), 0);
        availableRestrictions.remove(command);
    }

    private void addCommandList(String name, String command) {
        commandList.addView(getView(name, "command_", command), 0);
        availableCommands.remove(command);
    }

    private void removeProhibition(String name) {
        removeView(prohibitionList, name);
        availableProhibitions.add(name);
    }

    private void removeTrigger(String name) {
        removeView(triggerList, name);
        availableTriggers.add(name);
    }

    private void removeRestriction(String name) {
        removeView(restrictionList, name);
        availableRestrictions.add(name);
    }

    private void removeCommand(String name) {
        removeView(commandList, name);
        availableCommands.add(name);
    }

    private void fillInArrayLists() {
        fillInAvailableCommands();
        fillInAvailableTriggers();
        fillInAvailableRestrictions();
        fillInAvailableProhibitions();

        if (!edit)
            return;
        //Todo db stuff here
        fillInAddedCommands();
        fillInRestrictionTriggers();
        fillInProhibitionTriggers();
        fillInNormalTriggers();
    }

    private void fillInAddedCommands() {
//        addedCommands.add();
//        availableCommands.remove()

    }

    private void fillInRestrictionTriggers() {
//        restrictionTriggers.add();
//        availableRestrictions.remove()
    }

    private void fillInProhibitionTriggers() {
//        prohibitionTriggers.add();
//        prohibitionTriggers.remove();
    }

    private void fillInNormalTriggers() {
//        normalTriggers.add();
//        normalTriggers.remove();
    }

    private void fillInAvailableTriggers() {
        //Todo consider removing ssid/bssid in favor of a checkbox in the dialog for for the bssid or ssid
        availableTriggers.add("BLUETOOTH_SSID");
        availableTriggers.add("BLUETOOTH_BSSID");
        availableTriggers.add("WIFI_SSID");
        availableTriggers.add("WIFI_BSSID");
        availableTriggers.add("BATTERY_TEMPERATURE");
        availableTriggers.add("BATTERY_PERCENTAGE");
        availableTriggers.add("BATTERY_CHARGING");
        availableTriggers.add("NFC");
        availableTriggers.add("EARPHONE_JACK");
        availableTriggers.add("DOCK");
        availableTriggers.add("TIME");
        availableTriggers.add("LOCATION");
        availableTriggers.add("APP_LAUNCH");
    }

    private void fillInAvailableProhibitions() {
        availableProhibitions.add("BLUETOOTH_SSID");
        availableProhibitions.add("BLUETOOTH_BSSID");
        availableProhibitions.add("WIFI_SSID");
        availableProhibitions.add("WIFI_BSSID");
        availableProhibitions.add("BATTERY_TEMPERATURE");
        availableProhibitions.add("BATTERY_PERCENTAGE");
        availableProhibitions.add("BATTERY_CHARGING");
        availableProhibitions.add("NFC");
        availableProhibitions.add("EARPHONE_JACK");
        availableProhibitions.add("DOCK");
        availableProhibitions.add("TIME");
        availableProhibitions.add("LOCATION");
        availableProhibitions.add("APP_LAUNCH");
    }


    private void fillInAvailableRestrictions() {
        availableRestrictions.add("BLUETOOTH_SSID");
        availableRestrictions.add("BLUETOOTH_BSSID");
        availableRestrictions.add("WIFI_SSID");
        availableRestrictions.add("WIFI_BSSID");
        availableRestrictions.add("BATTERY_TEMPERATURE");
        availableRestrictions.add("BATTERY_PERCENTAGE");
        availableRestrictions.add("BATTERY_CHARGING");
        availableRestrictions.add("NFC");
        availableRestrictions.add("EARPHONE_JACK");
        availableRestrictions.add("DOCK");
        availableRestrictions.add("TIME");
        availableRestrictions.add("LOCATION");
        availableRestrictions.add("APP_LAUNCH");
    }

    private void fillInAvailableCommands() {
        //Todo reorder these
        availableCommands.add("WIFI_SETTING");
        availableCommands.add("BLUETOOTH_SETTING");
        availableCommands.add("DATA_SETTING");
        availableCommands.add("BRIGHTNESS_SETTING");
        availableCommands.add("BRIGHTNESS_AUTO_SETTING");
        availableCommands.add("SILENT_MODE_SETTING");
        availableCommands.add("NOTIFICATION_VOLUME_SETTING");
        availableCommands.add("ALARM_VOLUME_SETTING");
        availableCommands.add("START_MUSIC_SETTING");
        availableCommands.add("LAUNCH_APP_SETTING");
        availableCommands.add("WALLPAPER_SETTING");
        availableCommands.add("RINGTONE_SETTING");
        availableCommands.add("MEDIA_VOLUME_SETTING");
        availableCommands.add("RINGER_VOLUME_SETTING");
        availableCommands.add("AUTO_ROTATION_SETTING");
        availableCommands.add("SLEEP_TIMEOUT_SETTING");
    }
}
