package edu.dosw.styleradar.chat.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.chat.application.dto.request.SendMessageRequest;
import edu.dosw.styleradar.chat.application.dto.response.ConversationResponse;
import edu.dosw.styleradar.chat.application.dto.response.MessageResponse;
import edu.dosw.styleradar.chat.application.mapper.in.MessageDtoInMapper;
import edu.dosw.styleradar.chat.application.mapper.out.ConversationDtoOutMapper;
import edu.dosw.styleradar.chat.application.mapper.out.MessageDtoOutMapper;
import edu.dosw.styleradar.chat.domain.port.in.GetMessagesUseCase;
import edu.dosw.styleradar.chat.domain.port.in.GetMyConversationsUseCase;
import edu.dosw.styleradar.chat.domain.port.in.SendMessageUseCase;
import edu.dosw.styleradar.chat.domain.port.in.StartConversationUseCase;
import edu.dosw.styleradar.chat.infrastructure.adapter.in.rest.doc.ChatApi;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST adapter that exposes the chat use cases. */
@RestController
public class ChatController implements ChatApi {
    private final StartConversationUseCase startConversation;
    private final GetMyConversationsUseCase getMyConversations;
    private final GetMessagesUseCase getMessages;
    private final SendMessageUseCase sendMessage;
    private final MessageDtoInMapper messageIn;
    private final ConversationDtoOutMapper conversationOut;
    private final MessageDtoOutMapper messageOut;

    public ChatController(StartConversationUseCase startConversation,
                          GetMyConversationsUseCase getMyConversations,
                          GetMessagesUseCase getMessages, SendMessageUseCase sendMessage,
                          MessageDtoInMapper messageIn, ConversationDtoOutMapper conversationOut,
                          MessageDtoOutMapper messageOut) {
        this.startConversation = startConversation;
        this.getMyConversations = getMyConversations;
        this.getMessages = getMessages;
        this.sendMessage = sendMessage;
        this.messageIn = messageIn;
        this.conversationOut = conversationOut;
        this.messageOut = messageOut;
    }

    @Override
    public ResponseEntity<ConversationResponse> start(AuthenticatedUser user, Long listingId) {
        return ResponseEntity.ok(conversationOut.toResponse(
                startConversation.start(user.userId(), listingId)));
    }

    @Override
    public ResponseEntity<List<ConversationResponse>> getMyConversations(AuthenticatedUser user) {
        return ResponseEntity.ok(conversationOut.toResponse(getMyConversations.fetch(user.userId())));
    }

    @Override
    public ResponseEntity<List<MessageResponse>> getMessages(AuthenticatedUser user, Long id) {
        return ResponseEntity.ok(messageOut.toResponse(getMessages.fetch(user.userId(), id)));
    }

    @Override
    public ResponseEntity<MessageResponse> sendMessage(AuthenticatedUser user, Long id,
                                                         SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(messageOut.toResponse(
                sendMessage.send(user.userId(), id, messageIn.toText(request))));
    }
}
