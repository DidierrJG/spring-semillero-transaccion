package com.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        @Schema(description = "Marca de tiempo del error")
        LocalDateTime timestamp,

        @Schema(description = "Código de estado HTTP", example = "400")
        int status,

        @Schema(description = "Categoría del error", example = "Bad Request")
        String error,

        @Schema(description = "Mensaje descriptivo del error", example = "Debe existir exactamente un integrante con rol de líder")
        String mensaje,

        @Schema(description = "Detalles específicos del fallo")
        List<String> detalles
) {}
