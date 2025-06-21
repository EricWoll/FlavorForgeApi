package com.flavor.forge.AWS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
public class AwsService {
    @Autowired
    private S3Client s3Client; // our s3 client bean

    @Value("${aws.s3.bucket-name}")
    private String bucketName; // bucket name -> we can also make this not a service and a regular class that takes in client and bucket name in ctor

    @Value("${forge.app.noImage}")
    private String emptyImage;

    private Logger logger = LoggerFactory.getLogger(AwsService.class);

    public List<S3Object> listObjects() {
        ListObjectsV2Response response = s3Client.listObjectsV2(builder -> builder.bucket(bucketName));
        return response.contents();
    }

    public boolean doesFileExist(String bucketName, String objectKey) {
        try {
            logger.info("Checking if file exists: {}", objectKey);
            // Actually execute the request to check existence
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build());
            logger.info("File exists: {}", objectKey);
            return true;
        } catch (NoSuchKeyException e) {
            logger.info("File does not exist: {}", objectKey);
            return false;
        } catch (S3Exception e) {
            logger.error("Error checking file existence for {}: {}", objectKey, e.getMessage());
            return false;
        }
    }

    public String uploadFileToS3(MultipartFile file, String objectKey, String newObjectKey, Boolean updateFile) throws IOException {
        logger.info("Starting file upload process for: {}", newObjectKey);

        // Validate inputs
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }
        if (newObjectKey == null || newObjectKey.trim().isEmpty()) {
            throw new IllegalArgumentException("New object key cannot be null or empty");
        }

        // Handle existing file deletion if needed
        if (objectKey != null && !objectKey.trim().isEmpty() &&
                !Objects.equals(objectKey, emptyImage) &&
                Boolean.TRUE.equals(updateFile)) {

            logger.info("Checking for existing file to delete: {}", objectKey);
            if (doesFileExist(bucketName, objectKey)) {
                logger.info("Deleting existing file: {}", objectKey);
                deleteFileFromS3(objectKey);
                logger.info("Completed file deletion: {}", objectKey);
            }
        }

        // Build upload request
        logger.info("Building upload request for: {}", newObjectKey);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(newObjectKey)
                .contentType(file.getContentType()) // Add content type
                .contentLength(file.getSize())      // Add content length
                .build();

        // Upload file to S3
        try {
            logger.info("Starting file upload: {}", newObjectKey);
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            logger.info("Completed file upload: {}", newObjectKey);

            // Construct and return the S3 URL
            String s3Url = String.format("https://%s.s3.amazonaws.com/%s", bucketName, newObjectKey);
            logger.info("Generated S3 URL: {}", s3Url);
            return s3Url;

        } catch (S3Exception e) {
            logger.error("S3 error uploading file {}: {}", newObjectKey, e.getMessage());
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("IO error uploading file {}: {}", newObjectKey, e.getMessage());
            throw e; // Re-throw IOException as declared
        } catch (Exception e) {
            logger.error("Unexpected error uploading file {}: {}", newObjectKey, e.getMessage());
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    public boolean deleteFileFromS3(String objectKey) {
        if (objectKey == null || objectKey.trim().isEmpty()) {
            logger.warn("Cannot delete file: object key is null or empty");
            return false;
        }

        try {
            if (doesFileExist(bucketName, objectKey)) {
                logger.info("Deleting file from S3: {}", objectKey);
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build();
                s3Client.deleteObject(deleteRequest);
                logger.info("Successfully deleted file: {}", objectKey);
                return true;
            } else {
                logger.info("File does not exist, skipping deletion: {}", objectKey);
                return false;
            }
        } catch (S3Exception e) {
            logger.error("Error deleting file {}: {}", objectKey, e.getMessage());
            return false;
        }
    }
}
