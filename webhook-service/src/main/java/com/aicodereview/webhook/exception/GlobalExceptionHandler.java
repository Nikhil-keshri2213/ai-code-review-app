package com.aicodereview.webhook.exception;

import com.aicodereview.common.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidSignatureException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidSignature(
            InvalidSignatureException ex,
            HttpServletRequest request) {
        log.warn("Invalid signature: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of(
                        401,
                        "Unauthorized",
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {
        log.error("Runtime error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(
                        500,
                        "Internal Server Error",
                        "An unexpected error occurred",
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        log.error("Unexpected error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(
                        500,
                        "Internal Server Error",
                        "Unexpected error occurred",
                        request.getRequestURI()
                ));
    }
}


// Your final common module structure should look like this:
// ```
// common/src/main/java/com/ai/codereview/common/
// ├── config/
// │   └── JacksonConfig.java
// ├── dto/
// │   ├── PullRequestEvent.java
// │   ├── ReviewRequest.java
// │   ├── ReviewResult.java
// │   ├── ApiErrorResponse.java
// │   └── ApiResponse.java
// └── enums/
//     ├── Severity.java
//     └── ReviewCategory.java