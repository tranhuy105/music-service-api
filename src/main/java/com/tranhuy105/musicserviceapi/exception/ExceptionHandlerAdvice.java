package com.tranhuy105.musicserviceapi.exception;

import com.tranhuy105.musicserviceapi.dto.ExceptionResponseDto;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class ExceptionHandlerAdvice {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDto> handleException(Exception ex) {
        logger.error("Unhandled Exception: ",ex);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(
                ExceptionResponseDto.builder()
                        .message("Unhandled Exception: "+ ex.getMessage())
                        .httpStatus(INTERNAL_SERVER_ERROR)
                        .build()
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponseDto> handleException(BadCredentialsException ex) {
        return ResponseEntity.status(UNAUTHORIZED)
                .body(
                        ExceptionResponseDto.builder()
                                .message(ex.getMessage())
                                .httpStatus(UNAUTHORIZED)
                                .build()
                );
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<ExceptionResponseDto> handleException(ObjectNotFoundException ex) {
        return ResponseEntity.status(NOT_FOUND)
                .body(ExceptionResponseDto.builder()
                        .message(ex.getMessage())
                        .httpStatus(NOT_FOUND)
                        .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponseDto> handleException(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(BAD_REQUEST)
                .body(
                        ExceptionResponseDto.builder()
                                .message(ex.getMessage())
                                .httpStatus(BAD_REQUEST)
                                .build()
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponseDto> handleException(MethodArgumentNotValidException ex) {
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        Map<String, String> map = new HashMap<>();
        errors.forEach(
                error -> {
                    String key = ((FieldError) error).getField();
                    String val = error.getDefaultMessage();
                    map.put(key, val);
                }
        );
        return ResponseEntity.status(BAD_REQUEST)
                .body(ExceptionResponseDto.builder()
                        .httpStatus(BAD_REQUEST)
                        .message("Invalid Request Body")
                        .details(map)
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponseDto> handleException(AccessDeniedException ex) {
        return ResponseEntity.status(FORBIDDEN).body(ExceptionResponseDto.builder()
                .message(ex.getMessage()).httpStatus(FORBIDDEN).build());
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ExceptionResponseDto> handleException(MalformedJwtException ex) {
        return ResponseEntity.status(FORBIDDEN).body(ExceptionResponseDto.builder()
                .message(ex.getMessage()).httpStatus(FORBIDDEN).build());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ExceptionResponseDto> handleException(DuplicateKeyException ex) {
        return ResponseEntity.status(BAD_REQUEST).body(ExceptionResponseDto.builder()
                .message("Record Already Exists").httpStatus(BAD_REQUEST).details(Map.of("SQLException", ex.getMessage())).build());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionResponseDto> handleException(DataIntegrityViolationException ex) {
        return ResponseEntity.status(BAD_REQUEST).body(ExceptionResponseDto.builder()
                .message("Constraint Fail").httpStatus(BAD_REQUEST).details(Map.of("SQLException", ex.getMessage())).build());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ExceptionResponseDto> handleException(ExpiredJwtException ex) {
        return ResponseEntity.status(BAD_REQUEST).body(ExceptionResponseDto.builder()
                .message(ex.getMessage()).httpStatus(BAD_REQUEST).build());
    }
}