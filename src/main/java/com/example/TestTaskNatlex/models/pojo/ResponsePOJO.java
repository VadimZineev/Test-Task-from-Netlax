package com.example.TestTaskNatlex.models.pojo;

import com.example.TestTaskNatlex.models.enums.ExecutionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"status", "id"})
public class ResponsePOJO {

    @JsonProperty("status")
    private ExecutionStatus status;

    @JsonProperty("id")
    private Integer id;
}
