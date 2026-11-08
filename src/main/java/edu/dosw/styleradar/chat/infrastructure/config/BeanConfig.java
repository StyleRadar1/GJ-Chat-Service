package edu.dosw.styleradar.chat.infrastructure.config;

import edu.dosw.styleradar.chat.application.usecase.CloseConversationsOnSaleUseCaseImpl;
import edu.dosw.styleradar.chat.application.usecase.GetMessagesUseCaseImpl;
import edu.dosw.styleradar.chat.application.usecase.GetMyConversationsUseCaseImpl;
import edu.dosw.styleradar.chat.application.usecase.SendMessageUseCaseImpl;
import edu.dosw.styleradar.chat.application.usecase.StartConversationUseCaseImpl;
import edu.dosw.styleradar.chat.domain.port.in.CloseConversationsOnSaleUseCase;
import edu.dosw.styleradar.chat.domain.port.in.GetMessagesUseCase;
import edu.dosw.styleradar.chat.domain.port.in.GetMyConversationsUseCase;
import edu.dosw.styleradar.chat.domain.port.in.SendMessageUseCase;
import edu.dosw.styleradar.chat.domain.port.in.StartConversationUseCase;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.EventPublisherPort;
import edu.dosw.styleradar.chat.domain.port.out.MessageRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.ProcessedEventPort;
import edu.dosw.styleradar.chat.domain.port.out.ListingQueryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Wires the use cases as Spring beans so the application layer stays framework free. */
@Configuration
public class BeanConfig {
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public StartConversationUseCase startConversationUseCase(ConversationRepositoryPort conversations,
                                                                 ListingQueryPort listings, Clock clock) {
        return new StartConversationUseCaseImpl(conversations, listings, clock);
    }

    @Bean
    public GetMyConversationsUseCase getMyConversationsUseCase(ConversationRepositoryPort conversations) {
        return new GetMyConversationsUseCaseImpl(conversations);
    }

    @Bean
    public GetMessagesUseCase getMessagesUseCase(ConversationRepositoryPort conversations,
                                                             MessageRepositoryPort messages) {
        return new GetMessagesUseCaseImpl(conversations, messages);
    }

    @Bean
    public SendMessageUseCase sendMessageUseCase(ConversationRepositoryPort conversations,
                                                     MessageRepositoryPort messages,
                                                     ListingQueryPort listings, EventPublisherPort events,
                                                     Clock clock) {
        return new SendMessageUseCaseImpl(conversations, messages, listings, events, clock);
    }

    @Bean
    public CloseConversationsOnSaleUseCase closeConversationsOnSaleUseCase(
            ConversationRepositoryPort conversations, ProcessedEventPort processedEvents) {
        return new CloseConversationsOnSaleUseCaseImpl(conversations, processedEvents);
    }
}
