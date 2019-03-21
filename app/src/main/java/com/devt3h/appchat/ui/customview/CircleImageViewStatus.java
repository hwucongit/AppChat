package com.devt3h.appchat.ui.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.devt3h.appchat.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class CircleImageViewStatus extends CircleImageView {
    private Paint paint;
    private boolean isOnline = true;
    public CircleImageViewStatus(Context context, AttributeSet attrs) {
        super(context, attrs);
        inits(attrs, 0);
    }

    public CircleImageViewStatus(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inits(attrs, defStyle);
    }
    private void inits( AttributeSet attrs, int defStyle){
        TypedArray  typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CircleImageViewStatus, defStyle, 0);
        isOnline = typedArray.getBoolean(R.styleable.CircleImageViewStatus_isOnline, false);
        typedArray.recycle();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#8BC34A"));
    }

    public void setOnline(boolean isOnline){
        if(isOnline == this.isOnline){
            return;
        }
        this.isOnline = isOnline;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isOnline){
            int paWidth = getWidth()/2;
            int paHeight = getWidth()/2;
            canvas.rotate(45, paWidth, paHeight);
            canvas.drawCircle(paWidth*2, paHeight, 8, paint);
        }

    }
}
