package com.dragon.stepbystep.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BadgeListResponseDto {
    private List<BadgeResponseDto> badges;
    private CursorPagingDto paging;
    private Integer myPoint;
}