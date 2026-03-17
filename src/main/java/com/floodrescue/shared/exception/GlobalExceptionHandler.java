package com.floodrescue.shared.exception;

import com.floodrescue.shared.dto.ApiResult;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<Void>> handleBusiness(BusinessException ex) {
        return ResponseEntity.badRequest().body(ApiResult.error(ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResult<Void>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResult.error(ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResult<Void>> handleForbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResult.error(ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResult<Void>> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResult.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(ApiResult.error("Dữ liệu không hợp lệ", errors));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResult<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String message = resolveDataIntegrityMessage(ex);
        log.warn("Data integrity violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResult.error(message));
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResult<Void>> handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        log.warn("Lỗi định dạng dữ liệu: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResult.error("Định dạng dữ liệu gửi lên không đúng (ví dụ: gõ sai kiểu chữ, sai định dạng số). Vui lòng kiểm tra lại."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleUnhandled(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResult.error("Hệ thống gặp lỗi, vui lòng thử lại"));
    }

    private String resolveDataIntegrityMessage(DataIntegrityViolationException ex) {
        String rawMessage = ex.getMostSpecificCause() == null
                ? ex.getMessage()
                : ex.getMostSpecificCause().getMessage();
        String normalized = rawMessage == null ? "" : rawMessage.toLowerCase(Locale.ROOT);

        if (normalized.contains("data too long") || normalized.contains("data truncation")) {
            return "Dữ liệu lưu vượt quá giới hạn cho phép. Vui lòng rút gọn nội dung hoặc kiểm tra cấu hình dữ liệu.";
        }
        if (normalized.contains("foreign key") || normalized.contains("constraint fails")) {
            return "Không thể cập nhật dữ liệu vì đang có ràng buộc liên kết trong hệ thống.";
        }
        if (normalized.contains("duplicate") || normalized.contains("unique")) {
            return "Dữ liệu bị trùng với bản ghi hiện có trong hệ thống.";
        }

        return "Không thể lưu dữ liệu do vi phạm ràng buộc hệ thống. Vui lòng kiểm tra lại dữ liệu gửi lên.";
    }
}
