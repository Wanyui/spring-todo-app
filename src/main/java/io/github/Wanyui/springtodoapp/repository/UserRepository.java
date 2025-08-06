package io.github.Wanyui.springtodoapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.github.Wanyui.springtodoapp.entity.User;

/**
 * The UserRepository interface extends JpaRepository to provide basic CRUD operations for the User entity.
 * It also provides additional methods to find users by username or email, and to check if a user with a given username or email already exists.
 * 
 * @author Wanyui/Jano
 * @version 1.0
 * @since 2025-08-06
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	/**
	 * Finds a user by their username.
	 * 
	 * @param username the username of the user to find
	 * @return an Optional containing the user if found, or empty if not found
	 */
	Optional<User> findByUsername(String username);
	
	/**
	 * Finds a user by their email.
	 * 
	 * @param email the email of the user to find
	 * @return an Optional containing the user if found, or empty if not found
	 */
	Optional<User> findByEmail(String email);
	
	/**
	 * Checks if a user with a given username already exists.
	 * 
	 * @param username the username to check
	 * @return true if a user with the given username exists, false otherwise
	 */
	boolean existsByUsername(String username);
	
	/**
	 * Checks if a user with a given email already exists.
	 * 
	 * @param email the email to check
	 * @return true if a user with the given email exists, false otherwise
	 */
	boolean existsByEmail(String email);
}
