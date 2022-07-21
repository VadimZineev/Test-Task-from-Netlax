package com.example.TestTaskNatlex.service;

import com.example.TestTaskNatlex.dao.SectionDAO;
import com.example.TestTaskNatlex.enums.ExecutionStatus;
import com.example.TestTaskNatlex.models.persistence.Attachment;
import com.example.TestTaskNatlex.models.persistence.Job;
import com.example.TestTaskNatlex.models.response.JobResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Slf4j
@Service
@EnableAsync
public class FileService {

    private final SectionDAO sectionDAO;

    @Autowired
    public FileService(SectionDAO sectionDAO) {
        this.sectionDAO = sectionDAO;
    }

    @Async
    public void fileProcessor() throws InterruptedException {
        Thread.sleep(10000);
        List<Attachment> attachmentList = sectionDAO.findJobWithStatusInProgress();
        attachmentList.parallelStream().forEach(attachment -> {
            try {
                File file = reconstructFile(attachment.getContext(), attachment.getName());
                var excelMap = fileParser(file);
                sectionDAO.saveFromFile(excelMap, attachment.getId());
                sectionDAO.updateStatusJob(attachment.getGuid(), ExecutionStatus.DONE, true);
            } catch (IOException e) {
                sectionDAO.updateStatusJob(attachment.getGuid(), ExecutionStatus.ERROR, true);
            }
        });
    }

    public List<JobResponse> getListIdJob() {
        List<JobResponse> responseList = new ArrayList<>();
        var jobList = sectionDAO.findAttachmentWithStatusNotStarted();
        jobList.forEach(job -> {
            JobResponse response = new JobResponse();
            response.setId(job.getId());
            response.setStatus(job.getStatusExport());
            responseList.add(response);
        });
        return responseList;
    }

    private HashMap<Integer, List<String>> fileParser(File file) throws IOException {
        HashMap<Integer, List<String>> excelMap = new HashMap<>();
        HSSFWorkbook excelBook = new HSSFWorkbook(new FileInputStream(file));
        HSSFSheet excelSheet = excelBook.getSheet("Sheet1");
        excelSheet.forEach(row -> {
            List<String> cellList = new ArrayList<>();
            row.forEach(cell -> {
                cellList.add(cell.getStringCellValue().isEmpty() ? null : cell.getStringCellValue());
            });
            excelMap.put(row.getRowNum(), cellList);
        });
        excelMap.remove(0);
        return excelMap;
    }

    public File fileConverter(MultipartFile multipartFile) throws IOException {
        File file = new File("src/main/resources/temp/" + Objects.requireNonNull(multipartFile.getOriginalFilename()));
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(multipartFile.getBytes());
        fileOutputStream.close();
        return file;
    }

    @Async(value = "taskExecutor")
    public void addFiles(HashMap<String, File> UUIDAndFileMap) {
        UUIDAndFileMap.forEach((uuid, file) -> {
            try {
                byte[] fileContent = FileUtils.readFileToByteArray(file);
                file.delete();
                String content = Base64.getEncoder().encodeToString(fileContent);
                int id = sectionDAO.saveFile(content, file.getName(), ExecutionStatus.DONE, uuid);
                sectionDAO.updateStatusJob(uuid, ExecutionStatus.DONE, false);
                log.info("new id is: {}", id);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                file.delete();
            }
        });
    }

    public JobResponse createJob(String uuid, String name) {
        JobResponse response = new JobResponse();
        int id = sectionDAO.getIdJob(uuid, name);
        response.setName(name);
        response.setId(id);
        log.info("Job is [{}]", response);
        return response;
    }

    public File findFile(Integer id) {
        Attachment attachment = sectionDAO.getAttachmentById(id);
        return reconstructFile(attachment.getContext(), attachment.getName());
    }

    public File reconstructFile(String context, String fileName) {
        byte[] decodedBytes = Base64.getDecoder().decode(context);
        File file = new File(fileName);
        try {
            FileUtils.writeByteArrayToFile(file, decodedBytes);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return file;
    }

    public ExecutionStatus checkStatus(Integer id) {
        return sectionDAO.getAttachmentStatus(id);
    }

    public Job checkStatusParse(Integer id) {
        return sectionDAO.getJob(id);
    }

    public HashMap<String, File> toMap(List<MultipartFile> multipartFiles) {
        HashMap<String, File> map = new HashMap<>();
        multipartFiles.forEach(multipartFile -> {
            UUID uuid = UUID.randomUUID();
            try {
                var file = fileConverter(multipartFile);
                map.put(uuid.toString(), file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return map;
    }
}
