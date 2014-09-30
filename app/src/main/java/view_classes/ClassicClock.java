package view_classes;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.micnubinub.mrautomatic.R;


/**
 * Created by root on 14/08/14.
 */
public class ClassicClock extends Clock {

    private int hour_hand_color,
            minute_hand_color,
            second_hand_color,
            big_circle_color;
    private int small_line_color,
            big_line_color;
    private float small_line_length = 0.08f,
            small_bottom_circle_radius = 0.07f,
            small_top_circle_radius = 0.05f,
            hour_hand_length = 0.5f,
            big_line_length = 1.3f,
            minute_hand_length = 0.7f,
            second_hand_length = 0.85f,
            hour_hand_width = 0.06f,
            minute_hand_width = 0.04f,
            second_hand_width = 0.02f,
            small_line_width = 0.01f,
            big_line_width = 0.02f;
    private int[] secondPoints, minutePoints, hourPoints;


    public ClassicClock(Context context) {
        super(context);
        init(context);
    }

    public ClassicClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context) {
        this.context = context;
        res = context.getResources();
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeCap(Paint.Cap.ROUND);

        second_hand_color = res.getColor(R.color.red);
        minute_hand_color = res.getColor(R.color.white);
        hour_hand_color = res.getColor(R.color.white);
        big_circle_color = res.getColor(R.color.dark_grey);
        small_line_color = res.getColor(R.color.white);
        big_line_color = res.getColor(R.color.white);
        getColors();
        //Todo
    }


    private void setStrokeWidth(int width) {
        this.paint.setStrokeWidth(width);
    }

    private void drawLines(Canvas canvas, int l) {
        int[] points;
        for (int i = 0; i < 360; i += 6) {
            if (i % 30 == 0) {
                paint.setColor(big_line_color);
                points = calculateStaticLinePoints((int) ((float) l * big_line_length), i);
                paint.setStrokeWidth((int) ((float) r * big_line_width));
            } else {
                paint.setColor(small_line_color);
                points = calculateStaticLinePoints(l, i);
                paint.setStrokeWidth((int) ((float) r * small_line_width));
            }
            canvas.drawLine(points[0], points[1], points[2], points[3], paint);
        }

    }

    private int[] calculateStaticLinePoints(int l, int angle) {
        int[] lines = new int[4];
        int[] nums = calculateLinePoints(r - l, angle);
        lines[0] = nums[0];
        lines[1] = nums[1];

        nums = calculateLinePoints(r, angle);
        lines[2] = nums[0];
        lines[3] = nums[1];

        return lines;
    }

    private void drawNormalClock(Canvas canvas) {

        setTime();


        //Todo draw shadows big shot level maximum
        if (size_changed_update) {
            width = canvas.getWidth();
            height = canvas.getHeight();
            r = Math.min(height, width) / 2;
            cx = width / 2;
            cy = height / 2;
            size_changed_update = false;
        }

        paint.setColor(big_circle_color);
        canvas.drawCircle(cx, cy, r, paint);


        drawLines(canvas, (int) ((float) r * small_line_length));

        paint.setColor(hour_hand_color);
        canvas.drawCircle(cx, cy, (int) ((float) r * small_bottom_circle_radius), paint);
        setStrokeWidth((int) ((float) r * hour_hand_width));
        hourPoints = calculateLinePoints((int) ((float) r * hour_hand_length), hours);
        canvas.drawLine(cx, cy, hourPoints[0], hourPoints[1], paint);

        paint.setColor(minute_hand_color);
        setStrokeWidth((int) ((float) r * minute_hand_width));
        minutePoints = calculateLinePoints((int) ((float) r * minute_hand_length), minutes);
        canvas.drawLine(cx, cy, minutePoints[0], minutePoints[1], paint);

        paint.setColor(second_hand_color);
        secondPoints = calculateLinePoints((int) ((float) r * second_hand_length), seconds);
        setStrokeWidth((int) ((float) r * second_hand_width));
        canvas.drawLine(cx, cy, secondPoints[0], secondPoints[1], paint);
        canvas.drawCircle(cx, cy, (int) ((float) r * small_top_circle_radius), paint);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawNormalClock(canvas);

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        size_changed_update = true;
        super.onSizeChanged(w, h, oldw, oldh);

    }

    public void setSizeChangedUpdate(boolean size_changed_update) {
        this.size_changed_update = size_changed_update;
    }

    public int getHour_hand_color() {
        return hour_hand_color;
    }

    public void setHour_hand_color(int hour_hand_color) {
        this.hour_hand_color = hour_hand_color;
    }

    public int getMinute_hand_color() {
        return minute_hand_color;
    }

    public void setMinute_hand_color(int minute_hand_color) {
        this.minute_hand_color = minute_hand_color;
    }

    public int getSecond_hand_color() {
        return second_hand_color;
    }

    public void setSecond_hand_color(int second_hand_color) {
        this.second_hand_color = second_hand_color;
    }

    public int getBig_circle_color() {
        return big_circle_color;
    }

    public void setBig_circle_color(int big_circle_color) {
        this.big_circle_color = big_circle_color;
    }

    public int getSmall_line_color() {
        return small_line_color;
    }

    public void setSmall_line_color(int small_line_color) {
        this.small_line_color = small_line_color;
    }

    public int getBig_line_color() {
        return big_line_color;
    }

    public void setBig_line_color(int big_line_color) {
        this.big_line_color = big_line_color;
    }

    public float getSmall_line_length() {
        return small_line_length;
    }

    public void setSmall_line_length(float small_line_length) {
        this.small_line_length = small_line_length;
    }

    public float getSmall_bottom_circle_radius() {
        return small_bottom_circle_radius;
    }

    public void setSmall_bottom_circle_radius(float small_bottom_circle_radius) {
        this.small_bottom_circle_radius = small_bottom_circle_radius;
    }

    public float getSmall_top_circle_radius() {
        return small_top_circle_radius;
    }

    public void setSmall_top_circle_radius(float small_top_circle_radius) {
        this.small_top_circle_radius = small_top_circle_radius;
    }

    public float getHour_hand_length() {
        return hour_hand_length;
    }

    public void setHour_hand_length(float hour_hand_length) {
        this.hour_hand_length = hour_hand_length;
    }

    public float getBig_line_length() {
        return big_line_length;
    }

    public void setBig_line_length(float big_line_length) {
        this.big_line_length = big_line_length;
    }

    public float getMinute_hand_length() {
        return minute_hand_length;
    }

    public void setMinute_hand_length(float minute_hand_length) {
        this.minute_hand_length = minute_hand_length;
    }

    public float getSecond_hand_length() {
        return second_hand_length;
    }

    public void setSecond_hand_length(float second_hand_length) {
        this.second_hand_length = second_hand_length;
    }

    public float getHour_hand_width() {
        return hour_hand_width;
    }

    public void setHour_hand_width(float hour_hand_width) {
        this.hour_hand_width = hour_hand_width;
    }

    public float getMinute_hand_width() {
        return minute_hand_width;
    }

    public void setMinute_hand_width(float minute_hand_width) {
        this.minute_hand_width = minute_hand_width;
    }

    public float getSecond_hand_width() {
        return second_hand_width;
    }

    public void setSecond_hand_width(float second_hand_width) {
        this.second_hand_width = second_hand_width;
    }

    public float getSmall_line_width() {
        return small_line_width;
    }

    public void setSmall_line_width(float small_line_width) {
        this.small_line_width = small_line_width;
    }

    public float getBig_line_width() {
        return big_line_width;
    }

    public void setBig_line_width(float big_line_width) {
        this.big_line_width = big_line_width;
    }


    public void setSize_changed_update(boolean size_changed_update) {
        this.size_changed_update = size_changed_update;
    }

}
