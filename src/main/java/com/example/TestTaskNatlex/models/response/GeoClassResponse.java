package com.example.TestTaskNatlex.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"name", "code"})
public class GeoClassResponse {

    @JsonProperty("name")
    private String name;

    @JsonProperty("code")
    private String code;
}
