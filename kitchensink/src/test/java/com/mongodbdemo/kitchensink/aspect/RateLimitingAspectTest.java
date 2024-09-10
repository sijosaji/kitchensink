package com.mongodbdemo.kitchensink.aspect;

import com.mongodbdemo.kitchensink.helper.UserContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitingAspectTest {

    @InjectMocks
    private RateLimitingAspect rateLimitingAspect;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JoinPoint joinPoint;

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
        String expectedUrl = "http://localhost:8080/rate-limit?userId=testUserId";
        HttpEntity<Void> entity = new HttpEntity<>(new HttpHeaders());

        // Act
        rateLimitingAspect.rateLimit(joinPoint);

        // Assert
        verify(restTemplate, times(1)).exchange(eq(expectedUrl), eq(HttpMethod.GET), eq(entity), eq(Void.class));
        UserContext.clear();
    }

    @Test
    void rateLimitShouldNotCallRateLimitServiceWhenUserIdIsNull() {
        // Arrange
        UserContext.setUserId(null);

        // Act & Assert
        assertDoesNotThrow(() -> rateLimitingAspect.rateLimit(joinPoint));

        // Verify that restTemplate.exchange is not called
        verify(restTemplate, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Void.class));
        UserContext.clear();
    }

    @Test
    void buildRateLimitUrlShouldReturnCorrectUrl() {
        // Arrange
        String userId = "testUserId";
        String expectedUrl = "http://localhost:8080/rate-limit?userId=testUserId";

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
}
