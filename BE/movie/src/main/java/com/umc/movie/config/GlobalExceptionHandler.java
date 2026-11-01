package com.umc.movie.config;

import com.umc.movie.dto.ApiResponse;
import com.umc.movie.exception.ApiException;
import jakarta.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private ResponseEntity<ApiResponse<Void>> error(int status, String code, String message) {
    return ResponseEntity.status(status).body(ApiResponse.failure(code, message));
  }

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiResponse<Void>> business(ApiException e) {
    return error(e.getStatus(), e.getCode(), e.getMessage());
  }

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    ConstraintViolationException.class,
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class
  })
  public ResponseEntity<ApiResponse<Void>> validation(Exception e) {
    return error(400, "COMMON400", "입력 값의 형식과 범위를 확인해 주세요.");
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ApiResponse<Void>> status(ResponseStatusException e) {
    return error(
        e.getStatusCode().value(), "COMMON" + e.getStatusCode().value(), "요청한 리소스를 확인해 주세요.");
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<Void>> integrity(DataIntegrityViolationException e) {
    for (Throwable cause = e; cause != null; cause = cause.getCause()) {
      if (cause instanceof SQLException sql && sql.getErrorCode() == 1062) {
        String message = String.valueOf(sql.getMessage()).toLowerCase(Locale.ROOT);
        if (message.contains("uk_member_email"))
          return error(409, "MEMBER4091", "이미 사용 중인 이메일입니다.");
        if (message.contains("uk_member_nickname"))
          return error(409, "MEMBER4092", "이미 사용 중인 닉네임입니다.");
        if (message.contains("uk_rating_member_movie"))
          return error(409, "RATING4091", "이미 이 영화에 평점을 남기셨습니다.");
        return error(409, "COMMON409", "이미 등록된 데이터입니다.");
      }
    }
    return error(409, "COMMON409", "데이터 제약 조건을 확인해 주세요.");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> unexpected(Exception e) {
    return error(500, "COMMON500", "서버 오류가 발생했습니다.");
  }
}
