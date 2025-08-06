package io.github.Wanyui.springtodoapp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import io.github.Wanyui.springtodoapp.entity.User;

/**
 * Test class for UserRepository.
 * 
 * @author Wanyui/Jano
 * @version 1.0
 * @since 2025-08-06
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;

    @BeforeEach
	@SuppressWarnings("unused")
    void setUp() {
        // Clear the database before each test
        userRepository.deleteAll();
        
        // Create a test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
    }

    @Test
    @DisplayName("Should save user successfully")
    void shouldSaveUserSuccessfully() {
        // When
        User savedUser = userRepository.save(testUser);
        entityManager.flush();
        
        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        // Note: Password hashing is tested separately in BCryptPasswordConverterTest
    }

    @Test
    @DisplayName("Should find user by ID")
    void shouldFindUserById() {
        // Given
        User savedUser = userRepository.save(testUser);
        entityManager.flush();
        
        // When
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        
        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should return empty when user not found by ID")
    void shouldReturnEmptyWhenUserNotFoundById() {
        // When
        Optional<User> foundUser = userRepository.findById(999L);
        
        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should find all users")
    void shouldFindAllUsers() {
        // Given
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("password123");
        user1.setEmail("user1@example.com");
        
        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password456");
        user2.setEmail("user2@example.com");
        
        userRepository.save(user1);
        userRepository.save(user2);
        entityManager.flush();
        
        // When
        List<User> allUsers = userRepository.findAll();
        
        // Then
        assertThat(allUsers).hasSize(2);
        assertThat(allUsers).extracting("username").containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
        // Given
        User savedUser = userRepository.save(testUser);
        entityManager.flush();
        assertThat(userRepository.findById(savedUser.getId())).isPresent();
        
        // When
        userRepository.delete(savedUser);
        
        // Then
        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
        // Given
        User savedUser = userRepository.save(testUser);
        entityManager.flush();
        
        // When
        savedUser.setUsername("updateduser");
        savedUser.setEmail("updated@example.com");
        User updatedUser = userRepository.save(savedUser);
        entityManager.flush();
        
        // Then
        assertThat(updatedUser.getUsername()).isEqualTo("updateduser");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.getId()).isEqualTo(savedUser.getId());
    }

    @Test
    @DisplayName("Should count users correctly")
    void shouldCountUsersCorrectly() {
        // Given
        assertThat(userRepository.count()).isEqualTo(0);
        
        userRepository.save(testUser);
        entityManager.flush();
        
        // When & Then
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should count active users correctly")
    void shouldCountActiveUsersCorrectly() {
        // Given
        User activeUser1 = new User();
        activeUser1.setUsername("active1");
        activeUser1.setPassword("password123");
        activeUser1.setEmail("active1@example.com");
        
        User activeUser2 = new User();
        activeUser2.setUsername("active2");
        activeUser2.setPassword("password123");
        activeUser2.setEmail("active2@example.com");
        
        // Note: We can't create a user with empty email due to validation constraints
        // The countActiveUsers query will filter out users with empty/null fields
        
        userRepository.save(activeUser1);
        userRepository.save(activeUser2);
        entityManager.flush();
        
        // When
        long activeCount = userRepository.countActiveUsers();
        
        // Then
        assertThat(activeCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Should test new repository methods work correctly")
    void shouldTestNewRepositoryMethodsWorkCorrectly() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setEmail("test@example.com");

        userRepository.save(user);
        entityManager.flush();

        // When - Test that the new methods work
        long activeCount = userRepository.countActiveUsers();
        long totalCount = userRepository.count();
        
        // Then
        assertThat(activeCount).isEqualTo(1);
        assertThat(totalCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Should count users created after specific date")
    void shouldCountUsersCreatedAfter() {
        // Given
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("password123");
        user1.setEmail("user1@example.com");
        
        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password123");
        user2.setEmail("user2@example.com");
        
        userRepository.save(user1);
        userRepository.save(user2);
        entityManager.flush();
        
        // When - Count users created after a date before the test
        long count = userRepository.countByCreatedAfter(java.time.LocalDateTime.now().minusDays(1));
        
        // Then
        assertThat(count).isEqualTo(2);
    }
} 