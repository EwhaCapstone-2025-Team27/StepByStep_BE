package com.dragon.stepbystep.controller;

import com.dragon.stepbystep.common.ApiResponse;
import com.dragon.stepbystep.domain.enums.BoardSearchType;
import com.dragon.stepbystep.dto.*;
import com.dragon.stepbystep.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.nio.file.AccessDeniedException;
import java.security.Principal;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // 게시글 작성
    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<BoardPostResponseDto>> createPost(
            Principal principal,
            @Valid @RequestBody BoardPostCreateRequestDto request
    ) {
        Long userId = Long.valueOf(principal.getName());
        BoardPostResponseDto response = boardService.createPost(userId, request.getContent());
        return ResponseEntity.ok(ApiResponse.success("게시글 작성 성공!", response));
    }

    // 게시글 전체 조회
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

    // 특정 게시글 조회
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<BoardPostDetailResponseDto>> getPost(@PathVariable Long postId){
        BoardPostDetailResponseDto response = boardService.getPost(postId);
        return ResponseEntity.ok(ApiResponse.success("특정 게시글 조회 성공!", response));
    }

    // 게시글 수정
    @PatchMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<BoardPostResponseDto>> updatePost(
            Principal principal,
            @PathVariable Long postId,
            @Valid @RequestBody BoardPostUpdateRequestDto request
    ) throws AccessDeniedException {
        Long userId = Long.valueOf(principal.getName());
        BoardPostResponseDto response = boardService.updatePost(postId, userId, request.getContent());
        return ResponseEntity.ok(ApiResponse.success("게시글 수정 성공!", response));
    }

    // 게시글 삭제
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            Principal principal,
            @PathVariable Long postId
    ) throws AccessDeniedException {
        Long userId = Long.valueOf(principal.getName());
        boardService.deletePost(postId, userId);
        return ResponseEntity.ok(ApiResponse.success("게시글 삭제 성공!", null));
    }

    // 게시글 좋아요
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<ApiResponse<BoardLikeResponseDto>> likePost(Principal principal, @PathVariable Long postId, @RequestBody(required = false) BoardLikeRequestDto request) {
        Long userId = Long.valueOf(principal.getName());
        BoardLikeResponseDto response = boardService.likePost(postId, userId);
        return ResponseEntity.ok(ApiResponse.success("게시글 좋아요 성공!", response));
    }

    // 게시글 좋아요 취소
    @DeleteMapping("/posts/{postId}/like")
    public ResponseEntity<ApiResponse<BoardLikeResponseDto>> unlikePost(
            Principal principal,
            @PathVariable Long postId
    ) {
        Long userId = Long.valueOf(principal.getName());
        BoardLikeResponseDto response = boardService.unlikePost(postId, userId);
        return ResponseEntity.ok(ApiResponse.success("게시글 좋아요 취소 성공!", response));
    }

    // 댓글 작성
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<BoardCommentResponseDto>> createComment(
            Principal principal,
            @PathVariable Long postId,
            @Valid @RequestBody BoardCommentRequestDto request
    ) {
        Long userId = Long.valueOf(principal.getName());
        BoardCommentResponseDto response = boardService.createComment(postId, userId, request.getContent());
        return ResponseEntity.ok(ApiResponse.success("댓글 작성 성공!", response));
    }

    // 댓글 수정
    @PatchMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<BoardCommentResponseDto>> updateComment(
            Principal principal,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody BoardCommentRequestDto request
    ) throws AccessDeniedException {
        Long userId = Long.valueOf(principal.getName());
        BoardCommentResponseDto response = boardService.updateComment(postId, commentId, userId, request.getContent());
        return ResponseEntity.ok(ApiResponse.success("댓글 수정 성공!", response));
    }

    // 댓글 삭제
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            Principal principal,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) throws AccessDeniedException {
        Long userId = Long.valueOf(principal.getName());
        boardService.deleteComment(postId, commentId, userId);
        return ResponseEntity.ok(ApiResponse.success("댓글 삭제 성공!", null));
    }
}