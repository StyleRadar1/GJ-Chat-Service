package edu.dosw.styleradar.chat.infrastructure.adapter.out.external;

import edu.dosw.styleradar.chat.domain.exception.DonationNotFoundException;
import edu.dosw.styleradar.chat.domain.exception.ExternalServiceUnavailableException;
import edu.dosw.styleradar.chat.domain.model.DonationSummary;
import edu.dosw.styleradar.chat.domain.port.out.DonationQueryPort;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto.ExternalDonationResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Reads donation requests from ms-donaciones through Feign. */
@Slf4j
@Component
public class DonationQueryAdapter implements DonationQueryPort {
    private final DonationsFeignClient client;

    public DonationQueryAdapter(DonationsFeignClient client) {
        this.client = client;
    }

    @Override
    public DonationSummary fetch(Long donationId) {
        ExternalDonationResponse response;
        try {
            response = client.getDonation(donationId);
        } catch (FeignException.NotFound e) {
            throw new DonationNotFoundException("No existe la solicitud de donación " + donationId);
        } catch (FeignException e) {
            log.error("El servicio de donaciones respondió {} para la donación {}", e.status(), donationId);
            throw new ExternalServiceUnavailableException("El servicio de donaciones no está disponible");
        }
        if (response == null || response.id() == null || response.donorId() == null) {
            throw new ExternalServiceUnavailableException("Respuesta incompleta del servicio de donaciones");
        }
        return new DonationSummary(response.id(), response.donorId(), response.foundationId(), response.status());
    }
}
