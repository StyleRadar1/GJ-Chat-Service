package edu.dosw.styleradar.chat.infrastructure.adapter.out.external;

import edu.dosw.styleradar.chat.domain.exception.ExternalServiceUnavailableException;
import edu.dosw.styleradar.chat.domain.exception.StoreNotFoundException;
import edu.dosw.styleradar.chat.domain.model.StoreSummary;
import edu.dosw.styleradar.chat.domain.port.out.StoreQueryPort;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto.ExternalMyStoreResponse;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto.ExternalStoreResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Reads ally stores from ms-tiendas through Feign. */
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
            throw new StoreNotFoundException("No existe el almacén aliado con NIT " + nit);
        } catch (FeignException e) {
            log.error("El servicio de tiendas respondió {} para el NIT {}", e.status(), nit);
            throw new ExternalServiceUnavailableException("El servicio de tiendas no está disponible");
        }
        if (response == null || response.nit() == null) {
            throw new ExternalServiceUnavailableException("Respuesta incompleta del servicio de tiendas");
        }
        return new StoreSummary(response.nit(), response.nombreComercial(), true);
    }

    @Override
    public StoreSummary fetchMyStore(Long userId) {
        ExternalMyStoreResponse response;
        try {
            response = client.getMyStore(userId);
        } catch (FeignException.NotFound e) {
            throw new StoreNotFoundException("El usuario no administra ningún almacén aliado");
        } catch (FeignException e) {
            log.error("El servicio de tiendas respondió {} para el usuario {}", e.status(), userId);
            throw new ExternalServiceUnavailableException("El servicio de tiendas no está disponible");
        }
        if (response == null || response.nit() == null) {
            throw new ExternalServiceUnavailableException("Respuesta incompleta del servicio de tiendas");
        }
        boolean verified = "APROBADO".equalsIgnoreCase(response.estadoVerificacion());
        return new StoreSummary(response.nit(), response.nombreComercial(), verified);
    }
}
