package com.dragon.stepbystep.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CursorPagingDto {
    private String nextCursor;
    private boolean hasNext;
}