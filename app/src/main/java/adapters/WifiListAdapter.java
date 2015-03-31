package adapters;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.micnubinub.mrautomatic.R;

import java.util.ArrayList;
import java.util.List;

import tools.CustomListView;
import tools.Device;

/**
 * Created by root on 25/08/14.
 */
public class WifiListAdapter extends BaseAdapter {
    private final Context context;
    private final CustomListView customListView;
    private final ArrayList<Device> devices = new ArrayList<Device>();
    private final WifiManager manager;
    private final BroadcastReceiver initReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                context.unregisterReceiver(this);
                startScan();
            }
        }
    };
    private List<ScanResult> wifi_scan_results;
    private final BroadcastReceiver scanCompleteReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                wifi_scan_results = manager.getScanResults();
                context.unregisterReceiver(this);

                for (int i = 0; i < wifi_scan_results.size(); i++) {
                    final Device device = new Device(wifi_scan_results.get(i).SSID.toString(), wifi_scan_results.get(i).BSSID.toString());
                    if (!devicesContains(device))
                        devices.add(device);
                }

                try {
                    notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e("WifiAdapter :", e.toString());
                }

                if (customListView.getAdapter() == WifiListAdapter.this) {
                    if (customListView != null)
                        customListView.refresh();
                } else {
                    customListView.setAdapter(WifiListAdapter.this);
                }
            }
        }
    };
    private int selected_item = 0;

    public WifiListAdapter(Context context, CustomListView customListView) {
        this.context = context;
        this.customListView = customListView;
        customListView.setAdapter(this);
        manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        customListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selected_item = i;
                notifyDataSetChanged();
            }
        });

        if (!manager.isWifiEnabled()) {
            context.registerReceiver(initReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            turnWifiOn();
        } else {
            startScan();
        }
    }

    private void startScan() {
        if ((manager != null) && turnWifiOn()) {
            try {
                registerReceiver();
                manager.startScan();
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    public Device getSelectedDevice() {
        return devices.get(selected_item);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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

    private boolean turnWifiOn() {

        if (manager == null)
            return false;

        if (!manager.isWifiEnabled())
            manager.setWifiEnabled(true);

        return manager.isWifiEnabled();
    }

    public void cancelScan() {
        unRegisterReceiver();
        try {
            context.unregisterReceiver(initReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unRegisterReceiver() {
        try {
            context.unregisterReceiver(scanCompleteReceiver);
        } catch (Exception e) {
        }
    }

    public void registerReceiver() {
        try {
            context.registerReceiver(scanCompleteReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
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
