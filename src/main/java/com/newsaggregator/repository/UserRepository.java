package com.newsaggregator.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.newsaggregator.model.User;

/*
 * UserRepository.java
 * 
 * This repository interface is used to interact with the MongoDB database users collection.
 * It provides methods to query the database for user information.
 */
public interface UserRepository extends MongoRepository<User, String> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    User findByEmailVerificationToken(String token);
    User findByUsername(String username);
    User findByEmail(String email);
}
