package com.example.blogapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.blogapp.R;
import com.example.blogapp.activity.BlogDetailActivity;
import com.example.blogapp.activity.CreateBlogActivity;
import com.example.blogapp.api.ApiService;
import com.example.blogapp.api.RetrofitClient;
import com.example.blogapp.entities.Blog;
import com.example.blogapp.dialog.OtpDialogManager;
import com.example.blogapp.dialog.PasswordDialogManager;
import com.example.blogapp.utils.SecureStorage;
import com.example.blogapp.utils.TimeUtils;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.BlogViewHolder> {
    private List<Blog> blogs;
    private Context context;
    private boolean showIconMore;
    private int selectedBlogPosition;
    private OtpDialogManager otpDialogManager;
    private PasswordDialogManager passwordDialogManager;
    public BlogAdapter(List<Blog> blogs,Context context,  boolean showIconMore) {
        this.context = context;
        this.blogs = blogs;
        this.showIconMore = showIconMore;
        this.otpDialogManager = new OtpDialogManager();
        this.passwordDialogManager = new PasswordDialogManager();
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_blog, parent, false);
        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        Blog blog = blogs.get(position);
        holder.title.setText(blog.getTitle());
        holder.userName.setText(blog.getAuthorName());
        String timeAgo = TimeUtils.getTimeAgo(blog.getPublishedDate());
        holder.publishedDate.setText(timeAgo);

        Glide.with(context)
                .load(blog.getFeaturedImageUrl())
                .placeholder(R.drawable.blog)
                .error(R.drawable.blog)
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> {
            checkBlogExistsAndNavigate(blog, position);
        });

        if (showIconMore) {
            holder.iconMore.setVisibility(View.VISIBLE);
            holder.iconMore.setOnClickListener(v -> showPopupMenu(holder.iconMore, position));
        } else {
            holder.iconMore.setVisibility(View.GONE);
        }
    }
    private void checkBlogExistsAndNavigate(Blog blog, int position) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getBlogDetails(blog.getId()).enqueue(new Callback<Blog>() {
            @Override
            public void onResponse(Call<Blog> call, Response<Blog> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Blog blogDetail = response.body();
                    Intent intent = new Intent(context, BlogDetailActivity.class);
                    intent.putExtra("BLOG_ID", blogDetail.getId());
                    intent.putExtra("BLOG_URL", blogDetail.getUrlHandle());
                    context.startActivity(intent);
                } else {
                    blogs.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, blogs.size());
                    Toast.makeText(context, "Bài viết không tồn tại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Blog> call, Throwable t) {
                Toast.makeText(context, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showPopupMenu(View anchor, int position) {
        PopupMenu popupMenu = new PopupMenu(context, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.menu_blog_options, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            Blog blog = blogs.get(position);
            if (item.getItemId() == R.id.action_edit) {
                Intent intent = new Intent(context, CreateBlogActivity.class);
                intent.putExtra("BLOG_ID", blog.getId());
                intent.putExtra("BLOG_TITLE", blog.getTitle());
                intent.putExtra("BLOG_SHORT_DESCRIPTION", blog.getShortDescription());
                intent.putExtra("BLOG_CONTENT", blog.getContent());
                intent.putExtra("BLOG_FEATURED_IMAGE", blog.getFeaturedImageUrl());
                String categoryId = blog.getCategories().isEmpty() ? "" : String.valueOf(blog.getCategories().get(0).getId());
                intent.putExtra("BLOG_CATEGORY_ID", categoryId);
                intent.putExtra("BLOG_IS_VISIBLE", blog.isVisible());

                context.startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.action_delete) {
                checkMfaVerified(position);
            }
            return false;
        });

        popupMenu.show();
    }
    private void checkMfaVerified(int position) {
        AlertDialog loadingDialog = showLoadingDialog();
        selectedBlogPosition = position;
        Blog blogToDelete = blogs.get(selectedBlogPosition);
        String blogId = blogToDelete.getId();
        String userId = SecureStorage.getInstance(context).getUserId();

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.mfaVerify(userId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null && response.body().string().contains("User current mfa verified")) {
                            deleteBlog(blogId);
                        } else {
                            showPasswordDialog();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorMessage = response.errorBody().string();
                            if (errorMessage.contains("User is not mfa verified")) {
                                showPasswordDialog();
                            } else {
                                Toast.makeText(context, "Lỗi kết nối MFA.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(context, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showPasswordDialog() {
        if (passwordDialogManager.isDialogShowing()) {
            return;
        }
        passwordDialogManager.showPasswordDialog(context, password -> verifyPassword(password));
    }
    private void verifyPassword(String password) {
        AlertDialog loadingDialog = showLoadingDialog();
        String userId = SecureStorage.getInstance(context).getUserId();
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.verifyPassword(userId, password).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null && response.body().string().contains("Password is correct")) {
                            if (passwordDialogManager.isDialogShowing()) {
                                passwordDialogManager.dismissDialog();
                            }
                            sendEmailDeleteOtp();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    passwordDialogManager.setError("Mật khẩu không đúng");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(context, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void sendEmailDeleteOtp(){
        AlertDialog loadingDialog = showLoadingDialog();
        Blog blogToDelete = blogs.get(selectedBlogPosition);
        String blogId = blogToDelete.getId();
        String userId = SecureStorage.getInstance(context).getUserId();
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.deletePostSendEmail(blogId, userId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                loadingDialog.dismiss();
                if(response.isSuccessful()){
                    showOtpDialog();
                } else {
                    Toast.makeText(context, "Gửi email thất bại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(context, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void deleteBlog(String postId) {
        AlertDialog loadingDialog = showLoadingDialog();
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.deleteBlog(postId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful()) {
                    blogs.remove(selectedBlogPosition);
                    notifyItemRemoved(selectedBlogPosition);
                    notifyItemRangeChanged(selectedBlogPosition, blogs.size());
                    Toast.makeText(context, "Xóa bài viết thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Gửi otp thất bại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(context, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showOtpDialog() {
        if (otpDialogManager.isDialogShowing()) return;
        otpDialogManager.showOtpDialog(context, new OtpDialogManager.OtpListener() {
            @Override
            public void onOtpConfirmed(String otp) {
                verifyOtp(otp);
            }

            @Override
            public void onResendOtp() {
                sendEmailDeleteOtp();
                Toast.makeText(context, "OTP đã được gửi lại!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyOtp(String otp) {
        Blog blogToDelete = blogs.get(selectedBlogPosition);
        String blogId = blogToDelete.getId();
        String userId = SecureStorage.getInstance(context).getUserId();

        AlertDialog loadingDialog = showLoadingDialog();

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.deletePostVerify(blogId, userId, otp).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful()) {
                    if (otpDialogManager.isDialogShowing()) {
                        otpDialogManager.dismissDialog();
                    }
                    deleteBlog(blogId);
                } else {
                    otpDialogManager.setError("Mã OTP không chính xác");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(context, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private AlertDialog showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(LayoutInflater.from(context).inflate(R.layout.dialog_loading, null));
        builder.setCancelable(false);
        AlertDialog loadingDialog = builder.create();
        loadingDialog.show();
        return loadingDialog;
    }
    @Override
    public int getItemCount() {
        return blogs.size();
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        ImageView image, iconMore;
        TextView title, userName, publishedDate;
        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.blog_img);
            title = itemView.findViewById(R.id.blog_title);
            userName = itemView.findViewById(R.id.blog_username);
            publishedDate = itemView.findViewById(R.id.blog_create_at);
            iconMore = itemView.findViewById(R.id.icon_more);
        }
    }
}
