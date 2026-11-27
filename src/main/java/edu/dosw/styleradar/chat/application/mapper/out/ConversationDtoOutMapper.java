package edu.dosw.styleradar.chat.application.mapper.out;

import edu.dosw.styleradar.chat.application.dto.response.ConversationResponse;
import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import edu.dosw.styleradar.chat.domain.model.ConversationType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Maps conversations to their REST response with viewer-aware counterpart display names. */
@Mapper
public interface ConversationDtoOutMapper {

    @Mapping(target = "participantDisplayName", expression = "java(resolveParticipantDisplayName(conversation, null, null))")
    ConversationResponse toResponse(Conversation conversation);

    default ConversationResponse toResponse(Conversation conversation, Long viewerId, String resolvedName) {
        if (conversation == null) {
            return null;
        }
        String displayName = resolveParticipantDisplayName(conversation, viewerId, resolvedName);
        return new ConversationResponse(
                conversation.getId(),
                toType(conversation.getType()),
                conversation.getListingId(),
                conversation.getGarmentName(),
                conversation.getBuyerId(),
                conversation.getSellerId(),
                conversation.getStoreNit(),
                conversation.getStoreName(),
                conversation.getDonationId(),
                toCode(conversation.getStatus()),
                conversation.getCreatedAt(),
                displayName
        );
    }

    default List<ConversationResponse> toResponse(List<Conversation> conversations) {
        return toResponse(conversations, null, Collections.emptyMap());
    }

    default List<ConversationResponse> toResponse(List<Conversation> conversations, Long viewerId,
                                                  Map<Long, String> resolvedNames) {
        if (conversations == null) {
            return Collections.emptyList();
        }
        return conversations.stream()
                .map(c -> {
                    Long counterpartId = resolveCounterpartUserId(c, viewerId);
                    String name = counterpartId != null && resolvedNames != null ? resolvedNames.get(counterpartId) : null;
                    return toResponse(c, viewerId, name);
                })
                .toList();
    }

    default Long resolveCounterpartUserId(Conversation conversation, Long viewerId) {
        if (conversation == null) {
            return null;
        }
        if (conversation.getType() == ConversationType.ALMACEN) {
            // For ALMACEN: if viewer is the buyer, there is no single counterpart user (counterpart is the store).
            // If viewer is store owner/admin, counterpart is the buyer.
            if (viewerId != null && viewerId.equals(conversation.getBuyerId())) {
                return null;
            }
            return conversation.getBuyerId();
        }
        if (viewerId != null && viewerId.equals(conversation.getBuyerId())) {
            return conversation.getSellerId();
        }
        if (viewerId != null && viewerId.equals(conversation.getSellerId())) {
            return conversation.getBuyerId();
        }
        // Default when viewer is not specified or third-party: counterpart is seller if buyer views, or buyer
        return conversation.getSellerId() != null ? conversation.getSellerId() : conversation.getBuyerId();
    }

    default String resolveParticipantDisplayName(Conversation conversation, Long viewerId, String resolvedName) {
        if (conversation == null) {
            return null;
        }
        if (conversation.getType() == ConversationType.ALMACEN) {
            // If buyer is viewing the store chat -> show store name
            if (viewerId == null || viewerId.equals(conversation.getBuyerId())) {
                if (conversation.getStoreName() != null && !conversation.getStoreName().isBlank()) {
                    return conversation.getStoreName();
                }
                return conversation.getStoreNit() != null ? "Tienda " + conversation.getStoreNit() : "Tienda aliada";
            }
            // If store admin is viewing -> show buyer name
            if (resolvedName != null && !resolvedName.isBlank()) {
                return resolvedName;
            }
            return "Comprador interesado";
        }

        if (conversation.getType() == ConversationType.DONACION) {
            if (resolvedName != null && !resolvedName.isBlank()) {
                return resolvedName;
            }
            // Safe fallback without exposing user id
            if (viewerId != null && viewerId.equals(conversation.getBuyerId())) {
                return "Fundación receptora";
            }
            return "Persona que dona";
        }

        // Segunda Mano
        if (resolvedName != null && !resolvedName.isBlank()) {
            return resolvedName;
        }
        // Safe generic fallback according to role
        if (viewerId != null && viewerId.equals(conversation.getBuyerId())) {
            return "Vendedor";
        }
        return "Comprador interesado";
    }

    default String toCode(ConversationStatus status) {
        return status != null ? status.code() : null;
    }

    default String toType(ConversationType type) {
        return type != null ? type.name() : ConversationType.SEGUNDA_MANO.name();
    }
}
