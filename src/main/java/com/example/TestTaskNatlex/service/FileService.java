package com.example.TestTaskNatlex.service;

import com.example.TestTaskNatlex.dao.SectionDAO;
import com.example.TestTaskNatlex.models.enums.ExecutionStatus;
import com.example.TestTaskNatlex.models.persistence.Attachment;
import com.example.TestTaskNatlex.models.response.JobResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Slf4j
@Service
public class FileService {

    private final SectionDAO sectionDAO;

    @Autowired
    public FileService(SectionDAO sectionDAO) {
        this.sectionDAO = sectionDAO;
    }

    public int fileProcessor() {
        Optional<Attachment> attachmentInOptional = sectionDAO.findAttachmentWithStatusInProgress();
        var attachment = attachmentInOptional.get();
        try {
            File file = reconstructFile(attachment.getContext(), attachment.getName());
            var excelMap = fileParser(file);
            sectionDAO.saveFromFile(excelMap, attachment.getId());
        } catch (IOException e) {
            sectionDAO.updateStatus(attachment.getId(), ExecutionStatus.ERROR);
            throw new RuntimeException(e);
        }
        return attachment.getId();
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
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(multipartFile.getBytes());
        fileOutputStream.close();
        return file;
    }

    public int addFile(MultipartFile multipartFile) throws IOException {
        var file = fileConverter(multipartFile);
        byte[] fileContent = FileUtils.readFileToByteArray(file);
        String content = Base64.getEncoder().encodeToString(fileContent);
        var id = sectionDAO.saveFile(content, file.getName(), ExecutionStatus.IN_PROGRESS);
        return id;
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
}
