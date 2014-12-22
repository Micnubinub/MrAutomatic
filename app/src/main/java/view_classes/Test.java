package view_classes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.micnubinub.mrautomatic.R;

/**
 * Created by root on 22/12/14.
 */
public class Test extends View {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public Test(Context context) {
        super(context);
        setBackgroundColor(getResources().getColor(R.color.black));
        paint.setStrokeWidth(5);
        paint.setColor(getResources().getColor(R.color.white));
    }

    public Test(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(getResources().getColor(R.color.black));
        paint.setStrokeWidth(5);
        paint.setColor(getResources().getColor(R.color.white));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = canvas.getWidth() / 2;
        int h = canvas.getHeight() / 2;
        int x = canvas.getWidth() / 4;
        int y = canvas.getHeight() / 4;
        drawRectangle(canvas, x, y, w, h, true, true, true, false);
        invalidate();
    }

    private void drawRectangle(Canvas canvas, int x, int y, int w, int h, boolean drawLeft, boolean drawRight, boolean drawTop, boolean drawBottom) {
        if (drawLeft)
            canvas.drawLine(x, y, x, y + h, paint);

        if (drawRight)
            canvas.drawLine(x + w, y, x + w, y + h, paint);

        if (drawTop)
            canvas.drawLine(x, y, x + w, y, paint);

        if (drawBottom)
            canvas.drawLine(x, y + h, x + w, y + h, paint);

    }
}
