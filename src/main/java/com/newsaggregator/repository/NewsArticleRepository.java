package com.newsaggregator.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.domain.Pageable;
import com.newsaggregator.model.NewsArticle;

import java.util.List;
import java.util.Optional;


public interface NewsArticleRepository extends MongoRepository<NewsArticle, String> {
    //boolean existsByTitle(String title);
    //Page<NewsArticle> findAllByOrderByPublishedAtDesc(Pageable pageable);
    boolean existsByUrl(String url);
    Optional<NewsArticle> findByUrl(String url);
    void deleteByPublishedAtBefore(LocalDateTime dateTime);
    @Query("{ '_id': { $in: ?0 } }")
    List<NewsArticle> findAllById(List<String> articleIds);
    List<NewsArticle> findByIsHeadlineTrue();
    List<NewsArticle> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String titleQuery, String descriptionQuery);
    Page<NewsArticle> findByIsHeadlineTrueOrderByPublishedAtDesc(Pageable pageable);
    Page<NewsArticle> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByPublishedAtDesc(String titleQuery, String descriptionQuery, Pageable pageable);
    Page<NewsArticle> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
    String titleQuery, String descriptionQuery, Pageable pageable);

}
