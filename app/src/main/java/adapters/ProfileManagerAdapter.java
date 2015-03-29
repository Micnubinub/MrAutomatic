package adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.micnubinub.mrautomatic.EditProfile;
import com.micnubinub.mrautomatic.Profile;
import com.micnubinub.mrautomatic.R;

import java.util.ArrayList;

import tools.TriggerOrCommand;
import tools.Utility;

/**
 * Created by root on 22/08/14.
 */
public class ProfileManagerAdapter extends BaseAdapter {
    private static View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                Intent intent = new Intent(view.getContext(), EditProfile.class);
                intent.putExtra(Utility.EDIT_PROFILE, (String) view.getTag());
                view.getContext().startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private final Context context;
    private ArrayList<Profile> profiles;
    private View.OnClickListener deleteListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Toast.makeText(view.getContext(), "Coming Soon", Toast.LENGTH_LONG).show();
//       Todo delete
            try {
                Utility.deleteProfile(view.getContext(), (String) view.getTag());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public ProfileManagerAdapter(Context context, ArrayList<Profile> profiles) {
        this.context = context;
        this.profiles = profiles;
    }

    private static String getString(Profile profile) {
        final StringBuilder builder = new StringBuilder();
        final ArrayList<TriggerOrCommand> commands = Utility.getCommands(profile.getTriggersOrCommands());
        final ArrayList<TriggerOrCommand> triggers = Utility.getTriggers(profile.getTriggersOrCommands());
        final ArrayList<TriggerOrCommand> restrictions = Utility.getRestrictions(profile.getTriggersOrCommands());
        final ArrayList<TriggerOrCommand> prohibitions = Utility.getProhibitions(profile.getTriggersOrCommands());

        if (triggers.size() > 0) {
            builder.append("Triggers >> ");
            for (TriggerOrCommand trigger : triggers) {
                builder.append(trigger.toString());
                if (triggers.indexOf(trigger) + 1 < triggers.size())
                    builder.append(" , ");
                else builder.append(" ");
            }
        }

        if (restrictions.size() > 0) {
            builder.append("Restrictions >> ");
            for (TriggerOrCommand trigger : restrictions) {
                builder.append(trigger.toString());
                if (restrictions.indexOf(trigger) + 1 < restrictions.size())
                    builder.append(" , ");
                else builder.append("   ");
            }
        }

        if (prohibitions.size() > 0) {
            builder.append("Prohibitions >> ");
            for (TriggerOrCommand trigger : prohibitions) {
                builder.append(trigger.toString());
                if (prohibitions.indexOf(trigger) + 1 < prohibitions.size())
                    builder.append(" , ");
                else builder.append("   ");
            }
        }

        if (commands.size() > 0) {
            builder.append("Commands >> ");
            for (TriggerOrCommand trigger : commands) {
                builder.append(trigger.toString());
                if (commands.indexOf(trigger) + 1 < commands.size())
                    builder.append(" , ");
                else builder.append("   ");
            }
        }

        return builder.toString();
    }

    @Override
    public int getCount() {
        return profiles.size();
    }

    @Override
    public Object getItem(int position) {
        return profiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertVew, ViewGroup parent) {
        final Profile profile = profiles.get(position);

        final View v = View.inflate(context, R.layout.profile_list_item, null);
        // ((ImageView) convertView.findViewById(R.id.trigger_icon)).setImageDrawable(...);
        ((TextView) v.findViewById(R.id.name)).setText(profile.getName());
        final TextView textView = (TextView) v.findViewById(R.id.trigger);
        textView.setText(getString(profile));
        textView.setSelected(true);

        v.findViewById(R.id.delete).setOnClickListener(deleteListener);
        v.findViewById(R.id.delete).setTag(profile.getiD());
        v.setTag(profile.getiD());
        v.setOnClickListener(listener);
        return v;
    }

    private final Drawable getDrawable(String triggerType) {

//        if (triggerType.equals(Utility.TRIGGER_BATTERY))
//            return context.getResources().getDrawable(R.drawable.ic_launcher);
//        if (triggerType.equals(Utility.TRIGGER_WIFI))
//            return context.getResources().getDrawable(R.drawable.ic_launcher);
//        if (triggerType.equals(Utility.TRIGGER_BLUETOOTH))
//            return context.getResources().getDrawable(R.drawable.ic_launcher);
        if (triggerType.equals(Utility.TRIGGER_LOCATION))
            return context.getResources().getDrawable(R.drawable.ic_launcher);
        if (triggerType.equals(Utility.TRIGGER_BATTERY))
            return context.getResources().getDrawable(R.drawable.ic_launcher);
        if (triggerType.equals(Utility.TRIGGER_TIME))
            return context.getResources().getDrawable(R.drawable.ic_launcher);
        return context.getResources().getDrawable(R.drawable.fab);
    }

    public void setProfiles(ArrayList<Profile> profiles) {
        this.profiles = profiles;
        getCount();
    }

    private static class ViewHolder {
        public TextView name;
        public ImageView icon;
        public TextView trigger;
    }

}
