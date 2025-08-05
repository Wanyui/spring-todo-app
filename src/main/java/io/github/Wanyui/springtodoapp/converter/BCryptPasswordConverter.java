package io.github.Wanyui.springtodoapp.converter;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA AttributeConverter for automatically hashing passwords using BCrypt.
 * 
 * This converter automatically encodes plain text passwords to BCrypt hashes
 * when saving to the database, and returns the hash when reading from the database.
 * 
 * @author Wanyui/Jano
 * @version 1.0
 * @since 2025-08-06
 */
@Converter(autoApply = true)
public class BCryptPasswordConverter implements AttributeConverter<String, String> {
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Converts a plain text password to a BCrypt hash for database storage.
     * 
     * @param password the plain text password to encode (can be null)
     * @return the BCrypt hash of the password, or null if password is null
     */
    @Override
    public String convertToDatabaseColumn(String password) {
        return password != null ? passwordEncoder.encode(password) : null;
    }

    /**
     * Converts a BCrypt hash from the database back to the hash string.
     * 
     * Note: BCrypt is a one-way hashing algorithm, so we cannot decode
     * the hash back to the original password. This method simply returns
     * the hash as stored in the database.
     * 
     * @param dbData the BCrypt hash from the database
     * @return the hash string (not the original password)
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData; // No decodificar, solo devolver el hash
    }
}
