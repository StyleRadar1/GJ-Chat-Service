package edu.dosw.styleradar.chat.infrastructure.adapter.out.external;

import edu.dosw.styleradar.chat.domain.exception.ListingNotFoundException;
import edu.dosw.styleradar.chat.domain.exception.ExternalServiceUnavailableException;
import edu.dosw.styleradar.chat.domain.model.ListingStatus;
import edu.dosw.styleradar.chat.domain.model.ListingSummary;
import edu.dosw.styleradar.chat.domain.port.out.ListingQueryPort;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto.ExternalListingResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Reads listings from the second-hand service through Feign. */
@Slf4j
@Component
public class ListingQueryAdapter implements ListingQueryPort {
    private final SecondHandFeignClient client;

    public ListingQueryAdapter(SecondHandFeignClient client) {
        this.client = client;
    }

    @Override
    public ListingSummary fetch(Long listingId) {
        ExternalListingResponse response;
        try {
            response = client.getListing(listingId);
        } catch (FeignException.NotFound e) {
            throw new ListingNotFoundException("No existe la publicación de segunda mano " + listingId);
        } catch (FeignException e) {
            log.error("El servicio de segunda mano respondió {} para la publicación {}", e.status(), listingId);
            throw new ExternalServiceUnavailableException("El servicio de publicaciones no está disponible");
        }
        return toDomain(response, listingId);
    }

    private ListingSummary toDomain(ExternalListingResponse response, Long listingId) {
        if (response == null || response.userId() == null || response.status() == null) {
            throw new ExternalServiceUnavailableException("Respuesta incompleta del servicio de publicaciones");
        }
        ListingStatus status;
        try {
            status = ListingStatus.fromCode(response.status());
        } catch (IllegalArgumentException e) {
            throw new ExternalServiceUnavailableException("Estado de publicación desconocido: " + response.status());
        }
        String name = response.resolvedGarmentName();
        return new ListingSummary(listingId, response.userId(), name, status);
    }
}
