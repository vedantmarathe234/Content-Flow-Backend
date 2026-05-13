package com.athenura.contentflow.content.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GoogleDriveService implements StorageService {

    @Override
    public String uploadFile(MultipartFile file) {

        return "https://drive.google.com/file/d/example_id/view";
    }

    @Override
    public String getProviderName() {
        return "DRIVE";
    }
}
