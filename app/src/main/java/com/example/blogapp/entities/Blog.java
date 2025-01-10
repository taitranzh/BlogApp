package com.example.blogapp.entities;

import java.util.List;

public class Blog {
    private String id;
    private String title;
    private String shortDescription;
    private String content;
    private String featuredImageUrl;
    private String urlHandle;
    private String publishedDate;
    private String authorName;
    private boolean isVisible;
    private List<Category> categories;

    public Blog(String id, String title, String shortDescription, String content, String featuredImageUrl, String urlHandle, String publishedDate, String authorName, boolean isVisible, List<Category> categories) {
        this.id = id;
        this.title = title;
        this.shortDescription = shortDescription;
        this.content = content;
        this.featuredImageUrl = featuredImageUrl;
        this.urlHandle = urlHandle;
        this.publishedDate = publishedDate;
        this.authorName = authorName;
        this.isVisible = isVisible;
        this.categories = categories;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFeaturedImageUrl() {
        return featuredImageUrl;
    }

    public void setFeaturedImageUrl(String featuredImageUrl) {
        this.featuredImageUrl = featuredImageUrl;
    }

    public String getUrlHandle() {
        return urlHandle;
    }

    public void setUrlHandle(String urlHandle) {
        this.urlHandle = urlHandle;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
}