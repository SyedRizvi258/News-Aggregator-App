package com.newsaggregator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.newsaggregator.model.NewsArticle;
import com.newsaggregator.service.FavoriteArticleService;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = {"http://localhost:3001", "https://quickbyte-t50m.onrender.com"}, allowCredentials = "true")
public class FavoriteArticleController {

    @Autowired
    private FavoriteArticleService favoriteArticleService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<NewsArticle>> getUserFavorites(@PathVariable String userId) {
        List<NewsArticle> favorites = favoriteArticleService.getFavoriteArticlesWithDetails(userId);
        return favorites.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(favorites);
    }

    @PostMapping("/{userId}/add/{articleId}")
    public ResponseEntity<String> addFavorite(@PathVariable String userId, @PathVariable String articleId) {
        favoriteArticleService.addFavoriteArticle(userId, articleId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Article added to favorites");
    }

    @DeleteMapping("/{userId}/remove/{articleId}")
    public ResponseEntity<String> removeFavorite(@PathVariable String userId, @PathVariable String articleId) {
        boolean removed = favoriteArticleService.removeFavoriteArticle(userId, articleId);
        return removed ? ResponseEntity.ok("Article removed from favorites") 
                       : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Article not found in favorites");
    }
}

