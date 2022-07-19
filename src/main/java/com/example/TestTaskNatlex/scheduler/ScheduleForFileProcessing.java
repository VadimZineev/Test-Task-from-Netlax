package com.example.TestTaskNatlex.scheduler;

import com.example.TestTaskNatlex.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
@EnableAsync
@Slf4j
public class ScheduleForFileProcessing {

    private final FileService fileService;

    @Autowired
    public ScheduleForFileProcessing(FileService fileService) {
        this.fileService = fileService;
    }

    @Scheduled(fixedRate = 30000)
    @Async
    public Integer scheduleFileProcessor() {
        try {
            return fileService.fileProcessor();
        } catch (NoSuchElementException e) {
            log.info("No value present");
        }
        log.info("Well!!! We waiting!!!");
        return 0;
    }
}
