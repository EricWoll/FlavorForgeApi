package com.flavor.forge.AWS;

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

@Service
public class AwsService {
    @Autowired
    private S3Client s3Client; // our s3 client bean

    @Value("${aws.s3.bucket-name}")
    private String bucketName; // bucket name -> we can also make this not a service and a regular class that takes in client and bucket name in ctor

    public List<S3Object> listObjects() {
        ListObjectsV2Response response = s3Client.listObjectsV2(builder -> builder.bucket(bucketName));
        return response.contents();
    }

    public void uploadFileToS3(String key, MultipartFile file) throws IOException {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        // Upload file to S3
        PutObjectResponse response = s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        // Handle response or perform other operations
    }
    public void deleteFileFromS3(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        // Delete file from S3
        DeleteObjectResponse response = s3Client.deleteObject(request);
        // Handle response or perform other operations
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