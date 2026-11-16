package edu.dosw.styleradar.chat.infrastructure.adapter.out.external;

import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto.ExternalDonationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Feign client to interact with ms-donaciones. */
@FeignClient(name = "donations", url = "${styleradar.services.donations.url:http://localhost:8080}")
public interface DonationsFeignClient {
    @GetMapping("/donaciones/{id}")
    ExternalDonationResponse getDonation(@PathVariable("id") Long id);
}
