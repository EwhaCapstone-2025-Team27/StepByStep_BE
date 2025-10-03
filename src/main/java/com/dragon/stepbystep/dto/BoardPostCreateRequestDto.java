package com.dragon.stepbystep.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BoardPostCreateRequestDto {

    @NotBlank(message = "게시글 내용은 필수입니다.")
    private String content;

    public BoardPostCreateRequestDto(String content) {
        this.content = content;
    }
}