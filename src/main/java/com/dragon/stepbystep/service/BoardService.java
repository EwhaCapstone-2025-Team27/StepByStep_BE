package com.dragon.stepbystep.service;

import com.dragon.stepbystep.domain.Board;
import com.dragon.stepbystep.domain.BoardComment;
import com.dragon.stepbystep.domain.BoardLike;
import com.dragon.stepbystep.domain.User;
import com.dragon.stepbystep.domain.enums.BoardSearchType;
import com.dragon.stepbystep.dto.*;
import com.dragon.stepbystep.exception.BoardCommentNotFoundException;
import com.dragon.stepbystep.exception.BoardNotFoundException;
import com.dragon.stepbystep.exception.BoardSearchResultNotFoundException;
import com.dragon.stepbystep.exception.UserNotFoundException;
import com.dragon.stepbystep.repository.BoardCommentRepository;
import com.dragon.stepbystep.repository.BoardLikeRepository;
import com.dragon.stepbystep.repository.BoardRepository;
import com.dragon.stepbystep.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final PointRewardService pointRewardService;

    // 게시글 작성
    @Transactional
    public BoardPostResponseDto createPost(Long authorId, String content) {
        if (authorId == null) {
            throw new IllegalArgumentException("작성자 정보가 필요합니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("내용을 입력해주세요.");
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(UserNotFoundException::new);

        Board newPost = Board.builder()
                .author(author)
                .authorNickname(author.getNickname())
                .content(content)
                .build();

        Board savedPost = boardRepository.save(newPost);
        boardRepository.flush();
        pointRewardService.rewardForBoardPost(authorId);
        return BoardPostResponseDto.from(savedPost, authorId);
    }

    // 특정 게시글 보기
    @Transactional(readOnly = true)
    public BoardPostListResponseDto getPosts(String keyword, BoardSearchType searchType, Pageable pageable, Long currentUserId) {
        Page<Board> boards = searchBoards(keyword, searchType, pageable);

        if (keyword != null && !keyword.isBlank() && boards.isEmpty()) {
            throw new BoardSearchResultNotFoundException();
        }

        Page<BoardPostSummaryDto> dtoPage = boards.map(board -> BoardPostSummaryDto.from(board, currentUserId));

        return BoardPostListResponseDto.builder()
                .content(dtoPage.getContent())
                .page(dtoPage.getNumber())
                .size(dtoPage.getSize())
                .totalElements(dtoPage.getTotalElements())
                .totalPages(dtoPage.getTotalPages())
                .last(dtoPage.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public BoardPostDetailResponseDto getPost(Long postId, Long currentUserId) {
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new BoardNotFoundException(postId));

        List<BoardCommentResponseDto> comments = boardCommentRepository.findByBoard_IdOrderByCreatedAtAsc(postId)
                .stream()
                .map(comment -> BoardCommentResponseDto.from(comment, currentUserId))
                .collect(Collectors.toList());

        return BoardPostDetailResponseDto.from(board, comments);
    }

    // 댓글 작성
    @Transactional
    public BoardCommentResponseDto createComment(Long postId, Long userId, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
        }

        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new BoardNotFoundException(postId));

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        BoardComment comment = BoardComment.builder()
                .board(board)
                .author(user)
                .authorNickname(user.getNickname())
                .content(content)
                .build();

        BoardComment savedComment = boardCommentRepository.save(comment);
        boardCommentRepository.flush();
        board.increaseCommentsCount();

        return BoardCommentResponseDto.from(savedComment, userId);
    }

    // 댓글 수정
    @Transactional
    public BoardCommentResponseDto updateComment(Long postId, Long commentId, Long userId, String content) throws AccessDeniedException {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
        }

        BoardComment comment = boardCommentRepository.findById(commentId)
                .orElseThrow(() -> new BoardCommentNotFoundException(commentId));

        if (!comment.getBoard().getId().equals(postId)) {
            throw new IllegalArgumentException("해당 게시글의 댓글이 아닙니다.");
        }

        if (!comment.isAuthor(userId)) {
            throw new AccessDeniedException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        comment.updateContent(content);
        boardCommentRepository.flush();

        return BoardCommentResponseDto.from(comment, userId);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long postId, Long commentId, Long userId) throws AccessDeniedException {
        BoardComment comment = boardCommentRepository.findById(commentId)
                .orElseThrow(() -> new BoardCommentNotFoundException(commentId));

        if (!comment.getBoard().getId().equals(postId)) {
            throw new IllegalArgumentException("해당 게시글의 댓글이 아닙니다.");
        }

        if (!comment.isAuthor(userId)) {
            throw new AccessDeniedException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        comment.getBoard().decreaseCommentsCount();
        boardCommentRepository.delete(comment);
    }

    // 게시글 수정
    @Transactional
    public BoardPostResponseDto updatePost(Long postId, Long authorId, String content) throws AccessDeniedException {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("내용을 수정해주세요.");
        }

        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new BoardNotFoundException(postId));

        if (!board.isAuthor(authorId)) {
            throw new AccessDeniedException("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        board.updateContent(content);
        boardRepository.flush();

        return BoardPostResponseDto.from(board, authorId);
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId, Long authorId) throws AccessDeniedException {
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new BoardNotFoundException(postId));

        if (!board.isAuthor(authorId)) {
            throw new AccessDeniedException("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        boardRepository.delete(board);
    }

    // 게시글 좋아요
    @Transactional
    public BoardLikeResponseDto likePost(Long postId, Long userId) {
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new BoardNotFoundException(postId));

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (!boardLikeRepository.existsByBoard_IdAndUser_Id(postId, userId)) {
            BoardLike boardLike = BoardLike.builder()
                    .board(board)
                    .user(user)
                    .build();
            boardLikeRepository.save(boardLike);
            board.increaseLikesCount();
        }

        return BoardLikeResponseDto.builder()
                .liked(true)
                .likeNum(board.getLikesCount())
                .build();
    }

    // 게시글 좋아요 취소
    @Transactional
    public BoardLikeResponseDto unlikePost(Long postId, Long userId) {
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new BoardNotFoundException(postId));

        userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        boardLikeRepository.findByBoard_IdAndUser_Id(postId, userId)
                .ifPresent(boardLike -> {
                    boardLikeRepository.delete(boardLike);
                    board.decreaseLikesCount();
                });

        return BoardLikeResponseDto.builder()
                .liked(false)
                .likeNum(board.getLikesCount())
                .build();
    }

    // 게시글 목록
    private Page<Board> searchBoards(String keyword, BoardSearchType searchType, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return boardRepository.findAll(pageable);
        }

        return switch (searchType) {
            case CONTENT -> boardRepository.findByContentContainingIgnoreCase(keyword, pageable);
            case NICKNAME -> boardRepository.findByAuthor_NicknameContainingIgnoreCase(keyword, pageable);
            case ALL -> boardRepository.findByContentContainingIgnoreCaseOrAuthor_NicknameContainingIgnoreCase(keyword, keyword, pageable);
        };
    }
}