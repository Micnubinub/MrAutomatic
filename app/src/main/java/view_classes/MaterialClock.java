package view_classes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.micnubinub.mrautomatic.R;


/**
 * Created by root on 14/08/14.
 */
public class MaterialClock extends Clock {

    //  private static final Paint shadow = new Paint();
    private int cx_second, cy_second,
            cx_hour, cy_hour,
            cx_minute, cy_minute,
            hour_circle_color,
            minute_circle_color,
            second_circle_color,
            hour_circle_center,
            minute_circle_center,
            second_circle_center,
            big_circle_color,
            hour_circle_radius,
            minute_circle_radius,
            second_circle_radius;

    private float
            hour_circle_fraction = 0.1f,
            minute_circle_fraction = 0.09f,
            second_circle_fraction = 0.08f;
    private int[] secondPoints, minutePoints, hourPoints;
    private boolean animating_start_up = false, animating_on_notifiation_received = false;
    private boolean size_changed_update = true;

    /*
        private OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                showThemer();
            }
        };
        private OnClickListener themerOnClick = new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {

                    case R.id.lines:

                        break;
                    case R.id.circle:

                        break;
                    case R.id.hour:

                        break;

                    case R.id.second:

                        break;

                    case R.id.minute:

                        break;


                }
            }
        };

    */
    public MaterialClock(Context context) {
        super(context);
        init(context);
    }

    public MaterialClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public static void unregisterClockThemerListener(ClassicClock clock) {
        clock.setOnClickListener(null);
        clock.setClickable(false);
    }

    private static void setUpHours(View view) {


    }

    private static void setUpMinutes(View view) {


    }

    public void sizeChanged(Canvas canvas) {
        width = canvas.getWidth();
        height = canvas.getHeight();
        r = Math.min(height, width) / 2;
        cx = width / 2;
        cy = height / 2;
        size_changed_update = false;
        hour_circle_radius = (int) (r * hour_circle_fraction);
        minute_circle_radius = (int) (r * minute_circle_fraction);
        second_circle_radius = (int) (r * second_circle_fraction);
        hour_circle_center = r - (hour_circle_radius / 2);
        minute_circle_center = r - hour_circle_radius - (minute_circle_radius / 2);
        second_circle_center = r - hour_circle_radius - minute_circle_radius - (second_circle_radius / 2);

    }

    private void drawNormalClock(Canvas canvas) {
        setTime();
        //Todo draw shadows big shot level maximum
        if (size_changed_update) {
            sizeChanged(canvas);
        }

        paint.setColor(big_circle_color);
        canvas.drawCircle(cx, cy, r, paint);


        //drawCircles(canvas);

        paint.setColor(hour_circle_color);
        hourPoints = calculateCenterPoints(hour_circle_center, hours);
        canvas.drawCircle(hourPoints[0], hourPoints[1], hour_circle_radius, paint);

        paint.setColor(minute_circle_color);
        minutePoints = calculateCenterPoints(minute_circle_center, minutes);
        canvas.drawCircle(minutePoints[0], minutePoints[1], minute_circle_radius, paint);

        paint.setColor(second_circle_color);
        secondPoints = calculateCenterPoints(second_circle_center, seconds);
        canvas.drawCircle(secondPoints[0], secondPoints[1], second_circle_radius, paint);
    }

    private int[] calculateCenterPoints(int l, int angle) {
        //Todo calc stopx[0], stopy[1];
        angle = ((angle - 90) % 360);
        int[] nums = new int[2];
        nums[0] = r + (int) (Math.cos(Math.toRadians(angle)) * l);
        nums[1] = r + (int) (Math.sin(Math.toRadians(angle)) * l);
        return nums;
    }

    private void setUpSeconds(View view) {


    }

    /*
        private void setUpLines(View view) {
            //  RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.);
            final EditText value = (EditText) view.findViewById(R.id.value);
            view.findViewById(R.id.reset).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Todo so a switch of radio buttons, and add stroke width
                    value.setText(String.valueOf(big_line_length));
                }
            });


        }

        private void setUpCircle(View view) {
            //  GridView gridView = (GridView) view.findViewById(R.id.circle_grid).findViewById(R.id.color_grid);
            //  gridView.setAdapter(new ColorAdapter(context, colors));
        }
    */
    private void init(Context context) {
        this.context = context;
        this.res = context.getResources();
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setStrokeCap(Paint.Cap.ROUND);
        //this.setOnClickListener(listener);
        second_circle_color = res.getColor(R.color.red);
        minute_circle_color = res.getColor(R.color.white);
        hour_circle_color = res.getColor(R.color.white);
        big_circle_color = res.getColor(R.color.dark_grey);
        getColors();
        //Todo
    }


    /*
        public void showThemer() {
            final Dialog dialog = new Dialog(context, R.style.CustomDialog);
            dialog.setContentView(R.layout.themer_non_material);
            final ClassicClock clock = (ClassicClock) dialog.findViewById(R.id.clock);
            clock.unregisterClockThemerListener(clock);
            clock.setSizeChangedUpdate(true);
            final FrameLayout container = (FrameLayout) dialog.findViewById(R.id.container);

            final View lines = View.inflate(context, R.layout.themer_lines_nonmaterial, null);
            final View circle = View.inflate(context, R.layout.themer_circle_nonmaterial, null);
            final View hour = View.inflate(context, R.layout.themer_hours_nonmaterial, null);
            final View second = View.inflate(context, R.layout.themer_seconds_nonmaterial, null);
            final View minute = View.inflate(context, R.layout.themer_minutes_nonmaterial, null);

            setUpMinutes(minute);
            setUpCircle(circle);
            setUpHours(hour);
            setUpLines(lines);
            setUpSeconds(second);

            container.addView(lines);
            container.addView(circle);
            container.addView(hour);
            container.addView(second);
            container.addView(minute);

            ((RadioButton) dialog.findViewById(R.id.circle)).setChecked(true);
            RadioGroup group = (RadioGroup) dialog.findViewById(R.id.group);
            group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case R.id.lines:
                            try {
                                circle.setVisibility(GONE);
                                hour.setVisibility(GONE);
                                minute.setVisibility(GONE);
                                second.setVisibility(GONE);
                                lines.setVisibility(VISIBLE);
                            } catch (Exception e) {

                            }

                            break;
                        case R.id.circle:
                            try {
                                hour.setVisibility(GONE);
                                lines.setVisibility(GONE);
                                minute.setVisibility(GONE);
                                second.setVisibility(GONE);
                                circle.setVisibility(VISIBLE);
                            } catch (Exception e) {

                            }

                            break;
                        case R.id.hour:
                            try {
                                circle.setVisibility(GONE);
                                lines.setVisibility(GONE);
                                minute.setVisibility(GONE);
                                second.setVisibility(GONE);
                                hour.setVisibility(VISIBLE);
                            } catch (Exception e) {

                            }

                            break;

                        case R.id.second:
                            try {
                                circle.setVisibility(GONE);
                                hour.setVisibility(GONE);
                                lines.setVisibility(GONE);
                                minute.setVisibility(GONE);
                                second.setVisibility(VISIBLE);
                            } catch (Exception e) {

                            }

                            break;

                        case R.id.minute:
                            try {
                                circle.setVisibility(GONE);
                                hour.setVisibility(GONE);
                                lines.setVisibility(GONE);
                                second.setVisibility(GONE);
                                minute.setVisibility(VISIBLE);
                            } catch (Exception e) {

                            }

                            break;

                    }
                }
            });

            dialog.show();
        }

    */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (draw) {
            drawNormalClock(canvas);
            invalidate();
        }
    }


    public void setSizeChangedUpdate(boolean size_changed_update) {
        this.size_changed_update = size_changed_update;
    }

    public int getHour_circle_color() {
        return hour_circle_color;
    }

    public void setHour_circle_color(int hour_circle_color) {
        this.hour_circle_color = hour_circle_color;
    }

    public int getMinute_circle_color() {
        return minute_circle_color;
    }

    public void setMinute_circle_color(int minute_circle_color) {
        this.minute_circle_color = minute_circle_color;
    }

    public int getSecond_circle_color() {
        return second_circle_color;
    }

    public void setSecond_circle_color(int second_circle_color) {
        this.second_circle_color = second_circle_color;
    }

    public int getBig_circle_color() {
        return big_circle_color;
    }

    public void setBig_circle_color(int big_circle_color) {
        this.big_circle_color = big_circle_color;
    }

    public void setSize_changed_update(boolean size_changed_update) {
        this.size_changed_update = size_changed_update;
    }

}
