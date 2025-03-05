package com.newsaggregator.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.List;


/*
 * FavoriteArticle.java
 * 
 * This class represents a user's favorite article in the application.
 * It is mapped to the "favorites" collection in the MongoDB database.
 */
@Document(collection = "favorites")
public class FavoriteArticle {

    @Id
    private String id;

    @Indexed(unique = true) // Ensure only one document per user
    private String userId;
    
    private List<String> articleIds = new ArrayList<>(); // Storing only article IDs instead of full NewsArticle objects
    
    // Default Constructor
    public FavoriteArticle() {
    }

    // Parameterized Constructor
    public FavoriteArticle(String userId, List<String> articleIds) {
        this.userId = userId;
        this.articleIds = articleIds;
    }

    // Getters and Setters
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

    public List<String> getArticleIds() {
        return articleIds;
    }
    public void setArticleIds(List<String> articleIds) {
        this.articleIds = articleIds;
    }
}
