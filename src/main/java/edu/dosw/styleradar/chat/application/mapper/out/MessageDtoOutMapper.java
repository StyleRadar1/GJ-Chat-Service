package edu.dosw.styleradar.chat.application.mapper.out;

import edu.dosw.styleradar.chat.application.dto.response.MessageResponse;
import edu.dosw.styleradar.chat.domain.model.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/** Maps messages to their REST response. */
@Mapper
public interface MessageDtoOutMapper {
    @Mapping(target = "text", source = "text.value")
    MessageResponse toResponse(Message message);

    List<MessageResponse> toResponse(List<Message> messages);
}
