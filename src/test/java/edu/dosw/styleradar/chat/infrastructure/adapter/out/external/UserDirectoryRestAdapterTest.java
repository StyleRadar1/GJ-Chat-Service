package edu.dosw.styleradar.chat.infrastructure.adapter.out.external;

import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto.BatchPublicProfilesRequest;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto.PublicProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDirectoryRestAdapterTest {
    @Mock
    private UsersDirectoryFeignClient client;

    private UserDirectoryRestAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UserDirectoryRestAdapter(client, "test-user", "test-pass");
    }

    @Test
    void findsDisplayNameSuccessfullyAndCaches() {
        when(client.getPublicProfile(7L)).thenReturn(new PublicProfileResponse(7L, "Sofía L."));

        Optional<String> name = adapter.findDisplayName(7L);
        assertThat(name).contains("Sofía L.");

        // Second call should hit the cache without calling client again
        Optional<String> cachedName = adapter.findDisplayName(7L);
        assertThat(cachedName).contains("Sofía L.");
        verify(client).getPublicProfile(7L);
        verifyNoMoreInteractions(client);
    }

    @Test
    void handlesClientExceptionGracefully() {
        when(client.getPublicProfile(99L)).thenThrow(new RuntimeException("Service down"));

        Optional<String> name = adapter.findDisplayName(99L);
        assertThat(name).isEmpty();
    }

    @Test
    void batchRetrievalAndCaching() {
        when(client.getPublicProfiles(any(BatchPublicProfilesRequest.class)))
                .thenReturn(List.of(
                        new PublicProfileResponse(1L, "Laura G."),
                        new PublicProfileResponse(2L, "Carlos M.")
                ));

        Map<Long, String> results = adapter.findDisplayNames(List.of(1L, 2L));
        assertThat(results).containsEntry(1L, "Laura G.").containsEntry(2L, "Carlos M.");

        // Repeating call should use cache
        Map<Long, String> cachedResults = adapter.findDisplayNames(List.of(1L));
        assertThat(cachedResults).containsEntry(1L, "Laura G.");
        verify(client).getPublicProfiles(any(BatchPublicProfilesRequest.class));
        verifyNoMoreInteractions(client);
    }

    @Test
    void handlesBatchExceptionGracefully() {
        when(client.getPublicProfiles(any(BatchPublicProfilesRequest.class)))
                .thenThrow(new RuntimeException("Timeout"));

        Map<Long, String> results = adapter.findDisplayNames(List.of(5L, 6L));
        assertThat(results).isEmpty();
    }
}
