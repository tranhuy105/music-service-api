package com.tranhuy105.musicserviceapi.exception;

import com.tranhuy105.musicserviceapi.dto.ExceptionResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class ExceptionHandlerAdvice {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDto> handleException(Exception ex) {
        logger.error("Unhandled Exception: ",ex);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(
                ExceptionResponseDto.builder()
                        .message("Internal Server Error: "+ ex.getMessage())
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
}