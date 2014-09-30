package view_classes;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.micnubinub.mrautomatic.R;


/**
 * Created by root on 27/09/14.
 */
public class AppIcon extends ViewGroup {
    static Resources resources;
    private TextView textView;
    private ImageView imageView;

    public AppIcon(Context context) {
        super(context);
        init(context);
    }

    public AppIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AppIcon, 0, 0);
        try {
            setImage(resources.getDrawable(array.getInt(R.styleable.AppIcon_image, R.drawable.ic_launcher)));
            setLabel(array.getString(R.styleable.AppIcon_textf));
        } catch (Exception e) {
        }
        array.recycle();
    }

    public AppIcon(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        textView = new TextView(context);
        textView.setMaxLines(1);
        imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }

    public void setLabel(String text) {
        textView.setText(text);
    }

    public void setImage(Drawable drawable) {
        imageView.setImageDrawable(drawable);
    }

    public void setImage(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    @Override
    protected void onLayout(boolean b, int i, int i2, int i3, int i4) {

        int currentChildRight;
        int currentChildBottom;
        int currentChildTop = 0;
        final int currentChildLeft = getPaddingLeft();

        for (int j = 0; j < getChildCount(); j++) {
            final View child = getChildAt(j);

            if (j > 0) {
                currentChildTop += getChildAt(j - 1).getHeight();
                currentChildBottom = currentChildTop + child.getMeasuredHeight();
            } else {
                currentChildTop = getPaddingTop();
                currentChildBottom = child.getMeasuredHeight() - getPaddingBottom();
            }
            currentChildRight = child.getMeasuredWidth() - getPaddingRight();
            child.layout(currentChildLeft, currentChildTop, currentChildRight, currentChildBottom);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //Todo setMeasuredDimension();
        //Todo  measureChildWithMargins()
        //Todo xmlns:custom="http://schemas.android.com/apk/res/com.packa..."
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int measuredHeight = 0;
        int measuredWidth = 0;

        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                //   child.measure(widthMeasureSpec, heightMeasureSpec);
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                measuredHeight += child.getMeasuredHeight();
                // measuredHeight = Math.max(measuredHeight, child.getMeasuredHeight());
                measuredWidth = Math.max(measuredWidth, child.getMeasuredWidth());
            }
        }
        setMeasuredDimension(resolveSizeAndState(measuredWidth, widthMeasureSpec, 0),
                resolveSizeAndState(measuredHeight, heightMeasureSpec, 0));

    }
}
