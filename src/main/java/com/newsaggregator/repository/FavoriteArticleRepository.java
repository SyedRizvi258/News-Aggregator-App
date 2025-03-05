package com.newsaggregator.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

import com.newsaggregator.model.FavoriteArticle;


/*
 * FavoriteArticleRepository.java
 * 
 * This repository interface is used to interact with the MongoDB database favorites collection.
 * It provides methods to query the database for favorite articles.
 */
public interface FavoriteArticleRepository extends MongoRepository<FavoriteArticle, String> {
    Optional<FavoriteArticle> findByUserId(String userId);
}
