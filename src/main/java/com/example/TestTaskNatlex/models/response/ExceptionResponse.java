package com.example.TestTaskNatlex.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"message", "id"})
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionResponse {

    @JsonProperty
    private String message;

    @JsonProperty
    private Integer id;
}
