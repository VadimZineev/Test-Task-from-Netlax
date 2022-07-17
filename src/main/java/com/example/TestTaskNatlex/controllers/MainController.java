package com.example.TestTaskNatlex.controllers;

import com.example.TestTaskNatlex.dao.SectionDAO;
import com.example.TestTaskNatlex.models.enums.ExecutionStatus;
import com.example.TestTaskNatlex.models.pojo.ResponsePOJO;
import com.example.TestTaskNatlex.models.pojo.SectionPOJO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
public class MainController {

    private SectionDAO sectionDAO;

    @Autowired
    public MainController(SectionDAO sectionDAO) {
        this.sectionDAO = sectionDAO;
    }

    @PostMapping("/sections")
    public ResponseEntity<ResponsePOJO> create(@Valid @RequestBody SectionPOJO sectionPOJO, BindingResult bindingResult) {
        ResponsePOJO response = new ResponsePOJO();
        if (bindingResult.hasErrors()) {
            response.setStatus(ExecutionStatus.ERROR);
            return ResponseEntity.badRequest().body(response);
        }
        response.setStatus(ExecutionStatus.SUCCESS);
        response.setId(sectionDAO.save(sectionPOJO));
        return ResponseEntity.ok(response);
    }
}
