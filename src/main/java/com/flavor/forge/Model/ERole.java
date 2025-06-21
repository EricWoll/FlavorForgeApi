package com.flavor.forge.Model;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum ERole {
    ANON("ANON"),
    FREE("FREE"),
    PRO("PRO"),
    PREMIUM("PREMIUM"),
    SYSTEM("SYSTEM");

    private final String role;

    ERole(String role) {
        this.role = role;
    }
/**
 * Returns a list of authorities for the role.
 * The role is prefixed with "ROLE_" to conform with Spring Security's requirements.
 *
 * @return a list containing a SimpleGrantedAuthority for the role
 */
    public List<SimpleGrantedAuthority> getAuthorities() {
        return new ArrayList<>(List.of(new SimpleGrantedAuthority("ROLE_" + this.role)));
    }
}
