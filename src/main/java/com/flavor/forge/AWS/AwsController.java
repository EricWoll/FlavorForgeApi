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
@RequestMapping("/api/v2/images")
public class AwsController {
    @Autowired
    private AwsService awsService;

//    @GetMapping
//    public ResponseEntity<byte[]> downloadFile(@RequestParam("filename") String fileName) throws IOException {
//        var fileContent= awsService.downloadFileFromS3(fileName);
//        // Set appropriate headers for the file content
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.IMAGE_JPEG); // Adjust based on your file type
//
//        // Return the file content as part of the response body
//        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
//    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("image") MultipartFile file,
            @RequestParam("objectKey") String objectKey,
            @RequestParam("newObjectKey") String newObjectKey,
            @RequestParam("updateFile") Boolean updateFile
    ) {
        try {
            // Validate file type or size if needed
            if (file.isEmpty()) {
                return new ResponseEntity<>("No file uploaded", HttpStatus.BAD_REQUEST);
            }

            // Upload the file to S3 and get the URL
            String s3Url = awsService.uploadFileToS3(file, objectKey, newObjectKey, updateFile);

            return new ResponseEntity<>(s3Url, HttpStatus.OK); // Return the S3 URL with 200 OK

        } catch (IOException e) {
            return new ResponseEntity<>("File upload failed", HttpStatus.INTERNAL_SERVER_ERROR); // Server error

        } catch (Exception e) {
            return new ResponseEntity<>("Unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<?> deleteFile (@PathVariable String fileName) throws IOException {
        awsService.deleteFileFromS3(fileName);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
