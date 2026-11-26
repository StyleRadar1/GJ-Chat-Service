package edu.dosw.styleradar.chat.domain.model;

import edu.dosw.styleradar.chat.domain.exception.InvalidMessageTextException;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validated message text: not blank, trimmed, at most 1000 characters,
 * free of offensive language and protected against sharing personal contact details.
 */
public record MessageText(String value) {
    public static final int MAX_LENGTH = 1000;

    // Detects phone numbers: 7 to 15 digits, supporting country code (+57), spaces, dots, dashes
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "(?:\\+?\\d{1,3}[-.\\s]?)?(?:3\\d{2}|[1-9]\\d{1,2})[-.\\s]?\\d{3}[-.\\s]?\\d{4}|\\b\\d{7,15}\\b"
    );

    // Detects email addresses (RFC 5322 simplified pattern)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",
        Pattern.CASE_INSENSITIVE
    );

    // Profanity list with 40+ variations, offensive terms, and slurs commonly found in regional slang
    private static final Set<String> PROHIBITED_WORDS = Set.of(
        "gonorrea", "gonorreas", "gonorriento",
        "hijueputa", "hijadeputa", "hijodeputa", "hijueputas", "hpta", "hptas", "hp",
        "malparido", "malparida", "malparidos", "malparidas",
        "mierda", "mierdas", "mierdoso",
        "puta", "putas", "putita", "putito", "puto", "putos", "prostituta", "prostituto",
        "carechimba", "chimba", "chimbas",
        "marica", "maricon", "maricones", "maricada",
        "pendejo", "pendeja", "pendejos", "pendejas", "pendejada",
        "idiota", "idiotas", "estupido", "estupida", "estupidos", "estupidas",
        "imbecil", "imbeciles", "tarado", "tarada", "bobo", "boba", "huevon", "guevon",
        "culiao", "culiada", "pirobo", "piroba", "zorra", "perra"
    );

    public MessageText {
        if (value == null || value.isBlank()) {
            throw new InvalidMessageTextException("El mensaje no puede estar vacío");
        }
        value = value.trim();
        if (value.length() > MAX_LENGTH) {
            throw new InvalidMessageTextException("El mensaje no puede superar " + MAX_LENGTH + " caracteres");
        }

        validatePrivateContactInfo(value);
        validateProfanity(value);
    }

    private static void validatePrivateContactInfo(String text) {
        if (EMAIL_PATTERN.matcher(text).find()) {
            throw new InvalidMessageTextException("No está permitido compartir correos electrónicos en el chat");
        }
        if (PHONE_PATTERN.matcher(text).find()) {
            throw new InvalidMessageTextException("No está permitido compartir números telefónicos ni datos de contacto");
        }
    }

    private static void validateProfanity(String text) {
        // Strip accents/diacritics and convert to lower case for case-insensitive and accented checks
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);

        // Split by non-alphanumeric characters to evaluate each token
        String[] tokens = normalized.split("[^a-z0-9]+");
        for (String token : tokens) {
            if (PROHIBITED_WORDS.contains(token)) {
                throw new InvalidMessageTextException("El mensaje contiene lenguaje inapropiado u ofensivo");
            }
        }
    }
}
