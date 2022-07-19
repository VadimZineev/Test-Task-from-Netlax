package com.example.TestTaskNatlex.models.response;

import com.example.TestTaskNatlex.models.enums.ExecutionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"status", "id"})
@AllArgsConstructor
@NoArgsConstructor
public class JobResponse {

    @JsonProperty("status")
    private ExecutionStatus status;

    @JsonProperty("job-id")
    private Integer id;
}
