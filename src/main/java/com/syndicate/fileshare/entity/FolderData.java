package com.syndicate.fileshare.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Set;

@Document(collection = "folders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderData {
    @MongoId
    @Field("_id")
    private String id;
    @Indexed
    @Field("folderName")
    private String folderName;
    @Field("owner")
    private String owner;
    @Field("filesList")
    private Set<String> filesList;
}
