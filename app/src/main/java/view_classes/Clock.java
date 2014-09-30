package view_classes;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.micnubinub.mrautomatic.R;

import tools.Tools;

/**
 * Created by root on 20/08/14.
 */
public class Clock extends View {
    public static final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static int seconds = 0;
    public static int minutes = 0;
    public static int hours = 0;
    public static int width, height, r, cx, cy;
    public static boolean size_changed_update = false;
    public static boolean draw = true;
    public static int[] secondPoints, minutePoints, hourPoints;
    public static Context context;
    public static Resources res;
    public static int[] colors;

    public Clock(Context context) {
        super(context);
    }

    public Clock(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static void setTime() {
        //Todo use the current time to set the angle of the hands
        long time = System.currentTimeMillis();
        setSeconds(Tools.getSeconds(time));
        setMinutes(Tools.getMinutes(time));
        setHours(Tools.getHours(time));
    }

    public static int getSeconds() {
        return seconds;
    }

    public static void setSeconds(int seconds) {
        Clock.seconds = (int) (((float) seconds / 60f) * 360f);
    }

    public static int getMinutes() {
        return minutes;
    }

    public static void setMinutes(int minutes) {
        Clock.minutes = (int) (((float) minutes / 60f) * 360f);
    }

    public static int getHours() {
        return hours;
    }

    public static void setHours(int hours) {
        Clock.hours = (int) (((float) hours / 12f) * 360f);
    }

    public static int getRadius() {
        return r;
    }

    public static void setRadius(int r) {
        Clock.r = r;
    }

    public static int getCx() {
        return cx;
    }

    public static void setCx(int cx) {
        Clock.cx = cx;
    }

    public static int getCy() {
        return cy;
    }

    public static void setCy(int cy) {
        Clock.cy = cy;
    }

    public static void getColors() {
        colors = new int[]{R.color.dark_orange,
                R.color.med,
                R.color.orange,
                R.color.yellow,
                R.color.lime,
                R.color.high,
                R.color.green,
                R.color.light_blue,
                R.color.extra_high,
                R.color.blue,
                R.color.purple,
                R.color.pink,
                R.color.red,
                R.color.low,
                R.color.white,
                R.color.ultra_light_grey,
                R.color.light_grey,
                R.color.lite_grey,
                R.color.shade,
                R.color.divider,
                R.color.dark_grey_text,
                R.color.dark_grey,
                R.color.dark_dark_grey,
                R.color.black};
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        size_changed_update = true;
        super.onSizeChanged(w, h, oldw, oldh);

    }

    public boolean isSize_changed_update() {
        return size_changed_update;
    }

    public void setSize_changed_update(boolean size_changed_update) {
        this.size_changed_update = size_changed_update;
    }

    public boolean isDraw() {
        return draw;
    }

    public void setDraw(boolean draw) {
        this.draw = draw;
    }

    public void showThemer() {

    }

    public void sizeChanged() {
    }

    public int[] calculateLinePoints(int distance, int angle) {

        int[] nums = new int[2];
        nums[0] = r + (int) (Math.cos(Math.toRadians(angle)) * distance);
        nums[1] = r + (int) (Math.sin(Math.toRadians(angle)) * distance);
        return nums;
    }
}
