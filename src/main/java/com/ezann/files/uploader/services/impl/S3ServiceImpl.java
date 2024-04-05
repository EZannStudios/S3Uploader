package com.ezann.files.uploader.services.impl;

import com.ezann.files.uploader.services.IS3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.utils.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class S3ServiceImpl implements IS3Service {

    @Value("${aws-bucket-name}")
    private String awsBucketName;

    @Value("${download-local-path}")
    private String downloadLocalPath;

    @Autowired
    private final S3Client s3Client;

    public S3ServiceImpl(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public ResponseEntity<String> uploadFile(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(awsBucketName)
                    .key(fileName)
                    .build();
            PutObjectResponse response =  s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            if (response.sdkHttpResponse().isSuccessful()) {
                return ResponseEntity.ok(file.getName() + " upload successfully");
            } else {
                return ResponseEntity.internalServerError().body("Cannot upload file: " + file.getOriginalFilename());
            }
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Cannot upload file: " + file.getOriginalFilename());
        }
    }

    public ResponseEntity<String> downloadFile(String fileName) {
        if (!isObjectPresentInBucket(fileName)) {
            return fileNotFoundError(fileName);
        }

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(awsBucketName)
                .key(fileName)
                .build();
        ResponseInputStream<GetObjectResponse> response;

        try {
            response = s3Client.getObject(request);
        } catch (S3Exception e) {
            return fileNotFoundError(fileName);
        }

        try (FileOutputStream fos = new FileOutputStream(downloadLocalPath + fileName)){
            writeFile(fos, response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("An error occurred while reading the file: " + fileName);
        }
        return ResponseEntity.ok(fileName + " downloaded to " + downloadLocalPath);
    }

    @Override
    public ResponseEntity<String> renameFile(String oldFileName, String newFileName) {
        if(StringUtils.isBlank(oldFileName) || StringUtils.isBlank(newFileName)) {
            return ResponseEntity.badRequest().body("one or both fileNames are null or empty");
        }

        if(oldFileName.equals(newFileName)) {
            return ResponseEntity.badRequest().body("both fileNames are equal");
        }

        if (!isObjectPresentInBucket(oldFileName)) {
            return fileNotFoundError(oldFileName);
        }
        CopyObjectRequest request = CopyObjectRequest.builder()
                .sourceBucket(awsBucketName)
                .sourceKey(oldFileName)
                .destinationBucket(awsBucketName)
                .destinationKey(newFileName)
                .build();

        try {
            s3Client.copyObject(request);
            deleteFile(oldFileName);
        } catch (S3Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred while trying to update the file: "
                    + oldFileName);
        }

        return ResponseEntity.ok(oldFileName + " renamed to " + newFileName);
    }

    @Override
    public ResponseEntity<String> deleteFile(String fileName) {
        if (!isObjectPresentInBucket(fileName)) {
            return fileNotFoundError(fileName);
        }

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(awsBucketName)
                .key(fileName)
                .build();
        try {
            s3Client.deleteObject(request);
        } catch (S3Exception e) {
            return ResponseEntity.internalServerError().body("Cannot delete file: " + fileName);
        }
        return ResponseEntity.ok(fileName + " deleted");
    }

    public ResponseEntity<List<String>> getFileList() {
        ListObjectsRequest request = ListObjectsRequest.builder()
                .bucket(awsBucketName)
                .build();
        List<S3Object> objects = s3Client.listObjects(request).contents();
        List<String> fileNames = new ArrayList<>();
        for(S3Object object : objects) {
            fileNames.add(object.key());
        }
        return ResponseEntity.ok(fileNames);
    }

    private boolean isObjectPresentInBucket(String objectKey) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(awsBucketName)
                    .key(objectKey)
                    .build();
            s3Client.headObject(request);
        } catch (S3Exception e) {
            if (HttpStatusCode.NOT_FOUND == e.statusCode()) {
                return false;
            }
        }
        return true;
    }

    private ResponseEntity<String> fileNotFoundError(String fileName) {
        return ResponseEntity.status(HttpStatusCode.NOT_FOUND).body("Cannot found file: " + fileName);
    }

    private void writeFile(FileOutputStream fos, ResponseInputStream<GetObjectResponse> response) throws IOException {
        byte[] readBuffer = new byte[1024];
        int readLength;

        while ((readLength = response.read(readBuffer)) > 0) {
            fos.write(readBuffer, 0, readLength);
        }
    }
}
