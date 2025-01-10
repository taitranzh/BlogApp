package com.example.blogapp.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class CustomSwipeRefreshLayout extends SwipeRefreshLayout {

    private float initialX, initialY;

    public CustomSwipeRefreshLayout(Context context) {
        super(context);
    }

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = ev.getX();
                initialY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = ev.getX() - initialX;
                float deltaY = ev.getY() - initialY;

                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    return false;
                }
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }
}
