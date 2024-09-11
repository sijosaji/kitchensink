package com.mongodbdemo.kitchensink.service;

import com.mongodbdemo.kitchensink.model.DatabaseSequence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class SequenceGeneratorServiceTest {

    @Mock
    private MongoOperations mongoOperations;

    @InjectMocks
    private SequenceGeneratorService sequenceGeneratorService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void generateSequenceWhenSequenceDoesNotExist_shouldInitializeAndReturnSequence() {
        // Arrange
        String seqName = "testSequence";
        DatabaseSequence mockSequence = new DatabaseSequence();
        mockSequence.setSeq(1L);
        when(mongoOperations.findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(DatabaseSequence.class)
        )).thenReturn(mockSequence);

        // Act
        long result = sequenceGeneratorService.generateSequence(seqName);

        // Assert
        assertEquals(1L, result);
    }

    @Test
    public void generateSequenceWhenSequenceExists_shouldIncrementAndReturnNewSequence() {
        // Arrange
        String seqName = "testSequence";
        DatabaseSequence mockSequence = new DatabaseSequence();
        mockSequence.setSeq(2L);  // Existing sequence value
        when(mongoOperations.findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(DatabaseSequence.class)
        )).thenReturn(mockSequence);

        // Act
        long result = sequenceGeneratorService.generateSequence(seqName);

        // Assert
        assertEquals(2L, result);
    }

    @Test
    public void generateSequenceWhenDatabaseReturnsNull_shouldReturnDefaultSequence() {
        // Arrange
        String seqName = "testSequence";
        when(mongoOperations.findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(DatabaseSequence.class)
        )).thenReturn(null);

        // Act
        long result = sequenceGeneratorService.generateSequence(seqName);

        // Assert
        assertEquals(1L, result);
    }
}
