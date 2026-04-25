package com.newsaggregator.repository;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;
import java.util.Optional;

import com.newsaggregator.model.NewsArticle;


/*
 * NewsArticleRepository.java
 * 
 * This repository interface is used to interact with the MongoDB database news collection.
 * It provides methods to query the database for news articles.
 */
public interface NewsArticleRepository extends MongoRepository<NewsArticle, String> {
    boolean existsByUrl(String url);
    Optional<NewsArticle> findByUrl(String url);
    void deleteByPublishedAtBefore(LocalDateTime dateTime);

    @Query("{ '_id': { $in: ?0 } }")
    List<NewsArticle> findAllById(List<String> articleIds);

    Page<NewsArticle> findByIsHeadlineTrue(Pageable pageable);
    Page<NewsArticle> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String titleQuery, String descriptionQuery, Pageable pageable);
}
