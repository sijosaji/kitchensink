package com.mongodbdemo.kitchensink.exceptionhandler;

import com.mongodbdemo.kitchensink.dto.ErrorResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST controllers.
 */
@RestControllerAdvice
public class MemberControllerExceptionHandler {

    @ExceptionHandler({HttpClientErrorException.class, ResponseStatusException.class})
    public ResponseEntity<ErrorResponse> handleHttpExceptions(Exception ex) {
        HttpStatusCode statusCode = extractStatusCode(ex);
        String message = extractMessage(ex);
        var response = ResponseEntity.status(statusCode);
        if (statusCode == HttpStatus.TOO_MANY_REQUESTS) {
            HttpClientErrorException clientErrorException = (HttpClientErrorException) ex;
            response.header("retry-after",
                    clientErrorException.getResponseHeaders().get("retry-after").get(0));
        }
        return response.body(new ErrorResponse(message));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateKeyException(DuplicateKeyException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("Check if any field on which uniqueness is defined is being duplicated"));
    }

     HttpStatusCode extractStatusCode(Exception ex) {
        if (ex instanceof HttpClientErrorException httpEx) {
            return httpEx.getStatusCode();
        } else if (ex instanceof ResponseStatusException statusEx) {
            return statusEx.getStatusCode();
        }
        throw new IllegalArgumentException("Unsupported exception type");
    }

    String extractMessage(Exception ex) {
        if (ex instanceof HttpClientErrorException httpEx) {
            return getStatusMessage(httpEx.getStatusCode());
        } else if (ex instanceof ResponseStatusException statusEx) {
            return statusEx.getReason() != null ? statusEx.getReason() : statusEx.getMessage();
        }
        throw new IllegalArgumentException("Unsupported exception type");
    }

    String getStatusMessage(HttpStatusCode status) {
        return switch (status) {
            case HttpStatus.UNAUTHORIZED -> "Provided request is unauthenticated";
            case HttpStatus.FORBIDDEN -> "Provided request is unauthorized";
            case HttpStatus.TOO_MANY_REQUESTS -> "Too many requests please try again later";
            default -> throw new IllegalStateException("Unexpected value: " + status);
        };
    }


}
