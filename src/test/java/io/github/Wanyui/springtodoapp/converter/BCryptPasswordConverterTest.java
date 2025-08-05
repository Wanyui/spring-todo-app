package io.github.Wanyui.springtodoapp.converter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Test class for BCryptPasswordConverter.
 * 
 * @author Wanyui/Jano
 * @version 1.0
 * @since 2025-08-06
 */
class BCryptPasswordConverterTest {

    private BCryptPasswordConverter converter;
    private BCryptPasswordEncoder encoder;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        converter = new BCryptPasswordConverter();
        encoder = new BCryptPasswordEncoder();
    }

    @Test
    @DisplayName("Should convert plain password to hash")
    void shouldConvertPlainPasswordToHash() {
        // Given
        String plainPassword = "password123";
        
        // When
        String hash = converter.convertToDatabaseColumn(plainPassword);
        
        // Then
        assertThat(hash).isNotNull();
        assertThat(hash).isNotEqualTo(plainPassword);
        assertThat(hash.length()).isEqualTo(60); // BCrypt hash is always 60 characters
        assertThat(encoder.matches(plainPassword, hash)).isTrue();
    }

    @Test
    @DisplayName("Should return null when password is null")
    void shouldReturnNullWhenPasswordIsNull() {
        // When
        String hash = converter.convertToDatabaseColumn(null);
        
        // Then
        assertThat(hash).isNull();
    }

    @Test
    @DisplayName("Should return hash as is when converting from database")
    void shouldReturnHashAsIsWhenConvertingFromDatabase() {
        // Given
        String hash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyi6yqjKqKqKqKqKqKqKqKqKqK";
        
        // When
        String result = converter.convertToEntityAttribute(hash);
        
        // Then
        assertThat(result).isEqualTo(hash);
    }

    @Test
    @DisplayName("Should return null when hash from database is null")
    void shouldReturnNullWhenHashFromDatabaseIsNull() {
        // When
        String result = converter.convertToEntityAttribute(null);
        
        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should generate different hashes for same password")
    void shouldGenerateDifferentHashesForSamePassword() {
        // Given
        String password = "password123";
        
        // When
        String hash1 = converter.convertToDatabaseColumn(password);
        String hash2 = converter.convertToDatabaseColumn(password);
        
        // Then
        assertThat(hash1).isNotEqualTo(hash2); // BCrypt generates different salts
        assertThat(encoder.matches(password, hash1)).isTrue();
        assertThat(encoder.matches(password, hash2)).isTrue();
    }

    @Test
    @DisplayName("Should handle empty password")
    void shouldHandleEmptyPassword() {
        // Given
        String emptyPassword = "";
        
        // When
        String hash = converter.convertToDatabaseColumn(emptyPassword);
        
        // Then
        assertThat(hash).isNotNull();
        assertThat(hash.length()).isEqualTo(60);
        assertThat(encoder.matches(emptyPassword, hash)).isTrue();
    }

    @Test
    @DisplayName("Should handle special characters in password")
    void shouldHandleSpecialCharactersInPassword() {
        // Given
        String specialPassword = "p@ssw0rd!@#$%^&*()";
        
        // When
        String hash = converter.convertToDatabaseColumn(specialPassword);
        
        // Then
        assertThat(hash).isNotNull();
        assertThat(hash.length()).isEqualTo(60);
        assertThat(encoder.matches(specialPassword, hash)).isTrue();
    }

    @Test
    @DisplayName("Should handle very long password")
    void shouldHandleVeryLongPassword() {
        // Given
        String longPassword = "a".repeat(72); // BCrypt limit is 72 bytes
        
        // When
        String hash = converter.convertToDatabaseColumn(longPassword);
        
        // Then
        assertThat(hash).isNotNull();
        assertThat(hash.length()).isEqualTo(60);
        assertThat(encoder.matches(longPassword, hash)).isTrue();
    }
} 