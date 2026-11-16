package edu.dosw.styleradar.chat.infrastructure.adapter.out.external;

import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto.ExternalMyStoreResponse;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto.ExternalStoreResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/** Feign client to interact with ms-tiendas. */
@FeignClient(name = "stores", url = "${styleradar.services.stores.url:http://localhost:8330}")
public interface StoresFeignClient {
    @GetMapping("/api/v1/almacenes/{nit}")
    ExternalStoreResponse getStore(@PathVariable("nit") String nit);

    @GetMapping("/api/v1/almacenes/mi-almacen")
    ExternalMyStoreResponse getMyStore(@RequestHeader("X-User-Id") Long userId);
}
