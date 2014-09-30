package view_classes;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import com.micnubinub.mrautomatic.R;


/**
 * Created by root on 24/08/14.
 */
public class MaterialRadioButton extends ViewGroup {
    private static final AccelerateInterpolator interpolator = new AccelerateInterpolator();
    private static Resources res;
    private static int width;
    private static int padding = 10;
    private static int duration = 450;
    private static Context context;
    private final ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int textSize;
    private String text = "";
    private RadioButton radioButton;
    private final OnClickListener l = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (radioButton != null)
                toggle();
        }
    };
    private int cx, cy, r, color_on, color_off, hole_r, inner_hole_r, color_hole;
    private boolean checked = false;
    private boolean updating = false;
    private float animated_value = 0;
    private OnCheckedChangedListener listener;
    private TextView textView;

    public MaterialRadioButton(Context context) {
        super(context);
        init(context);
    }

    public MaterialRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MaterialRadioButton, 0, 0);
        setChecked(a.getBoolean(R.styleable.MaterialRadioButton_checked, false));
        text = a.getString(R.styleable.MaterialRadioButton_text);
        textSize = a.getInt(R.styleable.MaterialRadioButton_textSize, 16);
        a.recycle();
        init(context);
    }

    public static int dpToPixels(int dp, Resources res) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }

    public static int spToPixels(int sp, Resources res) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, res.getDisplayMetrics());
    }

    @Override
    protected void onLayout(boolean b, int i, int i2, int i3, int i4) {
        int currentChildRight = 0;
        int currentChildBottom = 0;
        final int currentChildTop = 0;
        int currentChildLeft = 0;

        try {
            for (int j = 0; j < Math.min(getChildCount(), 2); j++) {

                final View child = getChildAt(j);

                switch (j) {
                    case 0:
                        currentChildRight = getPaddingLeft() + width;
                        currentChildBottom = getPaddingTop() + width;
                        break;
                    case 1:
                        currentChildLeft = getPaddingLeft() + width;
                        currentChildRight = currentChildLeft + child.getMeasuredWidth();
                        currentChildBottom = getPaddingTop() + child.getMeasuredHeight();
                        break;
                }

                child.layout(currentChildLeft, currentChildTop, currentChildRight, currentChildBottom);
            }
        } catch (Exception e) {
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //Todo setMeasuredDimension();
        //Todo  measureChildWithMargins()
        //Todo xmlns:custom="http://schemas.android.com/apk/res/com.packa..."
        // measureChildren(widthMeasureSpec, heightMeasureSpec);

        int measuredHeight = 0;
        int measuredWidth = 0;

        try {
            for (int i = 0; i < getChildCount(); i++) {
                final View child = getChildAt(i);
                measureChild(child, widthMeasureSpec, heightMeasureSpec);

                measuredHeight = Math.max(measuredHeight, child.getMeasuredHeight());
                measuredWidth += child.getMeasuredWidth();
            }
        } catch (Exception w) {
        }

        setMeasuredDimension(resolveSizeAndState(measuredWidth, widthMeasureSpec, 0),
                resolveSizeAndState(measuredHeight, heightMeasureSpec, 0));

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        r = (int) (0.7f * (Math.min((width - getPaddingLeft() - getPaddingRight()), (width - getPaddingBottom() - getPaddingTop())) / 2));
        cx = getPaddingLeft() + (width / 2);
        hole_r = (int) (r * 0.9f);
        inner_hole_r = (int) (r * 0.75f);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        animateSwitch();
        notifyListener();
    }

    public void toggle() {
        setChecked(!isChecked());
    }

    private void notifyListener() {
        if (listener != null)
            listener.onCheckedChange(this, isChecked());
    }

    public void setUpdating(boolean updating) {
        this.updating = updating;
    }

    private void setPaintColor(int color) {
        try {
            this.paint.setColor(color);
        } catch (Exception e) {
        }
    }

    public void setOffColor(int color_off) {
        this.color_off = color_off;
    }

    public void setOnColor(int color_on) {
        this.color_on = color_on;
    }

    public void setHoleColor(int hole_color) {
        this.color_hole = hole_color;
    }

    public void setAnimationDuration(int duration) {
        this.duration = duration;
        animator.setDuration(duration);
    }

    public void setText(String text) {
        if (text != null && text.length() > 0) {

            if (textView == null)
                textView = new TextView(context);

            textView.setTextSize(textSize);
            textView.setPadding(padding, padding, padding, padding);
            textView.setText(text);
            addView(textView, 1);
        } else {
            try {
                removeView(textView);
            } catch (Exception e) {
            }
            textView = null;
        }
        invalidate();
        calculateCy();
    }


    private void init(Context context) {
        MaterialRadioButton.context = context;
        res = context.getResources();
        width = dpToPixels(28, res);
        radioButton = new RadioButton(context);
        padding = dpToPixels(2, res);

        radioButton.setLayoutParams(new LayoutParams(width, Math.max(spToPixels(textSize < 16 ? 16 : textSize, res), width)));
        radioButton.setPadding(padding, padding, padding, padding);
        addView(radioButton, 0);

        setText(text);

        calculateCy();

        setOffColor(res.getColor(R.color.lite_grey));
        setOnColor(res.getColor(R.color.material_green_light));
        setHoleColor(res.getColor(R.color.white));

        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(color_off);

        animator.setInterpolator(interpolator);
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animated_value = ((Float) (animation.getAnimatedValue())).floatValue();
                MaterialRadioButton.this.invalidate();
            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                setUpdating(true);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setUpdating(false);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                setUpdating(false);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                setUpdating(true);
            }
        });
        setOnClickListener(l);
    }

    private void calculateCy() {
        if (textView == null)
            cy = Math.max(getHeight(), radioButton.getLayoutParams().height) / 2;
        else
            cy = Math.max(spToPixels(textSize, res) / 2 + textView.getPaddingTop(), (radioButton.getLayoutParams().height / 2));
        invalidate();
    }

    private void animateSwitch() {
        if (radioButton != null)
            radioButton.animateSwitch();
    }

    public void setOnCheckedChangeListener(OnCheckedChangedListener listener) {
        this.listener = listener;
    }

    @Override
    public void addView(View child) {
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        super.addView(child, index);
    }

    @Override
    public void addView(View child, int width, int height) {
        super.addView(child, width, height);
    }

    @Override
    public void addView(View child, LayoutParams params) {
        super.addView(child, params);
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        super.addView(child, index, params);
    }

    public interface OnCheckedChangedListener {
        public void onCheckedChange(MaterialRadioButton materialRadioButton, boolean isChecked);
    }

    class RadioButton extends View {

        public RadioButton(Context context) {
            super(context);
            //Todo draw shadow
            //  setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (isChecked())
                animateOn(canvas);
            else
                animateOff(canvas);

            if (updating)
                invalidate();

        }

        private void animateOff(Canvas canvas) {
            paint.setShadowLayer(7, 0, 0, Color.argb(255, 90, 90, 90));
            setPaintColor(color_hole);
            if (updating) {
                canvas.drawCircle(cx, cy, hole_r, paint);
                paint.setShadowLayer(0, 0, 0, 0);
                setPaintColor(color_on);
                canvas.drawCircle(cx, cy, inner_hole_r * animated_value, paint);
            } else {
                paint.setShadowLayer(0, 0, 0, 0);
                canvas.drawCircle(cx, cy, hole_r, paint);
            }
        }

        private void animateOn(Canvas canvas) {
            paint.setShadowLayer(7, 0, 0, Color.argb(255, 90, 90, 90));
            setPaintColor(color_hole);
            if (updating) {
                canvas.drawCircle(cx, cy, hole_r, paint);
                paint.setShadowLayer(0, 0, 0, 0);
                setPaintColor(color_on);
                canvas.drawCircle(cx, cy, inner_hole_r * animated_value, paint);
            } else {
                canvas.drawCircle(cx, cy, hole_r, paint);
                paint.setShadowLayer(0, 0, 0, 0);
                setPaintColor(color_on);
                canvas.drawCircle(cx, cy, inner_hole_r, paint);

            }
        }


        public void animateSwitch() {
            invalidate();
            try {
                if (animator.isRunning())
                    animator.cancel();

                if (isChecked())
                    animator.start();
                else
                    animator.reverse();
            } catch (Exception e) {
            }

        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (isChecked())
                animated_value = 1;
        }

    }
}
