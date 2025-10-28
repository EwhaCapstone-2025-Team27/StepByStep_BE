package com.dragon.stepbystep.exception;

import com.dragon.stepbystep.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException; // ⬅️ Spring Security 타입으로 교체!
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import jakarta.validation.ConstraintViolationException;
import java.net.ConnectException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // === 400 Validation 계열 ===

  // @RequestBody @Valid 실패
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodArgNotValid(MethodArgumentNotValidException e) {
    String message = e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .orElse("잘못된 요청입니다.");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(message));
  }

  // @ModelAttribute / 바인딩 실패
  @ExceptionHandler(BindException.class)
  public ResponseEntity<ApiResponse<Void>> handleBind(BindException e) {
    String message = e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .orElse("요청 바인딩 오류입니다.");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(message));
  }

  // @RequestParam/@PathVariable 제약 위반
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleConstraint(ConstraintViolationException e) {
    String message = e.getConstraintViolations().stream()
            .findFirst()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .orElse("유효성 검사 실패입니다.");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(message));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("필수 파라미터 누락: " + e.getParameterName()));
  }

  @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
  public ResponseEntity<ApiResponse<Void>> handleBadRequest(RuntimeException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(e.getMessage()));
  }

  // === 401 / 403 ===

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<Void>> handleBadCred(BadCredentialsException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(e.getMessage()));
  }

  // Spring Security AccessDeniedException
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error("요청한 리소스에 대한 권한이 없습니다."));
  }

  // === 404 (도메인 NotFound 예시 — 네 프로젝트 예외 유지) ===

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(e.getMessage()));
  }

  @ExceptionHandler(BoardNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleBoardNotFound(BoardNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(e.getMessage()));
  }

  @ExceptionHandler(BadgeNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleBadgeNotFound(BadgeNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(e.getMessage()));
  }

  // === 405 / 415 (HTTP 표준) ===

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(ApiResponse.error("허용되지 않은 HTTP 메서드입니다."));
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(ApiResponse.error("지원하지 않는 Content-Type 입니다."));
  }

  // === 409 / 400 (비즈니스 충돌) ===

  @ExceptionHandler({ DuplicateEmailException.class, DuplicateNicknameException.class })
  public ResponseEntity<ApiResponse<Void>> handleDuplicate(RuntimeException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(e.getMessage()));
  }

  @ExceptionHandler(BadgeAlreadyOwnedException.class)
  public ResponseEntity<ApiResponse<Void>> handleBadgeAlreadyOwned(BadgeAlreadyOwnedException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(e.getMessage()));
  }

  @ExceptionHandler(InsufficientPointsException.class)
  public ResponseEntity<ApiResponse<Void>> handleInsufficientPoints(InsufficientPointsException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(e.getMessage()));
  }

  @ExceptionHandler(UserDeletedException.class)
  public ResponseEntity<ApiResponse<Void>> handleUserDeleted(UserDeletedException e) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error(e.getMessage()));
  }

  // === 5xx Mail ===

  @ExceptionHandler(MailException.class)
  public ResponseEntity<ApiResponse<Void>> handleMailException(MailException e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("메일 서버 오류가 발생했습니다."));
  }

  // === WebClient/네트워크 계열 — FastAPI 프록시 핵심 ===

  // 4xx/5xx 응답 (FastAPI에서 상태코드와 본문이 온 경우)
  @ExceptionHandler(WebClientResponseException.class)
  public ResponseEntity<ApiResponse<Void>> handleWebClientResponse(WebClientResponseException e) {
    // e.getStatusCode()를 그대로 반영 (FE가 상태코드 기반 분기하는 경우 유용)
    HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
    String message = "AI 서버 응답 오류 (" + status.value() + ")";
    // 필요시: 본문 메시지를 노출하려면 아래로 교체
    // String message = Optional.ofNullable(e.getResponseBodyAsString()).filter(s -> !s.isBlank()).orElse("AI 서버 응답 오류 (" + status.value() + ")");
    return ResponseEntity.status(status).body(ApiResponse.error(message));
  }

  // 연결/타임아웃/네트워크
  @ExceptionHandler({ WebClientRequestException.class, ConnectException.class })
  public ResponseEntity<ApiResponse<Void>> handleWebClientRequest(Exception e) {
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(ApiResponse.error("AI 서버에 연결할 수 없습니다."));
  }

  // RestTemplate 사용 시 대비 (선택)
  @ExceptionHandler({ HttpClientErrorException.class, HttpServerErrorException.class })
  public ResponseEntity<ApiResponse<Void>> handleRestTemplate(HttpClientErrorException e) {
    return ResponseEntity.status(e.getStatusCode())
            .body(ApiResponse.error("외부 API 오류 (" + e.getStatusCode().value() + ")"));
  }

  // === 최종 안전망 ===
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleAny(Exception e) {
    // 운영이라면 여기서 로깅: log.error("Unhandled exception", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("서버 오류가 발생했습니다."));
  }
}
