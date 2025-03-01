package com.newsaggregator.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
import com.newsaggregator.model.FavoriteArticle;

public interface FavoriteArticleRepository extends MongoRepository<FavoriteArticle, String> {
    Optional<FavoriteArticle> findByUserId(String userId);
}
