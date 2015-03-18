package com.micnubinub.mrautomatic;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import adapters.BluetoothListAdapter;
import adapters.WifiListAdapter;
import time_picker.AbstractWheel;
import time_picker.NumericWheelAdapter;
import tools.CustomListView;
import tools.Device;
import tools.LinearLayoutList;
import tools.TriggerOrCommand;
import tools.TriggerOrCommand.Type;
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
    //TODO GO FROM THE BEGINNING TO THE END OF THE EDIT PROFILE CREATE
    //Todo copy from>> dialog with 4 ticks : triggers, restrictions, prohibitions and commands
    //Todo preference to play preview and add to all the seek bars
    //TODO ---- IMPORTANT check if all the strings are correct
    //Todo make google maps view
    /**
     * TODO IMPORTANT and time consuming :
     * make a view pager that has tabs for file chooser like the wallpaper, ringtone, notification,
     * use the code from bass jump
     * list the files and the system wallpapers/ringtones...
     */
    //Todo don't add item to the list of addedProhibTrigComm... if not properly setup

    public static final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private static final ArrayList<String> availableCommands = new ArrayList<String>(15);
    private static final ArrayList<String> availableTriggers = new ArrayList<String>(10);
    private static final ArrayList<String> availableProhibitions = new ArrayList<String>(10);
    private static final ArrayList<String> availableRestrictions = new ArrayList<String>(10);
    private static final ArrayList<TriggerOrCommand> addedCommands = new ArrayList<TriggerOrCommand>(15);
    private static final ArrayList<TriggerOrCommand> restrictionTriggers = new ArrayList<TriggerOrCommand>(10);
    private static final ArrayList<TriggerOrCommand> prohibitionTriggers = new ArrayList<TriggerOrCommand>(10);
    private static final ArrayList<TriggerOrCommand> normalTriggers = new ArrayList<TriggerOrCommand>(10);
    private static LinearLayout prohibitionList, triggerList, restrictionList, commandList;
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
            final String view = v.getTag().toString();
            if (view.startsWith("info")) {
                showInfo(view);
            } else if (v.getTag() instanceof Device) {
                toast(view);
            } else {
                checkLongString(view);
            }
        }
    };

    int brightness_auto_old_value, ringer_old_value, alarm_old_value;
    int wifi_old_value, bluetooth_old_value, brightness_old_value, media_volume, old_media_volume_value, notification_volume, old_notification_value, old_incoming_call_volume;
    private String profileId;
    private boolean edit = false;
    private EditText profile_name;
    private String currentScan, profile_name_text;
    private boolean trigger_device_picked = false;
    private WifiManager wifiManager;
    private ContentResolver contentResolver;
    private ContentValues content_values;
    private AudioManager audioManager;
    private Uri notification;
    private Cursor cursor;
    private SQLiteDatabase profiledb;
    //Todo items should be Wifi(asnd[ac:as:2s:as...])

    private static TriggerOrCommand getTriggerFromArray(Type type) {
        ArrayList<TriggerOrCommand> triggers = null;

        switch (type) {
            case RESTRICTIONS:
                triggers = restrictionTriggers;
                break;
            case TRIGGER:
                triggers = normalTriggers;
                break;
            case PROHIBITION:
                triggers = prohibitionTriggers;
                break;
            case COMMAND:
                triggers = addedCommands;
                break;
        }

        for (int i = 0; i < triggers.size(); i++) {
            final TriggerOrCommand trigger = triggers.get(i);
            if (trigger.getCategory().equals(type))
                return trigger;
        }
        return null;
    }

    private void checkLongString(String string) {
        try {
            final String[] split = string.split("_", 3);
            final String action = split[0].toLowerCase();
            final String type = split[1];
            final String actor = split[2];

            if (action.equals("add")) {
                if (type.contains("restri")) {
                    addRestrictionList(Utility.getTriggerName(actor), actor);
                } else if (type.contains("prohib")) {
                    addProhibitionList(Utility.getTriggerName(actor), actor);
                } else if (type.contains("trig")) {
                    addTriggerList(Utility.getTriggerName(actor), actor);
                } else {
                    addCommandList(Utility.getCommandName(actor), actor);
                }
                //Todo  showEditorDialog(actor);

            } else if (action.equals("open")) {
                //Todo showEditorDialog(actor);
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
        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.seekbar, null);
        ((TextView) view.findViewById(R.id.title)).setText("Alarm");
        ((TextView) view.findViewById(R.id.text)).setText("Alarm volume");

        final MaterialSeekBar materialSeekBar = (MaterialSeekBar) view.findViewById(R.id.material_seekbar);
        materialSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
        materialSeekBar.setOnProgressChangedListener(new MaterialSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int max, int progress) {
                ((TextView) view.findViewById(R.id.text)).setText(String.format("Alarm volume will be set to: %d", progress));
            }
        });

        final TriggerOrCommand command = getCommandFromArray(Utility.ALARM_VOLUME_SETTING);
        if (command != null)
            materialSeekBar.setProgress(Integer.parseInt(command.getValue()));
        else
            materialSeekBar.setProgress(5);
        view.findViewById(R.id.save_cancel).findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.save_cancel).findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCommandValue(Utility.ALARM_VOLUME_SETTING, String.valueOf(materialSeekBar.getProgress()));
            }
        });
        dialog.show();
    }

    private void showMediaVolumeDialog() {
        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.seekbar, null);
        ((TextView) view.findViewById(R.id.title)).setText("Media");
        ((TextView) view.findViewById(R.id.text)).setText("Media volume");

        final MaterialSeekBar materialSeekBar = (MaterialSeekBar) view.findViewById(R.id.material_seekbar);
        materialSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        materialSeekBar.setOnProgressChangedListener(new MaterialSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int max, int progress) {
                ((TextView) view.findViewById(R.id.text)).setText(String.format("Media volume will be set to: %d", progress));
            }
        });

        final TriggerOrCommand command = getCommandFromArray(Utility.MEDIA_VOLUME_SETTING);

        if (command != null)
            materialSeekBar.setProgress(Integer.parseInt(command.getValue()));
        else
            materialSeekBar.setProgress(5);

        view.findViewById(R.id.save_cancel).findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.save_cancel).findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCommandValue(Utility.MEDIA_VOLUME_SETTING, String.valueOf(materialSeekBar.getProgress()));
            }
        });
        dialog.show();
    }

    private void showNotificationVolumeDialog() {
        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.seekbar, null);
        ((TextView) view.findViewById(R.id.title)).setText("Notifications");
        ((TextView) view.findViewById(R.id.text)).setText("Notification volume");

        final MaterialSeekBar materialSeekBar = (MaterialSeekBar) view.findViewById(R.id.material_seekbar);
        materialSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
        materialSeekBar.setOnProgressChangedListener(new MaterialSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int max, int progress) {
                ((TextView) view.findViewById(R.id.text)).setText(String.format("Notification volume will be set to: %d", progress));
            }
        });

        final TriggerOrCommand command = getCommandFromArray(Utility.NOTIFICATION_VOLUME_SETTING);
        if (command != null)
            materialSeekBar.setProgress(Integer.parseInt(command.getValue()));
        else
            materialSeekBar.setProgress(5);

        view.findViewById(R.id.save_cancel).findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.save_cancel).findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCommandValue(Utility.NOTIFICATION_VOLUME_SETTING, String.valueOf(materialSeekBar.getProgress()));
            }
        });

        dialog.show();
    }

    private int dpToPixels(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void showRingtoneVolumeDialog() {
        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.seekbar, null);
        ((TextView) view.findViewById(R.id.title)).setText("Ringtones");
        ((TextView) view.findViewById(R.id.text)).setText("Ringtone volume");

        final MaterialSeekBar materialSeekBar = (MaterialSeekBar) view.findViewById(R.id.material_seekbar);
        materialSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_RING));
        materialSeekBar.setOnProgressChangedListener(new MaterialSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int max, int progress) {
                ((TextView) view.findViewById(R.id.text)).setText(String.format("Ringtone volume will be set to: %d", progress));
            }
        });

        final TriggerOrCommand command = getCommandFromArray(Utility.RINGER_VOLUME_SETTING);
        if (command != null)
            materialSeekBar.setProgress(Integer.parseInt(command.getValue()));
        else
            materialSeekBar.setProgress(5);

        view.findViewById(R.id.save_cancel).findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.save_cancel).findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCommandValue(Utility.RINGER_VOLUME_SETTING, String.valueOf(materialSeekBar.getProgress()));
            }
        });


        dialog.show();
    }

    private void setValue(Type triggerType, String type, String value) {
        TriggerOrCommand trigger = getTriggerFromArray(triggerType);
        if (trigger == null) {
            trigger = new TriggerOrCommand(triggerType, type, value);
            switch (triggerType) {
                case RESTRICTIONS:
                    restrictionTriggers.add(trigger);
                    availableRestrictions.remove(type);
                    break;
                case TRIGGER:
                    normalTriggers.add(trigger);
                    availableTriggers.remove(type);
                    break;
                case PROHIBITION:
                    prohibitionTriggers.add(trigger);
                    availableProhibitions.remove(type);
                    break;
            }
        } else {
            trigger.setValue(value);
        }

    }

    private void setCommandValue(String type, String value) {
        final TriggerOrCommand command = getCommandFromArray(type);
        if (command == null)
            addedCommands.add(new TriggerOrCommand(Type.TRIGGER, type, value));
        else
            command.setValue(value);
        availableCommands.remove(type);
    }

    private TriggerOrCommand getCommandFromArray(String name) {
        for (int i = 0; i < addedCommands.size(); i++) {
            final TriggerOrCommand command = addedCommands.get(i);
            if (command.getType().equals(name))
                return command;
        }
        return null;
    }

    private void showBrightnessDialog() {
        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.seekbar, null);

        ((TextView) view.findViewById(R.id.title)).setText("Brightness");
        final MaterialSeekBar materialSeekBar = (MaterialSeekBar) view.findViewById(R.id.material_seekbar);
        materialSeekBar.setMax(255);
        materialSeekBar.setProgress(50);
        materialSeekBar.setOnProgressChangedListener(new MaterialSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int max, int progress) {
                ((TextView) view.findViewById(R.id.text)).setText(String.format("Brightness will be set to : %d%s", Math.round((progress / (float) max) * 100), "%"));
            }
        });
        final MaterialCheckBox materialCheckBox = (MaterialCheckBox) view.findViewById(R.id.material_checkbox);
        materialCheckBox.setVisibility(View.VISIBLE);
        materialCheckBox.setText("Auto-Brightness");
        materialCheckBox.setOnCheckedChangeListener(new MaterialCheckBox.OnCheckedChangedListener() {
            @Override
            public void onCheckedChange(MaterialCheckBox materialCheckBox, boolean isChecked) {
                materialSeekBar.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                view.findViewById(R.id.text).setVisibility(isChecked ? View.GONE : View.VISIBLE);
            }
        });

        final TriggerOrCommand command = getCommandFromArray(Utility.BRIGHTNESS_SETTING);
        if (command != null) {
            if (command.getValue().equals("-1"))
                materialCheckBox.setChecked(true);
            else
                materialSeekBar.setProgress(Integer.parseInt(command.getValue()));
        } else
            materialSeekBar.setProgress(5);

        view.findViewById(R.id.save_cancel).findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.save_cancel).findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCommandValue(Utility.BRIGHTNESS_SETTING, materialCheckBox.isChecked() ? "-1" : String.valueOf(materialSeekBar.getProgress()));
            }
        });

        dialog.show();
    }

    private Dialog getDialog() {
        return new Dialog(this, R.style.CustomDialog);
    }

    private void showBatteryDialog(final Type triggerType) {
        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.seekbar, null);

        ((TextView) view.findViewById(R.id.title)).setText("Battery");
        final MaterialSeekBar materialSeekBar = (MaterialSeekBar) view.findViewById(R.id.material_seekbar);
        materialSeekBar.setMax(100);
        materialSeekBar.setProgress(50);

        materialSeekBar.setOnProgressChangedListener(new MaterialSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int max, int progress) {
                ((TextView) view.findViewById(R.id.text)).setText(String.format("Triggered when less than : %d%s", progress, "%"));
            }
        });

        final MaterialCheckBox materialCheckBox = (MaterialCheckBox) view.findViewById(R.id.material_checkbox);
        materialCheckBox.setVisibility(View.VISIBLE);
        materialCheckBox.setText("Charging");
        materialCheckBox.setOnCheckedChangeListener(new MaterialCheckBox.OnCheckedChangedListener() {
            @Override
            public void onCheckedChange(MaterialCheckBox materialCheckBox, boolean isChecked) {
                materialSeekBar.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                view.findViewById(R.id.text).setVisibility(isChecked ? View.GONE : View.VISIBLE);
            }
        });

        final TriggerOrCommand trigger = getTriggerFromArray(triggerType);
        if (trigger != null) {
            if (trigger.getValue().equals("-1"))
                materialCheckBox.setChecked(true);
            else
                materialSeekBar.setProgress(Integer.parseInt(trigger.getValue()));
        } else
            materialSeekBar.setProgress(50);

        view.findViewById(R.id.save_cancel).findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.save_cancel).findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setValue(triggerType, Utility.TRIGGER_BATTERY, materialCheckBox.isChecked() ? "-1" : String.valueOf(materialSeekBar.getProgress()));
            }
        });

        dialog.show();
    }


    private void showBatteryTemperatureDialog(final Type triggerType) {
        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.seekbar, null);

        ((TextView) view.findViewById(R.id.title)).setText("Battery temperate");
        final MaterialSeekBar materialSeekBar = (MaterialSeekBar) view.findViewById(R.id.material_seekbar);
        materialSeekBar.setMax(60);

        materialSeekBar.setOnProgressChangedListener(new MaterialSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int max, int progress) {
                ((TextView) view.findViewById(R.id.text)).setText(String.format("Triggered when the temp greater than : %d %s", progress, "degCelsius"));
            }
        });

        materialSeekBar.setProgress(20);

        final TriggerOrCommand trigger = getTriggerFromArray(triggerType);

        if (trigger != null)
            materialSeekBar.setProgress(Integer.parseInt(trigger.getValue()));


        view.findViewById(R.id.save_cancel).findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.save_cancel).findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setValue(triggerType, Utility.TRIGGER_BATTERY_TEMPERATURE, String.valueOf(materialSeekBar.getProgress()));
            }
        });

        dialog.show();
    }


    private void showDataDialog() {
        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.radio_group, null);

        ((TextView) view.findViewById(R.id.title)).setText("Data");
        final MaterialRadioGroup materialRadioGroup = (MaterialRadioGroup) view.findViewById(R.id.material_radio_group);
        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, dpToPixels(40));

        final MaterialRadioButton button1 = new MaterialRadioButton(this);
        button1.setLayoutParams(params);
        button1.setText("Off");
        final MaterialRadioButton button2 = new MaterialRadioButton(this);
        button2.setLayoutParams(params);
        button2.setText("2G");
        final MaterialRadioButton button3 = new MaterialRadioButton(this);
        button3.setLayoutParams(params);
        button3.setText("3G");
        final MaterialRadioButton button4 = new MaterialRadioButton(this);
        button4.setLayoutParams(params);
        button4.setText("4G");

        materialRadioGroup.addView(button1);
        materialRadioGroup.addView(button2);
        materialRadioGroup.addView(button3);
        materialRadioGroup.addView(button4);

        final TriggerOrCommand command = getCommandFromArray(Utility.DATA_SETTING);
        if (command != null)
            materialRadioGroup.setSelected(Integer.parseInt(command.getValue()));

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCommandValue(Utility.DATA_SETTING, String.valueOf(materialRadioGroup.getSelection()));
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void snapShot() {
        //Todo add all available triggers, if multiple triggers available allow them to remove some e.g multiple wifi devices
    }

    private void showSleepTimeoutDialog() {
        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.radio_group, null);

        ((TextView) view.findViewById(R.id.title)).setText("Data");
        final MaterialRadioGroup materialRadioGroup = (MaterialRadioGroup) view.findViewById(R.id.material_radio_group);
        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, dpToPixels(40));

        //Todo get the values from this and add them to the profile service

        final MaterialRadioButton button1 = new MaterialRadioButton(this);
        button1.setLayoutParams(params);
        button1.setText("10 secs");
        final MaterialRadioButton button2 = new MaterialRadioButton(this);
        button2.setLayoutParams(params);
        button2.setText("30 secs");
        final MaterialRadioButton button3 = new MaterialRadioButton(this);
        button3.setLayoutParams(params);
        button3.setText("1 min");
        final MaterialRadioButton button4 = new MaterialRadioButton(this);
        button4.setLayoutParams(params);
        button4.setText("2 min");
        final MaterialRadioButton button5 = new MaterialRadioButton(this);
        button5.setLayoutParams(params);
        button5.setText("5 min");
        final MaterialRadioButton button6 = new MaterialRadioButton(this);
        button6.setLayoutParams(params);
        button6.setText("10 min");
        final MaterialRadioButton button7 = new MaterialRadioButton(this);
        button7.setLayoutParams(params);
        button7.setText("15 min");
        final MaterialRadioButton button8 = new MaterialRadioButton(this);
        button8.setLayoutParams(params);
        button8.setText("30 min");

        materialRadioGroup.addView(button1);
        materialRadioGroup.addView(button2);
        materialRadioGroup.addView(button3);
        materialRadioGroup.addView(button5);
        materialRadioGroup.addView(button6);
        materialRadioGroup.addView(button7);
        materialRadioGroup.addView(button8);

        final TriggerOrCommand command = getCommandFromArray(Utility.SLEEP_TIMEOUT_SETTING);
        if (command != null)
            materialRadioGroup.setSelected(Integer.parseInt(command.getValue()));

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCommandValue(Utility.DATA_SETTING, String.valueOf(materialRadioGroup.getSelection()));
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    private void showMusicPlayerDialog() {
        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.radio_group, null);

        ((TextView) view.findViewById(R.id.title)).setText("Media Control");
        final MaterialRadioGroup materialRadioGroup = (MaterialRadioGroup) view.findViewById(R.id.material_radio_group);
        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, dpToPixels(40));

        //Todo get the values from this and add them to the profile service

        final MaterialRadioButton button1 = new MaterialRadioButton(this);
        button1.setText("Play");
        button1.setLayoutParams(params);
        final MaterialRadioButton button2 = new MaterialRadioButton(this);
        button2.setLayoutParams(params);
        button2.setText("Pause");
        final MaterialRadioButton button3 = new MaterialRadioButton(this);
        button3.setLayoutParams(params);
        button3.setText("Previous");
        final MaterialRadioButton button4 = new MaterialRadioButton(this);
        button4.setLayoutParams(params);
        button4.setText("Skip");

        materialRadioGroup.addView(button1);
        materialRadioGroup.addView(button2);
        materialRadioGroup.addView(button3);
        materialRadioGroup.addView(button4);

        final TriggerOrCommand command = getCommandFromArray(Utility.MEDIA_CONTROL_SETTING);
        if (command != null)
            materialRadioGroup.setSelected(Integer.parseInt(command.getValue()));

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCommandValue(Utility.MEDIA_CONTROL_SETTING, String.valueOf(materialRadioGroup.getSelection()));
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showAutoRotationDialog() {
        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.switch_item, null);
        ((TextView) view.findViewById(R.id.title)).setText("Auto-rotation");
        final MaterialSwitch materialSwitch = (MaterialSwitch) view.findViewById(R.id.material_switch);
        materialSwitch.setText("Auto-rotation");

        final TriggerOrCommand command = getCommandFromArray(Utility.AUTO_ROTATION_SETTING);
        if (command != null)
            materialSwitch.setChecked(command.getValue().equals("1"));

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCommandValue(Utility.AUTO_ROTATION_SETTING, materialSwitch.isChecked() ? "1" : "0");
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showBluetoothDialog() {
        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.switch_item, null);
        ((TextView) view.findViewById(R.id.title)).setText("Bluetooth");
        final MaterialSwitch materialSwitch = (MaterialSwitch) view.findViewById(R.id.material_switch);
        materialSwitch.setText("Bluetooth");

        final TriggerOrCommand command = getCommandFromArray(Utility.BLUETOOTH_SETTING);
        if (command != null)
            materialSwitch.setChecked(command.getValue().equals("1"));

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCommandValue(Utility.BLUETOOTH_SETTING, materialSwitch.isChecked() ? "1" : "0");
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showWifiDialog() {
        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.switch_item, null);
        ((TextView) view.findViewById(R.id.title)).setText("Wifi");
        final MaterialSwitch materialSwitch = (MaterialSwitch) view.findViewById(R.id.material_switch);
        materialSwitch.setText("Wifi");

        final TriggerOrCommand command = getCommandFromArray(Utility.WIFI_SETTING);
        if (command != null)
            materialSwitch.setChecked(command.getValue().equals("1"));

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCommandValue(Utility.WIFI_SETTING, materialSwitch.isChecked() ? "1" : "0");
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showBluetoothDevicePickerDialog() {
        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.custom_device_picker_list_view, null);
        ((TextView) view.findViewById(R.id.title)).setText("Bluetooth Trigger");
        final BluetoothListAdapter listAdapter = new BluetoothListAdapter(this, new CustomListView(view));
        view.findViewById(R.id.save_cancel).findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listAdapter.cancelScan();
                //Todo use this view.setOnClickListener(tagClickListener);
                Toast.makeText(EditProfile.this, listAdapter.getSelectedDevice().toString(), Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.save_cancel).findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listAdapter.cancelScan();
                dialog.dismiss();
            }
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void showWifiDevicePickerDialog(final Type triggerType) {
        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.custom_device_picker_list_view, null);
        ((TextView) view.findViewById(R.id.title)).setText("Wifi Trigger");
        final WifiListAdapter listAdapter = new WifiListAdapter(this, new CustomListView(view));
        view.findViewById(R.id.save_cancel).findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listAdapter.cancelScan();
                //Todo use this use this view.setOnClickListener(tagClickListener);
                Toast.makeText(EditProfile.this, listAdapter.getSelectedDevice().toString(), Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.save_cancel).findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listAdapter.cancelScan();
                dialog.dismiss();
            }
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void showSilentModeDialog() {
        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.switch_item, null);
        ((TextView) view.findViewById(R.id.title)).setText("Silent Mode");
        final MaterialSwitch materialSwitch = (MaterialSwitch) view.findViewById(R.id.material_switch);
        materialSwitch.setText("Silent Mode");

        final TriggerOrCommand command = getCommandFromArray(Utility.SILENT_MODE_SETTING);
        if (command != null)
            materialSwitch.setChecked(command.getValue().equals("1"));

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCommandValue(Utility.SILENT_MODE_SETTING, materialSwitch.isChecked() ? "1" : "0");
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showRingtoneDialog() {
        //Todo make dialog
        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.restrictions_dialog, null);

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCommandValue("Type", "value");
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showWallPaperDialog() {
        //Todo make dialog
        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.restrictions_dialog, null);
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCommandValue("Type", "value");
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showAppLauncherDialog() {
        //Todo make dialog
        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.restrictions_dialog, null);
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCommandValue("Type", "value");
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    private void showAppLaunchListenerDialog() {
        //Todo make dialog
        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.restrictions_dialog, null);
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setValue(Type.TRIGGER, "Type", "value");
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    private void showNFCDialog() {
        //Todo make dialog

        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.restrictions_dialog, null);
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setValue(Type.TRIGGER, "Type", "value");
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    private void showLocationDialog(final Type triggerType) {
        //Todo make dialog
        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.restrictions_dialog, null);
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setValue(triggerType, "Type", "value");
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    private void showDockDialog(final Type triggerType) {
        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.checkbox_item, null);

        ((TextView) view.findViewById(R.id.title)).setText("Dock");
        final MaterialCheckBox materialCheckBox = (MaterialCheckBox) view.findViewById(R.id.material_checkbox);
        materialCheckBox.setText("Device docked");
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setValue(triggerType, Utility.TRIGGER_DOCK, materialCheckBox.isChecked() ? "1" : "0");
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showHeadPhoneJackDialog(final Type triggerType) {
        final Dialog dialog = getDialog();
        final View view = View.inflate(EditProfile.this, R.layout.checkbox_item, null);
        ((TextView) view.findViewById(R.id.title)).setText("Headphone Jack");
        final MaterialCheckBox materialCheckBox = (MaterialCheckBox) view.findViewById(R.id.material_checkbox);
        materialCheckBox.setText("Jack connect");

        //Todo use type instead
        final TriggerOrCommand trigger = getTriggerFromArray(triggerType);
        if (trigger != null)
            materialCheckBox.setChecked(trigger.getValue().equals("1"));

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setValue(triggerType, Utility.TRIGGER_EARPHONE_JACK, materialCheckBox.isChecked() ? "1" : "0");
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showTimeDialog(final Type triggerType) {
        //Todo check dialog
        //Todo consider adding weekdays above the tome (with just one letter like the clock app >> s m t w ... s and a check box to check all, the weekdays or the weekends
        final Dialog dialog = getDialog();

        final View view = View.inflate(EditProfile.this, R.layout.time_picker, null);
        dialog.setContentView(view);
        final AbstractWheel hours = (AbstractWheel) dialog.findViewById(R.id.hours);
        final AbstractWheel minutes = (AbstractWheel) dialog.findViewById(R.id.minutes);

        hours.setViewAdapter(new NumericWheelAdapter(this, 0, 23));
        hours.setCyclic(true);

        minutes.setViewAdapter(new NumericWheelAdapter(this, 0, 59));
        minutes.setCyclic(true);

        hours.setCurrentItem(12);
        minutes.setCurrentItem(30);

        dialog.findViewById(R.id.save_cancel).findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Todo implement this
                final int h = hours.getCurrentItem();
                final int m = minutes.getCurrentItem();
                setValue(Type.TRIGGER, "Type", "value");
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.save_cancel).findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showEditorDialog(final Type triggerType, String command) {
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
        } else if (command.equals(Utility.BRIGHTNESS_SETTING)) {
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
        } else if (command.equals(Utility.MEDIA_CONTROL_SETTING)) {
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
        } else if (command.equals(Utility.TRIGGER_BATTERY)) {
            showBatteryDialog(triggerType);
        } else if (command.equals(Utility.TRIGGER_BATTERY_TEMPERATURE)) {
            showBatteryTemperatureDialog(triggerType);
        } else if (command.equals(Utility.TRIGGER_BLUETOOTH)) {
            showBluetoothDevicePickerDialog();
        } else if (command.equals(Utility.TRIGGER_NFC)) {
            showNFCDialog();
        } else if (command.equals(Utility.TRIGGER_LOCATION)) {
            showLocationDialog(triggerType);
        } else if (command.equals(Utility.TRIGGER_EARPHONE_JACK)) {
            showHeadPhoneJackDialog(triggerType);
        } else if (command.equals(Utility.TRIGGER_DOCK)) {
            showDockDialog(triggerType);
        } else if (command.equals(Utility.TRIGGER_TIME)) {
            showTimeDialog(triggerType);
        } else if (command.equals(Utility.TRIGGER_WIFI)) {
            showWifiDevicePickerDialog(triggerType);
        }
    }

    private void showInfo(String infoType) {
        if (infoType.contains("restric")) {
            showHelpDialog("Restriction Help", "Restrictions work on a MUST basis, thus ALL of them must be triggered for the profile to be set, provided all the other conditions passed.");
        } else if (infoType.contains("prohib")) {
            showHelpDialog("Prohibition Help", "Prohibitions work on a NOT basis, thus if any ONE of them is triggered the profile is not set.");
        } else if (infoType.contains("trig")) {
            showHelpDialog("Trigger Help", "Triggers work on an OR basis, thus it only needs ONE of them to be triggered for the profile to be set, provided all the other conditions passed.");
        } else {
            showHelpDialog("TriggerOrCommand Help", "Commands are carried out when all the conditions are satisfied, for example if the wifi commands has its switch turned on, wifi will be turned on, other wise it will be turned off.");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_manager_editor);
        final Bundle bundle = getIntent().getExtras();
        try {
            profileId = bundle.getString(Utility.EDIT_PROFILE);
            toast(profileId);
            edit = !(profileId == null || profileId.length() < 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (edit) {
            ((TextView) findViewById(R.id.title)).setText("Edit Profile");
        } else {
            ((TextView) findViewById(R.id.title)).setText("New Profile");
        }
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        profile_name = (EditText) findViewById(R.id.profile_name);
        findViewById(R.id.cancel).setOnClickListener(listener);
        findViewById(R.id.save).setOnClickListener(listener);
        init();
        getLayouts();
        setInfoOnClickListeners();

        fillInArrayLists();
//        init();
        getOldValues();

    }


    private void getLayouts() {
        final View.OnClickListener l = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTriggerProhibOrRestricCommandDialog((Type) view.getTag());
            }
        };

        prohibitionList = (LinearLayout) findViewById(R.id.prohibitions).findViewById(R.id.content);
        ((TextView) (findViewById(R.id.prohibitions).findViewById(R.id.title))).setText("Prohibitions");
        ((Button) (findViewById(R.id.prohibitions).findViewById(R.id.add_item))).setText("Add Prohibition");
        findViewById(R.id.prohibitions).findViewById(R.id.add_item).setOnClickListener(l);
        findViewById(R.id.prohibitions).findViewById(R.id.add_item).setTag(Type.PROHIBITION);

        triggerList = (LinearLayout) findViewById(R.id.triggers).findViewById(R.id.content);
        ((TextView) (findViewById(R.id.triggers).findViewById(R.id.title))).setText("Triggers");
        ((Button) (findViewById(R.id.triggers).findViewById(R.id.add_item))).setText("Add Trigger");
        findViewById(R.id.triggers).findViewById(R.id.add_item).setOnClickListener(l);
        findViewById(R.id.triggers).findViewById(R.id.add_item).setTag(Type.TRIGGER);

        restrictionList = (LinearLayout) findViewById(R.id.restrictions).findViewById(R.id.content);
        ((TextView) (findViewById(R.id.restrictions).findViewById(R.id.title))).setText("Restrictions");
        ((Button) (findViewById(R.id.restrictions).findViewById(R.id.add_item))).setText("Add Restriction");
        findViewById(R.id.restrictions).findViewById(R.id.add_item).setOnClickListener(l);
        findViewById(R.id.restrictions).findViewById(R.id.add_item).setTag(Type.RESTRICTIONS);

        commandList = (LinearLayout) findViewById(R.id.commands).findViewById(R.id.content);
        ((TextView) (findViewById(R.id.commands).findViewById(R.id.title))).setText("Commands");
        ((Button) (findViewById(R.id.commands).findViewById(R.id.add_item))).setText("Add TriggerOrCommand");
        findViewById(R.id.commands).findViewById(R.id.add_item).setOnClickListener(l);
        findViewById(R.id.commands).findViewById(R.id.add_item).setTag(Type.COMMAND);

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
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // setOldValues();
        close();
    }

    private String getStringOfTriggers(ArrayList<TriggerOrCommand> triggers) {
        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < triggers.size(); i++) {
            final TriggerOrCommand trigger = triggers.get(i);
            builder.append(trigger.getCategory());
            builder.append(":");
            builder.append(trigger.getValue());

            if (i < (triggers.size() - 1))
                builder.append(",");
        }

        return builder.toString();
    }

    private String getStringOfCommands(ArrayList<TriggerOrCommand> commands) {
        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < commands.size(); i++) {
            final TriggerOrCommand command = commands.get(i);
            builder.append(command.getType());
            builder.append(":");
            builder.append(command.getValue());

            if (i < (commands.size() - 1))
                builder.append(",");
        }

        return builder.toString();
    }

    public void save() {
        // setOldValues();

        profiledb = profileDBHelper.getWritableDatabase();
        if (normalTriggers.size() < 1) {
            toast("You need a trigger device");
        } else {

            String profile_name_string = profile_name.getText().toString();

            if (profile_name_string == null || profile_name_string.length() < 1)
                profile_name_string = "Untitled";

            content_values = new ContentValues();

            content_values.put(ProfileDBHelper.PROFILE_NAME, profile_name_string);
            content_values.put(ProfileDBHelper.TRIGGERS, getStringOfTriggers(normalTriggers));
            content_values.put(ProfileDBHelper.PROHIBITIONS, getStringOfTriggers(prohibitionTriggers));
            content_values.put(ProfileDBHelper.RESTRICTIONS, getStringOfTriggers(restrictionTriggers));
            content_values.put(ProfileDBHelper.COMMANDS, getStringOfCommands(addedCommands));

            Log.e("Content values", content_values.toString());
            if (edit) {
                try {
                    Log.e("write update", "passed");
                    profiledb.update(ProfileDBHelper.PROFILE_TABLE, content_values, ProfileDBHelper.ID + "=" + profileId, null);
                } catch (Exception e) {
                    Log.e("write update", "failed");
                    e.printStackTrace();
                }
            } else {
                try {
                    profiledb.insert(ProfileDBHelper.PROFILE_TABLE, null, content_values);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            close();
            finish();
        }
    }

    public void getOldValues() {

        bluetooth_old_value = adapter.isEnabled() ? 1 : 0;

        wifi_old_value = wifiManager.isWifiEnabled() ? 1 : 0;


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

        if (bluetooth_old_value == 1) {
            if (!adapter.isEnabled())
                adapter.enable();
        } else if (adapter.isEnabled())
            adapter.disable();

        wifiManager.setWifiEnabled(wifi_old_value == 0 ? false : true);


    }

    private void init() {
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    }

    public void getProfileValues(String ID) {
        profiledb = profileDBHelper.getReadableDatabase();
        cursor.moveToPosition(Integer.parseInt(ID));

        //update_profile = cursor.getString(cursor.getColumnIndex(ProfileDBHelper.ID));
        profile_name_text = cursor.getString(cursor.getColumnIndex(ProfileDBHelper.PROFILE_NAME));
        close();

    }

    private void close() {
        try {
            cursor.close();
        } catch (Exception e) {
        }

        try {
            profiledb.close();
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
//                bssid = ((WirelessDevice) wifiListAdapter.getItem(position)).getAddress();
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
//                bssid = ((WirelessDevice) bluetoothListAdapter.getItem(position)).getAddress();
//                bluetoothListAdapter.setSelectedItem(position);
//            }
//        });
//
//        dialog.findViewById(R.id.save_cancel).findViewById(R.id.cancel).setOnClickListener(save_cancel);
//        dialog.findViewById(R.id.save_cancel).findViewById(R.id.save).setOnClickListener(save_cancel);
//        return dialog;
//    }


    private void showTriggerProhibOrRestricCommandDialog(final Type type) {
        final Dialog dialog = getDialog();
        dialog.setContentView(R.layout.trigger_chooser_dialog);
        final LinearLayoutList lll = (LinearLayoutList) dialog.findViewById(R.id.content);
        final TextView title = (TextView) dialog.findViewById(R.id.title);

        switch (type) {
            case TRIGGER:
                title.setText("Add trigger");
                break;
            case PROHIBITION:
                title.setText("Add prohibition");
                break;
            case RESTRICTIONS:
                title.setText("Add restriction");
                break;
            case COMMAND:
                title.setText("Add command");
                break;
        }
        lll.setType(type);
        populateLinearLayoutList(lll);
        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void populateLinearLayoutList(LinearLayoutList lll) {
        try {
            lll.removeAllViews();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<String> strings = availableTriggers;
        switch (lll.type) {
            case RESTRICTIONS:
                strings = availableRestrictions;
                break;
            case PROHIBITION:
                strings = availableProhibitions;
                break;
            case COMMAND:
                strings = availableCommands;
                break;
        }

        for (int i = 0; i < strings.size(); i++) {
            final String item = strings.get(i);


        }
        lll.invalidate();
    }

    private void showCopyFromChooserDialog() {
        final Dialog dialog = new Dialog(this, R.style.CustomDialog);
        //Todo create xml for this
        dialog.setContentView(R.layout.list_view);
        final View view = dialog.findViewById(R.id.a);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });

        dialog.show();
    }

    private void showHelpDialog(String titleText, String help) {
        final Dialog dialog = getDialog();
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

        view.findViewById(R.id.text).setTag("open_" + type + viewCommand);
        view.findViewById(R.id.text).setOnClickListener(tagClickListener);

        view.findViewById(R.id.remove).setTag("remove_" + type + viewCommand);
        view.findViewById(R.id.remove).setOnClickListener(tagClickListener);
        view.setTag(viewCommand);

        ((TextView) view.findViewById(R.id.text)).setText(viewName);

        return view;
    }

    private void removeView(LinearLayout layout, String name) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            final View view = layout.getChildAt(i);
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
        fillInAvailableItems(availableTriggers);
        fillInAvailableItems(availableRestrictions);
        fillInAvailableItems(availableProhibitions);

        if (!edit)
            return;

        final ProfileDBHelper profileDBHelper = new ProfileDBHelper(this);
        final SQLiteDatabase profiledb = profileDBHelper.getReadableDatabase();
        final String[] need = new String[]{ProfileDBHelper.PROFILE_NAME, ProfileDBHelper.ID, ProfileDBHelper.TRIGGERS, ProfileDBHelper.COMMANDS, ProfileDBHelper.PROHIBITIONS, ProfileDBHelper.RESTRICTIONS, ProfileDBHelper.PRIORITY};
        final Cursor cursor = profiledb.query(ProfileDBHelper.PROFILE_TABLE, need, null, null, null, null, null);
        try {
            cursor.moveToPosition(0);
        } catch (Exception e) {
        }

        loop:
        while (!cursor.isAfterLast()) {
            try {
                if ((cursor.getString(cursor.getColumnIndex(ProfileDBHelper.ID)).equals(profileId))) {
                    profile_name.setText(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.PROFILE_NAME)));
                    fillInAddedCommands(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.COMMANDS)));
                    fillInRestrictionTriggers(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.RESTRICTIONS)));
                    fillInProhibitionTriggers(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.PROHIBITIONS)));
                    fillInNormalTriggers(cursor.getString(cursor.getColumnIndex(ProfileDBHelper.TRIGGERS)));
                    break loop;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            cursor.moveToNext();
        }
        try {
            cursor.close();
            profiledb.close();
        } catch (Exception e) {
        }
    }

    private void fillInAddedCommands(String commands) {
        final ArrayList<TriggerOrCommand> setCommands = Utility.getCommands(commands);
        for (int i = 0; i < setCommands.size(); i++) {
            final TriggerOrCommand command = setCommands.get(i);
            //Todo fix this setCommandValue(command.getType(), command.getValue());
        }
    }

    private void fillInRestrictionTriggers(String restrictions) {
        final ArrayList<TriggerOrCommand> setRestrictions = Utility.getTriggers(restrictions);
        for (int i = 0; i < setRestrictions.size(); i++) {
            final TriggerOrCommand trigger = setRestrictions.get(i);
            //Todo  setValue(trigger.getCategory(), trigger.getValue());
        }
    }

    private void fillInProhibitionTriggers(String prohibitions) {
        final ArrayList<TriggerOrCommand> setProhibitions = Utility.getTriggers(prohibitions);
        for (int i = 0; i < setProhibitions.size(); i++) {
            final TriggerOrCommand trigger = setProhibitions.get(i);
            //Todo setValue(trigger.getCategory(), trigger.getValue());
        }
    }

    private void fillInNormalTriggers(String triggers) {
        final ArrayList<TriggerOrCommand> setTriggers = Utility.getTriggers(triggers);
        for (int i = 0; i < setTriggers.size(); i++) {
            final TriggerOrCommand trigger = setTriggers.get(i);
            //Todo setValue(trigger.getCategory(), trigger.getValue());
        }
    }

    private void fillInAvailableItems(ArrayList<String> container) {
        //Todo consider removing ssid/bssid in favor of a checkbox in the dialog for for the bssid or ssid
        if (container == null) return;
        if (container.size() > 0) container.clear();
        container.add("BLUETOOTH");
        container.add("WIFI");
        container.add("BATTERY_TEMPERATURE");
        container.add("BATTERY_CHARGING");
        container.add("NFC");
        container.add("EARPHONE_JACK");
        container.add("DOCK");
        container.add("TIME");
        container.add("LOCATION");
        container.add("APP_LAUNCH");
    }


    private void fillInAvailableCommands() {
        availableCommands.add("WIFI_SETTING");
        availableCommands.add("BLUETOOTH_SETTING");
        availableCommands.add("DATA_SETTING");
        availableCommands.add("BRIGHTNESS_SETTING");
        availableCommands.add("SILENT_MODE_SETTING");
        availableCommands.add("NOTIFICATION_VOLUME_SETTING");
        availableCommands.add("ALARM_VOLUME_SETTING");
        availableCommands.add("MEDIA_CONTROL_SETTING");
        availableCommands.add("LAUNCH_APP_SETTING");
        availableCommands.add("WALLPAPER_SETTING");
        availableCommands.add("RINGTONE_SETTING");
        availableCommands.add("MEDIA_VOLUME_SETTING");
        availableCommands.add("RINGER_VOLUME_SETTING");
        availableCommands.add("AUTO_ROTATION_SETTING");
        availableCommands.add("SLEEP_TIMEOUT_SETTING");
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
