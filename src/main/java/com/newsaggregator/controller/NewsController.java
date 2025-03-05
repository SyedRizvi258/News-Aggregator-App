package com.newsaggregator.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.newsaggregator.model.NewsArticle;
import com.newsaggregator.repository.NewsArticleRepository;
import com.newsaggregator.service.NewsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;


/*
 * NewsController.java
 * 
 * This controller class defines the REST API endpoints for fetching news articles.
 */
@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = {"http://localhost:3001", "https://quickbyte-t50m.onrender.com"}, allowCredentials = "true") // Allow cross-origin requests from the specified URLs
public class NewsController {

    @Autowired
    private NewsService newsService; // Service class to fetch news articles

    @Autowired
    private NewsArticleRepository newsArticleRepository; // To interact with the database for news articles


    /**
     * Get top headlines news articles for a specific country.
     * 
     * @param country The country code for which to fetch top headlines
     * @param page The page number of results to fetch
     * @param pageSize The number of results per page
     * @return A list of NewsArticle objects
     */
    @GetMapping("/top-headlines")
    public ResponseEntity<?> getTopHeadlines(@RequestParam String country, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "12") int pageSize) {
        List<NewsArticle> articles = newsService.fetchTopHeadlinesWithFallback(country, page, pageSize);
        return ResponseEntity.ok(articles);
    }

    
    /**
     * Search for news articles based on a query string.
     * 
     * @param query The search query string
     * @param sortBy The order in which to sort the results (e.g., "publishedAt", "relevancy", "popularity")
     * @param page The page number of results to fetch
     * @param pageSize The number of results per page
     * @return A list of NewsArticle objects
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchNews(
        @RequestParam String query,
        @RequestParam(defaultValue = "publishedAt") String sortBy, // Default to sorting by published date
        @RequestParam(defaultValue = "1") int page, // Default to first page
        @RequestParam(defaultValue = "12") int pageSize // Default to 12 results per page
    ) {
        List<NewsArticle> articles = newsService.fetchAndSaveNewsByQuery(query, sortBy, page, pageSize);
        return ResponseEntity.ok(articles);
    }


    /**
     * Get news articles by their IDs.
     * 
     * @param ids A list of article IDs
     * @return A list of NewsArticle objects
     */
    @GetMapping("/articles")
    public ResponseEntity<List<NewsArticle>> getArticlesByIds(@RequestParam List<String> ids) {
        List<NewsArticle> articles = newsArticleRepository.findAllById(ids);
        return ResponseEntity.ok(articles);
    }
}
