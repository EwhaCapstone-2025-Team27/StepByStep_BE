package com.dragon.stepbystep.dto;

import java.util.List;
import lombok.Data;

@Data
public class ChatResponse {
    private String answer;
    private List<String> citations;
}