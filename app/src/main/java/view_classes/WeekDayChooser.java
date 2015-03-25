package view_classes;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.micnubinub.mrautomatic.R;

import java.util.ArrayList;

/**
 * Created by Michael on 3/25/2015.
 */
public class WeekDayChooser extends ViewGroup {
    //Todo consider changing up pad instead of using extra pad
    private static final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final OnClickListener weekDayCClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!(view instanceof WeekDayView))
                return;

            ((WeekDayView) view).toggle();
        }
    };
    private static float textSize, r, tCX, tCY;
    private static int outCircleColor, pad, selectedColor, nonSelectedColor, extraPadding;

    public WeekDayChooser(Context context) {
        super(context);
        init();
    }

    public WeekDayChooser(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        final Resources res = getResources();
        outCircleColor = res.getColor(R.color.dark_grey_text);
        selectedColor = res.getColor(R.color.material_green_light);
        nonSelectedColor = res.getColor(R.color.material_red);

        for (WeekDay weekDay : WeekDay.values()) {
            final WeekDayView v = new WeekDayView(weekDay);
            addView(v);
        }
    }

    public void setSelectedW(final ArrayList<WeekDay> selectedDays) {
        for (int i = 0; i < getChildCount(); i++) {
            final View v = getChildAt(i);
            if (v instanceof WeekDayView) {
                final WeekDayView w = (WeekDayView) v;
                w.itemSelected = containsWeekDay(selectedDays, w.weekDay);
            }
        }
    }

    private boolean containsWeekDay(ArrayList<WeekDay> selectedDays, WeekDay w) {
        for (WeekDay selectedDay : selectedDays) {
            if (selectedDay == w)
                return true;
        }
        return false;
    }

    public ArrayList<WeekDay> selectedDays() {
        final ArrayList<WeekDay> weekDays = new ArrayList<>(7);
        for (int i = 0; i < getChildCount(); i++) {
            final View v = getChildAt(i);
            if (v instanceof WeekDayView) {
                if (((WeekDayView) v).itemSelected)
                    weekDays.add(((WeekDayView) v).weekDay);
            }
        }
        return weekDays;
    }

    @Override
    protected void onLayout(boolean b, int fi, int i2, int i3, int i4) {
//Todo check
        extraPadding = (getMeasuredWidth() - Math.round((pad * 8) + (r * 14))) / 2;
        for (int i = 0; i < getChildCount(); i++) {
            final View v = getChildAt(i);
            final int left = extraPadding + Math.round(pad + ((r + r + pad) * i));
            v.layout(left, pad, Math.round(left + r + r), getMeasuredHeight() - pad);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int measuredHeight = 0;
        int measuredWidth = 0;

        try {
            for (int i = 0; i < getChildCount(); i++) {
                final View child = getChildAt(i);
                measureChild(child, widthMeasureSpec, heightMeasureSpec);

                measuredHeight = Math.max(measuredHeight, child.getMeasuredHeight());
                measuredWidth += child.getMeasuredWidth();
            }
        } catch (Exception ignored) {
        }

        setMeasuredDimension(resolveSizeAndState(measuredWidth, widthMeasureSpec, 0),
                resolveSizeAndState(measuredHeight, heightMeasureSpec, 0));

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //Todo fill in
        pad = Math.min(w, h) / 15;
        r = Math.min((w / 7) - (8 * pad), h - pad - pad) / 2f;
        textSize = r * 1.6f;
        paint.setTextSize(textSize);
        final int d = Math.round(r + r);
        final LayoutParams params = new LayoutParams(d, d);
        for (int i = 0; i < getChildCount(); i++) {
            final View v = getChildAt(i);
            if (v instanceof WeekDayView) {
                v.setLayoutParams(params);
            }
        }
        tCX = 0.3f * r;
        tCY = (1.5f * r);
    }

    public enum WeekDay {
        SUN, MON, TUE, WED, THU, FRI, SAT
    }

    public class WeekDayView extends View {
        private WeekDay weekDay;
        private boolean itemSelected;

        public WeekDayView(WeekDay weekDay) {
            super(WeekDayChooser.this.getContext());
            this.weekDay = weekDay;
            setOnClickListener(weekDayCClickListener);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            paint.setColor(outCircleColor);
            canvas.drawCircle(r, r, r, paint);
            paint.setColor(itemSelected ? selectedColor : nonSelectedColor);
            canvas.drawCircle(r, r, r * 0.96f, paint);
            paint.setColor(0xffffffff);

            switch (weekDay) {
                case SUN:
                    canvas.drawText("S", tCX, tCY, paint);
                    break;
                case MON:
                    canvas.drawText("M", tCX, tCY, paint);
                    break;
                case TUE:
                    canvas.drawText("T", tCX, tCY, paint);
                    break;
                case WED:
                    canvas.drawText("W", tCX, tCY, paint);
                    break;
                case THU:
                    canvas.drawText("T", tCX, tCY, paint);
                    break;
                case FRI:
                    canvas.drawText("F", tCX, tCY, paint);
                    break;
                case SAT:
                    canvas.drawText("S", tCX, tCY, paint);
                    break;
            }
        }

        public void toggle() {
            itemSelected = !itemSelected;
        }
    }
}
