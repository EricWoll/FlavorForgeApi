package com.flavor.forge.AWS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/images")
public class AwsController {
    @Autowired
    private AwsService awsService;

    @GetMapping
    public ResponseEntity<byte[]> downloadFile(@RequestParam("filename") String fileName) throws IOException {
        var fileContent= awsService.downloadFileFromS3(fileName);
        // Set appropriate headers for the file content
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // Adjust based on your file type

        // Return the file content as part of the response body
        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestParam("image") MultipartFile file, @RequestParam("file_name") String key) throws IOException {
        awsService.uploadFileToS3(key, file);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<?> deleteFile (@PathVariable String fileName) throws IOException {
        awsService.deleteFileFromS3(fileName);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
