package tools;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by Michael on 3/11/2015.
 */
public class LinearLayoutList extends LinearLayout {
    private final OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case 2:

                    break;
                case 3:
                    remove((int) v.getTag());
                    break;
            }
        }
    };
    //Todo use this in the app
    private ArrayList<TriggerOrCommand> items = new ArrayList<>();


    public LinearLayoutList(Context context) {
        super(context);
    }

    public LinearLayoutList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LinearLayoutList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void editItem(int pos) {
        if (pos < 0 || items == null || pos >= items.size())
            return;
        final TriggerOrCommand triggerOrCommand = items.get(pos);
    }

    public void setItems(ArrayList<TriggerOrCommand> items) {
        this.items = items;
    }

    public void add(TriggerOrCommand triggerOrCommand) {
        if (items != null && !(items.contains(triggerOrCommand))) {
            items.add(triggerOrCommand);
            getViews();
        }
    }

    public void remove(TriggerOrCommand triggerOrCommand) {
        if (items != null && (items.contains(triggerOrCommand))) {
            items.remove(triggerOrCommand);
            getViews();
        }
    }

    public void remove(int triggerOrCommand) {
        if (items != null && (items.size() > triggerOrCommand)) {
            items.remove(triggerOrCommand);
            getViews();
        }
    }

    private void getViews() {
        //Todo
        removeAllViews();
        for (int i = 0; i < items.size(); i++) {
            final TriggerOrCommand triggerOrCommand = items.get(i);
            final View v = inflate(getContext(), 2, null);
            v.setTag(i);
            v.findViewById(3).setOnClickListener(listener);

        }
    }

}
