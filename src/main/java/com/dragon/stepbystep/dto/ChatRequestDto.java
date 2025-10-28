package com.dragon.stepbystep.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ChatRequestDto {

    @NotBlank
    private String model;
    private List<Map<String, String>> messages;
    private Map<String, Object> options;
}
