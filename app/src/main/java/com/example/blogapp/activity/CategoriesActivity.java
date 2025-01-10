package com.example.blogapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.example.blogapp.R;
import com.example.blogapp.adapter.CategoryTabAdapter;
import com.example.blogapp.api.ApiService;
import com.example.blogapp.api.RetrofitClient;
import com.example.blogapp.entities.Category;
import com.example.blogapp.utils.Utils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoriesActivity extends BaseActivity {
    ImageView imgBack;
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    private List<String> categoryNames;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        initUI();
        loadCategories();
        initListener();
    }
    private void initUI(){
        imgBack = findViewById(R.id.img_back);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager2 = findViewById(R.id.viewPager);
    }

    private void initListener(){
        imgBack.setOnClickListener(v -> {
            Intent intent = new Intent(CategoriesActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void loadCategories() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getCategories(Utils.NUMBER_CATEGORIES).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> categories = response.body();
                    categoryNames = new ArrayList<>();

                    for (Category category : categories) {
                        categoryNames.add(category.getName());
                    }

                    CategoryTabAdapter adapter = new CategoryTabAdapter(CategoriesActivity.this, categories);
                    viewPager2.setAdapter(adapter);

                    new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
                        tab.setText(categoryNames.get(position));
                    }).attach();
                } else {
                    Toast.makeText(CategoriesActivity.this, "Không thể lấy danh sách thể loại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(CategoriesActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
