package com.example.demo.dto.response;

import java.util.List;

public record SemilleroDetalleResponse(
        Long id,
        String nombre,
        String sigla,
        String grupoInvestigacion,
        String mision,
        String vision,
        String objetivoGeneral,
        String estado,
        DirectorInfo director,
        List<String> lineas,
        List<IntegranteInfo> integrantes) {

    public record DirectorInfo(String nombre, String correo) {}

    public record IntegranteInfo(String codigo, String nombre, String programa, boolean esLider) {}
}