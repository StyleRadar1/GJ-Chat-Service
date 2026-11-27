package edu.dosw.styleradar.chat.infrastructure.adapter.out.external;

import edu.dosw.styleradar.chat.domain.port.out.UserDirectoryPort;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto.BatchPublicProfilesRequest;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto.PublicProfileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter that queries user display names from ms-usuarios internal endpoint.
 * Tolerant to external failures and caches resolved names in-memory.
 */
@Slf4j
@Component
public class UserDirectoryRestAdapter implements UserDirectoryPort {
    private final UsersDirectoryFeignClient client;
    private final String basicAuthHeader;
    private final Map<Long, String> cache = new ConcurrentHashMap<>();

    public UserDirectoryRestAdapter(
            UsersDirectoryFeignClient client,
            @Value("${styleradar.services.users.username:service-chat}") String username,
            @Value("${styleradar.services.users.password:service-secret}") String password) {
        this.client = client;
        String credentials = username + ":" + password;
        this.basicAuthHeader = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Optional<String> findDisplayName(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        if (cache.containsKey(userId)) {
            return Optional.ofNullable(cache.get(userId));
        }
        try {
            PublicProfileResponse profile = client.getPublicProfile(userId);
            if (profile != null && profile.displayName() != null && !profile.displayName().isBlank()) {
                cache.put(userId, profile.displayName());
                return Optional.of(profile.displayName());
            }
        } catch (Exception e) {
            log.warn("Could not retrieve public profile for userId: {}", userId, e);
        }
        return Optional.empty();
    }

    @Override
    public Map<Long, String> findDisplayNames(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, String> result = new HashMap<>();
        List<Long> missingIds = userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .filter(id -> {
                    if (cache.containsKey(id)) {
                        result.put(id, cache.get(id));
                        return false;
                    }
                    return true;
                })
                .toList();

        if (missingIds.isEmpty()) {
            return result;
        }

        try {
            List<PublicProfileResponse> profiles = client.getPublicProfiles(new BatchPublicProfilesRequest(missingIds));
            if (profiles != null) {
                for (PublicProfileResponse profile : profiles) {
                    if (profile != null && profile.id() != null && profile.displayName() != null) {
                        cache.put(profile.id(), profile.displayName());
                        result.put(profile.id(), profile.displayName());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not batch retrieve public profiles for {} users", missingIds.size(), e);
        }
        return result;
    }
}
