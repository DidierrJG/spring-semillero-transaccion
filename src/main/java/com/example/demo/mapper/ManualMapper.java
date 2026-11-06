package com.example.demo.mapper;

import com.example.demo.dto.request.DirectorRequest;
import com.example.demo.dto.request.IntegranteRequest;
import com.example.demo.dto.request.SemilleroRequest;
import com.example.demo.dto.response.SemilleroDetalleResponse;
import com.example.demo.entity.Director;
import com.example.demo.entity.Integrante;
import com.example.demo.entity.Linea;
import com.example.demo.entity.Semillero;

/**
 * Mapper manual ilustrativo requerido por la Sesión 2 de la guía (Pasos A y B)
 * para evidenciar la refactorización hacia MapStruct.
 */
public final class ManualMapper {

    private ManualMapper() {
    }

    public static Director toDirectorEntity(DirectorRequest request) {
        if (request == null) return null;
        Director director = new Director();
        director.setNombre(request.nombre());
        director.setCorreo(request.correo());
        return director;
    }

    public static Integrante toIntegranteEntity(IntegranteRequest request) {
        if (request == null) return null;
        Integrante integrante = new Integrante();
        integrante.setCodigo(request.codigo());
        integrante.setNombre(request.nombre());
        integrante.setPrograma(request.programa());
        integrante.setEsLider(request.esLider());
        return integrante;
    }

    public static Semillero toSemilleroEntity(SemilleroRequest request) {
        if (request == null) return null;
        Semillero semillero = new Semillero();
        semillero.setNombre(request.nombre());
        semillero.setSigla(request.sigla());
        semillero.setGrupoInvestigacion(request.grupoInvestigacion());
        semillero.setMision(request.mision());
        semillero.setVision(request.vision());
        semillero.setObjetivoGeneral(request.objetivoGeneral());
        return semillero;
    }

    public SemilleroDetalleResponse toDetalle(Semillero s) {
        return new SemilleroDetalleResponse(
                s.getId(), s.getNombre(), s.getSigla(), s.getGrupoInvestigacion(),
                s.getMision(), s.getVision(), s.getObjetivoGeneral(),
                /* estado: igual que en SemilleroResponse */ "ACTIVO",
                new SemilleroDetalleResponse.DirectorInfo(
                        s.getDirector().getNombre(), s.getDirector().getCorreo()),
                s.getLineas().stream().map(Linea::getNombre).toList(),
                s.getIntegrantes().stream()
                        .map(i -> new SemilleroDetalleResponse.IntegranteInfo(
                                i.getCodigo(), i.getNombre(), i.getPrograma(), i.isEsLider()))
                        .toList());
    }
}
