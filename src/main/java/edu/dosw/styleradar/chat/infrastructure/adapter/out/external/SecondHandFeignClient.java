package edu.dosw.styleradar.chat.infrastructure.adapter.out.external;

import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto.ExternalListingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Feign client of the second-hand listings API. */
@FeignClient(name = "second-hand", url = "${styleradar.services.second-hand.url}")
public interface SecondHandFeignClient {
    @GetMapping("/api/v1/segunda-mano/{id}")
    ExternalListingResponse getListing(@PathVariable("id") Long id);
}
