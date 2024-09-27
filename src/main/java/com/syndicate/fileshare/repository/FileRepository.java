package com.syndicate.fileshare.repository;

import com.syndicate.fileshare.entity.FileData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends MongoRepository<FileData, String> {
    Optional<FileData> findByFileName(String s);
    Optional<String> findFileNameById(String id);
    void deleteById(String id);
}
