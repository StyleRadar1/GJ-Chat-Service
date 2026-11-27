package edu.dosw.styleradar.chat.domain.port.out;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/** Port to query public user display names from the users directory service. */
public interface UserDirectoryPort {
    Optional<String> findDisplayName(Long userId);

    Map<Long, String> findDisplayNames(Collection<Long> userIds);
}
