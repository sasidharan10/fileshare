package com.syndicate.fileshare.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document(collection = "files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileData {
    @MongoId
    @Field("_id")
    private String id;
    @Field("fileName")
    @Indexed
    private String fileName;
    @Field("folderId")
    private String folderId;
    @Field("creator")
    private String creator;
}
