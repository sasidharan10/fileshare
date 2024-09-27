package com.syndicate.fileshare.repository;

import com.syndicate.fileshare.entity.MyUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<MyUser, String> {
    Optional<MyUser> findByUsername(String username);
}

