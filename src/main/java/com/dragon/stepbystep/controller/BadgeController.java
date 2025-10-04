package com.dragon.stepbystep.controller;

import com.dragon.stepbystep.common.ApiResponse;
import com.dragon.stepbystep.dto.BadgeListResponseDto;
import com.dragon.stepbystep.dto.BadgePurchaseRequestDto;
import com.dragon.stepbystep.dto.BadgePurchaseResponseDto;
import com.dragon.stepbystep.service.BadgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping
    public ResponseEntity<ApiResponse<BadgeListResponseDto>> getBadges(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "cursor", required = false) String cursor
    ) {
        BadgeListResponseDto response = badgeService.getBadges(limit, cursor);
        return ResponseEntity.ok(ApiResponse.success("배지 목록 조회 성공!", response));
    }

    @PostMapping("/purchase")
    public ResponseEntity<ApiResponse<BadgePurchaseResponseDto>> purchaseBadge(
            Principal principal,
            @Valid @RequestBody BadgePurchaseRequestDto requestDto
    ) {
        Long userId = Long.valueOf(principal.getName());
        BadgePurchaseResponseDto response = badgeService.purchaseBadge(userId, requestDto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("배지 구매 성공!", response));
    }
}