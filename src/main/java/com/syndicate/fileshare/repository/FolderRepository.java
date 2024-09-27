package com.syndicate.fileshare.repository;

import com.syndicate.fileshare.entity.FolderData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends MongoRepository<FolderData, String> {
    Optional<FolderData> findByFolderName(String s);
}
