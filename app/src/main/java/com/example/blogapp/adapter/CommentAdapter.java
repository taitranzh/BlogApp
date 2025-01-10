package com.example.blogapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blogapp.R;
import com.example.blogapp.activity.SubCommentActivity;
import com.example.blogapp.api.ApiService;
import com.example.blogapp.api.RetrofitClient;
import com.example.blogapp.entities.Comment;
import com.example.blogapp.utils.SecureStorage;
import com.example.blogapp.utils.TimeUtils;
import com.example.blogapp.utils.Utils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private final List<Comment> comments;
    private final Context context;
    private final boolean isSubCommentActivity;

    public CommentAdapter(Context context, List<Comment> comments, boolean isSubCommentActivity) {
        this.context = context;
        this.comments = comments;
        this.isSubCommentActivity = isSubCommentActivity;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.tvAuthorName.setText(comment.getAuthorName());
        holder.tvContent.setText(comment.getContent());
        String timeAgo = TimeUtils.getTimeAgo(comment.getUpdatedDate());
        holder.tvDate.setText(timeAgo);

        if (!isSubCommentActivity) {
            holder.tvReplyCount.setVisibility(View.VISIBLE);
            if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
                String updateText = comment.getReplies().size() + " phản hồi";
                holder.tvReplyCount.setText(updateText);
            } else {
                holder.tvReplyCount.setText("0 phản hồi");
            }

            holder.tvReplyCount.setOnClickListener(v -> {
                Intent intent = new Intent(context, SubCommentActivity.class);
                intent.putExtra("POST_ID", comment.getBlogPostId());
                intent.putExtra("PARENT_ID", comment.getId());
                intent.putExtra("COMMENT_AUTHOR", comment.getAuthorName());
                intent.putExtra("COMMENT_CONTENT", comment.getContent());
                intent.putExtra("COMMENT_DATE", comment.getUpdatedDate().substring(0, 10));
                context.startActivity(intent);
            });
        } else {
            holder.tvReplyCount.setVisibility(View.GONE);
        }

        String currentUserId = SecureStorage.getInstance(context).getUserId();
        if (currentUserId != null && currentUserId.equals(comment.getUserId())) {
            holder.imgDelete.setVisibility(View.VISIBLE);

            holder.imgDelete.setOnClickListener(v -> showDeleteConfirmationDialog(comment.getId(), position));
        } else {
            holder.imgDelete.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthorName, tvContent, tvDate, tvReplyCount;
        ImageView imgEdit, imgDelete;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthorName = itemView.findViewById(R.id.tv_user_comment);
            tvContent = itemView.findViewById(R.id.tv_content_comment);
            tvDate = itemView.findViewById(R.id.tv_comment_date);
            tvReplyCount = itemView.findViewById(R.id.tv_reply);
//            imgEdit = itemView.findViewById(R.id.img_edit);
            imgDelete = itemView.findViewById(R.id.img_delete);
        }
    }

    private void deleteComment(String commentId, int position){
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.deleteComment(commentId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    comments.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, comments.size());
                    Toast.makeText(context, "Bình luận đã được xóa.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Không thể xóa bình luận. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog(String commentId, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa bình luận này?")
                .setPositiveButton("Đúng", (dialog, which) -> deleteComment(commentId, position))
                .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                .show();
    }

}
