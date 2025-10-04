package com.dragon.stepbystep.dto;

import com.dragon.stepbystep.domain.PointHistory;
import com.dragon.stepbystep.domain.enums.PointHistoryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PointHistoryItemDto {
    private Long id;
    private PointHistoryType type;
    private String title;
    private Integer pointChange;
    private Integer balanceAfter;
    private LocalDateTime createdAt;

    public static PointHistoryItemDto from(PointHistory history) {
        return new PointHistoryItemDto(
                history.getId(),
                history.getType(),
                history.getTitle(),
                history.getPointChange(),
                history.getBalanceAfter(),
                history.getCreatedAt()
        );
    }
}