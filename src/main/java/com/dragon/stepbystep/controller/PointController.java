package com.dragon.stepbystep.controller;

import com.dragon.stepbystep.common.ApiResponse;
import com.dragon.stepbystep.dto.MyPointResponseDto;
import com.dragon.stepbystep.dto.PointHistoryListResponseDto;
import com.dragon.stepbystep.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/points/me")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @GetMapping
    public ResponseEntity<ApiResponse<MyPointResponseDto>> getMyPoint(Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        MyPointResponseDto response = pointService.getMyPoint(userId);
        return ResponseEntity.ok(ApiResponse.success("내 포인트 조회 성공!", response));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PointHistoryListResponseDto>> getPointHistories(
            Principal principal,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "cursor", required = false) String cursor
    ) {
        Long userId = Long.valueOf(principal.getName());
        PointHistoryListResponseDto response = pointService.getHistories(userId, limit, cursor);
        return ResponseEntity.ok(ApiResponse.success("포인트 내역 조회 성공!", response));
    }
}