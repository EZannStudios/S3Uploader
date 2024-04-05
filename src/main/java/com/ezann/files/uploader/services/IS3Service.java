package com.ezann.files.uploader.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IS3Service {
    ResponseEntity<String> uploadFile(MultipartFile file);
    ResponseEntity<String> downloadFile(String fileName);

    ResponseEntity<String> renameFile(String oldFileName, String newFileName);

    ResponseEntity<String> deleteFile(String fileName);

    ResponseEntity<List<String>> getFileList();
}
