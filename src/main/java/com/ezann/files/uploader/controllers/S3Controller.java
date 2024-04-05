package com.ezann.files.uploader.controllers;

import com.ezann.files.uploader.services.IS3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class S3Controller {

    @Autowired
    IS3Service s3Service;

    @GetMapping("/download/{fileName}")
    public ResponseEntity<String> downloadFile(@PathVariable("fileName") String fileName) {
        return s3Service.downloadFile(fileName);
    }

    @GetMapping("/list/")
    public ResponseEntity<List<String>> getFilesList() {
        return s3Service.getFileList();
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        return s3Service.uploadFile(file);
    }

    @PutMapping("/rename")
    public ResponseEntity<String> renameFile(@RequestParam("oldFileName") String oldFileName,
                                             @RequestParam("newFileName") String newFileName) {
        return s3Service.renameFile(oldFileName, newFileName);
    }

    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<String> deleteFile(@PathVariable("fileName") String fileName) {
        return s3Service.deleteFile(fileName);
    }
}
