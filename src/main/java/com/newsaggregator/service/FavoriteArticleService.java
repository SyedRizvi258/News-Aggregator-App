package com.newsaggregator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import com.newsaggregator.model.FavoriteArticle;
import com.newsaggregator.model.NewsArticle;
import java.util.Optional;

import com.newsaggregator.repository.FavoriteArticleRepository;
import com.newsaggregator.repository.NewsArticleRepository;


/*
 * FavoriteArticleService.java
 * 
 * This service class handles the business logic for managing favorite articles.
    - Gets the list of article IDs that a user has favorited.
    - Gets the full news article details for a user's favorite articles.
    - Adds an article to the user's favorites.
    - Removes an article from the user's favorites.
 */
@Service
public class FavoriteArticleService {

    @Autowired
    private FavoriteArticleRepository favoriteArticleRepository; // To interact with the database for favorite articles

    @Autowired
    private NewsArticleRepository newsArticleRepository; // To interact with the database for news articles


    /**
     * Get the list of article IDs that a user has favorited.
     * 
     * @param userId The ID of the user
     * @return A list of article IDs that the user has favorited
     */
    public List<String> getFavoriteArticles(String userId) {
        return favoriteArticleRepository.findByUserId(userId)
                .map(FavoriteArticle::getArticleIds)
                .orElse(List.of()); // Return empty list if user has no favorites
    }


    /**
     * Get the full news article details for a user's favorite articles.
     * 
     * @param userId The ID of the user
     * @return A list of NewsArticle objects for the user's favorite articles
     */
    public List<NewsArticle> getFavoriteArticlesWithDetails(String userId) {
        List<String> articleIds = getFavoriteArticles(userId);
        return articleIds.isEmpty() ? List.of() : newsArticleRepository.findAllById(articleIds); 
    }


    /**
     * Add an article to the user's favorites.
     * 
     * @param userId The ID of the user
     * @param articleId The ID of the article to add to favorites
     */
    @Transactional
    public void addFavoriteArticle(String userId, String articleId) {
        // Get the user's favorite articles or create a new entry if it doesn't exist
        FavoriteArticle favoriteArticle = favoriteArticleRepository.findByUserId(userId)
                .orElseGet(() -> new FavoriteArticle(userId, new ArrayList<>()));

        // If the article is not already in the user's favorites, add it
        if (!favoriteArticle.getArticleIds().contains(articleId)) {
            favoriteArticle.getArticleIds().add(articleId);
            favoriteArticleRepository.save(favoriteArticle);
        }
    }


    /**
     * Remove an article from the user's favorites.
     * 
     * @param userId The ID of the user
     * @param articleId The ID of the article to remove from favorites
     * @return true if the article was removed, false otherwise
     */
    @Transactional
    public boolean removeFavoriteArticle(String userId, String articleId) {
        Optional<FavoriteArticle> favoriteArticleOpt = favoriteArticleRepository.findByUserId(userId);
    
        // If the user has favorite articles, remove the specified article
        if (favoriteArticleOpt.isPresent()) {
            FavoriteArticle favoriteArticle = favoriteArticleOpt.get();
            boolean removed = favoriteArticle.getArticleIds().remove(articleId);
    
            if (removed) { // Proceed only if an article was actually removed
                if (favoriteArticle.getArticleIds().isEmpty()) {
                    favoriteArticleRepository.delete(favoriteArticle); // Delete if empty
                } else {
                    favoriteArticleRepository.save(favoriteArticle); // Save changes if not empty
                }
            }
            return removed; // Return true if removed, false otherwise
        }
        return false; // Return false if user has no favorites
    }    
}
