package edu.dosw.styleradar.chat.domain.model;

/** Lifecycle of a conversation; the code is the value stored in the database and exposed in JSON. */
public enum ConversationStatus {
    OPEN("ABIERTA"),
    CLOSED("CERRADA");

    private final String code;

    ConversationStatus(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static ConversationStatus fromCode(String code) {
        for (ConversationStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown conversation status: " + code);
    }
}
