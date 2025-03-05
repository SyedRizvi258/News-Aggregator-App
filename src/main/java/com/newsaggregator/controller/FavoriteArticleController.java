package com.newsaggregator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.newsaggregator.model.NewsArticle;
import com.newsaggregator.service.FavoriteArticleService;
import java.util.List;


/*
 * FavoriteArticleController.java
 * 
 * This controller class defines the REST API endpoints for managing favorite articles.
 */
@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = {"http://localhost:3001", "https://quickbyte-t50m.onrender.com"}, allowCredentials = "true") // Allow cross-origin requests from the specified URLs
public class FavoriteArticleController {

    @Autowired
    private FavoriteArticleService favoriteArticleService; // Service class to manage favorite articles


    /**
     * Get the list of favorite articles for a user.
     * 
     * @param userId The ID of the user
     * @return A list of NewsArticle objects
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<NewsArticle>> getUserFavorites(@PathVariable String userId) {
        List<NewsArticle> favorites = favoriteArticleService.getFavoriteArticlesWithDetails(userId);
        return favorites.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(favorites);
    }


    /**
     * Add an article to a user's favorites.
     * 
     * @param userId The ID of the user
     * @param articleId The ID of the article to add
     * @return A response entity with a success message
     */
    @PostMapping("/{userId}/add/{articleId}")
    public ResponseEntity<String> addFavorite(@PathVariable String userId, @PathVariable String articleId) {
        favoriteArticleService.addFavoriteArticle(userId, articleId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Article added to favorites");
    }


    /**
     * Remove an article from a user's favorites.
     * 
     * @param userId The ID of the user
     * @param articleId The ID of the article to remove
     * @return A response entity with a success message or a not found status
     */
    @DeleteMapping("/{userId}/remove/{articleId}")
    public ResponseEntity<String> removeFavorite(@PathVariable String userId, @PathVariable String articleId) {
        boolean removed = favoriteArticleService.removeFavoriteArticle(userId, articleId);
        return removed ? ResponseEntity.ok("Article removed from favorites") 
                       : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Article not found in favorites");
    }
}
