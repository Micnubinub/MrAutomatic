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

    public CustomListView(Context context) {
        view = View.inflate(context, R.layout.custom_list_view, null);
        textView = view.findViewById(R.id.message);
        listView = (ListView) view.findViewById(R.id.list);
    }

    public CustomListView(View v) {
        view = v;
        textView = view.findViewById(R.id.message);
        listView = (ListView) view.findViewById(R.id.list);
    }

    public View getView() {
        return view;
    }


    public void setAdapter(BaseAdapter adapter) {
        if (adapter != null && adapter.getCount() > 0) {
            if (textView != null)
                textView.setVisibility(View.GONE);
            if (listView != null)
                listView.setAdapter(adapter);
        }
    }
}
