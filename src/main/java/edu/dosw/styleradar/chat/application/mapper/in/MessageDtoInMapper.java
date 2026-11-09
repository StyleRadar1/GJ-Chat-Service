package edu.dosw.styleradar.chat.application.mapper.in;

import edu.dosw.styleradar.chat.application.dto.request.SendMessageRequest;
import edu.dosw.styleradar.chat.domain.model.MessageText;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps the send request to the message text value object. */
@Mapper
public interface MessageDtoInMapper {
    @Mapping(target = "value", source = "text")
    MessageText toText(SendMessageRequest request);
}
