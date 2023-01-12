package com.syberry.bakery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationDto {
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;
    @NotNull
    private Integer code;
}
