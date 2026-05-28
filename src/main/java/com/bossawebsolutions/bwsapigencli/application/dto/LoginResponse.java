package com.bossawebsolutions.bwsapigencli.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private long expiresAt;
    private String email;
}