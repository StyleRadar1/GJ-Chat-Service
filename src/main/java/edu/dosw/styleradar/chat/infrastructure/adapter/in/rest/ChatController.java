package edu.dosw.styleradar.chat.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.chat.application.dto.request.SendMessageRequest;
import edu.dosw.styleradar.chat.application.dto.response.ConversationResponse;
import edu.dosw.styleradar.chat.application.dto.response.MessageResponse;
import edu.dosw.styleradar.chat.application.dto.response.PageResponse;
import edu.dosw.styleradar.chat.application.mapper.in.MessageDtoInMapper;
import edu.dosw.styleradar.chat.application.mapper.out.ConversationDtoOutMapper;
import edu.dosw.styleradar.chat.application.mapper.out.MessageDtoOutMapper;
import edu.dosw.styleradar.chat.domain.exception.AccessDeniedDomainException;
import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.port.in.GetMessagesUseCase;
import edu.dosw.styleradar.chat.domain.port.in.GetMyConversationsUseCase;
import edu.dosw.styleradar.chat.domain.port.in.GetStoreConversationsUseCase;
import edu.dosw.styleradar.chat.domain.port.in.SendMessageUseCase;
import edu.dosw.styleradar.chat.domain.port.in.StartConversationUseCase;
import edu.dosw.styleradar.chat.domain.port.in.StartDonationConversationUseCase;
import edu.dosw.styleradar.chat.domain.port.in.StartStoreConversationUseCase;
import edu.dosw.styleradar.chat.domain.port.out.UserDirectoryPort;
import edu.dosw.styleradar.chat.infrastructure.adapter.in.rest.doc.ChatApi;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.metrics.ChatMetrics;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.security.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** REST adapter that exposes chat use cases across second-hand, ally stores, and donations. */
@RestController
@Validated
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
    private final UserDirectoryPort userDirectory;

    @Autowired
    public ChatController(StartConversationUseCase startConversation,
                          StartStoreConversationUseCase startStoreConversation,
                          GetStoreConversationsUseCase getStoreConversations,
                          StartDonationConversationUseCase startDonationConversation,
                          GetMyConversationsUseCase getMyConversations,
                          GetMessagesUseCase getMessages, SendMessageUseCase sendMessage,
                          MessageDtoInMapper messageIn, ConversationDtoOutMapper conversationOut,
                          MessageDtoOutMapper messageOut,
                          ChatMetrics metrics,
                          @Autowired(required = false) UserDirectoryPort userDirectory) {
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
        this.userDirectory = userDirectory;
    }

    /** Compatibility constructor for existing unit tests. */
    public ChatController(StartConversationUseCase startConversation,
                          StartStoreConversationUseCase startStoreConversation,
                          GetStoreConversationsUseCase getStoreConversations,
                          StartDonationConversationUseCase startDonationConversation,
                          GetMyConversationsUseCase getMyConversations,
                          GetMessagesUseCase getMessages, SendMessageUseCase sendMessage,
                          MessageDtoInMapper messageIn, ConversationDtoOutMapper conversationOut,
                          MessageDtoOutMapper messageOut,
                          ChatMetrics metrics) {
        this(startConversation, startStoreConversation, getStoreConversations,
                startDonationConversation, getMyConversations, getMessages, sendMessage,
                messageIn, conversationOut, messageOut, metrics, null);
    }

    private ConversationResponse mapWithCounterpartName(Conversation conversation, Long viewerId) {
        if (conversation == null) {
            return null;
        }
        Long counterpartId = conversationOut.resolveCounterpartUserId(conversation, viewerId);
        String name = null;
        if (counterpartId != null && userDirectory != null) {
            name = userDirectory.findDisplayName(counterpartId).orElse(null);
        }
        return conversationOut.toResponse(conversation, viewerId, name);
    }

    private List<ConversationResponse> mapWithCounterpartNames(List<Conversation> list, Long viewerId) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, String> names = Collections.emptyMap();
        if (userDirectory != null) {
            Set<Long> counterpartIds = list.stream()
                    .map(c -> conversationOut.resolveCounterpartUserId(c, viewerId))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            names = userDirectory.findDisplayNames(counterpartIds);
        }
        return conversationOut.toResponse(list, viewerId, names);
    }

    @Override
    public ResponseEntity<ConversationResponse> start(AuthenticatedUser user, Long publicationId,
                                                       SendMessageRequest request) {
        var conversation = startConversation.start(user.userId(), publicationId, messageIn.toText(request));
        metrics.recordConversationCreated(conversation.getType());
        return ResponseEntity.ok(mapWithCounterpartName(conversation, user.userId()));
    }

    /** Compatibility overload for pre-existing internal tests/callers; REST uses the initial-message request. */
    public ResponseEntity<ConversationResponse> start(AuthenticatedUser user, Long publicationId) {
        var conversation = startConversation.start(user.userId(), publicationId);
        metrics.recordConversationCreated(conversation.getType());
        return ResponseEntity.ok(mapWithCounterpartName(conversation, user.userId()));
    }

    @Override
    public ResponseEntity<ConversationResponse> startStoreConversation(AuthenticatedUser user, String nit,
                                                                       SendMessageRequest request) {
        var conversation = startStoreConversation.start(user.userId(), nit, messageIn.toText(request));
        metrics.recordConversationCreated(conversation.getType());
        return ResponseEntity.ok(mapWithCounterpartName(conversation, user.userId()));
    }

    @Override
    public ResponseEntity<PageResponse<ConversationResponse>> getStoreConversations(AuthenticatedUser user,
                                                                                     int page, int size) {
        var result = getStoreConversations.fetchForStoreAdmin(user.userId(), page, size);
        List<ConversationResponse> items = mapWithCounterpartNames(result.content(), user.userId());
        return ResponseEntity.ok(new PageResponse<>(items, result.page(), result.size(),
                result.totalElements(), result.totalPages()));
    }

    @Override
    public ResponseEntity<PageResponse<ConversationResponse>> getStoreConversationsByNit(AuthenticatedUser user,
                                                                                          String nit,
                                                                                          int page, int size) {
        var result = getStoreConversations.fetchForStore(user.userId(), nit, page, size);
        List<ConversationResponse> items = mapWithCounterpartNames(result.content(), user.userId());
        return ResponseEntity.ok(new PageResponse<>(items, result.page(), result.size(),
                result.totalElements(), result.totalPages()));
    }

    /** Compatibility overload for unit callers; the REST route uses paged results. */
    public ResponseEntity<List<ConversationResponse>> getStoreConversations(AuthenticatedUser user) {
        return ResponseEntity.ok(mapWithCounterpartNames(
                getStoreConversations.fetchForStoreAdmin(user.userId()), user.userId()));
    }

    @Override
    public ResponseEntity<ConversationResponse> startDonationConversation(AuthenticatedUser user, Long donationId) {
        var conversation = startDonationConversation.start(user.userId(), donationId);
        metrics.recordConversationCreated(conversation.getType());
        return ResponseEntity.ok(mapWithCounterpartName(conversation, user.userId()));
    }

    @Override
    public ResponseEntity<PageResponse<ConversationResponse>> getMyConversations(AuthenticatedUser user,
                                                                                  int page, int size) {
        var result = getMyConversations.fetch(user.userId(), page, size);
        List<ConversationResponse> items = mapWithCounterpartNames(result.content(), user.userId());
        return ResponseEntity.ok(new PageResponse<>(items, result.page(), result.size(),
                result.totalElements(), result.totalPages()));
    }

    @Override
    public ResponseEntity<PageResponse<ConversationResponse>> getUserConversations(AuthenticatedUser user,
                                                                                    Long userId,
                                                                                    int page, int size) {
        if (userId != null && !userId.equals(user.userId())) {
            throw new AccessDeniedDomainException("Solo puede consultar sus propias conversaciones");
        }
        return getMyConversations(user, page, size);
    }

    /** Compatibility overload for unit callers; the REST route uses paged results. */
    public ResponseEntity<List<ConversationResponse>> getMyConversations(AuthenticatedUser user) {
        return ResponseEntity.ok(mapWithCounterpartNames(getMyConversations.fetch(user.userId()), user.userId()));
    }

    @Override
    public ResponseEntity<PageResponse<MessageResponse>> getMessages(AuthenticatedUser user, Long id,
                                                                      int page, int size) {
        var result = getMessages.fetch(user.userId(), id, page, size);
        return ResponseEntity.ok(PageResponse.map(result, messageOut::toResponse));
    }

    /** Compatibility overload for unit callers; the REST route uses paged results. */
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
