package com.example.TestTaskNatlex.service;

import com.example.TestTaskNatlex.dao.JobDAO;
import com.example.TestTaskNatlex.enums.ExecutionStatus;
import com.example.TestTaskNatlex.models.persistence.Attachment;
import com.example.TestTaskNatlex.models.persistence.Job;
import com.example.TestTaskNatlex.models.response.JobResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

/**
 * Service for working with XLS files
 */
@Slf4j
@Service
@EnableAsync
public class FileService {

    private final JobDAO jobDAO;

    @Autowired
    public FileService(JobDAO jobDAO) {
        this.jobDAO = jobDAO;
    }

    /**
     * Method gets jobs list with status IN_PROGRESS
     * restores files from database, parses files, saves result in database,
     * updates job's status to DONE, throw an exception
     * if parsing failed and updates job's status to ERROR
     */
    @Async(value = "taskExecutor")
    public void fileProcessor() throws InterruptedException {
        Thread.sleep(10000); // only for demonstration
        List<Attachment> attachmentList = jobDAO.findJobWithStatusInProgress();
        attachmentList.parallelStream().forEach(attachment -> {
            File file = reconstructFile(attachment.getContext(), attachment.getName());
            var excelMap = fileParser(file);
            if (!excelMap.isEmpty()) {
                jobDAO.saveFromFile(excelMap, attachment.getId());
                jobDAO.updateStatusJob(attachment.getGuid(), ExecutionStatus.DONE, true);
            } else jobDAO.updateStatusJob(attachment.getGuid(), ExecutionStatus.ERROR, true);
        });
    }

    /**
     * @return all jobs response where status is NOT_STARTED
     * and updates job's status to IN_PROGRESS
     */
    public List<JobResponse> getListIdJob() {
        List<JobResponse> responseList = new ArrayList<>();
        var jobList = jobDAO.findAttachmentWithStatusNotStarted();
        jobList.forEach(job -> {
            JobResponse response = new JobResponse();
            response.setId(job.getId());
            response.setStatus(job.getStatusExport());
            responseList.add(response);
        });
        return responseList;
    }

    /**
     * Transfer data from excel file to hashmap
     *
     * @param file - XLS file
     * @return - HashMap with data from excel, where key - row number, value = list of value from cell
     * @throws IOException
     */
    private HashMap<Integer, List<String>> fileParser(File file) {
        HashMap<Integer, List<String>> excelMap = new HashMap<>();
        try {
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
            file.delete();
        } catch (IOException e) {
            log.info("Wrong File!");
            file.delete();
        }
        return excelMap;
    }

    /**
     * Converts files from multipart file to file
     *
     * @param multipartFile - MultipartFile from request
     * @return - Converted file
     * @throws IOException
     */
    public File fileConverter(MultipartFile multipartFile) throws IOException {
        File file = new File("src/main/resources/temp/" + Objects.requireNonNull(multipartFile.getOriginalFilename()));
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(multipartFile.getBytes());
        fileOutputStream.close();
        return file;
    }

    /**
     * Reads file to byte array, changes content to BASE64,
     * saves in database, updates job's status to DONE
     *
     * @param UUIDAndFileMap - map where key = UUID from job, value = file
     */
    @Async(value = "taskExecutor")
    public void addFiles(HashMap<String, File> UUIDAndFileMap) {
        UUIDAndFileMap.forEach((uuid, file) -> {
            try {
                byte[] fileContent = FileUtils.readFileToByteArray(file);
                file.delete();
                String content = Base64.getEncoder().encodeToString(fileContent);
                int id = jobDAO.saveFile(content, file.getName(), ExecutionStatus.DONE, uuid);
                jobDAO.updateStatusJob(uuid, ExecutionStatus.DONE, false);
                log.info("new id is: {}", id);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                file.delete();
            }
        });
    }

    /**
     * Creates job
     *
     * @param uuid - unique id job
     * @param name - file name
     * @return - JobResponse
     */
    public JobResponse createJob(String uuid, String name) {
        JobResponse response = new JobResponse();
        int id = jobDAO.getIdJob(uuid, name);
        response.setName(name);
        response.setId(id);
        log.info("Job is [{}]", response);
        return response;
    }

    /**
     * Finds file by id
     *
     * @param id - id file
     * @return - file
     */
    public File findFile(Integer id) {
        Attachment attachment = jobDAO.getAttachmentById(id);
        return reconstructFile(attachment.getContext(), attachment.getName());
    }

    /**
     * Reads content, changes to byte array from BASE64 to File
     *
     * @param context  - content to BASE64
     * @param fileName - file name
     * @return - File
     */
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

    /**
     * Checks import status
     *
     * @param id - id for Table Attachment
     * @return - status from table Attachment
     */
    public ExecutionStatus checkStatus(Integer id) {
        return jobDAO.getAttachmentStatus(id);
    }

    /**
     * Checks parsing status
     *
     * @param id - id for Table Job
     * @return - status from table Job
     */
    public Job checkStatusParse(Integer id) {
        return jobDAO.getJob(id);
    }

    /**
     * Creates UUID for each files, converts files from multipart file to file
     *
     * @param multipartFiles - multipart File from request
     * @return - map where key = UUID, value = File
     */
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