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

@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = "http://localhost:3001", allowCredentials = "true")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @Autowired
    private NewsArticleRepository newsArticleRepository;

    // Endpoint for top headlines
    @GetMapping("/top-headlines")
    public ResponseEntity<?> getTopHeadlines(@RequestParam String country, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "12") int pageSize) {
        List<NewsArticle> articles = newsService.fetchTopHeadlinesWithFallback(country, page, pageSize);
        return ResponseEntity.ok(articles);
    }

    // Endpoint for news articles based on a search query
    @GetMapping("/search")
    public ResponseEntity<?> searchNews(
        @RequestParam String query, 
        //@RequestParam(required = false) String fromDate, 
        //@RequestParam(required = false) String toDate, 
        @RequestParam(defaultValue = "publishedAt") String sortBy,
        @RequestParam(defaultValue = "1") int page, // Default to first page
        @RequestParam(defaultValue = "12") int pageSize // Default to 12 results per page
    ) {
        List<NewsArticle> articles = newsService.fetchAndSaveNewsByQuery(query, sortBy, page, pageSize);
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/articles")
    public ResponseEntity<List<NewsArticle>> getArticlesByIds(@RequestParam List<String> ids) {
        List<NewsArticle> articles = newsArticleRepository.findAllById(ids);
        return ResponseEntity.ok(articles);
    }

}
