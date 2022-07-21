package com.example.TestTaskNatlex.controllers;

import com.example.TestTaskNatlex.enums.ExecutionStatus;
import com.example.TestTaskNatlex.models.response.ExceptionResponse;
import com.example.TestTaskNatlex.models.response.JobResponse;
import com.example.TestTaskNatlex.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class FileController {

    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "/import", consumes = "multipart/form-data", produces = "application/json")
    public List<JobResponse> postJobWithFile(@RequestParam("file") List<MultipartFile> files) {
        var UUIDAndFilesMap = fileService.toMap(files);
        List<JobResponse> jobResponseList = new ArrayList<>();
        UUIDAndFilesMap.forEach((uuid, file) -> {
            var job = fileService.createJob(uuid, file.getName());
            jobResponseList.add(job);
        });
        fileService.addFiles(UUIDAndFilesMap);
        return jobResponseList;
    }

    @GetMapping(value = "/import")
    public JobResponse getStatusJob(@RequestParam("id") Integer id) {
        ExecutionStatus status;
        try {
            status = fileService.checkStatus(id);
        } catch (NullPointerException e) {
            status = ExecutionStatus.ERROR;
        }
        log.info(status.name());
        return new JobResponse(null, status, id);
    }

    @GetMapping(value = "/export")
    public List<JobResponse> getAllJobIdAndStartExport() throws InterruptedException {
        var listJob = fileService.getListIdJob();
        fileService.fileProcessor();
        return listJob;
    }

    @GetMapping(value = "/export/{id}")
    public JobResponse getResultJobById(@PathVariable("id") Integer id) {
        JobResponse response = new JobResponse();
        var job = fileService.checkStatusParse(id);
        response.setId(job.getId());
        response.setName(job.getName());
        response.setStatus(job.getStatusExport());
        return response;
    }

    @GetMapping(value = "/export/{id}/file")
    public ResponseEntity getFileById(@PathVariable("id") Integer id) {
        try {
            var job = fileService.checkStatusParse(id);
            if (!job.getStatusExport().equals(ExecutionStatus.IN_PROGRESS)) {
                File file = fileService.findFile(id);
                return ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=" + file.getName())
                        .contentLength(file.length())
                        .contentType(MediaType.parseMediaType("multipart/form-data"))
                        .body(new FileSystemResource(file));
            } else {
                return ResponseEntity.status(HttpStatus.LOCKED)
                        .body(new ExceptionResponse("Sorry! But this Job is IN_PROGRESS now. Please, try again later", job.getId()));
            }
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ExceptionResponse("File with job_id is not found!", id));
        }
    }
}