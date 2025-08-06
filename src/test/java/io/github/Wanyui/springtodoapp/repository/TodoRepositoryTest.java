package io.github.Wanyui.springtodoapp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import io.github.Wanyui.springtodoapp.entity.Todo;
import io.github.Wanyui.springtodoapp.entity.User;

/**
 * Test class for TodoRepository.
 * 
 * @author Wanyui/Jano
 * @version 1.0
 * @since 2025-08-06
 */
@DataJpaTest
@ActiveProfiles("test")
class TodoRepositoryTest {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Todo testTodo;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        // Clear the database before each test
        todoRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create a test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        testUser = userRepository.save(testUser);
        
        // Create a test todo
        testTodo = new Todo();
        testTodo.setTitle("Test Todo");
        testTodo.setDescription("Test Description");
        testTodo.setDone(false);
        testTodo.setUser(testUser);
    }

    @Test
    @DisplayName("Should save todo successfully")
    void shouldSaveTodoSuccessfully() {
        // When
        Todo savedTodo = todoRepository.save(testTodo);
        
        // Then
        assertThat(savedTodo).isNotNull();
        assertThat(savedTodo.getId()).isNotNull();
        assertThat(savedTodo.getTitle()).isEqualTo("Test Todo");
        assertThat(savedTodo.getDescription()).isEqualTo("Test Description");
        assertThat(savedTodo.isDone()).isFalse();
        assertThat(savedTodo.getUser()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("Should find todo by ID")
    void shouldFindTodoById() {
        // Given
        Todo savedTodo = todoRepository.save(testTodo);
        
        // When
        Optional<Todo> foundTodo = todoRepository.findById(savedTodo.getId());
        
        // Then
        assertThat(foundTodo).isPresent();
        assertThat(foundTodo.get().getTitle()).isEqualTo("Test Todo");
        assertThat(foundTodo.get().getUser()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("Should return empty when todo not found by ID")
    void shouldReturnEmptyWhenTodoNotFoundById() {
        // When
        Optional<Todo> foundTodo = todoRepository.findById(999L);
        
        // Then
        assertThat(foundTodo).isEmpty();
    }

    @Test
    @DisplayName("Should find todos by user")
    void shouldFindTodosByUser() {
        // Given
        todoRepository.save(testTodo);
        
        Todo todo2 = new Todo();
        todo2.setTitle("Second Todo");
        todo2.setDescription("Second Description");
        todo2.setDone(true);
        todo2.setUser(testUser);
        todoRepository.save(todo2);
        
        // When
        List<Todo> userTodos = todoRepository.findByUser(testUser);
        
        // Then
        assertThat(userTodos).hasSize(2);
        assertThat(userTodos).extracting("title").containsExactlyInAnyOrder("Test Todo", "Second Todo");
    }

    @Test
    @DisplayName("Should return empty list when user has no todos")
    void shouldReturnEmptyListWhenUserHasNoTodos() {
        // When
        List<Todo> userTodos = todoRepository.findByUser(testUser);
        
        // Then
        assertThat(userTodos).isEmpty();
    }

    @Test
    @DisplayName("Should find todos by user and done status")
    void shouldFindTodosByUserAndDoneStatus() {
        // Given
        Todo pendingTodo = new Todo();
        pendingTodo.setTitle("Pending Todo");
        pendingTodo.setDescription("Pending Description");
        pendingTodo.setDone(false);
        pendingTodo.setUser(testUser);
        todoRepository.save(pendingTodo);
        
        Todo completedTodo = new Todo();
        completedTodo.setTitle("Completed Todo");
        completedTodo.setDescription("Completed Description");
        completedTodo.setDone(true);
        completedTodo.setUser(testUser);
        todoRepository.save(completedTodo);
        
        // When
        List<Todo> pendingTodos = todoRepository.findByUserAndDone(testUser, false);
        List<Todo> completedTodos = todoRepository.findByUserAndDone(testUser, true);
        
        // Then
        assertThat(pendingTodos).hasSize(1);
        assertThat(pendingTodos.get(0).getTitle()).isEqualTo("Pending Todo");
        assertThat(pendingTodos.get(0).isDone()).isFalse();
        
        assertThat(completedTodos).hasSize(1);
        assertThat(completedTodos.get(0).getTitle()).isEqualTo("Completed Todo");
        assertThat(completedTodos.get(0).isDone()).isTrue();
    }

    @Test
    @DisplayName("Should find all todos")
    void shouldFindAllTodos() {
        // Given
        Todo todo1 = new Todo();
        todo1.setTitle("Todo 1");
        todo1.setDescription("Description 1");
        todo1.setDone(false);
        todo1.setUser(testUser);
        
        Todo todo2 = new Todo();
        todo2.setTitle("Todo 2");
        todo2.setDescription("Description 2");
        todo2.setDone(true);
        todo2.setUser(testUser);
        
        todoRepository.save(todo1);
        todoRepository.save(todo2);
        
        // When
        List<Todo> allTodos = todoRepository.findAll();
        
        // Then
        assertThat(allTodos).hasSize(2);
        assertThat(allTodos).extracting("title").containsExactlyInAnyOrder("Todo 1", "Todo 2");
    }

    @Test
    @DisplayName("Should delete todo successfully")
    void shouldDeleteTodoSuccessfully() {
        // Given
        Todo savedTodo = todoRepository.save(testTodo);
        assertThat(todoRepository.findById(savedTodo.getId())).isPresent();
        
        // When
        todoRepository.delete(savedTodo);
        
        // Then
        assertThat(todoRepository.findById(savedTodo.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should update todo successfully")
    void shouldUpdateTodoSuccessfully() {
        // Given
        Todo savedTodo = todoRepository.save(testTodo);
        
        // When
        savedTodo.setTitle("Updated Todo");
        savedTodo.setDescription("Updated Description");
        savedTodo.setDone(true);
        Todo updatedTodo = todoRepository.save(savedTodo);
        
        // Then
        assertThat(updatedTodo.getTitle()).isEqualTo("Updated Todo");
        assertThat(updatedTodo.getDescription()).isEqualTo("Updated Description");
        assertThat(updatedTodo.isDone()).isTrue();
        assertThat(updatedTodo.getId()).isEqualTo(savedTodo.getId());
    }

    @Test
    @DisplayName("Should handle todo with null description")
    void shouldHandleTodoWithNullDescription() {
        // Given
        testTodo.setDescription(null);
        
        // When
        Todo savedTodo = todoRepository.save(testTodo);
        
        // Then
        assertThat(savedTodo.getDescription()).isNull();
        assertThat(savedTodo.getTitle()).isEqualTo("Test Todo");
    }

    @Test
    @DisplayName("Should handle todo with empty description")
    void shouldHandleTodoWithEmptyDescription() {
        // Given
        testTodo.setDescription("");
        
        // When
        Todo savedTodo = todoRepository.save(testTodo);
        
        // Then
        assertThat(savedTodo.getDescription()).isEqualTo("");
    }

    @Test
    @DisplayName("Should count todos correctly")
    void shouldCountTodosCorrectly() {
        // Given
        assertThat(todoRepository.count()).isEqualTo(0);
        
        todoRepository.save(testTodo);
        
        // When & Then
        assertThat(todoRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle multiple users with todos")
    void shouldHandleMultipleUsersWithTodos() {
        // Given
        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password456");
        user2.setEmail("user2@example.com");
        user2 = userRepository.save(user2);
        
        Todo todo1 = new Todo();
        todo1.setTitle("User 1 Todo");
        todo1.setDescription("User 1 Description");
        todo1.setDone(false);
        todo1.setUser(testUser);
        todoRepository.save(todo1);
        
        Todo todo2 = new Todo();
        todo2.setTitle("User 2 Todo");
        todo2.setDescription("User 2 Description");
        todo2.setDone(true);
        todo2.setUser(user2);
        todoRepository.save(todo2);
        
        // When
        List<Todo> user1Todos = todoRepository.findByUser(testUser);
        List<Todo> user2Todos = todoRepository.findByUser(user2);
        
        // Then
        assertThat(user1Todos).hasSize(1);
        assertThat(user1Todos.get(0).getTitle()).isEqualTo("User 1 Todo");
        assertThat(user1Todos.get(0).getUser()).isEqualTo(testUser);
        
        assertThat(user2Todos).hasSize(1);
        assertThat(user2Todos.get(0).getTitle()).isEqualTo("User 2 Todo");
        assertThat(user2Todos.get(0).getUser()).isEqualTo(user2);
    }

    @Test
    @DisplayName("Should handle todo status changes")
    void shouldHandleTodoStatusChanges() {
        // Given
        Todo savedTodo = todoRepository.save(testTodo);
        assertThat(savedTodo.isDone()).isFalse();
        
        // When
        savedTodo.setDone(true);
        Todo updatedTodo = todoRepository.save(savedTodo);
        
        // Then
        assertThat(updatedTodo.isDone()).isTrue();
        
        // When finding by status
        List<Todo> doneTodos = todoRepository.findByUserAndDone(testUser, true);
        List<Todo> pendingTodos = todoRepository.findByUserAndDone(testUser, false);
        
        // Then
        assertThat(doneTodos).hasSize(1);
        assertThat(pendingTodos).isEmpty();
    }
} 