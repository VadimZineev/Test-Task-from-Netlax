package com.example.TestTaskNatlex.models.persistence;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "section")
public class Section {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    @JsonProperty("name")
    private String name;

    @Column(name = "attachment_id")
    private int attachmentId;

    @OneToMany(mappedBy = "section")
    @JsonProperty("geologicalClasses")
    private List<GeoClass> geoClassList;
}
