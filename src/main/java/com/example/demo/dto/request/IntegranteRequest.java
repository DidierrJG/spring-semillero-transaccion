package com.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record IntegranteRequest(
        @NotBlank(message = "El código del estudiante es obligatorio")
        @Schema(description = "Código estudiantil", example = "2026001")
        String codigo,

        @NotBlank(message = "El nombre del estudiante es obligatorio")
        @Schema(description = "Nombre del integrante", example = "Ana Ejemplo")
        String nombre,

        @NotBlank(message = "El programa académico es obligatorio")
        @Schema(description = "Programa académico del integrante", example = "Ingeniería de Sistemas")
        String programa,

        @Schema(description = "Indica si el integrante es líder del semillero (debe haber exactamente uno)", example = "true")
        boolean esLider
) {}
