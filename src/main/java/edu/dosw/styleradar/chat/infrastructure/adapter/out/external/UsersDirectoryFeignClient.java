package edu.dosw.styleradar.chat.infrastructure.adapter.out.external;

import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto.BatchPublicProfilesRequest;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto.PublicProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/** Feign client of the internal users directory API. */
@FeignClient(name = "users-directory", url = "${styleradar.services.users.url:http://localhost:8080}",
        configuration = edu.dosw.styleradar.chat.infrastructure.config.UsersDirectoryFeignConfig.class)
public interface UsersDirectoryFeignClient {
    @GetMapping("/api/v1/internal/users/{userId}/public-profile")
    PublicProfileResponse getPublicProfile(@PathVariable("userId") Long userId);

    @PostMapping("/api/v1/internal/users/public-profiles")
    List<PublicProfileResponse> getPublicProfiles(@RequestBody BatchPublicProfilesRequest request);
}
