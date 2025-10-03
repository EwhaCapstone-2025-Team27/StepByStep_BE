package com.dragon.stepbystep.controller;

import com.dragon.stepbystep.common.ApiResponse;
import com.dragon.stepbystep.domain.enums.BoardSearchType;
import com.dragon.stepbystep.dto.BoardPostCreateRequestDto;
import com.dragon.stepbystep.dto.BoardPostListResponseDto;
import com.dragon.stepbystep.dto.BoardPostResponseDto;
import com.dragon.stepbystep.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.security.Principal;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<BoardPostResponseDto>> createPost(
            Principal principal,
            @Valid @RequestBody BoardPostCreateRequestDto request
    ) {
        Long userId = Long.valueOf(principal.getName());
        BoardPostResponseDto response = boardService.createPost(userId, request.getContent());
        return ResponseEntity.ok(ApiResponse.success("게시글 작성 성공!", response));
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<BoardPostListResponseDto>> getPosts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "searchType", required = false, defaultValue = "ALL") String searchTypeValue,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        BoardSearchType searchType = BoardSearchType.from(searchTypeValue);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        BoardPostListResponseDto response = boardService.getPosts(keyword, searchType, pageable);

        return ResponseEntity.ok(ApiResponse.success("게시글 목록 조회 성공!", response));
    }
}