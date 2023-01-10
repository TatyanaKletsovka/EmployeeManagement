package com.syberry.bakery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto implements AuthResponse{
    private String cookie;
    private String refreshCookie;
    private UserDto userDto;
}
