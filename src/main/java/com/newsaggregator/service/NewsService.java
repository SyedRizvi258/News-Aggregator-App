package com.newsaggregator.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsaggregator.model.NewsArticle;
import com.newsaggregator.repository.NewsArticleRepository;

@Service
public class NewsService {

    @Autowired
    private NewsArticleRepository newsArticleRepository;

    @Value("${newsapi.key}")
    private String apiKey;

    // Helper method to fetch and save articles from the News API
    private List<NewsArticle> fetchAndSaveArticles(String url, boolean isHeadline) {
        List<NewsArticle> savedArticles = new ArrayList<>();
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode articlesNode = rootNode.path("articles");

            if (articlesNode.isArray()) {
                for (JsonNode articleNode : articlesNode) {
                    String articleUrl = articleNode.path("url").asText();
                    
                    // For headlines, try to find existing article
                    NewsArticle article;
                    if (isHeadline) {
                        article = newsArticleRepository.findByUrl(articleUrl)
                            .orElse(new NewsArticle());
                    } else {
                        // For search, only proceed if article doesn't exist
                        if (newsArticleRepository.existsByUrl(articleUrl)) {
                            continue;
                        }
                        article = new NewsArticle();
                    }
                    
                    // Update/set all properties
                    article.setTitle(articleNode.path("title").asText());
                    article.setDescription(articleNode.path("description").asText());
                    article.setUrl(articleUrl);
                    article.setSourceName(articleNode.path("source").path("name").asText());
                    article.setPublishedAt(articleNode.path("publishedAt").asText());
                    article.setContent(articleNode.path("content").asText());
                    article.setImageUrl(articleNode.path("urlToImage").asText());
                    article.setIsHeadline(isHeadline);
            
                    newsArticleRepository.save(article);
                    savedArticles.add(article);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Return empty list to trigger database fallback
            return new ArrayList<>();
        }
        return savedArticles;
    }

    // Fetch and save top headlines from the News API
    public List<NewsArticle> fetchAndSaveTopHeadlines(String country, int page, int pageSize) {
        String url = "https://newsapi.org/v2/top-headlines?country=" + country 
                + "&page=" + page
                + "&pageSize=" + pageSize
                + "&apiKey=" + apiKey;
        return fetchAndSaveArticles(url, true);
    }

    // Fetch and save top headlines from the News API with a fallback to recent articles from the database
    public List<NewsArticle> fetchTopHeadlinesWithFallback(String country, int page, int pageSize) {
        List<NewsArticle> apiArticles = fetchAndSaveTopHeadlines(country, page, pageSize );

        // Get ALL headline articles from database (without pagination)
        List<NewsArticle> dbArticles = newsArticleRepository.findByIsHeadlineTrue();

        // Create a map of URL to article from API results
        Map<String, NewsArticle> articleMap = new HashMap<>();
        for (NewsArticle article : apiArticles) {
            articleMap.put(article.getUrl(), article);
        }
        
        // Add database articles that aren't duplicates of API articles
        for (NewsArticle article : dbArticles) {
            if (!articleMap.containsKey(article.getUrl())) {
                articleMap.put(article.getUrl(), article);
            }
        }
        
        // Convert map values to list and sort by published date (newest first)
        List<NewsArticle> allArticles = new ArrayList<>(articleMap.values());
        allArticles.sort(Comparator.comparing(NewsArticle::getPublishedAt).reversed());
        
        // Apply pagination manually
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allArticles.size());
        
        // Handle case where startIndex is beyond available articles
        if (startIndex >= allArticles.size()) {
            return new ArrayList<>();
        }

        return allArticles.subList(startIndex, endIndex);
    }

    // Fetch and save news articles based on a search query
    public List<NewsArticle> fetchAndSaveNewsByQuery(String query, String sortBy, int page, int pageSize) {
        String url = "https://newsapi.org/v2/everything?q=" + query
                + "&sortBy=" + sortBy
                + "&language=en"
                + "&page=" + page
                + "&pageSize=" + pageSize
                + "&apiKey=" + apiKey;

        List<NewsArticle> apiArticles = fetchAndSaveArticles(url, false);

        // Get ALL matching articles from database (without pagination)
        List<NewsArticle> dbArticles = newsArticleRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            query, query);
        
        // Create a map of URL to article from API results
        Map<String, NewsArticle> articleMap = new HashMap<>();
        for (NewsArticle article : apiArticles) {
            articleMap.put(article.getUrl(), article);
        }
        
        // Add database articles that aren't duplicates of API articles
        for (NewsArticle article : dbArticles) {
            if (!articleMap.containsKey(article.getUrl())) {
                articleMap.put(article.getUrl(), article);
            }
        }
        
        // Convert map values to list and sort by published date (newest first)
        List<NewsArticle> allArticles = new ArrayList<>(articleMap.values());
        allArticles.sort(Comparator.comparing(NewsArticle::getPublishedAt).reversed());
        
        // Apply pagination manually
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allArticles.size());
        
        // Handle case where startIndex is beyond available articles
        if (startIndex >= allArticles.size()) {
            return new ArrayList<>();
        }
        
        return allArticles.subList(startIndex, endIndex);
    }

    // Periodically fetch top headlines every 3 hours
    @Scheduled(fixedRate = 10800000) // 3 hours in milliseconds
    public void fetchLatestTopHeadlines() {
        System.out.println("Fetching latest top headlines...");
        fetchAndSaveTopHeadlines("us", 1, 20);
    }

    // Periodically delete articles older than 30 days
    @Scheduled(cron = "0 0 12 * * ?") // Everyday at 12:00 PM
    public void deleteOldArticles() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(30);
        newsArticleRepository.deleteByPublishedAtBefore(oneMonthAgo);
    }

}
