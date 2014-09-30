package view_classes;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.micnubinub.mrautomatic.R;


/**
 * Created by root on 24/08/14.
 */
public class MaterialCheckBox extends View {
    private static Resources res;
    private static int duration = 600;
    private final ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
    private final DecelerateInterpolator interpolator = new DecelerateInterpolator();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final OnClickListener l = new OnClickListener() {
        @Override
        public void onClick(View v) {
            MaterialCheckBox.this.toggle();
        }
    };
    private float line_pos;
    private int r, width, color_on, color_off, hole_r, color_hole;
    private boolean checked = false;
    private boolean updating = false;
    private float animated_value = 0;
    private OnCheckedChangedListener listener;

    public MaterialCheckBox(Context context) {
        super(context);
        init(context);
    }

    public MaterialCheckBox(Context context, int width, int height) {
        super(context);
        init(context);
        //Todo
    }

    public MaterialCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        res = context.getResources();
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
                MaterialCheckBox.this.invalidate();
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

    private void animateOn(Canvas canvas) {

        setPaintColor(color_on);
        canvas.drawLine(getPaddingLeft(), line_pos, (float) (width - getPaddingRight()), line_pos, paint);
        canvas.drawCircle((r + (animated_value * (width - r - r))), line_pos, r, paint);

        setPaintColor(color_hole);
        if (updating)
            canvas.drawCircle((r + (animated_value * (width - r - r))), line_pos, hole_r * (1 - animated_value), paint);
        else {
        }

    }

    private void animateOff(Canvas canvas) {
        setPaintColor(color_off);
        canvas.drawLine(getPaddingLeft(), line_pos, (float) (width - getPaddingRight()), line_pos, paint);
        canvas.drawCircle((r + (animated_value * (width - r - r))), line_pos, r, paint);

        setPaintColor(color_hole);
        if (updating)
            canvas.drawCircle((r + (animated_value * (width - r - r))), line_pos, hole_r * animated_value, paint);
        else
            canvas.drawCircle((r + (animated_value * (width - r - r))), line_pos, hole_r, paint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        paint.setStrokeWidth(0.15f * Math.min(w, h));
        line_pos = h / 2;
        r = Math.min((w - getPaddingLeft() - getPaddingRight()), (h - getPaddingBottom() - getPaddingTop())) / 2;
        hole_r = (int) (r * 0.85f);
        width = w;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        notifyListener();
    }

    public void toggle() {
        setChecked(!isChecked());
        animateSwitch();
    }

    private void notifyListener() {
        if (listener != null)
            listener.onCheckedChange(isChecked());
    }

    private void animateSwitch() {
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

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isChecked())
            animated_value = 1;
    }

    public void setAnimationDuration(int duration) {
        MaterialCheckBox.duration = duration;
        animator.setDuration(duration);
    }

    public void setOnCheckedChangeListener(OnCheckedChangedListener listener) {
        this.listener = listener;
    }

    public interface OnCheckedChangedListener {
        public void onCheckedChange(boolean isChecked);
    }
}
