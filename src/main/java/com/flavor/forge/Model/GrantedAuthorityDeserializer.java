package com.flavor.forge.Model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;

public class GrantedAuthorityDeserializer extends JsonDeserializer<SimpleGrantedAuthority> {

    @Override
    public SimpleGrantedAuthority deserialize(JsonParser p, DeserializationContext ct) throws IOException {
        // Parse the JSON text and return a SimpleGrantedAuthority instance
        return new SimpleGrantedAuthority(p.getText());
    }
}