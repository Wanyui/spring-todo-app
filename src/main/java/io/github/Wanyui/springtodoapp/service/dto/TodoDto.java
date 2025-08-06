package io.github.Wanyui.springtodoapp.service.dto;

import java.time.LocalDateTime;

import io.github.Wanyui.springtodoapp.entity.Todo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Data Transfer Object for Todo entity.
 * 
 * This DTO is used to transfer todo data between layers without exposing
 * the entity directly. It provides a clean interface for API responses
 * and service layer operations.
 * 
 * @author Wanyui/Jano
 * @version 1.0
 * @since 2025-08-06
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString(exclude = "userId")
public class TodoDto {
    
    private Long id;
    private String title;
    private String description;
    private boolean done;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Creates a TodoDto from a Todo entity.
     * 
     * @param todo the Todo entity to convert
     * @return the corresponding TodoDto
     */
    public static TodoDto fromEntity(Todo todo) {
        if (todo == null) {
            return null;
        }
        return new TodoDto(
            todo.getId(),
            todo.getTitle(),
            todo.getDescription(),
            todo.isDone(),
            todo.getUser() != null ? todo.getUser().getId() : null,
            todo.getCreatedAt(),
            todo.getUpdatedAt()
        );
    }
    
    /**
     * Converts a TodoDto back to a Todo entity.
     * 
     * @return the corresponding Todo entity
     */
    public Todo toEntity() {
        Todo todo = new Todo();
        todo.setId(this.id);
        todo.setTitle(this.title);
        todo.setDescription(this.description);
        todo.setDone(this.done);
        return todo;
    }
}
