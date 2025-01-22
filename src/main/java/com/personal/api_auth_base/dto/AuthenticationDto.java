package com.personal.api_auth_base.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticationDto {
    private String accessToken;
    private String refreshToken;
}
