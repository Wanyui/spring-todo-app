package io.github.Wanyui.springtodoapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.Wanyui.springtodoapp.entity.User;

/**
 * Unit tests for UserDto class.
 * 
 * Tests the conversion methods between User entity and UserDto,
 * as well as the basic functionality of the DTO.
 * 
 * @author Wanyui/Jano
 * @version 1.0
 * @since 2025-08-06
 */
class UserDtoTest {

    private User testUser;
    private UserDto testUserDto;
    private LocalDateTime testDateTime;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        testDateTime = LocalDateTime.now();
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        testUser.setCreatedAt(testDateTime);
        testUser.setUpdatedAt(testDateTime);
        
        testUserDto = new UserDto();
        testUserDto.setId(1L);
        testUserDto.setUsername("testuser");
        testUserDto.setEmail("test@example.com");
        testUserDto.setCreatedAt(testDateTime);
        testUserDto.setUpdatedAt(testDateTime);
    }

    @Test
    @DisplayName("Should create UserDto from User entity")
    void shouldCreateUserDtoFromUserEntity() {
        UserDto result = UserDto.fromEntity(testUser);
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getCreatedAt()).isEqualTo(testDateTime);
        assertThat(result.getUpdatedAt()).isEqualTo(testDateTime);
    }

    @Test
    @DisplayName("Should return null when creating UserDto from null entity")
    void shouldReturnNullWhenCreatingUserDtoFromNullEntity() {
        UserDto result = UserDto.fromEntity(null);
        
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should create User entity from UserDto")
    void shouldCreateUserEntityFromUserDto() {
        User result = testUserDto.toEntity();
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        // Password should be null as it's not included in DTO
        assertThat(result.getPassword()).isNull();
    }

    @Test
    @DisplayName("Should handle User entity with null timestamps")
    void shouldHandleUserEntityWithNullTimestamps() {
        testUser.setCreatedAt(null);
        testUser.setUpdatedAt(null);
        
        UserDto result = UserDto.fromEntity(testUser);
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should handle UserDto with null values")
    void shouldHandleUserDtoWithNullValues() {
        testUserDto.setId(null);
        testUserDto.setUsername(null);
        testUserDto.setEmail(null);
        testUserDto.setCreatedAt(null);
        testUserDto.setUpdatedAt(null);
        
        User result = testUserDto.toEntity();
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getUsername()).isNull();
        assertThat(result.getEmail()).isNull();
    }

    @Test
    @DisplayName("Should create UserDto with all constructor parameters")
    void shouldCreateUserDtoWithAllConstructorParameters() {
        UserDto dto = new UserDto(2L, "newuser", "new@example.com", testDateTime, testDateTime);
        
        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getUsername()).isEqualTo("newuser");
        assertThat(dto.getEmail()).isEqualTo("new@example.com");
        assertThat(dto.getCreatedAt()).isEqualTo(testDateTime);
        assertThat(dto.getUpdatedAt()).isEqualTo(testDateTime);
    }

    @Test
    @DisplayName("Should create empty UserDto with no-args constructor")
    void shouldCreateEmptyUserDtoWithNoArgsConstructor() {
        UserDto dto = new UserDto();
        
        assertThat(dto.getId()).isNull();
        assertThat(dto.getUsername()).isNull();
        assertThat(dto.getEmail()).isNull();
        assertThat(dto.getCreatedAt()).isNull();
        assertThat(dto.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should set and get UserDto properties correctly")
    void shouldSetAndGetUserDtoPropertiesCorrectly() {
        UserDto dto = new UserDto();
        
        dto.setId(3L);
        dto.setUsername("setuser");
        dto.setEmail("set@example.com");
        dto.setCreatedAt(testDateTime);
        dto.setUpdatedAt(testDateTime);
        
        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getUsername()).isEqualTo("setuser");
        assertThat(dto.getEmail()).isEqualTo("set@example.com");
        assertThat(dto.getCreatedAt()).isEqualTo(testDateTime);
        assertThat(dto.getUpdatedAt()).isEqualTo(testDateTime);
    }

    @Test
    @DisplayName("Should have correct equals and hashCode implementation")
    void shouldHaveCorrectEqualsAndHashCodeImplementation() {
        UserDto dto1 = new UserDto(1L, "user1", "user1@example.com", testDateTime, testDateTime);
        UserDto dto2 = new UserDto(1L, "user1", "user1@example.com", testDateTime, testDateTime);
        UserDto dto3 = new UserDto(2L, "user2", "user2@example.com", testDateTime, testDateTime);
        
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(dto3);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.hashCode()).isNotEqualTo(dto3.hashCode());
    }
}
