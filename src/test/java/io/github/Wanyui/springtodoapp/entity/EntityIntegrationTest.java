package io.github.Wanyui.springtodoapp.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration test for User and Todo entities working together.
 * 
 * @author Wanyui/Jano
 * @version 1.0
 * @since 2025-08-06
 */
class EntityIntegrationTest {

    private User user;
    private Todo todo1;
    private Todo todo2;

    @BeforeEach
	@SuppressWarnings("unused")
    void setUp() {
        // Create user
        user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setEmail("test@example.com");
        
        // Create todos
        todo1 = new Todo();
        todo1.setTitle("First Todo");
        todo1.setDescription("First Description");
        todo1.setDone(false);
        
        todo2 = new Todo();
        todo2.setTitle("Second Todo");
        todo2.setDescription("Second Description");
        todo2.setDone(true);
    }

    @Test
    @DisplayName("Should create user with multiple todos")
    void shouldCreateUserWithMultipleTodos() {
        // When
        user.addTodo(todo1);
        user.addTodo(todo2);
        
        // Then
        assertThat(user.getTodos()).hasSize(2);
        assertThat(user.getTodos()).contains(todo1);
        assertThat(user.getTodos()).contains(todo2);
        assertThat(todo1.getUser()).isEqualTo(user);
        assertThat(todo2.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("Should get done and not done todos correctly")
    void shouldGetDoneAndNotDoneTodosCorrectly() {
        // Given
        user.addTodo(todo1);
        user.addTodo(todo2);
        
        // When
        Set<Todo> doneTodos = user.getDoneTodos();
        Set<Todo> notDoneTodos = user.getNotDoneTodos();
        
        // Then
        assertThat(doneTodos).hasSize(1);
        assertThat(doneTodos).contains(todo2);
        assertThat(notDoneTodos).hasSize(1);
        assertThat(notDoneTodos).contains(todo1);
    }

    @Test
    @DisplayName("Should toggle todo status correctly")
    void shouldToggleTodoStatusCorrectly() {
        // Given
        user.addTodo(todo1);
        assertThat(todo1.isDone()).isFalse();
        
        // When
        todo1.toggleDone();
        
        // Then
        assertThat(todo1.isDone()).isTrue();
        assertThat(user.getDoneTodos()).contains(todo1);
        assertThat(user.getNotDoneTodos()).doesNotContain(todo1);
    }

    @Test
    @DisplayName("Should update todo correctly")
    void shouldUpdateTodoCorrectly() {
        // Given
        user.addTodo(todo1);
        String newTitle = "Updated Title";
        String newDescription = "Updated Description";
        
        // When
        todo1.update(newTitle, newDescription);
        
        // Then
        assertThat(todo1.getTitle()).isEqualTo(newTitle);
        assertThat(todo1.getDescription()).isEqualTo(newDescription);
        assertThat(user.getTodos()).contains(todo1);
    }

    @Test
    @DisplayName("Should remove todo from user correctly")
    void shouldRemoveTodoFromUserCorrectly() {
        // Given
        user.addTodo(todo1);
        user.addTodo(todo2);
        assertThat(user.getTodos()).hasSize(2);
        
        // When
        user.removeTodo(todo1);
        
        // Then
        assertThat(user.getTodos()).hasSize(1);
        assertThat(todo1.getUser()).isNull();
        assertThat(todo2.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("Should handle bidirectional relationship correctly")
    void shouldHandleBidirectionalRelationshipCorrectly() {
        // When
        user.addTodo(todo1);
        
        // Then
        assertThat(user.getTodos()).contains(todo1);
        assertThat(todo1.getUser()).isEqualTo(user);
        
        // When changing user
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password123");
        newUser.setEmail("new@example.com");
        todo1.setUser(newUser);
        
        // Then
        assertThat(todo1.getUser()).isEqualTo(newUser);
        // Note: The todo is still in the original user's set because we didn't remove it
        assertThat(user.getTodos()).contains(todo1);
    }

    @Test
    @DisplayName("Should handle complex todo operations")
    void shouldHandleComplexTodoOperations() {
        // Given
        user.addTodo(todo1);
        user.addTodo(todo2);
        
        // When - complex operations
        todo1.toggleDone(); // todo1 becomes done
        todo2.toggleDone(); // todo2 becomes not done
        todo1.update("Updated First Todo", "Updated First Description");
        todo2.update("Updated Second Todo", null);
        
        // Then
        assertThat(todo1.isDone()).isTrue();
        assertThat(todo2.isDone()).isFalse();
        assertThat(todo1.getTitle()).isEqualTo("Updated First Todo");
        assertThat(todo2.getTitle()).isEqualTo("Updated Second Todo");
        assertThat(todo2.getDescription()).isNull();
        
        assertThat(user.getDoneTodos()).hasSize(1);
        assertThat(user.getDoneTodos()).contains(todo1);
        assertThat(user.getNotDoneTodos()).hasSize(1);
        assertThat(user.getNotDoneTodos()).contains(todo2);
    }

    @Test
    @DisplayName("Should handle user with no todos")
    void shouldHandleUserWithNoTodos() {
        // Then
        assertThat(user.getTodos()).isEmpty();
        assertThat(user.getDoneTodos()).isEmpty();
        assertThat(user.getNotDoneTodos()).isEmpty();
    }

    @Test
    @DisplayName("Should handle todo with null description")
    void shouldHandleTodoWithNullDescription() {
        // Given
        todo1.setDescription(null);
        user.addTodo(todo1);
        
        // When
        todo1.update("New Title", "New Description");
        
        // Then
        assertThat(todo1.getDescription()).isEqualTo("New Description");
        assertThat(user.getTodos()).contains(todo1);
    }
} 