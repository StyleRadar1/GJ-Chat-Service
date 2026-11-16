package edu.dosw.styleradar.chat.infrastructure.adapter.out.external;

import edu.dosw.styleradar.chat.domain.exception.ListingNotFoundException;
import edu.dosw.styleradar.chat.domain.exception.ExternalServiceUnavailableException;
import edu.dosw.styleradar.chat.domain.model.ListingStatus;
import edu.dosw.styleradar.chat.domain.model.ListingSummary;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto.ExternalListingResponse;
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

/** Tests for the listings adapter error translation. */
@ExtendWith(MockitoExtension.class)
class ListingQueryAdapterTest {
    @Mock
    private SecondHandFeignClient client;
    @InjectMocks
    private ListingQueryAdapter adapter;

    @Test
    void translatesTheServiceResponse() {
        when(client.getListing(10L)).thenReturn(new ExternalListingResponse(10L, 2L,
                new ExternalListingResponse.Garment("Chaqueta de jean"), "DISPONIBLE", null));

        ListingSummary summary = adapter.fetch(10L);

        assertThat(summary.id()).isEqualTo(10L);
        assertThat(summary.sellerId()).isEqualTo(2L);
        assertThat(summary.garmentName()).isEqualTo("Chaqueta de jean");
        assertThat(summary.status()).isEqualTo(ListingStatus.AVAILABLE);
    }

    @Test
    void acceptsAListingWithoutGarment() {
        when(client.getListing(10L)).thenReturn(new ExternalListingResponse(10L, 2L, null, "VENDIDA", null));

        assertThat(adapter.fetch(10L).garmentName()).isNull();
    }

    @Test
    void a404IsListingNotFound() {
        when(client.getListing(10L)).thenThrow(error(404));

        assertThatThrownBy(() -> adapter.fetch(10L)).isInstanceOf(ListingNotFoundException.class);
    }

    @Test
    void otherErrorIsServiceUnavailable() {
        when(client.getListing(10L)).thenThrow(error(500));

        assertThatThrownBy(() -> adapter.fetch(10L)).isInstanceOf(ExternalServiceUnavailableException.class);
    }

    @Test
    void anIncompleteResponseIsServiceUnavailable() {
        when(client.getListing(10L)).thenReturn(new ExternalListingResponse(10L, null, null, "DISPONIBLE", null));

        assertThatThrownBy(() -> adapter.fetch(10L)).isInstanceOf(ExternalServiceUnavailableException.class);
    }

    @Test
    void anUnknownStatusIsServiceUnavailable() {
        when(client.getListing(10L)).thenReturn(new ExternalListingResponse(10L, 2L, null, "OTRO", null));

        assertThatThrownBy(() -> adapter.fetch(10L)).isInstanceOf(ExternalServiceUnavailableException.class);
    }

    private static FeignException error(int status) {
        Request request = Request.create(Request.HttpMethod.GET, "http://segunda-mano/api/v1/segunda-mano/10",
                Map.of(), null, StandardCharsets.UTF_8, null);
        Response response = Response.builder().status(status).reason("error").request(request).headers(Map.of()).build();
        return FeignException.errorStatus("SegundaManoFeignClient#obtenerPublicacion", response);
    }
}
