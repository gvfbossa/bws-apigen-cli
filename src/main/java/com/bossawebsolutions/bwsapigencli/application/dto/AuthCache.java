package com.bossawebsolutions.bwsapigencli.application.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthCache {
    private String token;
    private long expiry;
    private String machineHash;
    private String email;
}