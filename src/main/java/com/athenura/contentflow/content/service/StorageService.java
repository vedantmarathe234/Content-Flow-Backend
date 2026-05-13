package com.athenura.contentflow.content.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadFile(MultipartFile file);
    String getProviderName();
}