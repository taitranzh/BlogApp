package com.example.blogapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.blogapp.R;

public class ContactActivity extends BaseActivity {
    ImageView imgBack;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        initUI();
        initListener();
    }

    private void initUI(){
        imgBack = findViewById(R.id.img_back);
    }

    private void initListener(){
        imgBack.setOnClickListener(v -> {
            Intent intent = new Intent(ContactActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}
