package com.example.blogapp.entities;

public class Category {
    private String id;
    private String name;
    private String urlHandle;

    public Category(String id, String name, String urlHandle) {
        this.id = id;
        this.name = name;
        this.urlHandle = urlHandle;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrlHandle() {
        return urlHandle;
    }

    public void setUrlHandle(String urlHandle) {
        this.urlHandle = urlHandle;
    }
}
