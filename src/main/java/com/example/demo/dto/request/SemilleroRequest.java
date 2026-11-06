package com.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SemilleroRequest(
        @NotBlank(message = "El nombre del semillero es obligatorio")
        @Schema(description = "Nombre del semillero", example = "Semillero de Aplicaciones Web")
        String nombre,

        @NotBlank(message = "La sigla del semillero es obligatoria")
        @Schema(description = "Sigla única del semillero", example = "SAW-01")
        String sigla,

        @NotBlank(message = "El grupo de investigación es obligatorio")
        @Schema(description = "Grupo de investigación asociado", example = "Grupo de Ingeniería de Software")
        String grupoInvestigacion,

        @Schema(description = "Misión del semillero", example = "Promover la investigación aplicada")
        String mision,

        @Schema(description = "Visión del semillero", example = "Desarrollar soluciones universitarias")
        String vision,

        @Schema(description = "Objetivo general", example = "Fortalecer competencias investigativas")
        String objetivoGeneral,

        @NotNull(message = "Los datos del director son obligatorios")
        @Valid
        @Schema(description = "Docente director responsable del semillero")
        DirectorRequest director,

        @NotEmpty(message = "Debe existir al menos una línea de investigación")
        @Schema(description = "Líneas de investigación trabajadas", example = "[\"Aplicaciones Web\", \"Sistemas Distribuidos\"]")
        List<@NotBlank(message = "El nombre de la línea no puede estar vacío") String> lineas,

        @NotEmpty(message = "Debe existir al menos un integrante")
        @Schema(description = "Lista de integrantes del semillero")
        List<@Valid IntegranteRequest> integrantes
) {}
