package com.mongodbdemo.kitchensink.exceptionhandler;

import com.mongodbdemo.kitchensink.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class MemberControllerExceptionHandlerTest {

    @InjectMocks
    private MemberControllerExceptionHandler exceptionHandler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void handleValidationExceptionShouldReturnBadRequestWithErrors() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        var fieldError = mock(org.springframework.validation.FieldError.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(fieldError.getField()).thenReturn("fieldName");
        when(fieldError.getDefaultMessage()).thenReturn("errorMessage");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Map.of("fieldName", "errorMessage"), response.getBody());
    }

    @Test
    public void handleHttpExceptionsWithHttpClientErrorException_shouldReturnCorrectStatusAndMessage() {
        // Arrange
        HttpClientErrorException ex = new HttpClientErrorException(HttpStatus.UNAUTHORIZED);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleHttpExceptions(ex);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Provided request is unauthenticated", response.getBody().error());
    }

    @Test
    public void handleHttpExceptionsWithResponseStatusException_shouldReturnCorrectStatusAndMessage() {
        // Arrange
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.FORBIDDEN, "Custom forbidden message");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleHttpExceptions(ex);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Custom forbidden message", response.getBody().error());
    }

    @Test
    public void extractStatusCodeWithHttpClientErrorException_shouldReturnCorrectStatusCode() {
        // Arrange
        HttpClientErrorException ex = new HttpClientErrorException(HttpStatus.NOT_FOUND);

        // Act
        HttpStatusCode statusCode = exceptionHandler.extractStatusCode(ex);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, statusCode);
    }

    @Test
    public void extractStatusCodeWithResponseStatusException_shouldReturnCorrectStatusCode() {
        // Arrange
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);

        // Act
        HttpStatusCode statusCode = exceptionHandler.extractStatusCode(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, statusCode);
    }

    @Test
    public void extractStatusCodeWithUnsupportedException_shouldThrowIllegalArgumentException() {
        // Arrange
        Exception ex = new Exception();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> exceptionHandler.extractStatusCode(ex));
    }

    @Test
    public void extractMessageWithHttpClientErrorException_shouldReturnCorrectMessage() {
        // Arrange
        HttpClientErrorException ex = new HttpClientErrorException(HttpStatus.UNAUTHORIZED);

        // Act
        String message = exceptionHandler.extractMessage(ex);

        // Assert
        assertEquals("Provided request is unauthenticated", message);
    }

    @Test
    public void extractMessageWithResponseStatusException_shouldReturnCorrectMessage() {
        // Arrange
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.FORBIDDEN, "Custom forbidden message");

        // Act
        String message = exceptionHandler.extractMessage(ex);

        // Assert
        assertEquals("Custom forbidden message", message);
    }

    @Test
    public void extractMessageWithUnsupportedException_shouldThrowIllegalArgumentException() {
        // Arrange
        Exception ex = new Exception();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> exceptionHandler.extractMessage(ex));
    }

    @Test
    public void getStatusMessageWithUnauthorizedStatus_shouldReturnCorrectMessage() {
        // Arrange
        HttpStatusCode status = HttpStatus.UNAUTHORIZED;

        // Act
        String message = exceptionHandler.getStatusMessage(status);

        // Assert
        assertEquals("Provided request is unauthenticated", message);
    }

    @Test
    public void getStatusMessageWithForbiddenStatus_shouldReturnCorrectMessage() {
        // Arrange
        HttpStatusCode status = HttpStatus.FORBIDDEN;

        // Act
        String message = exceptionHandler.getStatusMessage(status);

        // Assert
        assertEquals("Provided request is unauthorized", message);
    }

    @Test
    public void getStatusMessageWithUnexpectedStatus_shouldThrowIllegalStateException() {
        // Arrange
        HttpStatusCode status = HttpStatus.OK;

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> exceptionHandler.getStatusMessage(status));
    }

    @Test
    void handleHttpExceptionsShouldReturn429WithRetryAfterHeader() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.add("retry-after", "60"); // Mock header value
        HttpClientErrorException clientErrorException = new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests", headers, null, null);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleHttpExceptions(clientErrorException);

        // Assert
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("60", response.getHeaders().getFirst("retry-after"));
        assertEquals("Too many requests please try again later", response.getBody().error());
    }

    @Test
    void getStatusMessageShouldReturnMessageFor429Status() {
        // Arrange
        HttpStatusCode status = HttpStatus.TOO_MANY_REQUESTS;

        // Act
        String message = exceptionHandler.getStatusMessage(status);

        // Assert
        assertEquals("Too many requests please try again later", message);
    }

    @Test
    public void testHandleDuplicateKeyException() {
        DuplicateKeyException exception = new DuplicateKeyException("Duplicate key error");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateKeyException(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Check if any field on which uniqueness is defined is being duplicated", response.getBody().error());
    }

}
