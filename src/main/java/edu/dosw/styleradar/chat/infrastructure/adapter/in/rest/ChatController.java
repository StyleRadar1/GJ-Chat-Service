package edu.dosw.styleradar.chat.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.chat.application.dto.request.SendMessageRequest;
import edu.dosw.styleradar.chat.application.dto.response.ConversationResponse;
import edu.dosw.styleradar.chat.application.dto.response.MessageResponse;
import edu.dosw.styleradar.chat.application.mapper.in.MessageDtoInMapper;
import edu.dosw.styleradar.chat.application.mapper.out.ConversationDtoOutMapper;
import edu.dosw.styleradar.chat.application.mapper.out.MessageDtoOutMapper;
import edu.dosw.styleradar.chat.domain.port.in.GetMessagesUseCase;
import edu.dosw.styleradar.chat.domain.port.in.GetMyConversationsUseCase;
import edu.dosw.styleradar.chat.domain.port.in.GetStoreConversationsUseCase;
import edu.dosw.styleradar.chat.domain.port.in.SendMessageUseCase;
import edu.dosw.styleradar.chat.domain.port.in.StartConversationUseCase;
import edu.dosw.styleradar.chat.domain.port.in.StartDonationConversationUseCase;
import edu.dosw.styleradar.chat.domain.port.in.StartStoreConversationUseCase;
import edu.dosw.styleradar.chat.infrastructure.adapter.in.rest.doc.ChatApi;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.metrics.ChatMetrics;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST adapter that exposes chat use cases across second-hand, ally stores, and donations. */
@RestController
public class ChatController implements ChatApi {
    private final StartConversationUseCase startConversation;
    private final StartStoreConversationUseCase startStoreConversation;
    private final GetStoreConversationsUseCase getStoreConversations;
    private final StartDonationConversationUseCase startDonationConversation;
    private final GetMyConversationsUseCase getMyConversations;
    private final GetMessagesUseCase getMessages;
    private final SendMessageUseCase sendMessage;
    private final MessageDtoInMapper messageIn;
    private final ConversationDtoOutMapper conversationOut;
    private final MessageDtoOutMapper messageOut;
    private final ChatMetrics metrics;

    public ChatController(StartConversationUseCase startConversation,
                          StartStoreConversationUseCase startStoreConversation,
                          GetStoreConversationsUseCase getStoreConversations,
                          StartDonationConversationUseCase startDonationConversation,
                          GetMyConversationsUseCase getMyConversations,
                          GetMessagesUseCase getMessages, SendMessageUseCase sendMessage,
                          MessageDtoInMapper messageIn, ConversationDtoOutMapper conversationOut,
                          MessageDtoOutMapper messageOut,
                          ChatMetrics metrics) {
        this.startConversation = startConversation;
        this.startStoreConversation = startStoreConversation;
        this.getStoreConversations = getStoreConversations;
        this.startDonationConversation = startDonationConversation;
        this.getMyConversations = getMyConversations;
        this.getMessages = getMessages;
        this.sendMessage = sendMessage;
        this.messageIn = messageIn;
        this.conversationOut = conversationOut;
        this.messageOut = messageOut;
        this.metrics = metrics;
    }

    @Override
    public ResponseEntity<ConversationResponse> start(AuthenticatedUser user, Long listingId) {
        var conversation = startConversation.start(user.userId(), listingId);
        metrics.recordConversationCreated(conversation.getType());
        return ResponseEntity.ok(conversationOut.toResponse(conversation));
    }

    @Override
    public ResponseEntity<ConversationResponse> startStoreConversation(AuthenticatedUser user, String nit,
                                                                       SendMessageRequest request) {
        var conversation = startStoreConversation.start(user.userId(), nit, messageIn.toText(request));
        metrics.recordConversationCreated(conversation.getType());
        return ResponseEntity.ok(conversationOut.toResponse(conversation));
    }

    @Override
    public ResponseEntity<List<ConversationResponse>> getStoreConversations(AuthenticatedUser user) {
        return ResponseEntity.ok(conversationOut.toResponse(
                getStoreConversations.fetchForStoreAdmin(user.userId())));
    }

    @Override
    public ResponseEntity<ConversationResponse> startDonationConversation(AuthenticatedUser user, Long donationId) {
        var conversation = startDonationConversation.start(user.userId(), donationId);
        metrics.recordConversationCreated(conversation.getType());
        return ResponseEntity.ok(conversationOut.toResponse(conversation));
    }

    @Override
    public ResponseEntity<List<ConversationResponse>> getMyConversations(AuthenticatedUser user) {
        return ResponseEntity.ok(conversationOut.toResponse(getMyConversations.fetch(user.userId())));
    }

    @Override
    public ResponseEntity<List<ConversationResponse>> getUserConversations(AuthenticatedUser user, Long id) {
        return ResponseEntity.ok(conversationOut.toResponse(getMyConversations.fetchOwn(user.userId(), id)));
    }

    @Override
    public ResponseEntity<List<MessageResponse>> getMessages(AuthenticatedUser user, Long id) {
        return ResponseEntity.ok(messageOut.toResponse(getMessages.fetch(user.userId(), id)));
    }

    @Override
    public ResponseEntity<MessageResponse> sendMessage(AuthenticatedUser user, Long id,
                                                       SendMessageRequest request) {
        long start = System.currentTimeMillis();
        var message = sendMessage.send(user.userId(), id, messageIn.toText(request));
        metrics.recordMessageLatency(System.currentTimeMillis() - start);
        metrics.recordMessageSent(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(messageOut.toResponse(message));
    }
}
