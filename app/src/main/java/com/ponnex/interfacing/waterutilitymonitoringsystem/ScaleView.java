package com.ponnex.interfacing.waterutilitymonitoringsystem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by Ramos on 3/28/2016.
 */
public class ScaleView extends View {
    Paint paint = new Paint();
    float width, height;
    int screen_width;
    int screen_height;

    public ScaleView(Context context, AttributeSet foo) {
        super(context, foo);
        setBackgroundColor(Color.TRANSPARENT);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setAntiAlias(false);
        paint.setColor(Color.WHITE);

        Point size = new Point();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getSize(size);
        screen_width = size.x;
        screen_height = size.y;
    }

    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        width = w;
        height = h;
    }

    public void onDraw(Canvas canvas) {
        int scale = 1;
        int size = 0;
        for (int i = 0; ; ++i) {
            float y = i * (screen_height - getActionBarHeight()) / 20;
            if (y > screen_height - getActionBarHeight() - getStatusBarHeight()) {
                break;
            }

            switch (scale) {
                case 1:
                    size = screen_width - 10;
                    scale = 2;
                    break;
                case 2:
                    size = screen_width - 25;
                    scale = 3;
                    break;
                case 3:
                    size = screen_width - 50;
                    scale = 4;
                    break;
                case 4:
                    size = screen_width - 25;
                    scale = 1;
                    break;
            }

            canvas.drawLine(size, y, screen_width, y, paint);
        }
        super.onDraw(canvas);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int getActionBarHeight() {
        int actionBarHeight = 0;
        final TypedValue tv = new TypedValue();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        } else if (getContext().getTheme().resolveAttribute( android.support.v7.appcompat.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize( tv.data,getResources().getDisplayMetrics());
        }
        return (actionBarHeight + (actionBarHeight / 2)) - 4;
    }
}
