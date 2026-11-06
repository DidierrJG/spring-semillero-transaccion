package com.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record DirectorRequest(
        @NotBlank(message = "El nombre del director es obligatorio")
        @Schema(description = "Nombre completo del docente director", example = "Docente Ejemplo")
        String nombre,

        @NotBlank(message = "El correo del director es obligatorio")
        @Email(message = "El correo del director debe ser válido")
        @Schema(description = "Correo institucional del director", example = "docente@universidad.edu")
        String correo
) {}