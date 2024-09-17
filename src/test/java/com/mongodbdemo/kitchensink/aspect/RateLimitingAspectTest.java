package com.mongodbdemo.kitchensink.aspect;

import com.mongodbdemo.kitchensink.helper.UserContext;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitingAspectTest {

    @InjectMocks
    private RateLimitingAspect rateLimitingAspect;

    @Mock
    private RestTemplate restTemplate;

    private static final String TEST_URL = "http://example.com/rate-limit";
    private static final String USER_ID = "testUserId";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Set the rateLimitServiceUrl directly since it's a final field
        rateLimitingAspect = new RateLimitingAspect(restTemplate, "http://localhost:8080/rate-limit");
    }

    @Test
    void rateLimitShouldCallRateLimitServiceWhenUserIdIsNotNull() {
        // Arrange
        UserContext.setUserId("testUserId");
        String expectedUrl = "http://localhost:8080/rate-limit/testUserId";
        HttpEntity<Void> entity = new HttpEntity<>(new HttpHeaders());

        // Act
        rateLimitingAspect.rateLimit();

        // Assert
        verify(restTemplate, times(1)).exchange(eq(expectedUrl), eq(HttpMethod.PUT), eq(entity), eq(Void.class));
        UserContext.clear();
    }

    @Test
    void rateLimitShouldNotCallRateLimitServiceWhenUserIdIsNull() {
        // Arrange
        UserContext.setUserId(null);

        // Act & Assert
        assertDoesNotThrow(() -> rateLimitingAspect.rateLimit());

        // Verify that restTemplate.exchange is not called
        verify(restTemplate, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Void.class));
        UserContext.clear();
    }

    @Test
    void buildRateLimitUrlShouldReturnCorrectUrl() {
        // Arrange
        String userId = "testUserId";
        String expectedUrl = "http://localhost:8080/rate-limit/testUserId";

        // Act
        String actualUrl = rateLimitingAspect.buildRateLimitUrl(userId);

        // Assert
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    void createHttpEntityShouldReturnHttpEntityWithHeaders() {
        // Act
        HttpEntity<Void> entity = rateLimitingAspect.createHttpEntity();

        // Assert
        assertNotNull(entity);
        assertNotNull(entity.getHeaders());
    }

    @Test
    void testCallRateLimitServiceInternalServerError() {
        // Given an INTERNAL_SERVER_ERROR HttpClientErrorException
        HttpEntity<Void> entity = new HttpEntity<>(null);
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);

        // When the restTemplate.exchange method throws this exception
        doThrow(exception).when(restTemplate).exchange(TEST_URL, HttpMethod.PUT, entity, Void.class);

        // Then the method should log the error but not rethrow the exception
        rateLimitingAspect.callRateLimitService(TEST_URL, entity, USER_ID);

        // Verify that the exception was not rethrown
        verify(restTemplate, times(1)).exchange(TEST_URL, HttpMethod.PUT, entity, Void.class);
    }

    @Test
    void testCallRateLimitServiceOtherError() {
        // Given a BAD_REQUEST HttpClientErrorException
        HttpEntity<Void> entity = new HttpEntity<>(null);
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.BAD_REQUEST);

        // When the restTemplate.exchange method throws this exception
        doThrow(exception).when(restTemplate).exchange(TEST_URL, HttpMethod.PUT, entity, Void.class);

        // Then the exception should be propagated
        assertThrows(HttpClientErrorException.class, () ->
                rateLimitingAspect.callRateLimitService(TEST_URL, entity, USER_ID)
        );

    }
}
