package adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.micnubinub.mrautomatic.R;

import java.util.ArrayList;

import tools.App;
import tools.Utility;

/**
 * Created by Michael on 3/23/2015.
 */
public class AppAdapter extends BaseAdapter {
    static int selectedItem = 0;
    private final ListView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            selectedItem = i;
            try {
                notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    final ArrayList<App> apps;
    final Context context;


    public AppAdapter(ListView listView, Context context) {
        this.context = context;
        this.apps = Utility.getApps(context);
        listView.setAdapter(this);
        listView.setOnItemClickListener(listener);
    }

    @Override
    public int getCount() {
        return apps == null ? 0 : apps.size();
    }

    @Override
    public Object getItem(int i) {
        return apps.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final App app = apps.get(i);
        final View v = View.inflate(context, R.layout.app_launch_list_item, null);
        v.setBackgroundResource((i == selectedItem) ? R.drawable.button_pressed : R.drawable.button_normal);
        ((ImageView) v.findViewById(R.id.icon)).setImageDrawable(app.getIcon());
        ((TextView) v.findViewById(R.id.app_name)).setText(app.getName());
        return v;
    }

    public App getSelectedApp() {
        return ((apps == null) || (selectedItem < 0) || (selectedItem >= apps.size())) ? null : apps.get(selectedItem);
    }
}
