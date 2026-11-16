package edu.dosw.styleradar.chat.domain.port.out;

import edu.dosw.styleradar.chat.domain.model.StoreSummary;

/** Output port to query ally store information from ms-tiendas. */
public interface StoreQueryPort {
    StoreSummary fetch(String nit);
    StoreSummary fetchMyStore(Long userId);
}
