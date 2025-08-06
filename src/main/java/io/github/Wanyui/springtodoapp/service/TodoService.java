package io.github.Wanyui.springtodoapp.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import io.github.Wanyui.springtodoapp.entity.Todo;
import io.github.Wanyui.springtodoapp.entity.User;
import io.github.Wanyui.springtodoapp.repository.TodoRepository;
import io.github.Wanyui.springtodoapp.repository.UserRepository;
import io.github.Wanyui.springtodoapp.service.dto.TodoDto;
import io.github.Wanyui.springtodoapp.service.exception.ServiceException;

/**
 * Service class for Todo entity providing business logic and operations.
 * 
 * This service handles all todo-related business operations including:
 * - Todo creation and management
 * - User-specific todo operations
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
public class TodoService {
	
	private final TodoRepository todoRepository;
	private final UserRepository userRepository;

	/**
	 * Creates a new todo for a user.
	 * 
	 * @param todoDto the todo data to create
	 * @param userId the ID of the user who owns the todo
	 * @return the created todo as DTO
	 * @throws IllegalArgumentException if validation fails
	 * @throws EntityNotFoundException if user not found
	 * @throws ServiceException if database operation fails
	 */
	public TodoDto createTodo(TodoDto todoDto, Long userId) {
		// Validation checks
		if (todoDto == null) {
			log.error("Todo creation failed: data is null");
			throw new IllegalArgumentException("Todo data cannot be null");
		}
		
		if (userId == null) {
			log.error("Todo creation failed: user ID is null");
			throw new IllegalArgumentException("User ID cannot be null");
		}
		if (userId <= 0) {
			log.error("Todo creation failed: invalid user ID: {}", userId);
			throw new IllegalArgumentException("User ID must be positive");
		}
		
		// Title validation
		if (todoDto.getTitle() == null || todoDto.getTitle().trim().isEmpty()) {
			log.error("Todo creation failed: title is empty");
			throw new IllegalArgumentException("Todo title cannot be empty");
		}
		String title = todoDto.getTitle().trim();
		if (title.length() < 1 || title.length() > 100) {
			log.error("Todo creation failed: title length must be between 1 and 100 characters");
			throw new IllegalArgumentException("Todo title length must be between 1 and 100 characters");
		}
		
		// Description validation
		if (todoDto.getDescription() != null) {
			String description = todoDto.getDescription().trim();
			if (description.length() > 1000) {
				log.error("Todo creation failed: description too long");
				throw new IllegalArgumentException("Todo description cannot exceed 1000 characters");
			}
		}
		
		log.info("Creating todo '{}' for user ID: {}", title, userId);

		try {
			log.debug("Finding user with ID: {}", userId);
			User user = userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
			log.debug("Found user: {}", user.getUsername());

			// Convert DTO to entity
			Todo todo = todoDto.toEntity();
			todo.setUser(user);
			
			// Save and return
			Todo savedTodo = todoRepository.save(todo);
			log.info("Todo '{}' created successfully with ID: {} for user: {}", 
				savedTodo.getTitle(), savedTodo.getId(), user.getUsername());
			return TodoDto.fromEntity(savedTodo);
			
		} catch (DataAccessException e) {
			log.error("Database error while creating todo '{}' for user ID {}: {}", 
				todoDto.getTitle(), userId, e.getMessage(), e);
			throw new ServiceException("Error creating todo", e);
		}
	}

	/**
	 * Retrieves a todo by its ID.
	 * 
	 * @param id the ID of the todo to retrieve
	 * @return the todo with the given ID
	 * @throws IllegalArgumentException if ID is null
	 * @throws EntityNotFoundException if the todo with the given ID is not found
	 * @throws ServiceException if database operation fails
	 */
	@Transactional(readOnly = true)
	public TodoDto getTodoById(Long id) {
		log.debug("Retrieving todo with ID: {}", id);

		if (id == null) {
			log.error("Todo retrieval failed: ID is null");
			throw new IllegalArgumentException("Todo ID cannot be null");
		}
		if (id <= 0) {
			log.error("Todo retrieval failed: invalid ID: {}", id);
			throw new IllegalArgumentException("Todo ID must be positive");
		}
		
		try {
			Todo todo = todoRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Todo not found with id: " + id));
			log.info("Todo '{}' retrieved successfully for user: {}", 
				todo.getTitle(), todo.getUser().getUsername());
			return TodoDto.fromEntity(todo);
		} catch (DataAccessException e) {
			log.error("Database error while retrieving todo with ID {}: {}", id, e.getMessage(), e);
			throw new ServiceException("Error accessing todo data", e);
		}
	}

	/**
	 * Retrieves all todos for a specific user.
	 * 
	 * @param userId the ID of the user
	 * @return a list of todos for the user
	 * @throws IllegalArgumentException if userId is null
	 * @throws EntityNotFoundException if user not found
	 * @throws ServiceException if database operation fails
	 */
	@Transactional(readOnly = true)
	public List<TodoDto> getTodosByUserId(Long userId) {
		log.debug("Retrieving todos for user ID: {}", userId);

		if (userId == null) {
			log.error("Todo retrieval failed: user ID is null");
			throw new IllegalArgumentException("User ID cannot be null");
		}

		try {
			log.debug("Checking if user exists: {}", userId);
			if (!userRepository.existsById(userId)) {
				log.warn("Todo retrieval failed: user with ID {} not found", userId);
				throw new EntityNotFoundException("User not found with id: " + userId);
			}

			User user = userRepository.findById(userId).get();
			log.debug("Found user: {}", user.getUsername());
			
			List<Todo> todos = todoRepository.findByUser(user);
			log.info("Retrieved {} todos for user '{}'", todos.size(), user.getUsername());
			
			return todos.stream()
				.map(TodoDto::fromEntity)
				.collect(Collectors.toList());
				
		} catch (DataAccessException e) {
			log.error("Database error while retrieving todos for user ID {}: {}", userId, e.getMessage(), e);
			throw new ServiceException("Error retrieving todos for user", e);
		}
	}

	/**
	 * Retrieves todos for a user by done status.
	 * 
	 * @param userId the ID of the user
	 * @param done the done status to filter by
	 * @return a list of todos for the user with the specified done status
	 * @throws IllegalArgumentException if userId is null
	 * @throws EntityNotFoundException if user not found
	 * @throws ServiceException if database operation fails
	 */
	@Transactional(readOnly = true)
	public List<TodoDto> getTodosByUserIdAndDone(Long userId, boolean done) {
		log.debug("Retrieving todos for user ID: {} with done status: {}", userId, done);

		if (userId == null) {
			log.error("Todo retrieval failed: user ID is null");
			throw new IllegalArgumentException("User ID cannot be null");
		}

		try {
			log.debug("Checking if user exists: {}", userId);
			if (!userRepository.existsById(userId)) {
				log.warn("Todo retrieval failed: user with ID {} not found", userId);
				throw new EntityNotFoundException("User not found with id: " + userId);
			}

			User user = userRepository.findById(userId).get();
			log.debug("Found user: {}", user.getUsername());
			
			List<Todo> todos = todoRepository.findByUserAndDone(user, done);
			log.info("Retrieved {} {} todos for user '{}'", 
				todos.size(), done ? "completed" : "pending", user.getUsername());
			
			return todos.stream()
				.map(TodoDto::fromEntity)
				.collect(Collectors.toList());
				
		} catch (DataAccessException e) {
			log.error("Database error while retrieving todos for user ID {} with status {}: {}", 
				userId, done, e.getMessage(), e);
			throw new ServiceException("Error retrieving todos by status", e);
		}
	}

	/**
	 * Retrieves all todos.
	 * 
	 * @return a list of all todos as DTOs
	 * @throws ServiceException if database operation fails
	 */
	@Transactional(readOnly = true)
	public List<TodoDto> getAllTodos() {
		log.debug("Retrieving all todos");

		try {
			List<Todo> todos = todoRepository.findAll();
			log.info("Retrieved {} todos successfully", todos.size());
			return todos.stream()
				.map(TodoDto::fromEntity)
				.collect(Collectors.toList());
		} catch (DataAccessException e) {
			log.error("Database error while retrieving all todos: {}", e.getMessage(), e);
			throw new ServiceException("Error retrieving all todos", e);
		}
	}

	/**
	 * Updates an existing todo.
	 * 
	 * @param id the todo ID to update
	 * @param todoDto the updated todo data
	 * @return the updated todo as DTO
	 * @throws IllegalArgumentException if validation fails
	 * @throws EntityNotFoundException if todo not found
	 * @throws ServiceException if database operation fails
	 */
	public TodoDto updateTodo(Long id, TodoDto todoDto) {
		log.info("Updating todo with ID: {}", id);

		if (id == null) {
			log.error("Todo update failed: ID is null");
			throw new IllegalArgumentException("Todo ID cannot be null");
		}
		if (todoDto == null) {
			log.error("Todo update failed: data is null");
			throw new IllegalArgumentException("Todo data cannot be null");
		}

		try {
			log.debug("Finding existing todo with ID: {}", id);
			Todo existingTodo = todoRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Todo not found with id: " + id));
			log.debug("Found existing todo: '{}' for user: {}", 
				existingTodo.getTitle(), existingTodo.getUser().getUsername());

			// Update title with validation
			if (todoDto.getTitle() != null && !todoDto.getTitle().equals(existingTodo.getTitle())) {
				String newTitle = todoDto.getTitle().trim();
				if (newTitle.isEmpty()) {
					log.error("Todo update failed: title cannot be empty");
					throw new IllegalArgumentException("Todo title cannot be empty");
				}
				if (newTitle.length() < 1 || newTitle.length() > 100) {
					log.error("Todo update failed: title length must be between 1 and 100 characters");
					throw new IllegalArgumentException("Todo title length must be between 1 and 100 characters");
				}
				log.debug("Updating todo title from '{}' to '{}'", existingTodo.getTitle(), newTitle);
				existingTodo.setTitle(newTitle);
			}
			
			// Update description with validation
			if (todoDto.getDescription() != null && !todoDto.getDescription().equals(existingTodo.getDescription())) {
				String newDescription = todoDto.getDescription().trim();
				if (newDescription.length() > 1000) {
					log.error("Todo update failed: description too long");
					throw new IllegalArgumentException("Todo description cannot exceed 1000 characters");
				}
				log.debug("Updating todo description");
				existingTodo.setDescription(newDescription);
			}
			
			// Update done status
			if (todoDto.isDone() != existingTodo.isDone()) {
				log.debug("Updating todo done status from {} to {}", existingTodo.isDone(), todoDto.isDone());
				existingTodo.setDone(todoDto.isDone());
			}

			// Save and return
			Todo updatedTodo = todoRepository.save(existingTodo);
			log.info("Todo '{}' updated successfully for user: {}", 
				updatedTodo.getTitle(), updatedTodo.getUser().getUsername());
			return TodoDto.fromEntity(updatedTodo);
			
		} catch (DataAccessException e) {
			log.error("Database error while updating todo with ID {}: {}", id, e.getMessage(), e);
			throw new ServiceException("Error updating todo", e);
		}
	}

	/**
	 * Toggles the done status of a todo.
	 * 
	 * @param id the todo ID to toggle
	 * @return the updated todo as DTO
	 * @throws IllegalArgumentException if ID is null
	 * @throws EntityNotFoundException if todo not found
	 * @throws ServiceException if database operation fails
	 */
	public TodoDto toggleTodoDone(Long id) {
		log.info("Toggling todo done status for ID: {}", id);

		if (id == null) {
			log.error("Todo toggle failed: ID is null");
			throw new IllegalArgumentException("Todo ID cannot be null");
		}

		try {
			log.debug("Finding todo with ID: {}", id);
			Todo todo = todoRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Todo not found with id: " + id));
			log.debug("Found todo: '{}' (done: {}) for user: {}", 
				todo.getTitle(), todo.isDone(), todo.getUser().getUsername());

			boolean previousStatus = todo.isDone();
			todo.toggleDone();
			Todo updatedTodo = todoRepository.save(todo);
			
			log.info("Todo '{}' status toggled from {} to {} for user: {}", 
				updatedTodo.getTitle(), previousStatus, updatedTodo.isDone(), updatedTodo.getUser().getUsername());
			return TodoDto.fromEntity(updatedTodo);
			
		} catch (DataAccessException e) {
			log.error("Database error while toggling todo with ID {}: {}", id, e.getMessage(), e);
			throw new ServiceException("Error toggling todo status", e);
		}
	}

	/**
	 * Deletes a todo by ID.
	 * 
	 * @param id the todo ID to delete
	 * @throws IllegalArgumentException if ID is null
	 * @throws EntityNotFoundException if todo not found
	 * @throws ServiceException if database operation fails
	 */
	public void deleteTodo(Long id) {
		log.info("Deleting todo with ID: {}", id);

		if (id == null) {
			log.error("Todo deletion failed: ID is null");
			throw new IllegalArgumentException("Todo ID cannot be null");
		}

		try {
			log.debug("Checking if todo exists: {}", id);
			if (!todoRepository.existsById(id)) {
				log.warn("Todo deletion failed: todo with ID {} not found", id);
				throw new EntityNotFoundException("Todo not found with id: " + id);
			}

			// Get todo info for logging before deletion
			Todo todo = todoRepository.findById(id).get();
			log.debug("Found todo to delete: '{}' for user: {}", 
				todo.getTitle(), todo.getUser().getUsername());

			todoRepository.deleteById(id);
			log.info("Todo '{}' deleted successfully for user: {}", 
				todo.getTitle(), todo.getUser().getUsername());
		} catch (DataAccessException e) {
			log.error("Database error while deleting todo with ID {}: {}", id, e.getMessage(), e);
			throw new ServiceException("Error deleting todo", e);
		}
	}

	/**
	 * Deletes all todos for a specific user.
	 * 
	 * @param userId the ID of the user
	 * @throws IllegalArgumentException if userId is null
	 * @throws EntityNotFoundException if user not found
	 * @throws ServiceException if database operation fails
	 */
	public void deleteTodosByUserId(Long userId) {
		log.info("Deleting all todos for user ID: {}", userId);

		if (userId == null) {
			log.error("Todo deletion failed: user ID is null");
			throw new IllegalArgumentException("User ID cannot be null");
		}

		try {
			log.debug("Checking if user exists: {}", userId);
			if (!userRepository.existsById(userId)) {
				log.warn("Todo deletion failed: user with ID {} not found", userId);
				throw new EntityNotFoundException("User not found with id: " + userId);
			}

			User user = userRepository.findById(userId).get();
			log.debug("Found user: {}", user.getUsername());

			List<Todo> userTodos = todoRepository.findByUser(user);
			log.info("Deleting {} todos for user '{}'", userTodos.size(), user.getUsername());
			todoRepository.deleteAll(userTodos);
			log.info("All todos deleted successfully for user '{}'", user.getUsername());
			
		} catch (DataAccessException e) {
			log.error("Database error while deleting todos for user ID {}: {}", userId, e.getMessage(), e);
			throw new ServiceException("Error deleting todos for user", e);
		}
	}

	/**
	 * Counts the total number of todos.
	 * 
	 * @return the total number of todos
	 * @throws ServiceException if database operation fails
	 */
	@Transactional(readOnly = true)
	public long countTodos() {
		log.debug("Counting all todos");

		try {
			long count = todoRepository.count();
			log.info("Total todos count: {}", count);
			return count;
		} catch (DataAccessException e) {
			log.error("Database error while counting todos: {}", e.getMessage(), e);
			throw new ServiceException("Error counting todos", e);
		}
	}

	/**
	 * Counts todos for a specific user.
	 * 
	 * @param userId the ID of the user
	 * @return the number of todos for the user
	 * @throws IllegalArgumentException if userId is null
	 * @throws EntityNotFoundException if user not found
	 * @throws ServiceException if database operation fails
	 */
	@Transactional(readOnly = true)
	public long countTodosByUserId(Long userId) {
		log.debug("Counting todos for user ID: {}", userId);

		if (userId == null) {
			log.error("Todo count failed: user ID is null");
			throw new IllegalArgumentException("User ID cannot be null");
		}

		try {
			log.debug("Checking if user exists: {}", userId);
			if (!userRepository.existsById(userId)) {
				log.warn("Todo count failed: user with ID {} not found", userId);
				throw new EntityNotFoundException("User not found with id: " + userId);
			}

			User user = userRepository.findById(userId).get();
			log.debug("Found user: {}", user.getUsername());

			long count = todoRepository.countByUser(user);
			log.info("User '{}' has {} todos", user.getUsername(), count);
			return count;
			
		} catch (DataAccessException e) {
			log.error("Database error while counting todos for user ID {}: {}", userId, e.getMessage(), e);
			throw new ServiceException("Error counting todos for user", e);
		}
	}

	/**
	 * Counts done todos for a specific user.
	 * 
	 * @param userId the ID of the user
	 * @return the number of done todos for the user
	 * @throws IllegalArgumentException if userId is null
	 * @throws EntityNotFoundException if user not found
	 * @throws ServiceException if database operation fails
	 */
	@Transactional(readOnly = true)
	public long countDoneTodosByUserId(Long userId) {
		log.debug("Counting done todos for user ID: {}", userId);

		if (userId == null) {
			log.error("Todo count failed: user ID is null");
			throw new IllegalArgumentException("User ID cannot be null");
		}

		try {
			log.debug("Checking if user exists: {}", userId);
			if (!userRepository.existsById(userId)) {
				log.warn("Todo count failed: user with ID {} not found", userId);
				throw new EntityNotFoundException("User not found with id: " + userId);
			}

			User user = userRepository.findById(userId).get();
			log.debug("Found user: {}", user.getUsername());

			long count = todoRepository.countByUserAndDone(user, true);
			log.info("User '{}' has {} completed todos", user.getUsername(), count);
			return count;
			
		} catch (DataAccessException e) {
			log.error("Database error while counting done todos for user ID {}: {}", userId, e.getMessage(), e);
			throw new ServiceException("Error counting done todos for user", e);
		}
	}

	/**
	 * Gets overall todo statistics.
	 * 
	 * @return a map containing overall todo statistics
	 * @throws ServiceException if database operation fails
	 */
	@Transactional(readOnly = true)
	public Map<String, Object> getOverallTodoStatistics() {
		log.debug("Retrieving overall todo statistics");

		try {
			// Optimización: Usar consultas específicas en lugar de cargar todos los todos
			long totalTodos = todoRepository.count();
			long doneTodos = todoRepository.countByDone(true);
			long pendingTodos = totalTodos - doneTodos;

			Map<String, Object> stats = new HashMap<>();
			stats.put("totalTodos", totalTodos);
			stats.put("doneTodos", doneTodos);
			stats.put("pendingTodos", pendingTodos);
			stats.put("completionRate", totalTodos == 0 ? 0.0 : (double) doneTodos / totalTodos * 100);

			log.info("Overall todo statistics - Total: {}, Done: {}, Pending: {}, Completion Rate: {:.1f}%", 
				totalTodos, doneTodos, pendingTodos, totalTodos == 0 ? 0.0 : (double) doneTodos / totalTodos * 100);
			return stats;
		} catch (DataAccessException e) {
			log.error("Database error while retrieving overall todo statistics: {}", e.getMessage(), e);
			throw new ServiceException("Error retrieving overall todo statistics", e);
		}
	}
}
