package edu.dosw.styleradar.chat.infrastructure.adapter.out.external;

import edu.dosw.styleradar.chat.domain.exception.ExternalServiceUnavailableException;
import edu.dosw.styleradar.chat.domain.exception.StoreNotFoundException;
import edu.dosw.styleradar.chat.domain.exception.StoreUnavailableException;
import edu.dosw.styleradar.chat.domain.model.StoreSummary;
import edu.dosw.styleradar.chat.domain.port.out.StoreQueryPort;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto.ExternalFoundationResponse;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto.ExternalMyStoreResponse;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto.ExternalStoreResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Consulta almacenes aliados en ms-tiendas mediante Feign. */
@Slf4j
@Component
public class StoreQueryAdapter implements StoreQueryPort {
    private final StoresFeignClient client;

    public StoreQueryAdapter(StoresFeignClient client) {
        this.client = client;
    }

    @Override
    public StoreSummary fetch(String nit) {
        ExternalStoreResponse response;
        try {
            response = client.getStore(nit);
        } catch (FeignException.NotFound e) {
            throw new StoreUnavailableException("El almacén no existe o no está habilitado para chat");
        } catch (FeignException e) {
            log.error("El servicio de tiendas respondió {} para el NIT {}", e.status(), nit);
            throw new ExternalServiceUnavailableException("El servicio de tiendas no está disponible");
        }
        if (response == null || response.nit() == null) {
            throw new ExternalServiceUnavailableException("Respuesta incompleta del servicio de tiendas");
        }
        // El endpoint público de ms-tiendas solo devuelve almacenes aliados habilitados para consulta.
        return new StoreSummary(response.nit(), response.nombreComercial(), true);
    }

    @Override
    public StoreSummary fetchMyStore(Long userId) {
        return fetchMyStore(userId, null);
    }

    @Override
    public StoreSummary fetchMyStore(Long userId, String authorizationHeader) {
        ExternalMyStoreResponse response;
        try {
            response = authorizationHeader == null
                    ? client.getMyStore(userId, userId)
                    : client.getMyStoreWithAuthorization(userId, userId, authorizationHeader);
        } catch (FeignException.NotFound e) {
            throw new StoreNotFoundException("El usuario no administra ningún almacén aliado");
        } catch (FeignException e) {
            log.error("El servicio de tiendas respondió {} para el usuario {}", e.status(), userId);
            throw new ExternalServiceUnavailableException("El servicio de tiendas no está disponible");
        }
        if (response == null || response.nit() == null) {
            throw new ExternalServiceUnavailableException("Respuesta incompleta del servicio de tiendas");
        }
        if (!"APPROVED".equalsIgnoreCase(response.estadoVerificacion())) {
            throw new StoreUnavailableException("El almacén del usuario no está aprobado para operar como aliado");
        }
        return new StoreSummary(response.nit(), response.nombreComercial(), true);
    }

    @Override
    public Long fetchFoundationRepresentativeId(Long foundationId) {
        if (foundationId == null) {
            return null;
        }
        try {
            ExternalFoundationResponse response = client.getFoundation(foundationId, "ROLE_ADMIN_STYLERADAR");
            return response != null ? response.representanteId() : null;
        } catch (FeignException e) {
            log.warn("No se pudo obtener el representante de la fundacion {} desde ms-tiendas: {}",
                    foundationId, e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Error inesperado consultando fundacion {}: {}", foundationId, e.getMessage());
            return null;
        }
    }
}
