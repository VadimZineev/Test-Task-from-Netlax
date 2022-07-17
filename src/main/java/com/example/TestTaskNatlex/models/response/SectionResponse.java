package com.example.TestTaskNatlex.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"name", "geologicalClasses"})
public class SectionResponse {

    @NotNull
    @JsonProperty("name")
    private String name;

    @JsonProperty("geologicalClasses")
    private List<GeoClassResponse> geoClassResponseList;
}
