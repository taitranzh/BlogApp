package com.example.blogapp.adapter;

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

import com.bumptech.glide.Glide;
import com.example.blogapp.R;
import com.example.blogapp.activity.BlogDetailActivity;
import com.example.blogapp.api.ApiService;
import com.example.blogapp.api.RetrofitClient;
import com.example.blogapp.entities.Blog;
import com.example.blogapp.utils.TimeUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlogHotAdapter extends RecyclerView.Adapter<BlogHotAdapter.BlogHotViewHolder> {
    private List<Blog> blogsHot;
    private Context context;

    public BlogHotAdapter(List<Blog> blogsHot, Context context) {
        this.context = context;
        this.blogsHot = blogsHot;
    }

    @NonNull
    @Override
    public BlogHotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_blog_carousel, parent, false);
        return new BlogHotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogHotViewHolder holder, int position) {
        Blog blog = blogsHot.get(position);
        holder.carouselTitle.setText(blog.getTitle());
        holder.userName.setText(blog.getAuthorName());
        String timeAgo = TimeUtils.getTimeAgo(blog.getPublishedDate());
        holder.publishedDate.setText(timeAgo);

        Glide.with(context)
                .load(blog.getFeaturedImageUrl())
                .placeholder(R.drawable.blog)
                .error(R.drawable.blog)
                .into(holder.imageCarousel);

        holder.itemView.setOnClickListener(v -> {
            checkBlogExistsAndNavigate(blog, position);
        });
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
                    blogsHot.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, blogsHot.size());
                    Toast.makeText(context, "Bài viết không tồn tại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Blog> call, Throwable t) {
                Toast.makeText(context, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return blogsHot.size();
    }

    public static class BlogHotViewHolder extends RecyclerView.ViewHolder {
        ImageView imageCarousel;
        TextView carouselTitle, userName, publishedDate;

        public BlogHotViewHolder(@NonNull View itemView) {
            super(itemView);
            imageCarousel = itemView.findViewById(R.id.carousel_image);
            carouselTitle = itemView.findViewById(R.id.carousel_title);
            userName = itemView.findViewById(R.id.carousel_author);
            publishedDate = itemView.findViewById(R.id.carousel_time);
        }
    }
}

