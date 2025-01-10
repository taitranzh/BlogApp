package com.example.blogapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.blogapp.R;
import com.example.blogapp.api.ApiService;
import com.example.blogapp.api.RetrofitClient;
import com.example.blogapp.entities.Blog;
import com.example.blogapp.utils.SecureStorage;
import com.example.blogapp.utils.TimeUtils;
import com.example.blogapp.utils.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlogDetailActivity extends BaseActivity {
    private ImageView imgBack, imgBlog, imgUser, imgLike;
    private TextView tvTitle, tvName, tvDate, tvContent, tvLikeCount, tvCommentCount, tvDescription;
    private String postId, postUrl;
    private LinearLayout likeLayout, commentLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_detail);
        initUI();

        postId = getIntent().getStringExtra("BLOG_ID");
        postUrl = getIntent().getStringExtra("BLOG_URL");

        if (postId != null) {
            fetchBlogDetails();
            getCommentCount();
        }

        if(postUrl != null){
            fetchLikeCount();
            checkLiked();
        }

        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCommentCount();
    }

    private void updateUI(Blog blog){
        tvTitle.setText(blog.getTitle());
        tvName.setText(blog.getAuthorName());
        String timeAgo = TimeUtils.getTimeAgo(blog.getPublishedDate());
        tvDate.setText(timeAgo);
        tvContent.setText(blog.getContent());
        tvDescription.setText(blog.getShortDescription());
        Glide.with(this)
                .load(blog.getFeaturedImageUrl())
                .into(imgBlog);
    }

    private void initUI(){
        imgBack = findViewById(R.id.img_back);
        imgBlog = findViewById(R.id.img_blog);
        imgUser = findViewById(R.id.img_user);
        imgLike = findViewById(R.id.img_like);
        tvTitle = findViewById(R.id.tv_title);
        tvName = findViewById(R.id.tv_name);
        tvDate = findViewById(R.id.tv_date);
        tvContent = findViewById(R.id.tv_content);
        tvLikeCount = findViewById(R.id.tv_like_count);
        tvCommentCount = findViewById(R.id.tv_comment_count);
        tvDescription = findViewById(R.id.tv_decription);
        commentLayout = findViewById(R.id.comment_layout);
        likeLayout = findViewById(R.id.like_layout);
    }
    private void initListener(){
        imgBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        likeLayout.setOnClickListener(v -> {
            if (postUrl != null) {
                toggleLike();
            } else {
                Toast.makeText(BlogDetailActivity.this, "Không thể thích bài viết, bài viết không tồn tại.", Toast.LENGTH_SHORT).show();
            }
        });

        commentLayout.setOnClickListener(v -> {
            if (postId != null && postUrl != null) {
                Intent intent = new Intent(BlogDetailActivity.this, CommentActivity.class);
                intent.putExtra("POST_ID", postId);
                intent.putExtra("POST_URL", postUrl);
                startActivity(intent);
            } else {
                Toast.makeText(BlogDetailActivity.this, "Không thể mở bình luận, thông tin bài viết không hợp lệ.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchBlogDetails() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getBlogDetails(postId).enqueue(new Callback<Blog>() {
            @Override
            public void onResponse(Call<Blog> call, Response<Blog> response) {
                Blog blog = response.body();
                updateUI(blog);
            }

            @Override
            public void onFailure(Call<Blog> call, Throwable t) {
                Toast.makeText(BlogDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void checkLiked() {
        String userId = SecureStorage.getInstance(BlogDetailActivity.this).getUserId();
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.checkLiked(postUrl, userId).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean isLiked = response.body();
                    updateLikeUI(isLiked);
                } else {
                    Toast.makeText(BlogDetailActivity.this, "Không thể kiểm tra trạng thái thích.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Toast.makeText(BlogDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchLikeCount() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getLikeCount(postUrl).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int likeCount = response.body();
                    String likeText = likeCount + " lượt thích";
                    tvLikeCount.setText(likeText);
                } else {
                    Toast.makeText(BlogDetailActivity.this, "Không thể lấy số lượt thích.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Toast.makeText(BlogDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleLike() {
        String userId = SecureStorage.getInstance(BlogDetailActivity.this).getUserId();
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.likePost(postUrl, userId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int updatedLikeCount = response.body();
                    String likeText = updatedLikeCount + " lượt thích";
                    tvLikeCount.setText(likeText);
                    checkLiked();
                } else {
                    Toast.makeText(BlogDetailActivity.this, "Không thể thích bài viết.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Toast.makeText(BlogDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getCommentCount(){
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getCommentCount(postId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if(response.isSuccessful() && response.body() != null){
                    int updateCommentCount = response.body();
                    String commentText = updateCommentCount + " bình luận";
                    tvCommentCount.setText(commentText);
                }else {
                    Toast.makeText(BlogDetailActivity.this, "Không thể lấy số bình luận.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Toast.makeText(BlogDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLikeUI(boolean isLiked) {
        if (isLiked) {
            imgLike.setImageResource(R.drawable.like);
            imgLike.setColorFilter(ContextCompat.getColor(this, R.color.primary));
            tvLikeCount.setTextColor(ContextCompat.getColor(this, R.color.primary));
        } else {
            imgLike.setImageResource(R.drawable.unlike);
            imgLike.setColorFilter(ContextCompat.getColor(this, R.color.black));
            tvLikeCount.setTextColor(ContextCompat.getColor(this, R.color.black));
        }
    }
}
