package com.dragon.stepbystep.service;

import com.dragon.stepbystep.common.CursorUtils;
import com.dragon.stepbystep.domain.PointHistory;
import com.dragon.stepbystep.domain.User;
import com.dragon.stepbystep.dto.CursorPagingDto;
import com.dragon.stepbystep.dto.MyPointResponseDto;
import com.dragon.stepbystep.dto.PointHistoryItemDto;
import com.dragon.stepbystep.dto.PointHistoryListResponseDto;
import com.dragon.stepbystep.exception.UserNotFoundException;
import com.dragon.stepbystep.repository.PointHistoryRepository;
import com.dragon.stepbystep.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public MyPointResponseDto getMyPoint(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return MyPointResponseDto.from(user);
    }

    public PointHistoryListResponseDto getHistories(Long userId, Integer limitParam, String cursor) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        int limit = normalizeLimit(limitParam);
        Pageable pageable = PageRequest.of(0, limit + 1, Sort.by(Sort.Direction.DESC, "id"));

        PointHistoryCursor historyCursor = CursorUtils.decode(cursor, PointHistoryCursor.class);
        List<PointHistory> histories;
        if (historyCursor != null && historyCursor.lastId() != null) {
            histories = pointHistoryRepository.findByUserIdAndIdLessThan(userId, historyCursor.lastId(), pageable);
        } else {
            histories = pointHistoryRepository.findByUserId(userId, pageable);
        }

        boolean hasNext = histories.size() > limit;
        if (hasNext) {
            histories = histories.subList(0, limit);
        }

        String nextCursor = null;
        if (hasNext && !histories.isEmpty()) {
            PointHistory lastHistory = histories.get(histories.size() - 1);
            nextCursor = CursorUtils.encode(new PointHistoryCursor(lastHistory.getId(), lastHistory.getCreatedAt()));
        }

        List<PointHistoryItemDto> items = histories.stream()
                .map(PointHistoryItemDto::from)
                .toList();

        return new PointHistoryListResponseDto(
                user.getNickname(),
                user.getPoints(),
                items,
                new CursorPagingDto(nextCursor, hasNext)
        );
    }

    private int normalizeLimit(Integer limitParam) {
        int limit = limitParam == null ? DEFAULT_LIMIT : limitParam;
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private record PointHistoryCursor(Long lastId, LocalDateTime lastCreatedAt) {

    }
}