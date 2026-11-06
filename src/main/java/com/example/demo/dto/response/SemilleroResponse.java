package com.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record SemilleroResponse(
        @Schema(description = "Identificador único asignado al semillero", example = "1")
        Long id,

        @Schema(description = "Sigla del semillero", example = "SAW-01")
        String sigla,

        @Schema(description = "Total de integrantes registrados", example = "2")
        int totalIntegrantes,

        @Schema(description = "Estado del registro del semillero", example = "REGISTRADO")
        String estado
) {}
