package io.github.Wanyui.springtodoapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.github.Wanyui.springtodoapp.entity.Todo;
import io.github.Wanyui.springtodoapp.entity.User;

/**
 * The TodoRepository interface extends JpaRepository to provide basic CRUD operations for the Todo entity.
 * It also provides additional methods to find todos by user, and to find todos by user and done status.
 * 
 * @author Wanyui/Jano
 * @version 1.0
 * @since 2025-08-06
 */
@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

	/**
	 * Finds all todos for a given user.
	 * 
	 * @param user the user to find todos for
	 * @return a list of todos for the given user
	 */
	List<Todo> findByUser(User user);
	
	/**
	 * Finds all todos for a given user and done status.
	 * 
	 * @param user the user to find todos for
	 * @param done the done status to filter by
	 * @return a list of todos for the given user and done status
	 */
	List<Todo> findByUserAndDone(User user, boolean done);
}
