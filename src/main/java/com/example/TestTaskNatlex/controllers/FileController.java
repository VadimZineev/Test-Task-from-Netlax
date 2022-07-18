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
import java.util.ArrayList;
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
    public ResponseEntity<List<StatusResponse>> loadFile(@RequestParam("file") List<MultipartFile> files) {
        List<StatusResponse> responseList = new ArrayList<>();
        files.parallelStream().forEach(file -> {
            try {
                log.info("File name is - {}", file.getOriginalFilename());
                StatusResponse response = new StatusResponse();
                response.setId(fileService.fileProcessor(file));
                response.setStatus(ExecutionStatus.IN_PROGRESS);
                responseList.add(response);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        //response.setStatus(ExecutionStatus.IN_PROGRESS);
        return ResponseEntity.ok(responseList);
    }
}
