package com.example.TestTaskNatlex.controllers;

import com.example.TestTaskNatlex.models.enums.ExecutionStatus;
import com.example.TestTaskNatlex.models.response.StatusResponse;
import com.example.TestTaskNatlex.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
public class FileController {

    private FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "/import", headers = "content-type=multipart/*")
    public ResponseEntity<StatusResponse> loadFile(@RequestParam("file") List<MultipartFile> files) {
        StatusResponse response = new StatusResponse();
        files.parallelStream().forEach(file -> {
            try {
                log.info("File name is - {}", file.getOriginalFilename());
                fileService.fileProcessor(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        response.setStatus(ExecutionStatus.IN_PROGRESS);
        return ResponseEntity.ok(response);
    }
}
