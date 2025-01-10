package com.example.blogapp.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.blogapp.activity.CategoriesActivity;
import com.example.blogapp.entities.Category;
import com.example.blogapp.fragment.PostListFragment;

import java.util.List;

public class CategoryTabAdapter extends FragmentStateAdapter {
    private List<Category> categories;

    public CategoryTabAdapter(@NonNull CategoriesActivity activity, List<Category> categories) {
        super(activity);
        this.categories = categories;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle bundle = new Bundle();
        bundle.putString("categoryId", categories.get(position).getId());
        bundle.putString("categoryName", categories.get(position).getName());
        PostListFragment fragment = new PostListFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}

