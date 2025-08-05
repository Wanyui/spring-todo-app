package io.github.Wanyui.springtodoapp.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.persistence.Convert;

import io.github.Wanyui.springtodoapp.converter.BCryptPasswordConverter;

/**
 * The User entity represents a user of the application.
 * 
 * @author Wanyui/Jano
 * @version 1.0
 * @since 2025-08-06
 */

@Entity
@Table(name = "users", indexes = {
	@Index(name = "idx_user_username", columnList = "username", unique = true),
	@Index(name = "idx_user_email", columnList = "email", unique = true)	
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "username", "email"})
@ToString(exclude = {"password", "todos"})
public class User {

	public static final int MIN_USERNAME_LENGTH = 3;
	public static final int MAX_USERNAME_LENGTH = 100;
	public static final int MIN_PASSWORD_LENGTH = 6;
	public static final int MAX_PASSWORD_LENGTH = 100;
	public static final int MIN_EMAIL_LENGTH = 5;
	public static final int MAX_EMAIL_LENGTH = 100;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(
		min = MIN_USERNAME_LENGTH,
		max = MAX_USERNAME_LENGTH,
		message = "The username must be between " + MIN_USERNAME_LENGTH + " and " + MAX_USERNAME_LENGTH + " characters")
	@Column(length = 100, unique = true, nullable = false)
	private String username;

	@NotBlank
	@Size(
		min = MIN_PASSWORD_LENGTH,
		max = MAX_PASSWORD_LENGTH,
		message = "The password must be between " + MIN_PASSWORD_LENGTH + " and " + MAX_PASSWORD_LENGTH + " characters")
	@Column(length = 60, nullable = false) // BCrypt genera hashes de 60 caracteres
	@Convert(converter = BCryptPasswordConverter.class)
	private String password;

	@NotBlank
	@Size(
		min = MIN_EMAIL_LENGTH,
		max = MAX_EMAIL_LENGTH,
		message = "The email must be between " + MIN_EMAIL_LENGTH + " and " + MAX_EMAIL_LENGTH + " characters")
	@Email(message = "The email must be a valid email address")
	@Column(length = 100, unique = true, nullable = false)
	private String email;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;

	@OneToMany(
		mappedBy = "user",
		cascade = CascadeType.ALL,
		orphanRemoval = true,
		fetch = FetchType.LAZY
	)
	private Set<Todo> todos = new HashSet<>();

	/**
	 * Adds a todo to the user.
	 * 
	 * @param todo the todo to add
	 * @throws IllegalArgumentException if the todo already belongs to this user
	 * @return the added todo
	 */
	public void addTodo(Todo todo) {
		if (this.equals(todo.getUser())) {
			throw new IllegalArgumentException("Todo already belongs to this user");
		}
		this.todos.add(todo);
		todo.setUser(this);
	}

	/**
	 * Removes a todo from the user.
	 * 
	 * @param todo the todo to remove
	 * @throws IllegalArgumentException if the todo does not belong to this user
	 * @return the removed todo
	 */
	public void removeTodo(Todo todo) {
		if (!this.equals(todo.getUser())) {
			throw new IllegalArgumentException("Todo does not belong to this user");
		}
		this.todos.remove(todo);
		todo.setUser(null);
	}

	/**
	 * Gets all the done todos of the user.
	 * 
	 * @return the done todos of the user
	 */
	public Set<Todo> getDoneTodos() {
		return this.todos.stream()
			.filter(Todo::isDone)
			.collect(Collectors.toSet());
	}

	/**
	 * Gets all the not done todos of the user.
	 * 
	 * @return the not done todos of the user
	 */
	public Set<Todo> getNotDoneTodos() {
		return this.todos.stream()
			.filter(todo -> !todo.isDone())
			.collect(Collectors.toSet());
	}
}
