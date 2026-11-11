package edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.entity;

import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Stores the conversation status using its Spanish database code. */
@Converter
public class ConversationStatusConverter implements AttributeConverter<ConversationStatus, String> {
    @Override
    public String convertToDatabaseColumn(ConversationStatus status) {
        return status == null ? null : status.code();
    }

    @Override
    public ConversationStatus convertToEntityAttribute(String code) {
        return code == null ? null : ConversationStatus.fromCode(code);
    }
}
