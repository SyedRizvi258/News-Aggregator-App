package com.newsaggregator.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Comparator;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.newsaggregator.model.NewsArticle;
import com.newsaggregator.repository.NewsArticleRepository;


/*
 * NewsService.java
 * 
 * This service class handles fetching news articles from the News API and saving them to the database.
    - Uses the News API to fetch news articles based on the provided parameters.
    - Saves the fetched articles to the database.
    - Provides methods to fetch top headlines and news articles based on a search query.
    - Includes scheduled tasks to fetch top headlines every 3 hours and delete old articles every day.
 */
@Service
public class NewsService {

    @Autowired
    private NewsArticleRepository newsArticleRepository; // To interact with the database for news articles

    @Value("${newsapi.key}")
    private String apiKey; // API key for the News API


    /**
    * Helper method to fetch articles from the News API and save them to the database.
    * For headlines: updates existing articles if they already exist by URL.
    * For search: skips saving articles that already exist to avoid duplicates.
    * Parses the JSON response, extracts article data, and stores it in the database.
    *
    * @param url The API endpoint URL to fetch articles from.
    * @param isHeadline Indicates whether the articles are top headlines (true) or search results (false).
    * @return A list of saved NewsArticle objects. Returns an empty list if an error occurs.
    */
    private List<NewsArticle> fetchAndSaveArticles(String url, boolean isHeadline) {
        List<NewsArticle> savedArticles = new ArrayList<>();
        

        try {
            RestTemplate restTemplate = new RestTemplate(); 
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class); // Send a GET request to the API endpoint

            ObjectMapper objectMapper = new ObjectMapper(); // Create an object mapper to parse JSON
            JsonNode rootNode = objectMapper.readTree(response.getBody()); // Parse the JSON response
            JsonNode articlesNode = rootNode.path("articles"); // Extract the articles node from the JSON response

            // Iterate over each article in the response
            if (articlesNode.isArray()) {
                for (JsonNode articleNode : articlesNode) {
                    String articleUrl = articleNode.path("url").asText(); // Extract the URL of the article

                    NewsArticle article;

                    // For headlines, update existing articles if they already exist by URL
                    if (isHeadline) {
                        article = newsArticleRepository.findByUrl(articleUrl)
                            .orElse(new NewsArticle());
                    } else {
                        // For search results, skip saving articles that already exist to avoid duplicates
                        if (newsArticleRepository.existsByUrl(articleUrl)) {
                            continue;
                        }
                        article = new NewsArticle();
                    }
                    
                    // Update/set all fields of the article from the JSON node
                    article.setTitle(articleNode.path("title").asText());
                    article.setDescription(articleNode.path("description").asText());
                    article.setUrl(articleUrl);
                    article.setSourceName(articleNode.path("source").path("name").asText());
                    article.setPublishedAt(articleNode.path("publishedAt").asText());
                    article.setContent(articleNode.path("content").asText());
                    article.setImageUrl(articleNode.path("urlToImage").asText());
                    article.setIsHeadline(isHeadline);
                    
                    // Save the article to the database
                    newsArticleRepository.save(article);

                    // Add the saved article to the list
                    savedArticles.add(article);
                }
            }
        } catch (Exception e) { 
            // Return empty list to trigger database fallback
            return new ArrayList<>();
        }
        return savedArticles;
    }


    /**
     * Fetch and save top headlines from the News API based on the country code.
     * Saves the articles to the database and returns a list of saved NewsArticle objects.
     * 
     * @param country The country code for which to fetch top headlines (e.g., "us" for United States).
     * @param page The page number.
     * @param pageSize The number of articles per page to fetch.
     * @return A list of saved NewsArticle objects. Returns an empty list if an error occurs.
     */
    public List<NewsArticle> fetchAndSaveTopHeadlines(String country, int page, int pageSize) {
        String url = "https://newsapi.org/v2/top-headlines?country=" + country 
                + "&page=" + page
                + "&pageSize=" + pageSize
                + "&apiKey=" + apiKey;
        return fetchAndSaveArticles(url, true); // isHeadline = true for top headlines
    }

    /**
     * Fetch top headlines from the News API based on the country code.
     * If an error occurs during fetching, fallback to using the database to get headlines.
     * Combines API results with database results to ensure no duplicates and applies pagination.
     * 
     * @param country The country code for which to fetch top headlines (e.g., "us" for United States).
     * @param page The page number.
     * @param pageSize The number of articles per page to fetch.
     * @return A list of NewsArticle objects representing the top headlines.
     */
    public List<NewsArticle> fetchTopHeadlinesWithFallback(String country, int page, int pageSize) {
        List<NewsArticle> apiArticles = fetchAndSaveTopHeadlines(country, page, pageSize);

        // If an error occurred during API fetching, use database fallback
        List<NewsArticle> dbArticles = newsArticleRepository.findByIsHeadlineTrue();

        // Create a map of URL to article from API results
        Map<String, NewsArticle> articleMap = new HashMap<>();
        for (NewsArticle article : apiArticles) {
            articleMap.put(article.getUrl(), article);
        }
        
        // Add database articles to the map, overwriting any duplicates from the API
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

        // Return the paginated list of articles
        return allArticles.subList(startIndex, endIndex);
    }


    /**
     * Fetch and save news articles from the News API based on a search query.
     * Combines API results with database results to ensure no duplicates and applies pagination.
     * 
     * @param query The search query to fetch news articles for.
     * @param sortBy The sorting criteria for the articles (e.g., "publishedAt").
     * @param page The page number.
     * @param pageSize The number of articles per page to fetch.
     * @return A list of NewsArticle objects representing the search results.
     */
    public List<NewsArticle> fetchAndSaveNewsByQuery(String query, String sortBy, int page, int pageSize) {
        String url = "https://newsapi.org/v2/everything?q=" + query
                + "&sortBy=" + sortBy
                + "&language=en"
                + "&page=" + page
                + "&pageSize=" + pageSize
                + "&apiKey=" + apiKey;

        // Fetch and save articles from the API
        List<NewsArticle> apiArticles = fetchAndSaveArticles(url, false);

        // Get articles from the database that match the search query
        List<NewsArticle> dbArticles = newsArticleRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            query, query);
        
        // Create a map of URL to article from API results
        Map<String, NewsArticle> articleMap = new HashMap<>();
        for (NewsArticle article : apiArticles) {
            articleMap.put(article.getUrl(), article);
        }
        
        // Combine API and database results, removing duplicates
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
        
        // Return the paginated list of articles
        return allArticles.subList(startIndex, endIndex);
    }

    
    // Periodically fetch top headlines every 3 hours
    @Scheduled(fixedRate = 10800000) // 3 hours in milliseconds
    public void fetchLatestTopHeadlines() {
        System.out.println("Fetching latest top headlines...");
        fetchAndSaveTopHeadlines("us", 1, 20);
    }

    // Periodically delete articles older than 30 days to stop database from growing indefinitely
    @Scheduled(cron = "0 0 12 * * ?") // Everyday at 12:00 PM
    public void deleteOldArticles() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(30);
        newsArticleRepository.deleteByPublishedAtBefore(oneMonthAgo);
    }

}
