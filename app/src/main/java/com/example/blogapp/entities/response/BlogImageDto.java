package com.example.blogapp.entities.response;

public class BlogImageDto {
    private String id;
    private String fileName;
    private String fileExtension;
    private String title;
    private String url;
    private String createdDate;
    private String updatedDate;
    public BlogImageDto() {
    }
    public BlogImageDto(String id, String fileName, String fileExtension, String title, String url, String createdDate, String updatedDate) {
        this.id = id;
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.title = title;
        this.url = url;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }
}
