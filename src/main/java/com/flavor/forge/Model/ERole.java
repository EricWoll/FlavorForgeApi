package com.flavor.forge.Model;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum ERole {
    FREE("FREE"),
    ANON("ANON");

    private final String role;

    ERole(String role) {
        this.role = role;
    }

    public List<SimpleGrantedAuthority> getAuthorities() {
        return new ArrayList<>(List.of(new SimpleGrantedAuthority("ROLE_" + this.role)));
    }
}
