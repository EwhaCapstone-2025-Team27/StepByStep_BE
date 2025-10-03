package com.dragon.stepbystep.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardLikeResponseDto {
    private final boolean liked;
    private final int likeNum;
}