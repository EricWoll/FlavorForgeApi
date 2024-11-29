package com.flavor.forge.AWS;

import com.flavor.forge.Service.RecipeService;
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

    private Logger logger = LoggerFactory.getLogger(RecipeService.class);

    public List<S3Object> listObjects() {
        ListObjectsV2Response response = s3Client.listObjectsV2(builder -> builder.bucket(bucketName));
        return response.contents();
    }

    public boolean doesFileExist(String bucketName, String objectKey) {
        try {
            GetObjectAttributesRequest request = GetObjectAttributesRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }


    public void uploadFileToS3(MultipartFile file, String objectKey, String newObjectKey, Boolean updateFile) throws IOException {
        if (!Objects.equals(objectKey, emptyImage)) {
            if (updateFile && doesFileExist(bucketName, objectKey)) {
                deleteFileFromS3(objectKey);
            }
        }

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(newObjectKey)
                .build();

        // Upload file to S3
        try {
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (Exception e) {
            logger.error("There was an Error with uploading the Image File!");
        }


    }

    public boolean deleteFileFromS3(String objectKey) {
        if (doesFileExist(bucketName, objectKey)) {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(deleteRequest);
            return true;
        }
        return false;
    }

    public byte[] downloadFileFromS3(String key) throws IOException {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        // Download file from S3
        ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request);
        return response.readAllBytes();
    }
}