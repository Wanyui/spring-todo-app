package io.github.Wanyui.springtodoapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.Wanyui.springtodoapp.entity.Todo;
import io.github.Wanyui.springtodoapp.entity.User;

/**
 * Unit tests for TodoDto class.
 * 
 * Tests the conversion methods between Todo entity and TodoDto,
 * as well as the basic functionality of the DTO.
 * 
 * @author Wanyui/Jano
 * @version 1.0
 * @since 2025-08-06
 */
class TodoDtoTest {

    private Todo testTodo;
    private TodoDto testTodoDto;
    private User testUser;
    private LocalDateTime testDateTime;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        testDateTime = LocalDateTime.now();
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        
        testTodo = new Todo();
        testTodo.setId(1L);
        testTodo.setTitle("Test Todo");
        testTodo.setDescription("Test Description");
        testTodo.setDone(false);
        testTodo.setUser(testUser);
        testTodo.setCreatedAt(testDateTime);
        testTodo.setUpdatedAt(testDateTime);
        
        testTodoDto = new TodoDto();
        testTodoDto.setId(1L);
        testTodoDto.setTitle("Test Todo");
        testTodoDto.setDescription("Test Description");
        testTodoDto.setDone(false);
        testTodoDto.setUserId(1L);
        testTodoDto.setCreatedAt(testDateTime);
        testTodoDto.setUpdatedAt(testDateTime);
    }

    @Test
    @DisplayName("Should create TodoDto from Todo entity")
    void shouldCreateTodoDtoFromTodoEntity() {
        TodoDto result = TodoDto.fromEntity(testTodo);
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Todo");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.isDone()).isFalse();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getCreatedAt()).isEqualTo(testDateTime);
        assertThat(result.getUpdatedAt()).isEqualTo(testDateTime);
    }

    @Test
    @DisplayName("Should return null when creating TodoDto from null entity")
    void shouldReturnNullWhenCreatingTodoDtoFromNullEntity() {
        TodoDto result = TodoDto.fromEntity(null);
        
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should create Todo entity from TodoDto")
    void shouldCreateTodoEntityFromTodoDto() {
        Todo result = testTodoDto.toEntity();
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Todo");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.isDone()).isFalse();
        // User should be null as it's not set in toEntity method
        assertThat(result.getUser()).isNull();
    }

    @Test
    @DisplayName("Should handle Todo entity with null user")
    void shouldHandleTodoEntityWithNullUser() {
        testTodo.setUser(null);
        
        TodoDto result = TodoDto.fromEntity(testTodo);
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Todo");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.isDone()).isFalse();
        assertThat(result.getUserId()).isNull();
    }

    @Test
    @DisplayName("Should handle Todo entity with null timestamps")
    void shouldHandleTodoEntityWithNullTimestamps() {
        testTodo.setCreatedAt(null);
        testTodo.setUpdatedAt(null);
        
        TodoDto result = TodoDto.fromEntity(testTodo);
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Todo");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.isDone()).isFalse();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should handle TodoDto with null values")
    void shouldHandleTodoDtoWithNullValues() {
        testTodoDto.setId(null);
        testTodoDto.setTitle(null);
        testTodoDto.setDescription(null);
        testTodoDto.setUserId(null);
        testTodoDto.setCreatedAt(null);
        testTodoDto.setUpdatedAt(null);
        
        Todo result = testTodoDto.toEntity();
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getTitle()).isNull();
        assertThat(result.getDescription()).isNull();
    }

    @Test
    @DisplayName("Should create TodoDto with all constructor parameters")
    void shouldCreateTodoDtoWithAllConstructorParameters() {
        TodoDto dto = new TodoDto(2L, "New Todo", "New Description", true, 1L, testDateTime, testDateTime);
        
        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getTitle()).isEqualTo("New Todo");
        assertThat(dto.getDescription()).isEqualTo("New Description");
        assertThat(dto.isDone()).isTrue();
        assertThat(dto.getUserId()).isEqualTo(1L);
        assertThat(dto.getCreatedAt()).isEqualTo(testDateTime);
        assertThat(dto.getUpdatedAt()).isEqualTo(testDateTime);
    }

    @Test
    @DisplayName("Should create empty TodoDto with no-args constructor")
    void shouldCreateEmptyTodoDtoWithNoArgsConstructor() {
        TodoDto dto = new TodoDto();
        
        assertThat(dto.getId()).isNull();
        assertThat(dto.getTitle()).isNull();
        assertThat(dto.getDescription()).isNull();
        assertThat(dto.isDone()).isFalse(); // boolean defaults to false
        assertThat(dto.getUserId()).isNull();
        assertThat(dto.getCreatedAt()).isNull();
        assertThat(dto.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should set and get TodoDto properties correctly")
    void shouldSetAndGetTodoDtoPropertiesCorrectly() {
        TodoDto dto = new TodoDto();
        
        dto.setId(3L);
        dto.setTitle("Set Todo");
        dto.setDescription("Set Description");
        dto.setDone(true);
        dto.setUserId(2L);
        dto.setCreatedAt(testDateTime);
        dto.setUpdatedAt(testDateTime);
        
        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getTitle()).isEqualTo("Set Todo");
        assertThat(dto.getDescription()).isEqualTo("Set Description");
        assertThat(dto.isDone()).isTrue();
        assertThat(dto.getUserId()).isEqualTo(2L);
        assertThat(dto.getCreatedAt()).isEqualTo(testDateTime);
        assertThat(dto.getUpdatedAt()).isEqualTo(testDateTime);
    }

    @Test
    @DisplayName("Should have correct equals and hashCode implementation")
    void shouldHaveCorrectEqualsAndHashCodeImplementation() {
        TodoDto dto1 = new TodoDto(1L, "Todo1", "Desc1", false, 1L, testDateTime, testDateTime);
        TodoDto dto2 = new TodoDto(1L, "Todo1", "Desc1", false, 1L, testDateTime, testDateTime);
        TodoDto dto3 = new TodoDto(2L, "Todo2", "Desc2", true, 2L, testDateTime, testDateTime);
        
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(dto3);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.hashCode()).isNotEqualTo(dto3.hashCode());
    }

    @Test
    @DisplayName("Should handle done status correctly")
    void shouldHandleDoneStatusCorrectly() {
        TodoDto dto = new TodoDto();
        
        assertThat(dto.isDone()).isFalse(); // Default value
        
        dto.setDone(true);
        assertThat(dto.isDone()).isTrue();
        
        dto.setDone(false);
        assertThat(dto.isDone()).isFalse();
    }
}