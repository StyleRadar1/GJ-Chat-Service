package edu.dosw.styleradar.chat.infrastructure.adapter.in.amqp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Data of the segunda-mano.prenda.vendida event; JSON names follow the event contract. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ListingSoldData(@JsonProperty("publicacionId") Long listingId,
                              @JsonProperty("vendedorId") Long sellerId,
                              @JsonProperty("compradorId") Long buyerId) {
}
