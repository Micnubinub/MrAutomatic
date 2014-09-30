package view_classes;

/**
 * Created by root on 18/08/14.
 */


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.micnubinub.mrautomatic.R;


/**
 * Created by root on 14/08/14.
 */
public class GoogleClock extends Clock {

    private final RectF rectF = new RectF();
    //  private static final Paint shadow = new Paint();
    private int seconds = 0;
    private int minutes = 0;
    private int hours = 0;
    private int width, height, r, cx, cy,
            cx_second, cy_second,
            cx_hour, cy_hour,
            cx_minute, cy_minute,
            hour_arc_color,
            minute_arc_color,
            second_arc_color,
            minute_arc_radius,
            second_arc_radius;

    private float
            minute_arc_fraction = 0.8f,
            second_arc_fraction = 0.6f;
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
    public GoogleClock(Context context) {
        super(context);
        init(context);
    }

    public GoogleClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public static void unregisterClockThemerListener(ClassicClock clock) {
        clock.setOnClickListener(null);
        clock.setClickable(false);
    }


    private void sizeChanged(Canvas canvas) {
        width = canvas.getWidth();
        height = canvas.getHeight();
        r = Math.min(height, width) / 2;
        cx = width / 2;
        cy = height / 2;
        size_changed_update = false;

        minute_arc_radius = (int) (r * minute_arc_fraction);
        second_arc_radius = (int) (r * second_arc_fraction);

    }

    private void drawNormalClock(Canvas canvas) {
        setTime();
        //Todo draw shadows big shot level maximum
        if (size_changed_update) {
            sizeChanged(canvas);
        }

        paint.setColor(res.getColor(R.color.dark_grey));
        canvas.drawCircle(cx, cy, r, paint);


        //drawCircles(canvas);

        paint.setColor(hour_arc_color);
        setHourRectF();
        canvas.drawArc(rectF, -90f, (float) hours, true, paint);

        paint.setColor(minute_arc_color);
        setMinuteRectF();
        canvas.drawArc(rectF, -90f, (float) minutes, true, paint);

        paint.setColor(second_arc_color);
        setSecondRectF();
        canvas.drawArc(rectF, -90f, (float) seconds, true, paint);

    }

    private void setHourRectF() {
        rectF.set(
                (float) (width - (2 * r)) / 2,
                (float) (height - (2 * r)) / 2,
                (float) (width - ((width - (2 * r)) / 2)),
                (float) (height - ((height - (2 * r)) / 2)));


    }

    private void setMinuteRectF() {
        rectF.set(
                (float) (width - (2 * minute_arc_radius)) / 2,
                (float) (height - (2 * minute_arc_radius)) / 2,
                (float) (width - ((width - (2 * minute_arc_radius)) / 2)),
                (float) (height - ((height - (2 * minute_arc_radius)) / 2)));


    }

    private void setSecondRectF() {
        rectF.set(
                (float) (width - (2 * second_arc_radius)) / 2,
                (float) (height - (2 * second_arc_radius)) / 2,
                (float) (width - ((width - (2 * second_arc_radius)) / 2)),
                (float) (height - ((height - (2 * second_arc_radius)) / 2)));


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
        minute_arc_color = res.getColor(R.color.high);
        hour_arc_color = res.getColor(R.color.extra_high);
        second_arc_color = res.getColor(R.color.red);
        //getColors();
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
            final View circle = View.inflate(context, R.layout.themer_arc_nonmaterial, null);
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

    public int getHour_arc_color() {
        return hour_arc_color;
    }

    public void setHour_arc_color(int hour_arc_color) {
        this.hour_arc_color = hour_arc_color;
    }

    public int getMinute_arc_color() {
        return minute_arc_color;
    }

    public void setMinute_arc_color(int minute_arc_color) {
        this.minute_arc_color = minute_arc_color;
    }

    public int getSecond_arc_color() {
        return second_arc_color;
    }

    public void setSecond_arc_color(int second_arc_color) {
        this.second_arc_color = second_arc_color;
    }


    public void setSize_changed_update(boolean size_changed_update) {
        this.size_changed_update = size_changed_update;
    }

}
