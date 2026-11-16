package edu.dosw.styleradar.chat.domain.exception;

/** Raised when a store ally cannot be found in ms-tiendas. */
public class StoreNotFoundException extends ChatException {
    public StoreNotFoundException(String message) {
        super(message);
    }
}
