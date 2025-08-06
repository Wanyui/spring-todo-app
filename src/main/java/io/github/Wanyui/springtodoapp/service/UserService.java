package io.github.Wanyui.springtodoapp.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.github.Wanyui.springtodoapp.entity.User;
import io.github.Wanyui.springtodoapp.repository.UserRepository;
import io.github.Wanyui.springtodoapp.service.dto.UserDto;
import io.github.Wanyui.springtodoapp.service.exception.ServiceException;

/**
 * Service class for User entity providing business logic and operations.
 * 
 * This service handles all user-related business operations including:
 * - User registration and validation
 * - User management operations
 * - Business rule enforcement
 * - Data transfer between layers using DTOs
 * - Proper error handling with try-catch blocks
 * 
 * @author Wanyui/Jano
 * @version 1.0
 * @since 2025-08-06
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {
	
	private final UserRepository userRepository;

	/**
	 * Registers a new user with validation.
	 * 
	 * @param userDto the user data to register
	 * @return the registered user as DTO
	 * @throws IllegalArgumentException if validation fails
	 * @throws ServiceException if database operation fails
	 */
	public UserDto registerUser(UserDto userDto) {
		
		// Validation checks
		if (userDto == null) {
			log.error("User registration failed: data is null");
			throw new IllegalArgumentException("User data cannot be null");
		}

		// Username validation
		if (userDto.getUsername() == null || userDto.getUsername().trim().isEmpty()) {
			log.error("User registration failed: username is empty");
			throw new IllegalArgumentException("Username cannot be empty");
		}
		String username = userDto.getUsername().trim();
		if (username.length() < 3 || username.length() > 50) {
			log.error("User registration failed: username length must be between 3 and 50 characters");
			throw new IllegalArgumentException("Username length must be between 3 and 50 characters");
		}
		if (!username.matches("^[a-zA-Z0-9_]+$")) {
			log.error("User registration failed: username contains invalid characters");
			throw new IllegalArgumentException("Username can only contain letters, numbers, and underscores");
		}
		
		// Email validation
		if (userDto.getEmail() == null || userDto.getEmail().trim().isEmpty()) {
			log.error("User registration failed: email is empty");
			throw new IllegalArgumentException("Email cannot be empty");
		}
		String email = userDto.getEmail().trim().toLowerCase();
		if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
			log.error("User registration failed: invalid email format");
			throw new IllegalArgumentException("Invalid email format");
		}
		if (email.length() > 100) {
			log.error("User registration failed: email too long");
			throw new IllegalArgumentException("Email cannot exceed 100 characters");
		}
		
		log.info("Registering new user: {}", username);

		try {
			// Check uniqueness
			if (userRepository.existsByUsername(userDto.getUsername())) {
				log.warn("User registration failed: username '{}' already exists", userDto.getUsername());
				throw new IllegalArgumentException("Username already exists: " + userDto.getUsername());
			}
			if (userRepository.existsByEmail(userDto.getEmail())) {
				log.warn("User registration failed: email '{}' already exists", userDto.getEmail());
				throw new IllegalArgumentException("Email already exists: " + userDto.getEmail());
			}

			// Convert DTO to entity and save
			User user = userDto.toEntity();
			User savedUser = userRepository.save(user);
			log.info("User '{}' registered successfully with ID: {}", savedUser.getUsername(), savedUser.getId());
			
			return UserDto.fromEntity(savedUser);
			
		} catch (DataAccessException e) {
			log.error("Database error while registering user '{}': {}", userDto.getUsername(), e.getMessage(), e);
			throw new ServiceException("Error saving user data", e);
		}
	}

	/**
	 * Retrieves a user by their ID.
	 * 
	 * @param id the ID of the user to retrieve
	 * @return the user with the given ID
	 * @throws IllegalArgumentException if ID is null
	 * @throws EntityNotFoundException if the user with the given ID is not found
	 * @throws ServiceException if database operation fails
	 */
		@Transactional(readOnly = true)
	public UserDto getUserById(Long id) {
		log.debug("Retrieving user with ID: {}", id);

		if (id == null) {
			log.error("User retrieval failed: ID is null");
			throw new IllegalArgumentException("User ID cannot be null");
		}
		if (id <= 0) {
			log.error("User retrieval failed: invalid ID: {}", id);
			throw new IllegalArgumentException("User ID must be positive");
		}
		
		try {
			User user = userRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
			log.info("User '{}' retrieved successfully", user.getUsername());
			return UserDto.fromEntity(user);
		} catch (DataAccessException e) {
			log.error("Database error while retrieving user with ID {}: {}", id, e.getMessage(), e);
			throw new ServiceException("Error accessing user data", e);
		}
	}

	/**
	 * Finds a user by their username.
	 * 
	 * @param username the username to search for
	 * @return an Optional containing the user if found
	 * @throws IllegalArgumentException if username is null or empty
	 * @throws ServiceException if database operation fails
	 */
	@Transactional(readOnly = true)
	public Optional<UserDto> getUserByUsername(String username) {
		log.debug("Searching for user by username: {}", username);

		if (username == null || username.trim().isEmpty()) {
			log.error("User search failed: username is null or empty");
			throw new IllegalArgumentException("Username cannot be null or empty");
		}
		
		try {
			Optional<User> user = userRepository.findByUsername(username);
			if (user.isPresent()) {
				log.debug("User '{}' found by username", username);
			} else {
				log.debug("User '{}' not found by username", username);
			}
			return user.map(UserDto::fromEntity);
		} catch (DataAccessException e) {
			log.error("Database error while searching user by username '{}': {}", username, e.getMessage(), e);
			throw new ServiceException("Error searching user by username", e);
		}
	}

	/**
	 * Finds a user by their email.
	 * 
	 * @param email the email to search for
	 * @return an Optional containing the user if found
	 * @throws IllegalArgumentException if email is null or empty
	 * @throws ServiceException if database operation fails
	 */
	@Transactional(readOnly = true)
	public Optional<UserDto> getUserByEmail(String email) {
		log.debug("Searching for user by email: {}", email);

		if (email == null || email.trim().isEmpty()) {
			log.error("User search failed: email is null or empty");
			throw new IllegalArgumentException("Email cannot be null or empty");
		}
		
		try {
			Optional<User> user = userRepository.findByEmail(email);
			if (user.isPresent()) {
				log.debug("User '{}' found by email", user.get().getUsername());
			} else {
				log.debug("User not found by email: {}", email);
			}
			return user.map(UserDto::fromEntity);
		} catch (DataAccessException e) {
			log.error("Database error while searching user by email '{}': {}", email, e.getMessage(), e);
			throw new ServiceException("Error searching user by email", e);
		}
	}

	/**
	 * Retrieves all users.
	 * 
	 * @return a list of all users as DTOs
	 * @throws ServiceException if database operation fails
	 */
	@Transactional(readOnly = true)
	public List<UserDto> getAllUsers() {
		log.debug("Retrieving all users");

		try {
			List<User> users = userRepository.findAll();
			log.info("Retrieved {} users successfully", users.size());
			return users.stream()
				.map(UserDto::fromEntity)
				.collect(Collectors.toList());
		} catch (DataAccessException e) {
			log.error("Database error while retrieving all users: {}", e.getMessage(), e);
			throw new ServiceException("Error retrieving all users", e);
		}
	}

	/**
	 * Updates an existing user.
	 * 
	 * @param id the user ID to update
	 * @param userDto the updated user data
	 * @return the updated user as DTO
	 * @throws IllegalArgumentException if validation fails
	 * @throws EntityNotFoundException if user not found
	 * @throws ServiceException if database operation fails
	 */
	public UserDto updateUser(Long id, UserDto userDto) {
		log.info("Updating user with ID: {}", id);

		if (id == null) {
			log.error("User update failed: ID is null");
			throw new IllegalArgumentException("User ID cannot be null");
		}
		if (userDto == null) {
			log.error("User update failed: data is null");
			throw new IllegalArgumentException("User data cannot be null");
		}

		try {
			log.debug("Finding existing user with ID: {}", id);
			User existingUser = userRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
			log.debug("Found existing user: {}", existingUser.getUsername());

			// Validate username if changing
			if (userDto.getUsername() != null && !userDto.getUsername().equals(existingUser.getUsername())) {
				String newUsername = userDto.getUsername().trim();
				
				// Username validation
				if (newUsername.isEmpty()) {
					log.error("User update failed: username cannot be empty");
					throw new IllegalArgumentException("Username cannot be empty");
				}
				if (newUsername.length() < 3 || newUsername.length() > 50) {
					log.error("User update failed: username length must be between 3 and 50 characters");
					throw new IllegalArgumentException("Username length must be between 3 and 50 characters");
				}
				if (!newUsername.matches("^[a-zA-Z0-9_]+$")) {
					log.error("User update failed: username contains invalid characters");
					throw new IllegalArgumentException("Username can only contain letters, numbers, and underscores");
				}
				
				if (userRepository.existsByUsername(newUsername)) {
					log.warn("User update failed: username '{}' already exists", newUsername);
					throw new IllegalArgumentException("Username already exists: " + newUsername);
				}
				log.debug("Updating username from '{}' to '{}'", existingUser.getUsername(), newUsername);
				existingUser.setUsername(newUsername);
			}

			// Validate email if changing
			if (userDto.getEmail() != null && !userDto.getEmail().equals(existingUser.getEmail())) {
				String newEmail = userDto.getEmail().trim().toLowerCase();
				
				// Email validation
				if (newEmail.isEmpty()) {
					log.error("User update failed: email cannot be empty");
					throw new IllegalArgumentException("Email cannot be empty");
				}
				if (!newEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
					log.error("User update failed: invalid email format");
					throw new IllegalArgumentException("Invalid email format");
				}
				if (newEmail.length() > 100) {
					log.error("User update failed: email too long");
					throw new IllegalArgumentException("Email cannot exceed 100 characters");
				}
				
				if (userRepository.existsByEmail(newEmail)) {
					log.warn("User update failed: email '{}' already exists", newEmail);
					throw new IllegalArgumentException("Email already exists: " + newEmail);
				}
				log.debug("Updating email from '{}' to '{}'", existingUser.getEmail(), newEmail);
				existingUser.setEmail(newEmail);
			}

			// Save and return
			User updatedUser = userRepository.save(existingUser);
			log.info("User '{}' updated successfully", updatedUser.getUsername());
			return UserDto.fromEntity(updatedUser);
			
		} catch (DataAccessException e) {
			log.error("Database error while updating user with ID {}: {}", id, e.getMessage(), e);
			throw new ServiceException("Error updating user data", e);
		}
	}

	/**
	 * Deletes a user by ID.
	 * 
	 * @param id the user ID to delete
	 * @throws IllegalArgumentException if ID is null
	 * @throws EntityNotFoundException if user not found
	 * @throws ServiceException if database operation fails
	 */
	public void deleteUser(Long id) {
		log.info("Deleting user with ID: {}", id);

		if (id == null) {
			log.error("User deletion failed: ID is null");
			throw new IllegalArgumentException("User ID cannot be null");
		}

		try {
			if (!userRepository.existsById(id)) {
				log.warn("User deletion failed: user with ID {} not found", id);
				throw new EntityNotFoundException("User not found with id: " + id);
			}

			userRepository.deleteById(id);
			log.info("User with ID {} deleted successfully", id);
		} catch (DataAccessException e) {
			log.error("Database error while deleting user with ID {}: {}", id, e.getMessage(), e);
			throw new ServiceException("Error deleting user", e);
		}
	}

	/**
	 * Checks if a user exists by username.
	 * 
	 * @param username the username to check
	 * @return true if user exists, false otherwise
	 * @throws IllegalArgumentException if username is null or empty
	 * @throws ServiceException if database operation fails
	 */
	@Transactional(readOnly = true)
	public boolean existsByUsername(String username) {
		log.debug("Checking if user exists by username: {}", username);

		if (username == null || username.trim().isEmpty()) {
			log.error("Username existence check failed: username is null or empty");
			throw new IllegalArgumentException("Username cannot be null or empty");
		}
		
		try {
			boolean exists = userRepository.existsByUsername(username);
			log.debug("Username '{}' exists: {}", username, exists);
			return exists;
		} catch (DataAccessException e) {
			log.error("Database error while checking username existence '{}': {}", username, e.getMessage(), e);
			throw new ServiceException("Error checking username existence", e);
		}
	}

	/**
	 * Checks if a user exists by email.
	 * 
	 * @param email the email to check
	 * @return true if user exists, false otherwise
	 * @throws IllegalArgumentException if email is null or empty
	 * @throws ServiceException if database operation fails
	 */
	@Transactional(readOnly = true)
	public boolean existsByEmail(String email) {
		log.debug("Checking if user exists by email: {}", email);

		if (email == null || email.trim().isEmpty()) {
			log.error("Email existence check failed: email is null or empty");
			throw new IllegalArgumentException("Email cannot be null or empty");
		}
		
		try {
			boolean exists = userRepository.existsByEmail(email);
			log.debug("Email '{}' exists: {}", email, exists);
			return exists;
		} catch (DataAccessException e) {
			log.error("Database error while checking email existence '{}': {}", email, e.getMessage(), e);
			throw new ServiceException("Error checking email existence", e);
		}
	}

	/**
	 * Counts the total number of users.
	 * 
	 * @return the total number of users
	 * @throws ServiceException if database operation fails
	 */
	@Transactional(readOnly = true)
	public long countUsers() {
		log.debug("Counting all users");

		try {
			long count = userRepository.count();
			log.info("Total users count: {}", count);
			return count;
		} catch (DataAccessException e) {
			log.error("Database error while counting users: {}", e.getMessage(), e);
			throw new ServiceException("Error counting users", e);
		}
	}

	/**
	 * Counts users created after a specific date.
	 * 
	 * @param date the date to count from
	 * @return the number of users created after the specified date
	 * @throws IllegalArgumentException if date is null
	 * @throws ServiceException if database operation fails
	 */
	@Transactional(readOnly = true)
	public long countUsersCreatedAfter(LocalDateTime date) {
		if (date == null) {
			throw new IllegalArgumentException("Date cannot be null");
		}
		
		try {
			return userRepository.countByCreatedAfter(date);
		} catch (DataAccessException e) {
			log.error("Database error while counting users by date", e);
			throw new ServiceException("Error counting users by date", e);
		}
	}

	/**
	 * Searches users by username or email containing the given search term.
	 * 
	 * @param searchTerm the term to search for
	 * @return a list of users matching the search criteria
	 * @throws IllegalArgumentException if searchTerm is null or empty
	 * @throws ServiceException if database operation fails
	 */
	@Transactional(readOnly = true)
	public List<UserDto> searchUsers(String searchTerm) {
		log.debug("Searching users with term: {}", searchTerm);

		if (searchTerm == null || searchTerm.trim().isEmpty()) {
			log.error("User search failed: search term is null or empty");
			throw new IllegalArgumentException("Search term cannot be null or empty");
		}

		try {
			String trimmedSearchTerm = searchTerm.trim();
			List<User> users = userRepository.findByUsernameOrEmailContaining(trimmedSearchTerm);
			log.info("Found {} users matching search term: {}", users.size(), trimmedSearchTerm);
			return users.stream()
				.map(UserDto::fromEntity)
				.collect(Collectors.toList());
		} catch (DataAccessException e) {
			log.error("Database error while searching users with term '{}': {}", searchTerm, e.getMessage(), e);
			throw new ServiceException("Error searching users", e);
		}
	}

	/**
	 * Checks if a user is active (exists and has valid data).
	 * This method is optimized for quick status checks without loading full user data.
	 * 
	 * @param userId the ID of the user to check
	 * @return true if user is active, false otherwise
	 * @throws IllegalArgumentException if userId is null or invalid
	 * @throws ServiceException if database operation fails
	 */
	@Transactional(readOnly = true)
	public boolean isUserActive(Long userId) {
		log.debug("Checking if user is active: {}", userId);

		if (userId == null) {
			log.error("User activity check failed: ID is null");
			throw new IllegalArgumentException("User ID cannot be null");
		}
		if (userId <= 0) {
			log.error("User activity check failed: invalid ID: {}", userId);
			throw new IllegalArgumentException("User ID must be positive");
		}

		try {
			// Optimización: Solo verificar existencia y campos básicos
			Optional<User> user = userRepository.findById(userId);
			if (!user.isPresent()) {
				log.debug("User {} not found", userId);
				return false;
			}
			
			User foundUser = user.get();
			boolean isActive = foundUser.getUsername() != null && 
				!foundUser.getUsername().trim().isEmpty() &&
				foundUser.getEmail() != null && 
				!foundUser.getEmail().trim().isEmpty();
			
			log.debug("User {} is active: {}", userId, isActive);
			return isActive;
		} catch (DataAccessException e) {
			log.error("Database error while checking user activity for ID {}: {}", userId, e.getMessage(), e);
			throw new ServiceException("Error checking user activity", e);
		}
	}

	/**
	 * Gets user statistics (total count, active count, etc.).
	 * 
	 * @return a map containing user statistics
	 * @throws ServiceException if database operation fails
	 */
	@Transactional(readOnly = true)
	public Map<String, Object> getUserStatistics() {
		log.debug("Retrieving user statistics");

		try {
			// Optimización: Usar consultas específicas en lugar de cargar todos los usuarios
			long totalUsers = userRepository.count();
			long activeUsers = userRepository.countActiveUsers();
			
			Map<String, Object> stats = new HashMap<>();
			stats.put("totalUsers", totalUsers);
			stats.put("activeUsers", activeUsers);
			stats.put("inactiveUsers", totalUsers - activeUsers);
			
			log.info("User statistics - Total: {}, Active: {}, Inactive: {}", 
				totalUsers, activeUsers, totalUsers - activeUsers);
			return stats;
		} catch (DataAccessException e) {
			log.error("Database error while retrieving user statistics: {}", e.getMessage(), e);
			throw new ServiceException("Error retrieving user statistics", e);
		}
	}
}
