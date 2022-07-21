package com.example.TestTaskNatlex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
public class JobService {

    private FileService fileService;

    @Autowired
    public JobService(FileService fileService) {
        this.fileService = fileService;
    }

    public HashMap<String, File> createJob(List<MultipartFile> multipartFiles) {
        HashMap<String, File> map = new HashMap<>();
        multipartFiles.forEach(multipartFile -> {
            UUID uuid = UUID.randomUUID();
            try {
                var file = fileService.fileConverter(multipartFile);
                map.put(uuid.toString(), file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return map;
    }
}
