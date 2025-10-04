package com.dragon.stepbystep.exception;

import com.dragon.stepbystep.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    String message = e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getDefaultMessage())
            .orElse("잘못된 요청입니다.");
    return ResponseEntity.status(status)
            .body(ApiResponse.error(message));
  }

  // 401 인증 실패 (로그인 비밀번호 불일치)
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<Void>> handleBadCred(BadCredentialsException e) {
    HttpStatus status = HttpStatus.UNAUTHORIZED;
    return ResponseEntity.status(status)
            .body(ApiResponse.error(e.getMessage()));
  }

  // 400 잘못된 요청
  @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
  public ResponseEntity<ApiResponse<Void>> handleBadRequest(RuntimeException e) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status)
            .body(ApiResponse.error(e.getMessage()));
  }

  // 404 사용자 없음
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException e) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    return ResponseEntity.status(status)
            .body(ApiResponse.error(e.getMessage()));
  }

  // 404 게시글 없음
  @ExceptionHandler(BoardNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleBoardNotFound(BoardNotFoundException e) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    return ResponseEntity.status(status)
            .body(ApiResponse.error(e.getMessage()));
  }

  @ExceptionHandler(BadgeNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleBadgeNotFound(BadgeNotFoundException e) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    return ResponseEntity.status(status)
            .body(ApiResponse.error(e.getMessage()));
  }

  @ExceptionHandler(InsufficientPointsException.class)
  public ResponseEntity<ApiResponse<Void>> handleInsufficientPoints(InsufficientPointsException e) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status)
            .body(ApiResponse.error(e.getMessage()));
  }

  @ExceptionHandler({ DuplicateEmailException.class, DuplicateNicknameException.class })
  public ResponseEntity<ApiResponse<Void>> handleDuplicate(RuntimeException e) {
    HttpStatus status = HttpStatus.CONFLICT;
    return ResponseEntity.status(status)
            .body(ApiResponse.error(e.getMessage()));
  }

  @ExceptionHandler(BadgeAlreadyOwnedException.class)
  public ResponseEntity<ApiResponse<Void>> handleBadgeAlreadyOwned(BadgeAlreadyOwnedException e) {
    HttpStatus status = HttpStatus.CONFLICT;
    return ResponseEntity.status(status)
            .body(ApiResponse.error(e.getMessage()));
  }

  @ExceptionHandler(UserDeletedException.class)
  public ResponseEntity<ApiResponse<Void>> handleUserDeleted(UserDeletedException e) {
    HttpStatus status = HttpStatus.FORBIDDEN;
    return ResponseEntity.status(status)
            .body(ApiResponse.error(e.getMessage()));
  }

  // 400 필수 파라미터 누락
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException e) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    String msg = "필수 파라미터가 누락되었습니다: " + e.getParameterName();
    return ResponseEntity.status(status)
            .body(ApiResponse.error(msg));
  }

  // 403 권한 없음
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e) {
    HttpStatus status = HttpStatus.FORBIDDEN;
    return ResponseEntity.status(status)
            .body(ApiResponse.error("요청한 리소스에 대한 권한이 없습니다."));
  }

  @ExceptionHandler(MailException.class)
  public ResponseEntity<ApiResponse<Void>> handleMailException(MailException e) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    return ResponseEntity.status(status)
            .body(ApiResponse.error("메일 서버 오류가 발생했습니다."));
  }

  // 500 내부 서버 오류 (최종 예외 안전망)
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleAny(Exception e) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    // 운영 시에는 로깅: log.error("Unhandled exception", e);
    return ResponseEntity.status(status)
            .body(ApiResponse.error("서버 오류가 발생했습니다."));
  }
}