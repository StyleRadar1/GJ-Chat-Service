package edu.dosw.styleradar.chat.domain.model;

import edu.dosw.styleradar.chat.domain.exception.InvalidMessageTextException;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validated and sanitized message text: not blank, trimmed, at most 1000 characters,
 * with offensive words (including separated by spaces), emails, and phone numbers censored with asterisks.
 */
public record MessageText(String value) {
    public static final int MAX_LENGTH = 1000;

    // Detects phone numbers: 7 to 15 digits, supporting country code (+57), spaces, dots, dashes
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "(?:\\+?\\d{1,3}[-.\\s]?)?(?:3\\d{2}|[1-9]\\d{1,2})[-.\\s]?\\d{3}[-.\\s]?\\d{4}|\\b\\d{7,15}\\b"
    );

    // Detects email addresses
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",
        Pattern.CASE_INSENSITIVE
    );

    // Token boundary pattern to extract whole words
    private static final Pattern WORD_PATTERN = Pattern.compile("[a-zA-Z0-9áéíóúüñÁÉÍÓÚÜÑ]+");

    // Prohibited words and slurs commonly found in regional slang (including tetranutra, etc.)
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
        "culiao", "culiada", "pirobo", "piroba", "zorra", "perra",
        "tetranutra", "tetranutras"
    );

    // Regex pattern for words frequently disguised by inserting spaces between letters
    // Matches expressions like "p u t a", "h i j u e p u t a", "m i e r d a", "g o n o r r e a", etc.
    private static final Pattern SPACED_PROFANITY_PATTERN = Pattern.compile(
        "\\b(?:"
        + "h\\s*i\\s*j\\s*u\\s*e\\s*p\\s*u\\s*t\\s*a|h\\s*i\\s*j\\s*u\\s*e\\s*p\\s*u\\s*t\\s*a\\s*s|"
        + "h\\s*i\\s*j\\s*u\\s*e\\s+p\\s*u\\s*t\\s*a|"
        + "p\\s*u\\s*t\\s*a|p\\s*u\\s*t\\s*a\\s*s|p\\s*u\\s*t\\s*i\\s*t\\s*a|p\\s*u\\s*t\\s*i\\s*t\\s*o|p\\s*u\\s*t\\s*o|p\\s*u\\s*t\\s*o\\s*s|"
        + "g\\s*o\\s*n\\s*o\\s*r\\s*r\\s*e\\s*a|g\\s*o\\s*n\\s*o\\s*r\\s*r\\s*e\\s*a\\s*s|"
        + "m\\s*a\\s*l\\s*p\\s*a\\s*r\\s*i\\s*d\\s*o|m\\s*a\\s*l\\s*p\\s*a\\s*r\\s*i\\s*d\\s*a|"
        + "m\\s*i\\s*e\\s*r\\s*d\\s*a|"
        + "c\\s*a\\s*r\\s*e\\s*c\\s*h\\s*i\\s*m\\s*b\\s*a|"
        + "m\\s*a\\s*r\\s*i\\s*c\\s*a|m\\s*a\\s*r\\s*i\\s*c\\s*o\\s*n|"
        + "p\\s*e\\s*n\\s*d\\s*e\\s*j\\s*o|"
        + "t\\s*e\\s*t\\s*r\\s*a\\s*n\\s*u\\s*t\\s*r\\s*a"
        + ")\\b",
        Pattern.CASE_INSENSITIVE
    );

    public MessageText {
        if (value == null || value.isBlank()) {
            throw new InvalidMessageTextException("El mensaje no puede estar vacío");
        }
        value = value.trim();
        if (value.length() > MAX_LENGTH) {
            throw new InvalidMessageTextException("El mensaje no puede superar " + MAX_LENGTH + " caracteres");
        }

        value = sanitize(value);
    }

    private static String sanitize(String input) {
        String result = censorEmails(input);
        result = censorPhones(result);
        result = censorSpacedProfanity(result);
        return censorStandardProfanity(result);
    }

    private static String censorEmails(String text) {
        Matcher matcher = EMAIL_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "*".repeat(matcher.group().length()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String censorPhones(String text) {
        Matcher matcher = PHONE_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "*".repeat(matcher.group().length()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String censorSpacedProfanity(String text) {
        Matcher matcher = SPACED_PROFANITY_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "*".repeat(matcher.group().length()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String censorStandardProfanity(String text) {
        Matcher matcher = WORD_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String word = matcher.group();
            String normalizedWord = Normalizer.normalize(word, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "")
                    .toLowerCase(Locale.ROOT);

            if (PROHIBITED_WORDS.contains(normalizedWord)) {
                matcher.appendReplacement(sb, "*".repeat(word.length()));
            } else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(word));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
