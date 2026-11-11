package com.bytestorm.waygo.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @Email(message = "O e-mail deve ser válido")
    @NotBlank(message = "O e-mail é obrigatório")
    String email,
    
    @NotBlank(message = "A senha é obrigatória")
    String senha
) {

}
