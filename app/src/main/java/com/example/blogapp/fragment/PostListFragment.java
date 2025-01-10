package com.example.blogapp.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.blogapp.R;
import com.example.blogapp.adapter.BlogAdapter;
import com.example.blogapp.entities.Blog;
import com.example.blogapp.api.ApiService;
import com.example.blogapp.api.RetrofitClient;
import com.example.blogapp.layout.CustomSwipeRefreshLayout;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostListFragment extends Fragment {

    private RecyclerView recyclerView;
    private BlogAdapter blogAdapter;
    private List<Blog> blogList;
    private String categoryId;
    private CustomSwipeRefreshLayout swipeRefreshLayout;

    public PostListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_category, container, false);
        recyclerView = rootView.findViewById(R.id.rcv_category);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);

        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
        }

        swipeRefreshLayout.setOnRefreshListener(this::loadBlogPosts);
        loadBlogPosts();

        return rootView;
    }

    private void loadBlogPosts() {
        if (categoryId == null) {
            Toast.makeText(getContext(), "Không tìm thấy danh mục", Toast.LENGTH_SHORT).show();
            return;
        }
        swipeRefreshLayout.setRefreshing(true);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getBlogsByCategory(categoryId, 1000).enqueue(new Callback<List<Blog>>() {
            @Override
            public void onResponse(Call<List<Blog>> call, Response<List<Blog>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    blogList = response.body();
                    blogAdapter = new BlogAdapter(blogList, getContext(), false);
                    recyclerView.setAdapter(blogAdapter);
                } else {
                    Toast.makeText(getContext(), "Không thể tải bài viết", Toast.LENGTH_SHORT).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Blog>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBlogPosts();
    }
}
