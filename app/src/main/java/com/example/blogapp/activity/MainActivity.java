package com.example.blogapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blogapp.R;
import com.example.blogapp.adapter.BlogAdapter;
import com.example.blogapp.adapter.BlogHotAdapter;
import com.example.blogapp.api.ApiService;
import com.example.blogapp.api.RetrofitClient;
import com.example.blogapp.entities.Blog;
import com.example.blogapp.layout.CustomSwipeRefreshLayout;
import com.example.blogapp.utils.SecureStorage;
import com.example.blogapp.utils.Utils;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {
    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private ImageView userImg, searchImg;
    private TextView tvClose;
    private EditText edtSearch;
    private RecyclerView recyclerCarousel, recyclerList;
    private CustomSwipeRefreshLayout swipeRefreshLayout;
    private Runnable autoScrollRunnable;
    private int currentPosition = 0;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerCarousel);

        fetchBlogs();
        fetchBlogsHot();
        initListener();
        handler = new Handler();
        startAutoScroll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (edtSearch.getVisibility() == View.VISIBLE && !edtSearch.getText().toString().trim().isEmpty()) {
            fetchSearchBlogs(edtSearch.getText().toString().trim());
        } else {
            fetchBlogs();
        }
        fetchBlogsHot();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(autoScrollRunnable);
    }
    private void initUI(){
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigation_view);
        mDrawerLayout = findViewById(R.id.main);
        userImg = findViewById(R.id.user_img);
        searchImg = findViewById(R.id.img_search);
        recyclerCarousel = findViewById(R.id.recyclerCarousel);
        recyclerList = findViewById(R.id.recyclerList);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        tvClose = findViewById(R.id.tv_close);
        edtSearch = findViewById(R.id.edt_search);
    }
    private void initListener(){
        userImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UserInfoActivity.class);
                startActivity(intent);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_archive) {
                    Intent archiveIntent = new Intent(MainActivity.this, ArchiveActivity.class);
                    startActivity(archiveIntent);
                } else if (id == R.id.nav_like) {
                    Intent blogLikedIntent = new Intent(MainActivity.this, BlogLikedActivity.class);
                    startActivity(blogLikedIntent);
                } else if (id == R.id.nav_contact) {
                    Intent contactIntent = new Intent(MainActivity.this, ContactActivity.class);
                    startActivity(contactIntent);
                } else if (id == R.id.nav_categories) {
                    Intent categoryIntent = new Intent(MainActivity.this, CategoriesActivity.class);
                    startActivity(categoryIntent);
                } else if (id == R.id.nav_logout) {
                    logoutUser();
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });

        tvClose.setOnClickListener(v -> {
            edtSearch.setText("");
            edtSearch.setVisibility(View.GONE);
            tvClose.setVisibility(View.GONE);
            searchImg.setVisibility(View.VISIBLE);
            userImg.setVisibility(View.VISIBLE);
            recyclerCarousel.setVisibility(View.VISIBLE);
            fetchBlogs();
        });

        searchImg.setOnClickListener(v -> {
            edtSearch.setVisibility(View.VISIBLE);
            tvClose.setVisibility(View.VISIBLE);
            searchImg.setVisibility(View.GONE);
            userImg.setVisibility(View.GONE);
            edtSearch.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(edtSearch, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                String searchQuery = edtSearch.getText().toString().trim();

                if (searchQuery.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Vui lòng nhập từ khóa tìm kiếm!", Toast.LENGTH_SHORT).show();
                    return false;
                }
                fetchSearchBlogs(searchQuery);
                recyclerCarousel.setVisibility(View.GONE);
                return true;
            }
            return false;
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchBlogs();
            fetchBlogsHot();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void startAutoScroll() {
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (recyclerCarousel != null && recyclerCarousel.getLayoutManager() != null) {
                    int itemCount = recyclerCarousel.getAdapter() != null ? recyclerCarousel.getAdapter().getItemCount() : 0;
                    if (itemCount > 1) {
                        currentPosition = (currentPosition + 1) % itemCount;
                        recyclerCarousel.smoothScrollToPosition(currentPosition);
                        handler.postDelayed(this, 3000);
                    }
                }
            }
        };
        handler.postDelayed(autoScrollRunnable, 3000);
    }
    private void fetchBlogs(){
        swipeRefreshLayout.setRefreshing(true);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getBlogs(1000).enqueue(new Callback<List<Blog>>() {
            @Override
            public void onResponse(Call<List<Blog>> call, Response<List<Blog>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Blog> blogs = response.body();
                    setupBlogs(blogs);
                } else {
                    Toast.makeText(MainActivity.this, "Không thể lấy danh sách bài viết", Toast.LENGTH_SHORT).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Blog>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void fetchSearchBlogs(String searchQuery){
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getSearchBlogs(searchQuery, 1000).enqueue(new Callback<List<Blog>>() {
            @Override
            public void onResponse(Call<List<Blog>> call, Response<List<Blog>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Blog> blogs = response.body();
                    setupBlogs(blogs);
                } else {
                    Toast.makeText(MainActivity.this, "Không thể lấy danh sách bài viết", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Blog>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchBlogsHot(){
        swipeRefreshLayout.setRefreshing(true);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getBlogsHot(Utils.NUMBER_BLOGS_HOT).enqueue(new Callback<List<Blog>>() {
            @Override
            public void onResponse(Call<List<Blog>> call, Response<List<Blog>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Blog> blogsHot = response.body();
                    setupBlogsHot(blogsHot);
                } else {
                    Toast.makeText(MainActivity.this, "Không thể lấy danh sách bài viết", Toast.LENGTH_SHORT).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Blog>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    private void setupBlogs(List<Blog> blogs) {
        BlogAdapter blogAdapter = new BlogAdapter(blogs, this, false);
        recyclerList.setLayoutManager(new LinearLayoutManager(this));
        recyclerList.setAdapter(blogAdapter);
    }

    private void setupBlogsHot(List<Blog> blogs) {
        BlogHotAdapter blogHotAdapter = new BlogHotAdapter(blogs, this);
        recyclerCarousel.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerCarousel.setAdapter(blogHotAdapter);
    }
    private void logoutUser() {
        SecureStorage.getInstance(MainActivity.this).clearAccessToken();
        SecureStorage.getInstance(MainActivity.this).clearRefreshToken();
        SecureStorage.getInstance(MainActivity.this).clearUserId();
        saveLoginState(false);
        Intent logoutIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(logoutIntent);
        finish();
    }

    private void saveLoginState(boolean isLoggedIn) {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", isLoggedIn);
        editor.apply();
    }

}