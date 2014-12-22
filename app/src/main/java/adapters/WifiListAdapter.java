package adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.micnubinub.mrautomatic.R;

import java.util.ArrayList;
import java.util.List;

import tools.WirelessDevice;

/**
 * Created by root on 25/08/14.
 */
public class WifiListAdapter extends BaseAdapter {
    //Todo unregister receivers inondestroy
    private final Context context;
    private final ArrayList<WirelessDevice> devices = new ArrayList<WirelessDevice>();
    private final WifiManager manager;
    private List<ScanResult> wifi_scan_results;
    private int selected_item = -1;

    public WifiListAdapter(Context context) {
        super();
        this.context = context;
        manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        context.registerReceiver(receiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        turnWifiOn();
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
            convertView = View.inflate(context, R.layout.two_line_list, null);
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


    private boolean turnWifiOn() {

        if (manager == null)
            return false;

        if (!manager.isWifiEnabled())
            manager.setWifiEnabled(true);

        return true;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        getCount();
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

    private static class ViewHolder {
        TextView name;
        TextView bssid;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                context.unregisterReceiver(receiver);
                //Todo show that the device is currently scanning
                manager.startScan();
                wifi_scan_results = manager.getScanResults();
                registerReceiver();
            }
        }
    };


    private final BroadcastReceiver scanCompleteReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                context.unregisterReceiver(scanCompleteReceiver);

                for (int i = 0; i < wifi_scan_results.size(); i++) {
                    devices.add(new WirelessDevice(wifi_scan_results.get(i).SSID.toString(), wifi_scan_results.get(i).BSSID.toString()));
                }
                notifyDataSetChanged();
            }
        }
    };


}
