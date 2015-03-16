package tools;

import android.content.Context;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.micnubinub.mrautomatic.R;

/**
 * Created by Michael on 3/11/2015.
 */
public class CustomListView {
    private View view, textView;
    private ListView listView;
    private BaseAdapter adapter;

    public CustomListView(Context context) {
        view = View.inflate(context, R.layout.custom_device_picker_list_view, null);
        textView = view.findViewById(R.id.message);
        listView = (ListView) view.findViewById(R.id.list);
    }

    public CustomListView(View v) {
        view = v;
        textView = view.findViewById(R.id.message);
        listView = (ListView) view.findViewById(R.id.list);
    }

    public BaseAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(BaseAdapter adapter) {
        if (listView != null)
            listView.setAdapter(adapter);
        this.adapter = adapter;
        refresh();
    }

    public View getView() {
        return view;
    }

    public void setOnItemClickListener(ListView.OnItemClickListener onItemClickListener) {
        if (listView != null)
            listView.setOnItemClickListener(onItemClickListener);
    }

    public void refresh() {
        if (adapter != null && adapter.getCount() > 0) {
            if (textView != null)
                textView.setVisibility(View.GONE);

        } else {
            if (textView != null)
                textView.setVisibility(View.VISIBLE);
        }
    }
}
