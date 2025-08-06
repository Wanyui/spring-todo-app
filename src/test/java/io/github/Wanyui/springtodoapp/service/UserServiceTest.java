package io.github.Wanyui.springtodoapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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

import io.github.Wanyui.springtodoapp.entity.User;
import io.github.Wanyui.springtodoapp.repository.UserRepository;
import io.github.Wanyui.springtodoapp.service.dto.UserDto;
import io.github.Wanyui.springtodoapp.service.exception.ServiceException;
import jakarta.persistence.EntityNotFoundException;

/**
 * Unit tests for UserService class.
 * 
 * Tests all business logic methods including:
 * - User registration and management
 * - Data validation
 * - Error handling
 * - Statistics and counting
 * 
 * @author Wanyui/Jano
 * @version 1.0
 * @since 2025-08-06
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDto testUserDto;

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

        testUserDto = UserDto.fromEntity(testUser);
    }

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterUserSuccessfully() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDto result = userService.registerUser(testUserDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when registering user with existing username")
    void shouldThrowExceptionWhenRegisteringUserWithExistingUsername() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(testUserDto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Username already exists");
    }

    @Test
    @DisplayName("Should throw exception when registering user with existing email")
    void shouldThrowExceptionWhenRegisteringUserWithExistingEmail() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(testUserDto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email already exists");
    }

    @Test
    @DisplayName("Should throw exception when registering user with null data")
    void shouldThrowExceptionWhenRegisteringUserWithNullData() {
        // When & Then
        assertThatThrownBy(() -> userService.registerUser(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User data cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when registering user with empty username")
    void shouldThrowExceptionWhenRegisteringUserWithEmptyUsername() {
        // Given
        testUserDto.setUsername("");

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(testUserDto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Username cannot be empty");
    }

    @Test
    @DisplayName("Should throw exception when registering user with invalid email")
    void shouldThrowExceptionWhenRegisteringUserWithInvalidEmail() {
        // Given
        testUserDto.setEmail("invalid-email");

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(testUserDto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid email format");
    }

    @Test
    @DisplayName("Should get user by username successfully")
    void shouldGetUserByUsernameSuccessfully() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        // When
        Optional<UserDto> result = userService.getUserByUsername("testuser");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should return empty when user not found by username")
    void shouldReturnEmptyWhenUserNotFoundByUsername() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // When
        Optional<UserDto> result = userService.getUserByUsername("nonexistent");

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("Should get user by email successfully")
    void shouldGetUserByEmailSuccessfully() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // When
        Optional<UserDto> result = userService.getUserByEmail("test@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    void shouldReturnEmptyWhenUserNotFoundByEmail() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When
        Optional<UserDto> result = userService.getUserByEmail("nonexistent@example.com");

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("Should get all users successfully")
    void shouldGetAllUsersSuccessfully() {
        // Given
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        // When
        List<UserDto> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("user1");
        assertThat(result.get(1).getUsername()).isEqualTo("user2");
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDto updateDto = new UserDto();
        updateDto.setUsername("updateduser");
        updateDto.setEmail("updated@example.com");

        // When
        UserDto result = userService.updateUser(1L, updateDto);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent user")
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(999L, testUserDto))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("User not found with id");
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
        // Given
        when(userRepository.existsById(anyLong())).thenReturn(true);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent user")
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        // Given
        when(userRepository.existsById(anyLong())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(999L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("User not found with id");
    }

    @Test
    @DisplayName("Should check if username exists")
    void shouldCheckIfUsernameExists() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // When
        boolean result = userService.existsByUsername("testuser");

        // Then
        assertThat(result).isTrue();
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    @DisplayName("Should check if email exists")
    void shouldCheckIfEmailExists() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When
        boolean result = userService.existsByEmail("test@example.com");

        // Then
        assertThat(result).isTrue();
        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should count users successfully")
    void shouldCountUsersSuccessfully() {
        // Given
        when(userRepository.count()).thenReturn(5L);

        // When
        long result = userService.countUsers();

        // Then
        assertThat(result).isEqualTo(5L);
        verify(userRepository).count();
    }

    @Test
    @DisplayName("Should count users created after date")
    void shouldCountUsersCreatedAfterDate() {
        // Given
        LocalDateTime date = LocalDateTime.now().minusDays(7);
        when(userRepository.countByCreatedAfter(any(LocalDateTime.class))).thenReturn(3L);

        // When
        long result = userService.countUsersCreatedAfter(date);

        // Then
        assertThat(result).isEqualTo(3L);
        verify(userRepository).countByCreatedAfter(date);
    }

    @Test
    @DisplayName("Should search users successfully")
    void shouldSearchUsersSuccessfully() {
        // Given
        User user1 = new User();
        user1.setUsername("testuser1");
        user1.setEmail("test1@example.com");

        User user2 = new User();
        user2.setUsername("testuser2");
        user2.setEmail("test2@example.com");

        when(userRepository.findByUsernameOrEmailContaining(anyString()))
            .thenReturn(Arrays.asList(user1, user2));

        // When
        List<UserDto> result = userService.searchUsers("test");

        // Then
        assertThat(result).hasSize(2);
        verify(userRepository).findByUsernameOrEmailContaining("test");
    }

    @Test
    @DisplayName("Should check if user is active")
    void shouldCheckIfUserIsActive() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.isUserActive(1L);

        // Then
        assertThat(result).isTrue();
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return false when user is not active")
    void shouldReturnFalseWhenUserIsNotActive() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        boolean result = userService.isUserActive(999L);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("Should get user statistics successfully")
    void shouldGetUserStatisticsSuccessfully() {
        // Given
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countActiveUsers()).thenReturn(8L);

        // When
        Map<String, Object> result = userService.getUserStatistics();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("totalUsers")).isEqualTo(10L);
        assertThat(result.get("activeUsers")).isEqualTo(8L);
        assertThat(result.get("inactiveUsers")).isEqualTo(2L);
        verify(userRepository).count();
        verify(userRepository).countActiveUsers();
    }

    @Test
    @DisplayName("Should handle database exception when registering user")
    void shouldHandleDatabaseExceptionWhenRegisteringUser() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenThrow(new DataAccessException("Database error") {});

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(testUserDto))
            .isInstanceOf(ServiceException.class)
            .hasMessageContaining("Error saving user data");
    }

    @Test
    @DisplayName("Should handle database exception when getting user by username")
    void shouldHandleDatabaseExceptionWhenGettingUserByUsername() {
        // Given
        when(userRepository.findByUsername(anyString())).thenThrow(new DataAccessException("Database error") {});

        // When & Then
        assertThatThrownBy(() -> userService.getUserByUsername("testuser"))
            .isInstanceOf(ServiceException.class)
            .hasMessageContaining("Error searching user by username");
    }

    @Test
    @DisplayName("Should validate user ID is positive")
    void shouldValidateUserIdIsPositive() {
        // When & Then
        assertThatThrownBy(() -> userService.getUserById(-1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User ID must be positive");
    }

    @Test
    @DisplayName("Should validate user ID is not null")
    void shouldValidateUserIdIsNotNull() {
        // When & Then
        assertThatThrownBy(() -> userService.getUserById(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User ID cannot be null");
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void shouldGetUserByIdSuccessfully() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        // When
        UserDto result = userService.getUserById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void shouldThrowExceptionWhenUserNotFoundById() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(999L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("User not found with id");
    }
} 