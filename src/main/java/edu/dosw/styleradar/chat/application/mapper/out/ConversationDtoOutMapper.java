package edu.dosw.styleradar.chat.application.mapper.out;

import edu.dosw.styleradar.chat.application.dto.response.ConversationResponse;
import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import org.mapstruct.Mapper;

import java.util.List;

/** Maps conversations to their REST response. */
@Mapper
public interface ConversationDtoOutMapper {
    ConversationResponse toResponse(Conversation conversation);

    List<ConversationResponse> toResponse(List<Conversation> conversations);

    default String toCode(ConversationStatus status) {
        return status.code();
    }
}
