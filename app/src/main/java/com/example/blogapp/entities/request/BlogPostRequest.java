package com.example.blogapp.entities.request;

import java.util.List;

public class BlogPostRequest {
    private String title;
    private String shortDescription;
    private String content;
    private String featuredImageUrl;
    private String publishedDate;
    private String authorId;
    private boolean isVisible;
    private List<String> categories;

    public BlogPostRequest() {
    }

    public BlogPostRequest(String title, String shortDescription, String content, String featuredImageUrl, String publishedDate, String authorId, boolean isVisible, List<String> categories) {
        this.title = title;
        this.shortDescription = shortDescription;
        this.content = content;
        this.featuredImageUrl = featuredImageUrl;
        this.publishedDate = publishedDate;
        this.authorId = authorId;
        this.isVisible = isVisible;
        this.categories = categories;
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

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
}
