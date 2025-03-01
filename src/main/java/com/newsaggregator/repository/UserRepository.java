package com.newsaggregator.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.newsaggregator.model.User;

public interface UserRepository extends MongoRepository<User, String> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    User findByEmailVerificationToken(String token);
    User findByUsername(String username);
    User findByEmail(String email);
}
