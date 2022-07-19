package com.example.TestTaskNatlex.controllers;

import com.example.TestTaskNatlex.models.enums.ExecutionStatus;
import com.example.TestTaskNatlex.models.response.JobResponse;
import com.example.TestTaskNatlex.scheduler.ScheduleForFileProcessing;
import com.example.TestTaskNatlex.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class FileController {

    private final FileService fileService;

    private final ScheduleForFileProcessing schedule;

    @Autowired
    public FileController(FileService fileService, ScheduleForFileProcessing schedule) {
        this.fileService = fileService;
        this.schedule = schedule;
    }

    @PostMapping(value = "/import", consumes = "multipart/form-data", produces = "application/json")
    public List<JobResponse> postJobWithFile(@RequestParam("file") List<MultipartFile> files) {
        List<JobResponse> responseList = new ArrayList<>();
        files.parallelStream().forEach(multipartFile -> {
            try {
                var id = fileService.addFile(multipartFile);
                responseList.add(new JobResponse(ExecutionStatus.IN_PROGRESS, id));
                Thread.sleep(1);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        schedule.scheduleFileProcessor();
        return responseList;
    }

    @GetMapping(value = "/import")
    public JobResponse getStatusJob(@RequestParam("id") Integer id) {
        return new JobResponse(fileService.checkStatus(id), id);
    }


//    @Autowired
//    TaskScheduler taskScheduler;
//    ScheduledFuture<?> scheduledFuture;
//    @RequestMapping(value = "start", method = RequestMethod.GET)
//    public void start() throws Exception {
//        scheduledFuture = taskScheduler.scheduleAtFixedRate(m_sampletask.work(), FIXED_RATE);
//    }

    @GetMapping(value = "/export")
    public @ResponseBody ResponseEntity getFileById(@RequestParam("id") Integer id) {
        try {
            File file = fileService.findFile(id);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + file.getName())
                    .contentLength(file.length())
                    .contentType(MediaType.parseMediaType("multipart/form-data"))
                    .body(new FileSystemResource(file));
        } catch (NullPointerException e) {
            return ResponseEntity.notFound().build();
        }
    }
}