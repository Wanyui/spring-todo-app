package io.github.Wanyui.springtodoapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import io.github.Wanyui.springtodoapp.entity.Todo;
import io.github.Wanyui.springtodoapp.entity.User;
import io.github.Wanyui.springtodoapp.repository.TodoRepository;
import io.github.Wanyui.springtodoapp.repository.UserRepository;
import io.github.Wanyui.springtodoapp.service.dto.TodoDto;
import io.github.Wanyui.springtodoapp.service.exception.ServiceException;
import jakarta.persistence.EntityNotFoundException;

/**
 * Unit tests for TodoService class.
 * 
 * Tests all business logic methods including:
 * - Todo creation and management
 * - User-specific todo operations
 * - Data validation
 * - Error handling
 * - Statistics and counting
 * 
 * @author Wanyui/Jano
 * @version 1.0
 * @since 2025-08-06
 */
@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TodoService todoService;

    private User testUser;
    private Todo testTodo;
    private TodoDto testTodoDto;

    @BeforeEach
	@SuppressWarnings("unused")
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        testTodo = new Todo();
        testTodo.setId(1L);
        testTodo.setTitle("Test Todo");
        testTodo.setDescription("Test Description");
        testTodo.setDone(false);
        testTodo.setUser(testUser);
        testTodo.setCreatedAt(LocalDateTime.now());
        testTodo.setUpdatedAt(LocalDateTime.now());

        testTodoDto = TodoDto.fromEntity(testTodo);
    }

    @Test
    @DisplayName("Should create todo successfully")
    void shouldCreateTodoSuccessfully() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);

        // When
        TodoDto result = todoService.createTodo(testTodoDto, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Todo");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.isDone()).isFalse();
        verify(userRepository).findById(1L);
        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    @DisplayName("Should throw exception when creating todo with null data")
    void shouldThrowExceptionWhenCreatingTodoWithNullData() {
        // When & Then
        assertThatThrownBy(() -> todoService.createTodo(null, 1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Todo data cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when creating todo with null user ID")
    void shouldThrowExceptionWhenCreatingTodoWithNullUserId() {
        // When & Then
        assertThatThrownBy(() -> todoService.createTodo(testTodoDto, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User ID cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when creating todo with invalid user ID")
    void shouldThrowExceptionWhenCreatingTodoWithInvalidUserId() {
        // When & Then
        assertThatThrownBy(() -> todoService.createTodo(testTodoDto, -1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User ID must be positive");
    }

    @Test
    @DisplayName("Should throw exception when creating todo with empty title")
    void shouldThrowExceptionWhenCreatingTodoWithEmptyTitle() {
        // Given
        testTodoDto.setTitle("");

        // When & Then
        assertThatThrownBy(() -> todoService.createTodo(testTodoDto, 1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Todo title cannot be empty");
    }

    @Test
    @DisplayName("Should throw exception when creating todo with non-existent user")
    void shouldThrowExceptionWhenCreatingTodoWithNonExistentUser() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> todoService.createTodo(testTodoDto, 999L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("User not found with id");
    }

    @Test
    @DisplayName("Should get todo by ID successfully")
    void shouldGetTodoByIdSuccessfully() {
        // Given
        when(todoRepository.findById(anyLong())).thenReturn(Optional.of(testTodo));

        // When
        TodoDto result = todoService.getTodoById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Todo");
        verify(todoRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when todo not found by ID")
    void shouldThrowExceptionWhenTodoNotFoundById() {
        // Given
        when(todoRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> todoService.getTodoById(999L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Todo not found with id");
    }

    @Test
    @DisplayName("Should get todos by user ID successfully")
    void shouldGetTodosByUserIdSuccessfully() {
        // Given
        Todo todo1 = new Todo();
        todo1.setId(1L);
        todo1.setTitle("Todo 1");
        todo1.setUser(testUser);

        Todo todo2 = new Todo();
        todo2.setId(2L);
        todo2.setTitle("Todo 2");
        todo2.setUser(testUser);

        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(todoRepository.findByUser(any(User.class))).thenReturn(Arrays.asList(todo1, todo2));

        // When
        List<TodoDto> result = todoService.getTodosByUserId(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Todo 1");
        assertThat(result.get(1).getTitle()).isEqualTo("Todo 2");
        verify(userRepository).existsById(1L);
        verify(todoRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("Should throw exception when getting todos for non-existent user")
    void shouldThrowExceptionWhenGettingTodosForNonExistentUser() {
        // Given
        when(userRepository.existsById(anyLong())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> todoService.getTodosByUserId(999L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("User not found with id");
    }

    @Test
    @DisplayName("Should get todos by user ID and done status successfully")
    void shouldGetTodosByUserIdAndDoneStatusSuccessfully() {
        // Given
        Todo doneTodo = new Todo();
        doneTodo.setId(1L);
        doneTodo.setTitle("Done Todo");
        doneTodo.setDone(true);
        doneTodo.setUser(testUser);

        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(todoRepository.findByUserAndDone(any(User.class), any(Boolean.class)))
            .thenReturn(Arrays.asList(doneTodo));

        // When
        List<TodoDto> result = todoService.getTodosByUserIdAndDone(1L, true);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Done Todo");
        assertThat(result.get(0).isDone()).isTrue();
        verify(todoRepository).findByUserAndDone(testUser, true);
    }

    @Test
    @DisplayName("Should get all todos successfully")
    void shouldGetAllTodosSuccessfully() {
        // Given
        Todo todo1 = new Todo();
        todo1.setId(1L);
        todo1.setTitle("Todo 1");

        Todo todo2 = new Todo();
        todo2.setId(2L);
        todo2.setTitle("Todo 2");

        when(todoRepository.findAll()).thenReturn(Arrays.asList(todo1, todo2));

        // When
        List<TodoDto> result = todoService.getAllTodos();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Todo 1");
        assertThat(result.get(1).getTitle()).isEqualTo("Todo 2");
        verify(todoRepository).findAll();
    }

    @Test
    @DisplayName("Should update todo successfully")
    void shouldUpdateTodoSuccessfully() {
        // Given
        when(todoRepository.findById(anyLong())).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);

        TodoDto updateDto = new TodoDto();
        updateDto.setTitle("Updated Todo");
        updateDto.setDescription("Updated Description");
        updateDto.setDone(true);

        // When
        TodoDto result = todoService.updateTodo(1L, updateDto);

        // Then
        assertThat(result).isNotNull();
        verify(todoRepository).findById(1L);
        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent todo")
    void shouldThrowExceptionWhenUpdatingNonExistentTodo() {
        // Given
        when(todoRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> todoService.updateTodo(999L, testTodoDto))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Todo not found with id");
    }

    @Test
    @DisplayName("Should toggle todo done status successfully")
    void shouldToggleTodoDoneStatusSuccessfully() {
        // Given
        when(todoRepository.findById(anyLong())).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);

        // When
        TodoDto result = todoService.toggleTodoDone(1L);

        // Then
        assertThat(result).isNotNull();
        verify(todoRepository).findById(1L);
        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    @DisplayName("Should throw exception when toggling non-existent todo")
    void shouldThrowExceptionWhenTogglingNonExistentTodo() {
        // Given
        when(todoRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> todoService.toggleTodoDone(999L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Todo not found with id");
    }

    @Test
    @DisplayName("Should delete todo successfully")
    void shouldDeleteTodoSuccessfully() {
        // Given
        when(todoRepository.existsById(anyLong())).thenReturn(true);
        when(todoRepository.findById(anyLong())).thenReturn(Optional.of(testTodo));

        // When
        todoService.deleteTodo(1L);

        // Then
        verify(todoRepository).existsById(1L);
        verify(todoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent todo")
    void shouldThrowExceptionWhenDeletingNonExistentTodo() {
        // Given
        when(todoRepository.existsById(anyLong())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> todoService.deleteTodo(999L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Todo not found with id");
    }

    @Test
    @DisplayName("Should delete todos by user ID successfully")
    void shouldDeleteTodosByUserIdSuccessfully() {
        // Given
        Todo todo1 = new Todo();
        todo1.setId(1L);
        todo1.setTitle("Todo 1");
        todo1.setUser(testUser);

        Todo todo2 = new Todo();
        todo2.setId(2L);
        todo2.setTitle("Todo 2");
        todo2.setUser(testUser);

        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(todoRepository.findByUser(any(User.class))).thenReturn(Arrays.asList(todo1, todo2));

        // When
        todoService.deleteTodosByUserId(1L);

        // Then
        verify(userRepository).existsById(1L);
        verify(todoRepository).findByUser(testUser);
        verify(todoRepository).deleteAll(Arrays.asList(todo1, todo2));
    }

    @Test
    @DisplayName("Should throw exception when deleting todos for non-existent user")
    void shouldThrowExceptionWhenDeletingTodosForNonExistentUser() {
        // Given
        when(userRepository.existsById(anyLong())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> todoService.deleteTodosByUserId(999L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("User not found with id");
    }

    @Test
    @DisplayName("Should count todos successfully")
    void shouldCountTodosSuccessfully() {
        // Given
        when(todoRepository.count()).thenReturn(10L);

        // When
        long result = todoService.countTodos();

        // Then
        assertThat(result).isEqualTo(10L);
        verify(todoRepository).count();
    }

    @Test
    @DisplayName("Should count todos by user ID successfully")
    void shouldCountTodosByUserIdSuccessfully() {
        // Given
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(todoRepository.countByUser(any(User.class))).thenReturn(5L);

        // When
        long result = todoService.countTodosByUserId(1L);

        // Then
        assertThat(result).isEqualTo(5L);
        verify(todoRepository).countByUser(testUser);
    }

    @Test
    @DisplayName("Should throw exception when counting todos for non-existent user")
    void shouldThrowExceptionWhenCountingTodosForNonExistentUser() {
        // Given
        when(userRepository.existsById(anyLong())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> todoService.countTodosByUserId(999L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("User not found with id");
    }

    @Test
    @DisplayName("Should count done todos by user ID successfully")
    void shouldCountDoneTodosByUserIdSuccessfully() {
        // Given
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(todoRepository.countByUserAndDone(any(User.class), any(Boolean.class))).thenReturn(3L);

        // When
        long result = todoService.countDoneTodosByUserId(1L);

        // Then
        assertThat(result).isEqualTo(3L);
        verify(todoRepository).countByUserAndDone(testUser, true);
    }

    @Test
    @DisplayName("Should get overall todo statistics successfully")
    void shouldGetOverallTodoStatisticsSuccessfully() {
        // Given
        when(todoRepository.count()).thenReturn(10L);
        when(todoRepository.countByDone(any(Boolean.class))).thenReturn(6L);

        // When
        Map<String, Object> result = todoService.getOverallTodoStatistics();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("totalTodos")).isEqualTo(10L);
        assertThat(result.get("doneTodos")).isEqualTo(6L);
        assertThat(result.get("pendingTodos")).isEqualTo(4L);
        verify(todoRepository).count();
        verify(todoRepository).countByDone(true);
    }

    @Test
    @DisplayName("Should handle database exception when creating todo")
    void shouldHandleDatabaseExceptionWhenCreatingTodo() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(todoRepository.save(any(Todo.class))).thenThrow(new DataAccessException("Database error") {});

        // When & Then
        assertThatThrownBy(() -> todoService.createTodo(testTodoDto, 1L))
            .isInstanceOf(ServiceException.class)
            .hasMessageContaining("Error creating todo");
    }

    @Test
    @DisplayName("Should handle database exception when getting todo by ID")
    void shouldHandleDatabaseExceptionWhenGettingTodoById() {
        // Given
        when(todoRepository.findById(anyLong())).thenThrow(new DataAccessException("Database error") {});

        // When & Then
        assertThatThrownBy(() -> todoService.getTodoById(1L))
            .isInstanceOf(ServiceException.class)
            .hasMessageContaining("Error accessing todo data");
    }

    @Test
    @DisplayName("Should validate todo ID is positive")
    void shouldValidateTodoIdIsPositive() {
        // When & Then
        assertThatThrownBy(() -> todoService.getTodoById(-1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Todo ID must be positive");
    }

    @Test
    @DisplayName("Should validate todo ID is not null")
    void shouldValidateTodoIdIsNotNull() {
        // When & Then
        assertThatThrownBy(() -> todoService.getTodoById(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Todo ID cannot be null");
    }

    @Test
    @DisplayName("Should validate user ID is positive for todo operations")
    void shouldValidateUserIdIsPositiveForTodoOperations() {
        // When & Then
        assertThatThrownBy(() -> todoService.createTodo(testTodoDto, -1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User ID must be positive");
    }

    @Test
    @DisplayName("Should validate user ID is not null for todo operations")
    void shouldValidateUserIdIsNotNullForTodoOperations() {
        // When & Then
        assertThatThrownBy(() -> todoService.createTodo(testTodoDto, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User ID cannot be null");
    }

    @Test
    @DisplayName("Should validate todo title is not empty")
    void shouldValidateTodoTitleIsNotEmpty() {
        // Given
        testTodoDto.setTitle("");

        // When & Then
        assertThatThrownBy(() -> todoService.createTodo(testTodoDto, 1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Todo title cannot be empty");
    }

    @Test
    @DisplayName("Should validate todo title length")
    void shouldValidateTodoTitleLength() {
        // Given
        testTodoDto.setTitle("a".repeat(101)); // More than 100 characters

        // When & Then
        assertThatThrownBy(() -> todoService.createTodo(testTodoDto, 1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Todo title length must be between 1 and 100 characters");
    }

    @Test
    @DisplayName("Should validate todo description length")
    void shouldValidateTodoDescriptionLength() {
        // Given
        testTodoDto.setDescription("a".repeat(1001)); // More than 1000 characters

        // When & Then
        assertThatThrownBy(() -> todoService.createTodo(testTodoDto, 1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Todo description cannot exceed 1000 characters");
    }
} 