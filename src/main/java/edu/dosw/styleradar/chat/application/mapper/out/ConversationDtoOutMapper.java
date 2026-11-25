package edu.dosw.styleradar.chat.application.mapper.out;

import edu.dosw.styleradar.chat.application.dto.response.ConversationResponse;
import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import edu.dosw.styleradar.chat.domain.model.ConversationType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/** Maps conversations to their REST response. */
@Mapper
public interface ConversationDtoOutMapper {
    @Mapping(target = "participantDisplayName", expression = "java(resolveParticipantDisplayName(conversation))")
    ConversationResponse toResponse(Conversation conversation);

    List<ConversationResponse> toResponse(List<Conversation> conversations);

    default String resolveParticipantDisplayName(Conversation conversation) {
        if (conversation == null) {
            return null;
        }
        if (conversation.getType() == ConversationType.ALMACEN) {
            if (conversation.getStoreName() != null && !conversation.getStoreName().isBlank()) {
                return conversation.getStoreName();
            }
            return conversation.getStoreNit() != null ? "Tienda " + conversation.getStoreNit() : "Tienda aliada";
        }
        if (conversation.getType() == ConversationType.DONACION) {
            if (conversation.getDonationId() != null) {
                return "Donación #" + conversation.getDonationId();
            }
            return conversation.getSellerId() != null ? "Usuario #" + conversation.getSellerId() : "Donación";
        }
        if (conversation.getSellerId() != null) {
            return "Usuario #" + conversation.getSellerId();
        }
        return conversation.getBuyerId() != null ? "Usuario #" + conversation.getBuyerId() : null;
    }

    default String toCode(ConversationStatus status) {
        return status != null ? status.code() : null;
    }

    default String toType(ConversationType type) {
        return type != null ? type.name() : ConversationType.SEGUNDA_MANO.name();
    }
}
