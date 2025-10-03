package com.dragon.stepbystep.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BoardLikeRequestDto {
    private boolean liked;
    private Integer likeNum;
}