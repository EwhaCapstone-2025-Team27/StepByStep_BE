package com.dragon.stepbystep.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BoardPostCreateRequestDto {

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;

    public BoardPostCreateRequestDto(String content) {
        this.content = content;
    }
}