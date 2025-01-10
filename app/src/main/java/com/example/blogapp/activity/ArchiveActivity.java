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

public class ArchiveActivity extends BaseActivity {
    private RecyclerView recyclerBlogList;
    private FloatingActionButton fabCreateBlog;
    private ImageView imgBack;
    private CustomSwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);
        initUI();
        getBlogByAuthor();;
        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getBlogByAuthor();
    }

    private void initUI(){
        recyclerBlogList = findViewById(R.id.recycler_blog_list);
        fabCreateBlog = findViewById(R.id.fab_create_blog);
        imgBack = findViewById(R.id.img_back);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    private void initListener(){
        fabCreateBlog.setOnClickListener(v -> {
            Intent intent = new Intent(ArchiveActivity.this, CreateBlogActivity.class);
            startActivity(intent);
        });

        imgBack.setOnClickListener(v -> {
            Intent intent = new Intent(ArchiveActivity.this, MainActivity.class);
            startActivity(intent);
        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            getBlogByAuthor();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void getBlogByAuthor(){
        swipeRefreshLayout.setRefreshing(true);
        String userId = SecureStorage.getInstance(ArchiveActivity.this).getUserId();
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getBlogsByAuthor(userId, 1000).enqueue(new Callback<List<Blog>>() {
            @Override
            public void onResponse(Call<List<Blog>> call, Response<List<Blog>> response) {
                if(response.isSuccessful() && response.body() != null){
                    List<Blog> blogs = response.body();
                    BlogAdapter blogAdapter = new BlogAdapter(blogs, ArchiveActivity.this, true);
                    recyclerBlogList.setLayoutManager(new LinearLayoutManager(ArchiveActivity.this));
                    recyclerBlogList.setAdapter(blogAdapter);
                } else {
                    Toast.makeText(ArchiveActivity.this, "Không thể lấy danh sách bài viết của tôi", Toast.LENGTH_SHORT).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Blog>> call, Throwable t) {
                Toast.makeText(ArchiveActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}
