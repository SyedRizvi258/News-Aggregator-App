package com.newsaggregator.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


/*
 * NewsArticle.java
 * 
 * This class represents a news article in the application.
 * It is mapped to the "news" collection in the MongoDB database.
 */
@Document(collection = "news")
public class NewsArticle {
    
    @Id
    private String id;
    
    private String title;
    private String description;
    private String url;
    private String sourceName;
    private String publishedAt;
    private String content;
    private String imageUrl;
    private boolean isHeadline = false;
    
    // Default Constructor
    public NewsArticle() {
    }

    // Parameterized Constructor
    public NewsArticle(String title, String description, String url, String sourceName, String publishedAt, String content, String imageUrl, boolean isHeadline) {
        this.title = title;
        this.description = description;
        this.url = url;
        this.sourceName = sourceName;
        this.publishedAt = publishedAt;
        this.content = content;
        this.imageUrl = imageUrl;
        this.isHeadline = isHeadline;
    }

    // Getters and Setters
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

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public String getSourceName() {
        return sourceName;
    }
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getPublishedAt() {
        return publishedAt;
    }
    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean getIsHeadline() {
        return isHeadline;
    }
    public void setIsHeadline(boolean isHeadline) {
        this.isHeadline = isHeadline;
    }
}
