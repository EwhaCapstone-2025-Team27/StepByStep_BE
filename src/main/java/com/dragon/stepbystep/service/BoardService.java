package com.dragon.stepbystep.service;

import com.dragon.stepbystep.domain.Board;
import com.dragon.stepbystep.domain.User;
import com.dragon.stepbystep.domain.enums.BoardSearchType;
import com.dragon.stepbystep.dto.BoardPostListResponseDto;
import com.dragon.stepbystep.dto.BoardPostResponseDto;
import com.dragon.stepbystep.dto.BoardPostSummaryDto;
import com.dragon.stepbystep.exception.UserNotFoundException;
import com.dragon.stepbystep.repository.BoardRepository;
import com.dragon.stepbystep.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public BoardPostResponseDto createPost(Long authorId, String content) {
        if (authorId == null) {
            throw new IllegalArgumentException("작성자 정보가 필요합니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("게시글 내용은 필수입니다.");
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(UserNotFoundException::new);

        Board newPost = Board.builder()
                .author(author)
                .content(content)
                .build();

        Board savedPost = boardRepository.save(newPost);
        return BoardPostResponseDto.from(savedPost);
    }

    @Transactional(readOnly = true)
    public BoardPostListResponseDto getPosts(String keyword, BoardSearchType searchType, Pageable pageable) {
        Page<Board> boards = searchBoards(keyword, searchType, pageable);

        Page<BoardPostSummaryDto> dtoPage = boards.map(BoardPostSummaryDto::from);

        return BoardPostListResponseDto.builder()
                .content(dtoPage.getContent())
                .page(dtoPage.getNumber())
                .size(dtoPage.getSize())
                .totalElements(dtoPage.getTotalElements())
                .totalPages(dtoPage.getTotalPages())
                .last(dtoPage.isLast())
                .build();
    }

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