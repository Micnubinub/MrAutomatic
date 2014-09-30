package adapters;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.micnubinub.mrautomatic.R;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import tools.WirelessDevice;

/**
 * Created by root on 25/08/14.
 */
public class BluetoothListAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<WirelessDevice> devices = new ArrayList<WirelessDevice>();
    private final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                devices.add(new WirelessDevice(device.getName(), device.getAddress()));
            }
        }
    };
    private int selected_item = -1;

    public BluetoothListAdapter(Context context) {
        super();
        this.context = context;
        context.registerReceiver(initReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        turnBluetoothOn();
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        WirelessDevice device = devices.get(position);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.material_two_line_list, null);
            holder.name = (TextView) convertView.findViewById(R.id.primary);
            holder.bssid = (TextView) convertView.findViewById(R.id.secondary);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        try {
            holder.bssid.setText(device.getBssid());
            holder.name.setText(device.getName());
        } catch (Exception e) {
        }


        if (position == selected_item)
            convertView.setBackgroundColor(context.getResources().getColor(R.color.view_click_selector));
        else
            convertView.setBackgroundColor(0);

        return convertView;
    }

    private void getDiscoverableDevices() {
        if (adapter == null)
            if (turnBluetoothOn()) {

                try {
                    adapter.startDiscovery();
                    registerReceiver();
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            adapter.cancelDiscovery();
                        }
                    }, 12000);

                    for (BluetoothDevice device : adapter.getBondedDevices()) {
                        devices.add(new WirelessDevice(device.getName(), device.getAddress()));
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

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        getCount();
    }

    public void unRegisterReceiver() {
        try {
            context.unregisterReceiver(receiver);
        } catch (Exception e) {
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

    private static class ViewHolder {
        TextView name;
        TextView bssid;
    }

    private final BroadcastReceiver initReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                if (adapter.isEnabled()) {
                    getDiscoverableDevices();
                    context.unregisterReceiver(initReceiver);
                } else {
                    turnBluetoothOn();
                }
            }
        }
    };
}
