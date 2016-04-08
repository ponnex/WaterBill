package com.ponnex.interfacing.waterutilitymonitoringsystem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by Ramos on 3/28/2016.
 */
public class BottomView extends View {

    Paint paint = new Paint();
    Paint paint_drawable = new Paint();
    float width, height;
    int screen_width;
    int screen_height;
    Bitmap mBitmap;

    public BottomView(Context context, AttributeSet foo) {
        super(context, foo);
        setBackgroundColor(Color.TRANSPARENT);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(ContextCompat.getColor(context, android.R.color.white));

        paint_drawable.setAntiAlias(true);

        Point size = new Point();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getSize(size);
        screen_width = size.x;
        screen_height = size.y;
        mBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.logo), (getActionBarHeight() - (getActionBarHeight() / 8)), (getActionBarHeight() - (getActionBarHeight() / 8)), false);


    }

    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        width = w;
        height = h;
    }

    public void onDraw(Canvas canvas) {
        canvas.drawRect(0, screen_height - getActionBarHeight(), screen_width, screen_height, paint);
        canvas.drawCircle(screen_width / 2, screen_height - (getActionBarHeight() - (getActionBarHeight() / 4)), (getActionBarHeight() - (getActionBarHeight() / 3)), paint);
        canvas.drawBitmap(mBitmap, (screen_width / 2) - ((getActionBarHeight() * 7) / 16), screen_height - (getActionBarHeight() + getActionBarHeight() / 4), paint_drawable);
        super.onDraw(canvas);
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
