package io.github.Wanyui.springtodoapp.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Test class for Todo entity.
 * 
 * @author Wanyui/Jano
 * @version 1.0
 * @since 2025-08-06
 */
class TodoTest {

    private Validator validator;
    private Todo todo;
    private User user;

    @BeforeEach
	@SuppressWarnings("unused")
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setEmail("test@example.com");
        
        todo = new Todo();
        todo.setTitle("Test Todo");
        todo.setDescription("Test Description");
        todo.setDone(false);
        todo.setUser(user);
    }

    @Test
    @DisplayName("Should create todo with valid data")
    void shouldCreateTodoWithValidData() {
        // When
        Set<ConstraintViolation<Todo>> violations = validator.validate(todo);
        
        // Then
        assertThat(violations).isEmpty();
        assertThat(todo.getTitle()).isEqualTo("Test Todo");
        assertThat(todo.getDescription()).isEqualTo("Test Description");
        assertThat(todo.isDone()).isFalse();
        assertThat(todo.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("Should fail validation when title is blank")
    void shouldFailValidationWhenTitleIsBlank() {
        // Given
        todo.setTitle("");
        
        // When
        Set<ConstraintViolation<Todo>> violations = validator.validate(todo);
        
        // Then
        assertThat(violations).hasSize(2); // @NotBlank and @Size both fail
        assertThat(violations.stream().map(v -> v.getPropertyPath().toString())).contains("title");
    }

    @Test
    @DisplayName("Should fail validation when title is null")
    void shouldFailValidationWhenTitleIsNull() {
        // Given
        todo.setTitle(null);
        
        // When
        Set<ConstraintViolation<Todo>> violations = validator.validate(todo);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("title");
    }

    @Test
    @DisplayName("Should fail validation when title is too long")
    void shouldFailValidationWhenTitleIsTooLong() {
        // Given
        String longTitle = "a".repeat(101); // More than 100 characters
        todo.setTitle(longTitle);
        
        // When
        Set<ConstraintViolation<Todo>> violations = validator.validate(todo);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("title");
    }

    @Test
    @DisplayName("Should fail validation when description is too long")
    void shouldFailValidationWhenDescriptionIsTooLong() {
        // Given
        String longDescription = "a".repeat(1001); // More than 1000 characters
        todo.setDescription(longDescription);
        
        // When
        Set<ConstraintViolation<Todo>> violations = validator.validate(todo);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("description");
    }

    @Test
    @DisplayName("Should fail validation when user is null")
    void shouldFailValidationWhenUserIsNull() {
        // Given
        todo.setUser(null);
        
        // When
        Set<ConstraintViolation<Todo>> violations = validator.validate(todo);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("user");
    }

    @Test
    @DisplayName("Should toggle done status correctly")
    void shouldToggleDoneStatusCorrectly() {
        // Given
        assertThat(todo.isDone()).isFalse();
        
        // When
        todo.toggleDone();
        
        // Then
        assertThat(todo.isDone()).isTrue();
        
        // When
        todo.toggleDone();
        
        // Then
        assertThat(todo.isDone()).isFalse();
    }

    @Test
    @DisplayName("Should update title and description successfully")
    void shouldUpdateTitleAndDescriptionSuccessfully() {
        // Given
        String newTitle = "Updated Todo";
        String newDescription = "Updated Description";
        
        // When
        todo.update(newTitle, newDescription);
        
        // Then
        assertThat(todo.getTitle()).isEqualTo(newTitle);
        assertThat(todo.getDescription()).isEqualTo(newDescription);
    }

    @Test
    @DisplayName("Should update title and description with null description")
    void shouldUpdateTitleAndDescriptionWithNullDescription() {
        // Given
        String newTitle = "Updated Todo";
        
        // When
        todo.update(newTitle, null);
        
        // Then
        assertThat(todo.getTitle()).isEqualTo(newTitle);
        assertThat(todo.getDescription()).isNull();
    }

    @Test
    @DisplayName("Should throw exception when updating with empty title")
    void shouldThrowExceptionWhenUpdatingWithEmptyTitle() {
        // When & Then
        assertThatThrownBy(() -> todo.update("", "Description"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Title cannot be empty");
    }

    @Test
    @DisplayName("Should throw exception when updating with null title")
    void shouldThrowExceptionWhenUpdatingWithNullTitle() {
        // When & Then
        assertThatThrownBy(() -> todo.update(null, "Description"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Title cannot be empty");
    }

    @Test
    @DisplayName("Should throw exception when updating with whitespace title")
    void shouldThrowExceptionWhenUpdatingWithWhitespaceTitle() {
        // When & Then
        assertThatThrownBy(() -> todo.update("   ", "Description"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Title cannot be empty");
    }

    @Test
    @DisplayName("Should trim title and description when updating")
    void shouldTrimTitleAndDescriptionWhenUpdating() {
        // Given
        String titleWithSpaces = "  Test Title  ";
        String descriptionWithSpaces = "  Test Description  ";
        
        // When
        todo.update(titleWithSpaces, descriptionWithSpaces);
        
        // Then
        assertThat(todo.getTitle()).isEqualTo("Test Title");
        assertThat(todo.getDescription()).isEqualTo("Test Description");
    }

    @Test
    @DisplayName("Should handle done status correctly")
    void shouldHandleDoneStatusCorrectly() {
        // Given
        assertThat(todo.isDone()).isFalse();
        
        // When
        todo.setDone(true);
        
        // Then
        assertThat(todo.isDone()).isTrue();
    }

    @Test
    @DisplayName("Should handle timestamps correctly")
    void shouldHandleTimestampsCorrectly() {
        // When
        Todo newTodo = new Todo();
        newTodo.setTitle("New Todo");
        newTodo.setUser(user);
        
        // Then - timestamps are set by Hibernate, not by constructor
        assertThat(newTodo.getCreatedAt()).isNull(); // Not set until persisted
        assertThat(newTodo.getUpdatedAt()).isNull(); // Not set until persisted
    }

    @Test
    @DisplayName("Should handle user relationship correctly")
    void shouldHandleUserRelationshipCorrectly() {
        // Given
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password123");
        newUser.setEmail("new@example.com");
        
        // When
        todo.setUser(newUser);
        
        // Then
        assertThat(todo.getUser()).isEqualTo(newUser);
    }

    @Test
    @DisplayName("Should handle empty description correctly")
    void shouldHandleEmptyDescriptionCorrectly() {
        // Given
        todo.setDescription("");
        
        // When
        Set<ConstraintViolation<Todo>> violations = validator.validate(todo);
        
        // Then
        assertThat(violations).isEmpty(); // Empty description is allowed
        assertThat(todo.getDescription()).isEqualTo("");
    }

    @Test
    @DisplayName("Should handle null description correctly")
    void shouldHandleNullDescriptionCorrectly() {
        // Given
        todo.setDescription(null);
        
        // When
        Set<ConstraintViolation<Todo>> violations = validator.validate(todo);
        
        // Then
        assertThat(violations).isEmpty(); // Null description is allowed
        assertThat(todo.getDescription()).isNull();
    }
} 