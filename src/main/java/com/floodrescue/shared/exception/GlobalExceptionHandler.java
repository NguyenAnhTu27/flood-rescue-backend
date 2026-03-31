package com.floodrescue.shared.exception;

import com.floodrescue.shared.util.TextNormalizationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String INVALID_DATA_MESSAGE = "D\u1eef li\u1ec7u kh\u00f4ng h\u1ee3p l\u1ec7";
    private static final String DATA_INTEGRITY_MESSAGE = "Kh\u00f4ng th\u1ec3 x\u00f3a v\u00ec ng\u01b0\u1eddi d\u00f9ng \u0111ang c\u00f3 d\u1eef li\u1ec7u li\u00ean k\u1ebft trong h\u1ec7 th\u1ed1ng";
    private static final String GENERIC_ERROR_MESSAGE = "H\u1ec7 th\u1ed1ng g\u1eb7p l\u1ed7i, vui l\u00f2ng th\u1eed l\u1ea1i";

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusiness(BusinessException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "message", TextNormalizationUtil.cleanDisplayText(ex.getMessage())
        ));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "message", TextNormalizationUtil.cleanDisplayText(ex.getMessage())
        ));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "message", TextNormalizationUtil.cleanDisplayText(ex.getMessage())
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), TextNormalizationUtil.cleanDisplayText(error.getDefaultMessage())));
        return ResponseEntity.badRequest().body(Map.of(
                "message", INVALID_DATA_MESSAGE,
                "errors", errors
        ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "message", DATA_INTEGRITY_MESSAGE
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnhandled(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", GENERIC_ERROR_MESSAGE
        ));
    }
}
