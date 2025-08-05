package io.github.Wanyui.springtodoapp.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

/**
 * The Todo entity represents a todo of the application.
 * 
 * @author Wanyui/Jano
 * @version 1.0
 * @since 2025-08-06
 */

@Entity
@Table(indexes = {
	@Index(name = "idx_todo_user_id", columnList = "user_id"),
	@Index(name = "idx_todo_done", columnList = "done"),
	@Index(name = "idx_todo_created_at", columnList = "created_at"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "title", "description", "done"})
public class Todo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(min = 1, max = 100, message = "The title must be between 1 and 100 characters")
	@Column(length = 100, nullable = false)
	private String title;

	@Size(max = 1000, message = "The description must be less than 1000 characters")
	@Column(length = 1000)
	private String description;

	@NotNull
	@Column(nullable = false)
	private boolean done;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	@NotNull
	private User user;

	/**
	 * Toggles the done status of the todo.
	 * 
	 * @param done the new done status of the todo
	 * @return the new done status of the todo
	 */
	public void toggleDone() {
		this.done = !this.done;
	}

	/**
	 * Updates the title and description of the todo.
	 * 
	 * @param title the new title of the todo
	 * @param description the new description of the todo
	 * @return the updated todo
	 */
	public void update(String title, String description) {
		if (title == null || title.trim().isEmpty()) {
			throw new IllegalArgumentException("Title cannot be empty");
		}
		this.title = title.trim();
		this.description = description != null ? description.trim() : null;
	}
}
