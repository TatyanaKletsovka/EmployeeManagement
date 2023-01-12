package com.syberry.bakery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginWith2faDto implements AuthResponse{
    private String message;
}
