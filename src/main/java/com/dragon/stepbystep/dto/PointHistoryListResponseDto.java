package com.dragon.stepbystep.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PointHistoryListResponseDto {
    private String nickname;
    private Integer currentPoints;
    private List<PointHistoryItemDto> histories;
    private CursorPagingDto paging;
}