package edu.dosw.styleradar.chat.infrastructure.adapter.out.external;

import edu.dosw.styleradar.chat.domain.exception.ExternalServiceUnavailableException;
import edu.dosw.styleradar.chat.domain.exception.StoreNotFoundException;
import edu.dosw.styleradar.chat.domain.model.StoreSummary;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto.ExternalMyStoreResponse;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto.ExternalStoreResponse;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/** El almacén está disponible si ms-tiendas responde 200 con datos, y no lo está si hay 404 u otro error. */
@ExtendWith(MockitoExtension.class)
class StoreQueryAdapterTest {
    @Mock
    private StoresFeignClient client;
    @InjectMocks
    private StoreQueryAdapter adapter;

    @Test
    void aSuccessfulResponseMakesTheStoreAvailable() {
        when(client.getStore("900123456-1")).thenReturn(
                new ExternalStoreResponse("900123456-1", "Atelier Urbano", "Ropa", 4.5));

        StoreSummary store = adapter.fetch("900123456-1");

        assertThat(store.nit()).isEqualTo("900123456-1");
        assertThat(store.tradeName()).isEqualTo("Atelier Urbano");
        assertThat(store.isAvailable()).isTrue();
    }

    @Test
    void a404MeansTheStoreDoesNotExist() {
        when(client.getStore("999999-9")).thenThrow(error(404));

        assertThatThrownBy(() -> adapter.fetch("999999-9")).isInstanceOf(StoreNotFoundException.class);
    }

    @Test
    void anotherErrorMeansTheStoreServiceIsUnavailable() {
        when(client.getStore("900123456-1")).thenThrow(error(500));

        assertThatThrownBy(() -> adapter.fetch("900123456-1"))
                .isInstanceOf(ExternalServiceUnavailableException.class);
    }

    @Test
    void aResponseWithoutNitIsUnavailable() {
        when(client.getStore("900123456-1")).thenReturn(new ExternalStoreResponse(null, "Atelier", null, null));

        assertThatThrownBy(() -> adapter.fetch("900123456-1"))
                .isInstanceOf(ExternalServiceUnavailableException.class);
    }

    @Test
    void aNullResponseIsUnavailable() {
        when(client.getStore("900123456-1")).thenReturn(null);

        assertThatThrownBy(() -> adapter.fetch("900123456-1"))
                .isInstanceOf(ExternalServiceUnavailableException.class);
    }

    @Test
    void myStoreIsAvailableWhenTheStoresServiceReturns200() {
        when(client.getMyStore(9L)).thenReturn(new ExternalMyStoreResponse("900123456-1", "Atelier Urbano"));

        StoreSummary store = adapter.fetchMyStore(9L);

        assertThat(store.nit()).isEqualTo("900123456-1");
        assertThat(store.isAvailable()).isTrue();
    }

    @Test
    void aUserWithoutStoreGetsStoreNotFound() {
        when(client.getMyStore(9L)).thenThrow(error(404));

        assertThatThrownBy(() -> adapter.fetchMyStore(9L)).isInstanceOf(StoreNotFoundException.class);
    }

    @Test
    void myStoreFailsWhenTheStoresServiceIsDown() {
        when(client.getMyStore(9L)).thenThrow(error(503));

        assertThatThrownBy(() -> adapter.fetchMyStore(9L))
                .isInstanceOf(ExternalServiceUnavailableException.class);
    }

    @Test
    void myStoreWithoutNitIsUnavailable() {
        when(client.getMyStore(9L)).thenReturn(new ExternalMyStoreResponse(null, "Atelier"));

        assertThatThrownBy(() -> adapter.fetchMyStore(9L))
                .isInstanceOf(ExternalServiceUnavailableException.class);
    }

    private static FeignException error(int status) {
        Request request = Request.create(Request.HttpMethod.GET, "http://tiendas/api/v1/almacenes/900",
                Map.of(), null, StandardCharsets.UTF_8, null);
        Response response = Response.builder().status(status).reason("error").request(request).headers(Map.of()).build();
        return FeignException.errorStatus("StoresFeignClient#getStore", response);
    }
}
