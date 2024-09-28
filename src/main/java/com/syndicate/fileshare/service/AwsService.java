package com.syndicate.fileshare.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.syndicate.fileshare.entity.FileData;
import com.syndicate.fileshare.repository.FileRepository;
import com.syndicate.fileshare.repository.FolderRepository;
import com.syndicate.fileshare.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

@Service
public class AwsService {
    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    private final Logger LOGGER = LoggerFactory.getLogger(AwsService.class);

    public void uploadFileInS3(String currUsername, String targetFolder, MultipartFile file) throws Exception {
        try {
            File fileObj = convertMultiPartFileToFile(file);
            String fileName = targetFolder + "/" + file.getOriginalFilename();
            s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public byte[] downloadFileFromS3(String fileName) throws Exception {
        try {
            S3Object s3Object = s3Client.getObject(bucketName, fileName);
            S3ObjectInputStream inputStream = s3Object.getObjectContent();
            byte[] content = IOUtils.toByteArray(inputStream);
            return content;
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public void deleteFile(String fileName) throws Exception {
        try {
            s3Client.deleteObject(bucketName, fileName);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public void renameFileInS3(String oldFileName, String newFileName) throws Exception {
        try {
            // Step 1: Copy the object to the new key (new file name)
            CopyObjectRequest copyObjectRequest = new CopyObjectRequest()
                    .withSourceBucketName(bucketName)
                    .withSourceKey(oldFileName) // old file name
                    .withDestinationBucketName(bucketName)
                    .withDestinationKey(newFileName); // new file name
            s3Client.copyObject(copyObjectRequest);

            // Step 2: Delete the original object
            DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, oldFileName);
            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public void createFolderInS3(String folderName) throws Exception {
        try { // Ensure the folder name ends with a slash ("/")
            if (!folderName.endsWith("/")) {
                folderName += "/";
            }

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(0);

            // Create an empty object with the folder name as the key
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, folderName, new ByteArrayInputStream(new byte[0]), metadata);

            // S3 doesn't need actual content, so we use an empty body
            s3Client.putObject(putObjectRequest);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public void renameFolderInS3(String oldFolderName, String newFolderName) throws Exception {
        try {
            // Create ListObjectsRequest without using builder
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                    .withBucketName(bucketName)
                    .withPrefix(oldFolderName + "/");

            // Retrieve the list of objects
            ObjectListing listObjects = s3Client.listObjects(listObjectsRequest);

            // Iterate over the objects and rename each one
            listObjects.getObjectSummaries().forEach(object -> {
                String newKey = newFolderName + "/" + object.getKey().substring(oldFolderName.length() + 1);

                // Copy the object to the new key
                CopyObjectRequest copyObjectRequest = new CopyObjectRequest()
                        .withSourceBucketName(bucketName)
                        .withSourceKey(object.getKey())
                        .withDestinationBucketName(bucketName)
                        .withDestinationKey(newKey);
                s3Client.copyObject(copyObjectRequest);
                // Delete the old object
                DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, object.getKey());
                s3Client.deleteObject(deleteObjectRequest);
            });
            s3Client.deleteObject(bucketName, oldFolderName + "/");
        }catch (Exception e) {
            throw new Exception(e);
        }
    }


    public void deleteFolderInS3(String folderName) {
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(bucketName)
                .withPrefix(folderName + "/");

        // Retrieve the list of objects
        ObjectListing listObjects = s3Client.listObjects(listObjectsRequest);

        // Iterate over the objects and rename each one
        listObjects.getObjectSummaries().forEach(object -> {

            // Delete each object
            DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, object.getKey());
            s3Client.deleteObject(deleteObjectRequest);
        });
        s3Client.deleteObject(bucketName, folderName + "/");
    }


    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            LOGGER.info("Error converting multipartFile to file", e);
        }
        return convertedFile;
    }

}
