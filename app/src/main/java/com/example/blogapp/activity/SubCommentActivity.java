package com.example.blogapp.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

public class SubCommentActivity extends BaseActivity{
    private ImageView imgBack, imgSend;
    private TextView tvDate, tvUsername, tvContent;
    private EditText edtComment;
    private RecyclerView rcvSubComment;
    private String postId;
    private String parentId;
    private CustomSwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_comment);
        initUI();

        postId = getIntent().getStringExtra("POST_ID");
        parentId = getIntent().getStringExtra("PARENT_ID");
        String commentAuthor = getIntent().getStringExtra("COMMENT_AUTHOR");
        String commentContent = getIntent().getStringExtra("COMMENT_CONTENT");
        String commentDate = getIntent().getStringExtra("COMMENT_DATE");

        if (commentAuthor != null && commentContent != null && commentDate != null) {
            tvUsername.setText(commentAuthor);
            tvContent.setText(commentContent);
            tvDate.setText(commentDate);
        }

        if (postId != null && parentId != null) {
            fetchSubComments();
        }

        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchSubComments();
    }

    private void initUI(){
        imgBack = findViewById(R.id.img_back);
        imgSend = findViewById(R.id.img_send);
        tvDate = findViewById(R.id.tv_comment_date);
        tvUsername = findViewById(R.id.tv_user_comment);
        tvContent = findViewById(R.id.tv_content_comment);
        edtComment = findViewById(R.id.edt_comment);
        rcvSubComment = findViewById(R.id.rv_comments);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }
    private void initListener(){
        imgBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });
        imgSend.setOnClickListener(v -> postSubComment());
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchSubComments();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void postSubComment() {
        swipeRefreshLayout.setRefreshing(true);
        String content = edtComment.getText().toString().trim();
        if (content.isEmpty()) {
            return;
        }

        if (parentId == null) {
            Toast.makeText(SubCommentActivity.this, "Bình luận không còn tồn tại.", Toast.LENGTH_SHORT).show();
            getOnBackPressedDispatcher().onBackPressed();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        String userId = SecureStorage.getInstance(SubCommentActivity.this).getUserId();
        CommentRequest commentRequest = new CommentRequest(content, postId, userId, parentId);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.postComment(commentRequest).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    edtComment.setText("");
                    fetchSubComments();
                } else {
                    Toast.makeText(SubCommentActivity.this, "Không thể gửi bình luận", Toast.LENGTH_SHORT).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Toast.makeText(SubCommentActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    private void fetchSubComments() {
        swipeRefreshLayout.setRefreshing(true);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getSubComments(postId, parentId).enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Comment> subComments = response.body();
                    setupRecyclerView(subComments);
                } else {
                    Toast.makeText(SubCommentActivity.this, "Không thể lấy danh sách bình luận", Toast.LENGTH_SHORT).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                Toast.makeText(SubCommentActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void setupRecyclerView(List<Comment> comments){
        CommentAdapter adapter = new CommentAdapter(this, comments, true);
        rcvSubComment.setAdapter(adapter);
        rcvSubComment.setLayoutManager(new LinearLayoutManager(this));
    }

}
