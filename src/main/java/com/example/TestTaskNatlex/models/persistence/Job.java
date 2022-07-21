package com.example.TestTaskNatlex.models.persistence;

import com.example.TestTaskNatlex.enums.ExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "job")
public class Job {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "status_import")
    private ExecutionStatus statusImport;

    @Column(name = "status_export")
    private ExecutionStatus statusExport;

    @Column(name = "guid")
    private String guid;
}