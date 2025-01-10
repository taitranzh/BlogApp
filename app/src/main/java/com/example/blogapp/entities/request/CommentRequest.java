package com.example.blogapp.entities.request;

public class CommentRequest {
    private String content;
    private String postId;
    private String userId;
    private String parentId;

    public CommentRequest(String content, String postId, String userId) {
        this.content = content;
        this.postId = postId;
        this.userId = userId;
    }
    public CommentRequest(String content, String postId, String userId, String parentId) {
        this.content = content;
        this.postId = postId;
        this.userId = userId;
        this.parentId = parentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postUrl) {
        this.postId = postUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
