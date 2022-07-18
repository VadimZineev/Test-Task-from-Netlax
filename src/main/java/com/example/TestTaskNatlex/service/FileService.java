package com.example.TestTaskNatlex.service;

import com.example.TestTaskNatlex.dao.SectionDAO;
import com.example.TestTaskNatlex.models.persistence.GeoClass;
import com.example.TestTaskNatlex.models.persistence.Section;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
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

    public void fileProcessor(MultipartFile multipartFile) throws IOException {
        var file = fileConverter(multipartFile);
        fileParser(file);
    }

    private void fileParser(File file) throws IOException {
        HashMap<Section, List<GeoClass>> sectionListMap = new HashMap<>();

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
        log.info(excelMap.toString());
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
