package com.example.TestTaskNatlex.service;

import com.example.TestTaskNatlex.dao.SectionDAO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class FileService {

    private SectionDAO sectionDAO;

    @Autowired
    public FileService(SectionDAO sectionDAO) {
        this.sectionDAO = sectionDAO;
    }

    public int fileProcessor(MultipartFile multipartFile) throws IOException {
        var file = fileConverter(multipartFile);
        fileParser(file);
        byte[] content = FileUtils.readFileToByteArray(file);
        var encodeContent = Base64.getEncoder().encode(content);

        return sectionDAO.saveFile(encodeContent, file.getName());
    }

    private void fileParser(File file) throws IOException {
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
        sectionDAO.saveFromFile(excelMap);
    }

    private File fileConverter(MultipartFile multipartFile) throws IOException {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(multipartFile.getBytes());
        fileOutputStream.close();
        return file;
    }
}
