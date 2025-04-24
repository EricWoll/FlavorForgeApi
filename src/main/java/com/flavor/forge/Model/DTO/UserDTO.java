package com.flavor.forge.Model.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserDTO {
    private String username;
    private String email;
    private String password;
    private String imageId;
}
