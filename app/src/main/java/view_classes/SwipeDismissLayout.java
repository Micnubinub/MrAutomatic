package view_classes;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by root on 18/12/14.
 */
public class SwipeDismissLayout extends ViewGroup {
    //Todo touch slop

    public SwipeDismissLayout(Context context) {
        super(context);
    }

    public SwipeDismissLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
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


        int measuredHeight = 0;
        int measuredWidth = 0;

        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                measuredHeight += child.getMeasuredHeight();
                measuredWidth = Math.max(measuredWidth, child.getMeasuredWidth());
            }
        }
        setMeasuredDimension(resolveSizeAndState(measuredWidth, widthMeasureSpec, 0),
                resolveSizeAndState(measuredHeight, heightMeasureSpec, 0));

    }
}
