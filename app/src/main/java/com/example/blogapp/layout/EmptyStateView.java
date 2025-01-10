package com.example.blogapp.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.example.blogapp.R;

public class EmptyStateView extends LinearLayout {

    private ImageView emptyIcon;
    private TextView emptyText;

    public EmptyStateView(Context context) {
        super(context);
        init(context);
    }

    public EmptyStateView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EmptyStateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.empty_blog_layout, this, true);
        emptyIcon = findViewById(R.id.emptyIcon);
        emptyText = findViewById(R.id.emptyText);
    }

    public void setEmptyState(String message, @DrawableRes int iconRes) {
        emptyText.setText(message);
        emptyIcon.setImageResource(iconRes);
        setVisibility(View.VISIBLE);
    }

    public void hideEmptyState() {
        setVisibility(View.GONE);
    }
}
