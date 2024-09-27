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

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyUser {
    @MongoId
    @Field("_id")
    private String id;
    @Indexed
    @Field("username")
    private String username;
    @Field("password")
    private String password;
    @Field("role")
    private String role;
    @Field("folderList")
    private Set<String> folderList;

}
