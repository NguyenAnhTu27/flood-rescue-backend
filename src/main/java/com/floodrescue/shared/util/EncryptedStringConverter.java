package com.floodrescue.shared.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * JPA AttributeConverter that transparently encrypts/decrypts String fields.
 * Uses deterministic encryption so Spring Data derived queries (findByPhone, existsByEmail, etc.)
 * continue to work — the query parameter is converted before being sent to the database.
 */
@Converter
@Component
@Slf4j
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static final Pattern PLAIN_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PLAIN_PHONE = Pattern.compile("^[0-9+()\\-\\s.]{6,20}$");
    private static StringEncryptor encryptor;

    @Value("${app.encryption.secret}")
    public void setSecret(String secret) {
        EncryptedStringConverter.encryptor = new StringEncryptor(secret);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        return encryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        if (looksLikeLegacyPlainText(dbData)) {
            return dbData;
        }
        try {
            return encryptor.decrypt(dbData);
        } catch (RuntimeException ex) {
            if (looksLikeLegacyPlainText(dbData)) {
                log.warn("Encountered legacy plaintext value in encrypted column; returning raw value");
                return dbData;
            }
            throw ex;
        }
    }

    private boolean looksLikeLegacyPlainText(String value) {
        String normalized = value == null ? null : value.trim();
        if (normalized == null || normalized.isEmpty()) {
            return true;
        }
        return PLAIN_EMAIL.matcher(normalized).matches() || PLAIN_PHONE.matcher(normalized).matches();
    }
}
