package com.example.blogapp.activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blogapp.R;
import com.example.blogapp.adapter.CommentAdapter;
import com.example.blogapp.api.ApiService;
import com.example.blogapp.api.RetrofitClient;
import com.example.blogapp.entities.Comment;
import com.example.blogapp.entities.request.CommentRequest;
import com.example.blogapp.layout.CustomSwipeRefreshLayout;
import com.example.blogapp.utils.SecureStorage;
import com.example.blogapp.utils.Utils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentActivity extends BaseActivity{
    private ImageView imgBack, imgSend;
    private EditText edtComment;
    private RecyclerView rvComments;
    private String postId, postUrl;
    private CustomSwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        postId = getIntent().getStringExtra("POST_ID");
        postUrl = getIntent().getStringExtra("POST_URL");
        initUI();

        if(postUrl != null){
            getComment();
        }

        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getComment();
    }

    private void initUI(){
        imgBack = findViewById(R.id.img_back);
        imgSend = findViewById(R.id.img_send);
        edtComment = findViewById(R.id.edt_comment);
        rvComments = findViewById(R.id.rv_comments);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    private void initListener(){
        imgBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });
        imgSend.setOnClickListener(v -> postComment());
        swipeRefreshLayout.setOnRefreshListener(() -> {
            getComment();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void postComment() {
        swipeRefreshLayout.setRefreshing(true);
        String content = edtComment.getText().toString().trim();
        if (content.isEmpty()) {
            return;
        }
        String userId = SecureStorage.getInstance(CommentActivity.this).getUserId();
        CommentRequest commentRequest = new CommentRequest(content, postId, userId);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.postComment(commentRequest).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CommentActivity.this, "Bình luận thành công!", Toast.LENGTH_SHORT).show();
                    edtComment.setText("");
                    getComment();
                } else {
                    Toast.makeText(CommentActivity.this, "Không thể gửi bình luận", Toast.LENGTH_SHORT).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Toast.makeText(CommentActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void getComment() {
        swipeRefreshLayout.setRefreshing(true);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getComments(postUrl).enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Comment> comments = response.body();
                    setupRecyclerView(comments);
                } else {
                    Toast.makeText(CommentActivity.this, "Không thể lấy danh sách bình luận", Toast.LENGTH_SHORT).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                Toast.makeText(CommentActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void setupRecyclerView(List<Comment> comments) {
        CommentAdapter adapter = new CommentAdapter(this, comments, false);
        rvComments.setAdapter(adapter);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
    }

}
