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
 * Test class for User entity.
 * 
 * @author Wanyui/Jano
 * @version 1.0
 * @since 2025-08-06
 */
class UserTest {

    private Validator validator;
    private User user;
    private Todo todo;

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
    }

    @Test
    @DisplayName("Should create user with valid data")
    void shouldCreateUserWithValidData() {
        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        // Then
        assertThat(violations).isEmpty();
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getPassword()).isEqualTo("password123");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should fail validation when username is blank")
    void shouldFailValidationWhenUsernameIsBlank() {
        // Given
        user.setUsername("");
        
        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        // Then
        assertThat(violations).hasSize(2); // @NotBlank and @Size both fail
        assertThat(violations.stream().map(v -> v.getPropertyPath().toString())).contains("username");
    }

    @Test
    @DisplayName("Should fail validation when username is too short")
    void shouldFailValidationWhenUsernameIsTooShort() {
        // Given
        user.setUsername("ab"); // Less than MIN_USERNAME_LENGTH (3)
        
        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("username");
    }

    @Test
    @DisplayName("Should fail validation when password is too short")
    void shouldFailValidationWhenPasswordIsTooShort() {
        // Given
        user.setPassword("123"); // Less than MIN_PASSWORD_LENGTH (6)
        
        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("password");
    }

    @Test
    @DisplayName("Should fail validation when email is invalid")
    void shouldFailValidationWhenEmailIsInvalid() {
        // Given
        user.setEmail("invalid-email");
        
        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
    }

    @Test
    @DisplayName("Should add todo to user successfully")
    void shouldAddTodoToUserSuccessfully() {
        // When
        user.addTodo(todo);
        
        // Then
        assertThat(user.getTodos()).contains(todo);
        assertThat(todo.getUser()).isEqualTo(user);
        assertThat(user.getTodos()).hasSize(1);
    }

    @Test
    @DisplayName("Should throw exception when adding todo that already belongs to user")
    void shouldThrowExceptionWhenAddingTodoThatAlreadyBelongsToUser() {
        // Given
        user.addTodo(todo);
        
        // When & Then
        assertThatThrownBy(() -> user.addTodo(todo))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Todo already belongs to this user");
    }

    @Test
    @DisplayName("Should remove todo from user successfully")
    void shouldRemoveTodoFromUserSuccessfully() {
        // Given
        user.addTodo(todo);
        assertThat(user.getTodos()).hasSize(1);
        
        // When
        user.removeTodo(todo);
        
        // Then
        assertThat(user.getTodos()).hasSize(0);
        assertThat(todo.getUser()).isNull();
    }

    @Test
    @DisplayName("Should throw exception when removing todo that does not belong to user")
    void shouldThrowExceptionWhenRemovingTodoThatDoesNotBelongToUser() {
        // Given
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setPassword("password123");
        anotherUser.setEmail("another@example.com");
        anotherUser.addTodo(todo);
        
        // When & Then
        assertThatThrownBy(() -> user.removeTodo(todo))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Todo does not belong to this user");
    }

    @Test
    @DisplayName("Should get done todos correctly")
    void shouldGetDoneTodosCorrectly() {
        // Given
        Todo doneTodo = new Todo();
        doneTodo.setTitle("Done Todo");
        doneTodo.setDone(true);
        
        user.addTodo(todo);
        user.addTodo(doneTodo);
        
        // When
        Set<Todo> doneTodos = user.getDoneTodos();
        
        // Then
        assertThat(doneTodos).hasSize(1);
        assertThat(doneTodos).contains(doneTodo);
    }

    @Test
    @DisplayName("Should get not done todos correctly")
    void shouldGetNotDoneTodosCorrectly() {
        // Given
        Todo doneTodo = new Todo();
        doneTodo.setTitle("Done Todo");
        doneTodo.setDone(true);
        
        user.addTodo(todo);
        user.addTodo(doneTodo);
        
        // When
        Set<Todo> notDoneTodos = user.getNotDoneTodos();
        
        // Then
        assertThat(notDoneTodos).hasSize(1);
        assertThat(notDoneTodos).contains(todo);
    }

    @Test
    @DisplayName("Should have correct constants")
    void shouldHaveCorrectConstants() {
        // Then
        assertThat(User.MIN_USERNAME_LENGTH).isEqualTo(3);
        assertThat(User.MAX_USERNAME_LENGTH).isEqualTo(100);
        assertThat(User.MIN_PASSWORD_LENGTH).isEqualTo(6);
        assertThat(User.MAX_PASSWORD_LENGTH).isEqualTo(100);
        assertThat(User.MIN_EMAIL_LENGTH).isEqualTo(5);
        assertThat(User.MAX_EMAIL_LENGTH).isEqualTo(100);
    }

    @Test
    @DisplayName("Should handle multiple todos correctly")
    void shouldHandleMultipleTodosCorrectly() {
        // Given
        Todo todo2 = new Todo();
        todo2.setTitle("Second Todo");
        todo2.setDescription("Second Description");
        todo2.setDone(true);
        
        // When
        user.addTodo(todo);
        user.addTodo(todo2);
        
        // Then
        assertThat(user.getTodos()).hasSize(2);
        assertThat(user.getTodos()).contains(todo);
        assertThat(user.getTodos()).contains(todo2);
        assertThat(user.getDoneTodos()).hasSize(1);
        assertThat(user.getNotDoneTodos()).hasSize(1);
    }
} 