package edu.dosw.styleradar.chat.infrastructure.adapter.out.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Principal;
import java.util.List;
import java.util.Set;

/** Identity and roles of the caller, extracted from a validated JWT. */
public record AuthenticatedUser(Long userId, Set<String> roles, String authorizationHeader,
                                String storeNit) implements Principal {
    public AuthenticatedUser(Long userId, Set<String> roles) {
        this(userId, roles, null, null);
    }

    public AuthenticatedUser withStoreNit(String nit) {
        return new AuthenticatedUser(userId, roles, authorizationHeader, nit);
    }

    @Override
    public String getName() {
        return userId.toString();
    }

    public List<GrantedAuthority> authorities() {
        return roles.stream().sorted().<GrantedAuthority>map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();
    }
}
