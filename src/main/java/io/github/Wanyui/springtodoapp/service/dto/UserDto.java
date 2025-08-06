package io.github.Wanyui.springtodoapp.service.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import io.github.Wanyui.springtodoapp.entity.User;

/**
 * Data Transfer Object for User entity.
 * 
 * This DTO is used to transfer user data between layers without exposing
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
public class UserDto {

    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Creates a UserDto from a User entity.
     * 
     * @param user the User entity to convert
     * @return the corresponding UserDto
     */
    public static UserDto fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
    
    /**
     * Converts a UserDto back to a User entity.
     * 
     * @return the corresponding User entity
     */
    public User toEntity() {
        User user = new User();
        user.setId(this.id);
        user.setUsername(this.username);
        user.setEmail(this.email);
        return user;
    }
}
