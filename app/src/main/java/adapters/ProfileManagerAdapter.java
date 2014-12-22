package adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.micnubinub.mrautomatic.Profile;
import com.micnubinub.mrautomatic.R;

import java.util.ArrayList;

import tools.Utility;

/**
 * Created by root on 22/08/14.
 */
public class ProfileManagerAdapter extends BaseAdapter {
    private final Context context;
    private ArrayList<Profile> profiles;

    public ProfileManagerAdapter(Context context, ArrayList<Profile> profiles) {
        this.context = context;
        this.profiles = profiles;
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
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        final Profile profile = profiles.get(position);

        if (convertView == null) {
            convertView = View.inflate(context, R.layout.profile_list_item, null);
            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.trigger_icon);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.trigger = (TextView) convertView.findViewById(R.id.trigger);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(profile.getName());
//        holder.icon.setImageDrawable(getDrawable(profile.getTriggerType()));
//        if (profile.getTriggerType().equals(Utility.TRIGGER_BATTERY)) {
//            int i = 0;
//            try {
//                i = Integer.parseInt(profile.getTrigger());
//            } catch (Exception e) {
//                holder.trigger.setText(profile.getTriggerType() + " (" + profile.getTrigger() + ")");
//            }
//
//            if (i < 0)
//                holder.trigger.setText(profile.getTriggerType() + " (Charging)");
//            else
//                holder.trigger.setText(profile.getTriggerType() + " (" + profile.getTrigger() + ")");
//        } else
//            holder.trigger.setText(profile.getTriggerType() + " (" + profile.getTrigger() + ")");

        return convertView;
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
        if (triggerType.equals(Utility.TRIGGER_BATTERY_CHARGING))
            return context.getResources().getDrawable(R.drawable.ic_launcher);
        if (triggerType.equals(Utility.TRIGGER_TIME))
            return context.getResources().getDrawable(R.drawable.ic_launcher);

        //Todo make this equal to nfc
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
