package adapters;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.micnubinub.mrautomatic.EditProfile;
import com.micnubinub.mrautomatic.R;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import tools.CustomListView;
import tools.Device;

/**
 * Created by root on 25/08/14.
 */
public class BluetoothListAdapter extends BaseAdapter {
    private final Context context;
    private final CustomListView customListView;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                final Device d = new Device(device.getName(), device.getAddress());
                if (!devicesContains(d))
                    devices.add(d);
                try {
                    notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e("BluetoothAdapter :", e.toString());
                }

                if (customListView.getAdapter() == BluetoothListAdapter.this) {
                    if (customListView != null)
                        customListView.refresh();
                } else {
                    customListView.setAdapter(BluetoothListAdapter.this);
                }
            }
        }
    };
    private final ArrayList<Device> devices = new ArrayList<Device>();
    private final BluetoothAdapter adapter = EditProfile.adapter;
    private final BroadcastReceiver initReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                if (adapter.isEnabled()) {
                    startScan();
                    context.unregisterReceiver(this);
                } else {
                    turnBluetoothOn();
                }
            }
        }
    };
    private int selected_item;

    public BluetoothListAdapter(Context context, CustomListView customListView) {
        this.context = context;
        this.customListView = customListView;
        customListView.setAdapter(this);
        customListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selected_item = i;
            }
        });

        if (!adapter.isEnabled()) {
            context.registerReceiver(initReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            turnBluetoothOn();
        } else {
            startScan();
        }

    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public Device getSelectedDevice() {
        return devices.get(selected_item);
    }

    @Override
    public View getView(int position, View converiew, ViewGroup parent) {
        final Device device = devices.get(position);
        final View view = View.inflate(context, R.layout.two_line_list, null);
        ((TextView) view.findViewById(R.id.primary)).setText(device.getName());
        ((TextView) view.findViewById(R.id.secondary)).setText(device.getAddress());

        if (position == selected_item)
            view.setBackgroundColor(context.getResources().getColor(R.color.view_click_selector));
        else
            view.setBackgroundColor(0);

        return view;
    }

    private void startScan() {
        if ((adapter == null) && turnBluetoothOn()) {

            try {
                registerReceiver();
                adapter.startDiscovery();
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        cancelScan();
                    }
                }, 12000);

                for (BluetoothDevice device : adapter.getBondedDevices()) {
                    devices.add(new Device(device.getName(), device.getAddress()));
                    notifyDataSetChanged();
                }
            } catch (Exception e) {
            }
        } else {
            Toast.makeText(context, "Failed to scan, try again in 5 seconds", Toast.LENGTH_LONG).show();
        }

    }

    private boolean turnBluetoothOn() {

        if (adapter == null)
            return false;

        if (!adapter.isEnabled())
            adapter.enable();

        return true;
    }

    public void cancelScan() {
        try {
            if (adapter != null)
                adapter.cancelDiscovery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        unRegisterReceiver();
        try {
            context.unregisterReceiver(initReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unRegisterReceiver() {
        try {
            context.unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerReceiver() {
        try {
            context.registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        } catch (Exception e) {
        }
    }

    public int getSelectedItem() {
        return selected_item;
    }

    public void setSelectedItem(int selected_item) {
        this.selected_item = selected_item;
        notifyDataSetChanged();
    }

    private boolean devicesContains(Device device) {
        for (int i = 0; i < devices.size(); i++) {
            final Device d = devices.get(i);
            if (d.equals(device))
                return true;
        }
        return false;
    }
}
