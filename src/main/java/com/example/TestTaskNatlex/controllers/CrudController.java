package com.example.TestTaskNatlex.controllers;

import com.example.TestTaskNatlex.dao.SectionDAO;
import com.example.TestTaskNatlex.enums.ExecutionStatus;
import com.example.TestTaskNatlex.models.persistence.Section;
import com.example.TestTaskNatlex.models.response.SectionResponse;
import com.example.TestTaskNatlex.models.response.StatusResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/sections")
public class CrudController {

    private SectionDAO sectionDAO;

    @Autowired
    public CrudController(SectionDAO sectionDAO) {
        this.sectionDAO = sectionDAO;
    }
    @PostMapping
    public ResponseEntity<StatusResponse> create(@Valid @RequestBody Section section, BindingResult bindingResult) {
        StatusResponse response = new StatusResponse();
        if (bindingResult.hasErrors()) {
            response.setStatus(ExecutionStatus.ERROR);
            return ResponseEntity.badRequest().body(response);
        }
        response.setStatus(ExecutionStatus.SUCCESS);
        response.setId(sectionDAO.save(section));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SectionResponse>> read() {
        return ResponseEntity.ok(sectionDAO.showAll());
    }

    @PutMapping
    public ResponseEntity<StatusResponse> update(@Valid @RequestBody Section section, BindingResult bindingResult) {
        StatusResponse response = new StatusResponse();
        if (bindingResult.hasErrors()) {
            response.setStatus(ExecutionStatus.ERROR);
            return ResponseEntity.badRequest().body(response);
        }
        response.setStatus(sectionDAO.edit(section));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<StatusResponse> delete(@Valid @RequestBody Section section, BindingResult bindingResult) {
        StatusResponse response = new StatusResponse();
        if (bindingResult.hasErrors()) {
            response.setStatus(ExecutionStatus.ERROR);
            return ResponseEntity.badRequest().body(response);
        }
        response.setStatus(sectionDAO.delete(section));
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/by-code")
    public ResponseEntity<List<SectionResponse>> findByCode (@RequestParam("code") String code) {
        return ResponseEntity.ok(sectionDAO.showSectionByCode(code));
    }
}
