package com.cts.logichain360.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
     //validates and saves the file to local disk under the configured upload directory
     //return the generated stored filename and save in DB.
    String store(MultipartFile file);
}