package com.newsaggregator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newsaggregator.repository.FavoriteArticleRepository;
import com.newsaggregator.repository.NewsArticleRepository;

import java.util.ArrayList;
import java.util.List;
import com.newsaggregator.model.FavoriteArticle;
import com.newsaggregator.model.NewsArticle;
import java.util.Optional;

@Service
public class FavoriteArticleService {

    @Autowired
    private FavoriteArticleRepository favoriteArticleRepository;

    @Autowired
    private NewsArticleRepository newsArticleRepository;

    // Get list of article IDs that a user has favorited
    public List<String> getFavoriteArticles(String userId) {
        return favoriteArticleRepository.findByUserId(userId)
                .map(FavoriteArticle::getArticleIds)
                .orElse(List.of()); // Return empty list if user has no favorites
    }

    // Get full news article details for a user's favorite articles
    public List<NewsArticle> getFavoriteArticlesWithDetails(String userId) {
        List<String> articleIds = getFavoriteArticles(userId);
        return articleIds.isEmpty() ? List.of() : newsArticleRepository.findAllById(articleIds);
    }

    // Add an article to the user's favorites
    @Transactional
    public void addFavoriteArticle(String userId, String articleId) {
        FavoriteArticle favoriteArticle = favoriteArticleRepository.findByUserId(userId)
                .orElseGet(() -> new FavoriteArticle(userId, new ArrayList<>()));

        if (!favoriteArticle.getArticleIds().contains(articleId)) {
            favoriteArticle.getArticleIds().add(articleId);
            favoriteArticleRepository.save(favoriteArticle);
        }
    }

    // Remove an article from the user's favorites
    @Transactional
    public boolean removeFavoriteArticle(String userId, String articleId) {
        Optional<FavoriteArticle> favoriteArticleOpt = favoriteArticleRepository.findByUserId(userId);
    
        if (favoriteArticleOpt.isPresent()) {
            FavoriteArticle favoriteArticle = favoriteArticleOpt.get();
            boolean removed = favoriteArticle.getArticleIds().remove(articleId);
    
            if (removed) { // Proceed only if an article was actually removed
                if (favoriteArticle.getArticleIds().isEmpty()) {
                    favoriteArticleRepository.delete(favoriteArticle); // Delete if empty
                } else {
                    favoriteArticleRepository.save(favoriteArticle);
                }
            }
            return removed; // Return true if removed, false otherwise
        }
        return false; // Return false if user has no favorites
    }    
}