package tools;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.micnubinub.mrautomatic.R;

import java.util.ArrayList;

/**
 * Created by Michael on 3/11/2015.
 */
public class LinearLayoutList extends LinearLayout {
    public TriggerOrCommand.Type type;
    private OnClickListener listener;
    private ArrayList<Object> items = new ArrayList<>();

    public LinearLayoutList(Context context) {
        super(context);
    }

    public LinearLayoutList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

//    public void editItem(int pos) {
//        if (pos < 0 || items == null || pos >= items.size())
//            return;
//        final TriggerOrCommand triggerOrCommand = items.get(pos);
//    }


    public void setOnItemClickListener(OnClickListener l) {
        //Todo implement in all instances
        listener = l;
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setOnClickListener(l);
        }
    }

    public void setItems(ArrayList<String> items) {
        this.items = new ArrayList<>();
        for (String item : items) {
            this.items.add(item);
        }
        getViews();
    }

    public TriggerOrCommand.Type getType() {
        return type;
    }

    public void setType(TriggerOrCommand.Type type) {
        this.type = type;
    }

    public void add(TriggerOrCommand triggerOrCommand) {
        if (items == null)
            items = new ArrayList<>();

        if (!(items.contains(triggerOrCommand))) {
            items.add(triggerOrCommand);
            getViews();
        }
    }

    public void remove(String category) {
        if (items == null || items.size() < 1 || !(items.get(0) instanceof TriggerOrCommand))
            return;

        for (int i = 0; i < items.size(); i++) {
            final TriggerOrCommand triggerOrCommand = (TriggerOrCommand) items.get(i);
            if (triggerOrCommand.getCategory().equals(category)) {
                items.remove(i);
                getViews();
            }
        }
    }

    public void remove(TriggerOrCommand triggerOrCommand) {
        if (items != null && (items.contains(triggerOrCommand))) {
            items.remove(triggerOrCommand);
            getViews();
        }
    }

    public void remove(int triggerOrCommandOrAvailable) {
        if (items != null && (items.size() > triggerOrCommandOrAvailable)) {
            items.remove(triggerOrCommandOrAvailable);
            getViews();
        }
    }

    public String getCommandOrTrigger(View view) {
        final int index = indexOfChild(view);
        if (items == null || index < 0 || index >= items.size())
            return null;

        final Object o = items.get(index);
        if (o instanceof TriggerOrCommand)
            return ((TriggerOrCommand) o).getCategory();
        else if (o instanceof String)
            return (String) o;

        return null;
    }

    private void getViews() {
        //Todo
        removeAllViews();
        for (int i = 0; i < items.size(); i++) {
            final Object triggerOrCommandOrAvailable = items.get(i);
            final TextView v = (TextView) View.inflate(getContext(), R.layout.command_item, null);
            if (triggerOrCommandOrAvailable instanceof TriggerOrCommand)
                v.setText(Utility.getTriggerOrCommandName(((TriggerOrCommand) triggerOrCommandOrAvailable).getCategory()));
            else if (triggerOrCommandOrAvailable instanceof String)
                v.setText(Utility.getTriggerOrCommandName((String) triggerOrCommandOrAvailable));
            v.setTag(i);
            v.setOnClickListener(listener);
            addView(v);
        }
    }

}
