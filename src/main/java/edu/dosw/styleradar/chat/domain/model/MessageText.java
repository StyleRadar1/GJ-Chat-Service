package edu.dosw.styleradar.chat.domain.model;

import edu.dosw.styleradar.chat.domain.exception.InvalidMessageTextException;

/** Validated message text: not blank, trimmed and at most 1000 characters. */
public record MessageText(String value) {
    public static final int MAX_LENGTH = 1000;

    public MessageText {
        if (value == null || value.isBlank()) {
            throw new InvalidMessageTextException("El mensaje no puede estar vacío");
        }
        value = value.trim();
        if (value.length() > MAX_LENGTH) {
            throw new InvalidMessageTextException("El mensaje no puede superar " + MAX_LENGTH + " caracteres");
        }
    }
}
