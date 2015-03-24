package tools;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.micnubinub.mrautomatic.EditProfile;
import com.micnubinub.mrautomatic.R;

import java.util.ArrayList;

/**
 * Created by Michael on 3/11/2015.
 */
public class LinearLayoutList extends LinearLayout {
    private final OnClickListener scrollViewListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.delete:
                    remove((String) view.getTag());
                    break;
                case R.id.open:
                    final TriggerOrCommand t = getItemUsingCategory((String) view.getTag());
                    final EditProfile ed = EditProfile.editProfile;
                    if (ed != null)
                        ed.showEditorDialog(t.getType(), t.getCategory());
                    break;
            }
        }
    };
    public TriggerOrCommand.Type type;
    private OnClickListener stringListener;
    private ArrayList<Object> items = new ArrayList<>();

    public LinearLayoutList(Context context) {
        super(context);
    }

    public LinearLayoutList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void setOnItemClickListener(OnClickListener l) {
        //Todo implement in all instances
        this.stringListener = l;
        stringListener = l;
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setOnClickListener(l);
        }
    }

    public TriggerOrCommand.Type getType() {
        return type;
    }

    public void setType(TriggerOrCommand.Type type) {
        this.type = type;
    }

    public void add(TriggerOrCommand triggerOrCommand) {
        if (triggerOrCommand == null)
            return;
        if (items == null)
            items = new ArrayList<>();

        if (!containsCategory(triggerOrCommand)) {
            items.add(triggerOrCommand);
        } else {
            final TriggerOrCommand item = getItemUsingCategory(triggerOrCommand);
            final int i = items.indexOf(item);
            items.remove(item);
            items.add(i, triggerOrCommand);
        }

        getViews();
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
        removeAllViews();
        for (int i = 0; i < items.size(); i++) {
            final Object triggerOrCommandOrAvailable = items.get(i);
            View v = null;
            if (triggerOrCommandOrAvailable instanceof TriggerOrCommand) {
                final TriggerOrCommand t = (TriggerOrCommand) triggerOrCommandOrAvailable;
                Utility.getIcon(((TriggerOrCommand) triggerOrCommandOrAvailable).getCategory());
                v = View.inflate(getContext(), R.layout.command_list_item, null);
                v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                final View delete = v.findViewById(R.id.delete);
                final View open = v.findViewById(R.id.open);

                ((ImageView) open.findViewById(R.id.icon)).setImageResource(Utility.getIcon(t.getCategory()));
                ((TextView) open.findViewById(R.id.primary)).setText(Utility.getTriggerOrCommandName(t.getCategory()));
                ((TextView) open.findViewById(R.id.secondary)).setText(EditProfile.getDisplayString(t.getCategory(), t.getValue()));
                open.findViewById(R.id.secondary).setSelected(true);
                delete.setTag(t.getCategory());
                open.setTag(t.getCategory());
                delete.setOnClickListener(scrollViewListener);
                open.setOnClickListener(scrollViewListener);

            } else if (triggerOrCommandOrAvailable instanceof String) {
                v = View.inflate(getContext(), R.layout.command_item, null);
                ((TextView) v).setText(Utility.getTriggerOrCommandName((String) triggerOrCommandOrAvailable));
                if (stringListener != null)
                    v.setOnClickListener(stringListener);
            }

            if (v == null)
                return;

            addView(v);
        }

    }

    public boolean containsCategory(TriggerOrCommand triggerOrCommand) {
        return containsCategory(triggerOrCommand.getCategory());
    }

    public boolean containsCategory(String category) {
        for (int i = 0; i < items.size(); i++) {
            if (((TriggerOrCommand) items.get(i)).getCategory().equals(category))
                return true;
        }
        return false;
    }

    public TriggerOrCommand getItemUsingCategory(TriggerOrCommand triggerOrCommand) {
        return getItemUsingCategory(triggerOrCommand.getCategory());
    }

    public TriggerOrCommand getItemUsingCategory(String category) {
        for (int i = 0; i < items.size(); i++) {
            final TriggerOrCommand item = ((TriggerOrCommand) items.get(i));
            if (item.getCategory().equals(category))
                return item;
        }
        return null;
    }

    public int getCount() {
        return items == null ? 0 : items.size();
    }

    public ArrayList<TriggerOrCommand> getItems() {
        if (items == null)
            items = new ArrayList<>();
        final ArrayList<TriggerOrCommand> triggerOrCommands = new ArrayList<>(items.size());
        for (int i = 0; i < items.size(); i++) {
            triggerOrCommands.add((TriggerOrCommand) items.get(i));
        }
        return triggerOrCommands;
    }

    public void setItems(ArrayList<String> items) {
        this.items = new ArrayList<>();
        for (String item : items) {
            this.items.add(item);
        }
        getViews();
    }
}
