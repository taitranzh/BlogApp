package com.example.blogapp.entities;

import java.util.List;

public class Comment {
    private String id;
    private String userId;
    private String authorName;
    private String blogPostId;
    private String content;
    private String parentId;
    private String updatedDate;
    private List<Comment> replies;
    public Comment() {
    }
    public Comment(String id, String userId, String authorName, String blogPostId, String content, String parentId, String updatedDate, List<Comment> replies) {
        this.id = id;
        this.userId = userId;
        this.authorName = authorName;
        this.blogPostId = blogPostId;
        this.content = content;
        this.parentId = parentId;
        this.updatedDate = updatedDate;
        this.replies = replies;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getBlogPostId() {
        return blogPostId;
    }

    public void setBlogPostId(String blogPostId) {
        this.blogPostId = blogPostId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public List<Comment> getReplies() {
        return replies;
    }

    public void setReplies(List<Comment> replies) {
        this.replies = replies;
    }
}
