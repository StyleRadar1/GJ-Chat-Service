package edu.dosw.styleradar.chat.infrastructure.config;

import edu.dosw.styleradar.chat.application.usecase.CloseConversationsOnSaleUseCaseImpl;
import edu.dosw.styleradar.chat.application.usecase.GetMessagesUseCaseImpl;
import edu.dosw.styleradar.chat.application.usecase.GetMyConversationsUseCaseImpl;
import edu.dosw.styleradar.chat.application.usecase.GetStoreConversationsUseCaseImpl;
import edu.dosw.styleradar.chat.application.usecase.SendMessageUseCaseImpl;
import edu.dosw.styleradar.chat.application.usecase.StartConversationUseCaseImpl;
import edu.dosw.styleradar.chat.application.usecase.StartDonationConversationUseCaseImpl;
import edu.dosw.styleradar.chat.application.usecase.StartStoreConversationUseCaseImpl;
import edu.dosw.styleradar.chat.domain.port.in.CloseConversationsOnSaleUseCase;
import edu.dosw.styleradar.chat.domain.port.in.GetMessagesUseCase;
import edu.dosw.styleradar.chat.domain.port.in.GetMyConversationsUseCase;
import edu.dosw.styleradar.chat.domain.port.in.GetStoreConversationsUseCase;
import edu.dosw.styleradar.chat.domain.port.in.SendMessageUseCase;
import edu.dosw.styleradar.chat.domain.port.in.StartConversationUseCase;
import edu.dosw.styleradar.chat.domain.port.in.StartDonationConversationUseCase;
import edu.dosw.styleradar.chat.domain.port.in.StartStoreConversationUseCase;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.DonationQueryPort;
import edu.dosw.styleradar.chat.domain.port.out.EventPublisherPort;
import edu.dosw.styleradar.chat.domain.port.out.ListingQueryPort;
import edu.dosw.styleradar.chat.domain.port.out.MessageRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.ProcessedEventPort;
import edu.dosw.styleradar.chat.domain.port.out.StoreQueryPort;
import edu.dosw.styleradar.chat.application.usecase.RateLimitedSendMessageUseCase;
import edu.dosw.styleradar.chat.domain.port.out.MessageRateLimiterPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
                                                             ListingQueryPort listings, Clock clock,
                                                             SendMessageUseCase sendMessage) {
        return new StartConversationUseCaseImpl(conversations, listings, clock, sendMessage);
    }

    /** Backwards-compatible factory used by existing configuration tests. */
    public StartConversationUseCase startConversationUseCase(ConversationRepositoryPort conversations,
                                                             ListingQueryPort listings, Clock clock) {
        return new StartConversationUseCaseImpl(conversations, listings, clock);
    }

    @Bean
    public StartStoreConversationUseCase startStoreConversationUseCase(ConversationRepositoryPort conversations,
                                                                       StoreQueryPort stores,
                                                                       SendMessageUseCase sendMessage,
                                                                       Clock clock) {
        return new StartStoreConversationUseCaseImpl(conversations, stores, sendMessage, clock);
    }

    @Bean
    public StartDonationConversationUseCase startDonationConversationUseCase(ConversationRepositoryPort conversations,
                                                                             DonationQueryPort donations, Clock clock) {
        return new StartDonationConversationUseCaseImpl(conversations, donations, clock);
    }

    @Bean
    public GetStoreConversationsUseCase getStoreConversationsUseCase(ConversationRepositoryPort conversations,
                                                                     StoreQueryPort stores) {
        return new GetStoreConversationsUseCaseImpl(conversations, stores);
    }

    @Bean
    public GetMyConversationsUseCase getMyConversationsUseCase(ConversationRepositoryPort conversations) {
        return new GetMyConversationsUseCaseImpl(conversations);
    }

    @Bean
    public GetMessagesUseCase getMessagesUseCase(ConversationRepositoryPort conversations,
                                                 MessageRepositoryPort messages,
                                                 StoreQueryPort stores) {
        return new GetMessagesUseCaseImpl(conversations, messages, stores);
    }

    @Bean
    public SendMessageUseCase sendMessageUseCase(ConversationRepositoryPort conversations,
                                                 MessageRepositoryPort messages,
                                                 ListingQueryPort listings,
                                                 StoreQueryPort stores,
                                                 EventPublisherPort events,
                                                 Clock clock) {
        return new SendMessageUseCaseImpl(conversations, messages, listings, stores, events, clock);
    }

    @Bean
    @Primary
    public SendMessageUseCase rateLimitedSendMessageUseCase(
            @Qualifier("sendMessageUseCase") SendMessageUseCase delegate, MessageRateLimiterPort rateLimiter) {
        return new RateLimitedSendMessageUseCase(delegate, rateLimiter);
    }

    @Bean
    public CloseConversationsOnSaleUseCase closeConversationsOnSaleUseCase(
            ConversationRepositoryPort conversations, ProcessedEventPort processedEvents) {
        return new CloseConversationsOnSaleUseCaseImpl(conversations, processedEvents);
    }
}
