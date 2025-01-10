package com.example.blogapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blogapp.R;
import com.example.blogapp.adapter.BlogAdapter;
import com.example.blogapp.api.ApiService;
import com.example.blogapp.api.RetrofitClient;
import com.example.blogapp.entities.Blog;
import com.example.blogapp.layout.CustomSwipeRefreshLayout;
import com.example.blogapp.utils.SecureStorage;
import com.example.blogapp.utils.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlogLikedActivity extends BaseActivity {
    private RecyclerView recyclerBlogListLiked;
    private ImageView imgBack;
    private CustomSwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_liked);
        initUI();
        getBlogLiked();
        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getBlogLiked();
    }

    private void initUI(){
        recyclerBlogListLiked = findViewById(R.id.recycler_blog_list_liked);
        imgBack = findViewById(R.id.img_back);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    private void initListener(){
        imgBack.setOnClickListener(v -> {
            Intent intent = new Intent(BlogLikedActivity.this, MainActivity.class);
            startActivity(intent);
        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            getBlogLiked();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void getBlogLiked() {
        swipeRefreshLayout.setRefreshing(true);
        String userId = SecureStorage.getInstance(BlogLikedActivity.this).getUserId();
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getBlogsLiked(userId, 1000).enqueue(new Callback<List<Blog>>() {
            @Override
            public void onResponse(Call<List<Blog>> call, Response<List<Blog>> response) {
                if(response.isSuccessful() && response.body() != null){
                    List<Blog> blogs = response.body();
                    BlogAdapter blogAdapter = new BlogAdapter(blogs, BlogLikedActivity.this, false);
                    recyclerBlogListLiked.setLayoutManager(new LinearLayoutManager(BlogLikedActivity.this));
                    recyclerBlogListLiked.setAdapter(blogAdapter);
                } else {
                    Toast.makeText(BlogLikedActivity.this, "Không thể lấy danh sách bài viết đã thích", Toast.LENGTH_SHORT).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Blog>> call, Throwable t) {
                Toast.makeText(BlogLikedActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}
