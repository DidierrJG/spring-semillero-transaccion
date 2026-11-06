package com.example.demo.service;

import com.example.demo.dto.response.SemilleroDetalleResponse;
import com.example.demo.exception.SemilleroNoEncontradoException;
import com.example.demo.dto.request.IntegranteRequest;
import com.example.demo.dto.request.SemilleroRequest;
import com.example.demo.dto.response.SemilleroResponse;
import com.example.demo.entity.Director;
import com.example.demo.entity.Integrante;
import com.example.demo.entity.Linea;
import com.example.demo.entity.Semillero;
import com.example.demo.exception.ReglaNegocioException;
import com.example.demo.exception.SiglaDuplicadaException;
import com.example.demo.mapper.DirectorMapper;
import com.example.demo.mapper.IntegranteMapper;
import com.example.demo.mapper.SemilleroMapper;
import com.example.demo.repository.DirectorRepository;
import com.example.demo.repository.IntegranteRepository;
import com.example.demo.repository.LineaRepository;
import com.example.demo.repository.SemilleroRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SemilleroService {

    private final DirectorRepository directorRepository;
    private final SemilleroRepository semilleroRepository;
    private final LineaRepository lineaRepository;
    private final IntegranteRepository integranteRepository;

    private final DirectorMapper directorMapper;
    private final IntegranteMapper integranteMapper;
    private final SemilleroMapper semilleroMapper;

    // Bandera para pruebas pedagógicas de rollback (T7)
    private boolean simularFalloPostDirector = false;

    public SemilleroService(
            DirectorRepository directorRepository,
            SemilleroRepository semilleroRepository,
            LineaRepository lineaRepository,
            IntegranteRepository integranteRepository,
            DirectorMapper directorMapper,
            IntegranteMapper integranteMapper,
            SemilleroMapper semilleroMapper) {
        this.directorRepository = directorRepository;
        this.semilleroRepository = semilleroRepository;
        this.lineaRepository = lineaRepository;
        this.integranteRepository = integranteRepository;
        this.directorMapper = directorMapper;
        this.integranteMapper = integranteMapper;
        this.semilleroMapper = semilleroMapper;
    }

    public void setSimularFalloPostDirector(boolean simularFalloPostDirector) {
        this.simularFalloPostDirector = simularFalloPostDirector;
    }

    public boolean isSimularFalloPostDirector() {
        return simularFalloPostDirector;
    }

    @Transactional
    public SemilleroResponse crear(SemilleroRequest request) {
        // 1. R1: Validar que la sigla no exista previamente
        if (semilleroRepository.existsBySigla(request.sigla())) {
            throw new SiglaDuplicadaException("Ya existe un semillero registrado con la sigla: " + request.sigla());
        }

        // 2. R3: Validar que exista al menos una línea
        if (request.lineas() == null || request.lineas().isEmpty()) {
            throw new ReglaNegocioException("Debe existir al menos una línea de investigación");
        }

        // 3. R4: Validar que exista al menos un integrante
        List<IntegranteRequest> integrantesReq = request.integrantes();
        if (integrantesReq == null || integrantesReq.isEmpty()) {
            throw new ReglaNegocioException("Debe existir al menos un integrante");
        }

        // 4. R6: Verificar que haya exactamente un líder
        long totalLideres = integrantesReq.stream().filter(IntegranteRequest::esLider).count();
        if (totalLideres != 1) {
            throw new ReglaNegocioException("Debe existir exactamente un integrante con rol de líder. Líderes encontrados: " + totalLideres);
        }

        // 5. R5: Verificar que no se repitan códigos dentro del semillero
        Set<String> codigos = new HashSet<>();
        for (IntegranteRequest integranteReq : integrantesReq) {
            if (!codigos.add(integranteReq.codigo().trim())) {
                throw new ReglaNegocioException("No se permiten códigos estudiantiles repetidos en el semillero: " + integranteReq.codigo());
            }
        }

        // 6. Guardar o reutilizar el director
        Director director = directorRepository.findByCorreo(request.director().correo())
                .orElseGet(() -> directorRepository.save(directorMapper.toEntity(request.director())));

        // Simulación pedagógica de fallo post-director para verificar Rollback (T7)
        if (simularFalloPostDirector) {
            throw new RuntimeException("Fallo simulado después de guardar el director para verificar rollback (T7)");
        }

        // 7. Resolver o crear las líneas de investigación
        Set<Linea> lineas = new HashSet<>();
        for (String nombreLinea : request.lineas()) {
            String limpia = nombreLinea.trim();
            if (!limpia.isEmpty()) {
                Linea linea = lineaRepository.findByNombre(limpia)
                        .orElseGet(() -> lineaRepository.save(Linea.builder().nombre(limpia).build()));
                lineas.add(linea);
            }
        }

        // 8. Crear y guardar el semillero
        Semillero semillero = semilleroMapper.toEntity(request);
        semillero.setDirector(director);
        semillero.setLineas(lineas);

        // 9. Asociar los integrantes al semillero
        for (IntegranteRequest ir : integrantesReq) {
            Integrante integrante = integranteMapper.toEntity(ir);
            semillero.addIntegrante(integrante);
        }

        Semillero guardado = semilleroRepository.save(semillero);

        // 10. Retornar DTO de respuesta
        return new SemilleroResponse(
                guardado.getId(),
                guardado.getSigla(),
                guardado.getIntegrantes().size(),
                "REGISTRADO"
        );
    }
    
    @Transactional(readOnly = true)
    public List<SemilleroResponse> listar() {
        return semilleroRepository.findAll()
                .stream()
                .map(semilleroMapper::toResponse)
                .toList();
    }
    
    @Transactional
    public void eliminarSemillero(Long id) {

        if (!semilleroRepository.existsById(id)) {
            throw new ReglaNegocioException("El semillero no existe");
        }

        semilleroRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public SemilleroDetalleResponse obtenerDetalle(Long id) {
        Semillero s = semilleroRepository.findById(id)
                .orElseThrow(() -> new SemilleroNoEncontradoException(id));

        return new SemilleroDetalleResponse(
                s.getId(),
                s.getNombre(),
                s.getSigla(),
                s.getGrupoInvestigacion(),
                s.getMision(),
                s.getVision(),
                s.getObjetivoGeneral(),
                semilleroMapper.toResponse(s).estado(),
                new SemilleroDetalleResponse.DirectorInfo(
                        s.getDirector().getNombre(),
                        s.getDirector().getCorreo()),
                s.getLineas().stream().map(Linea::getNombre).sorted().toList(),
                s.getIntegrantes().stream()
                        .map(i -> new SemilleroDetalleResponse.IntegranteInfo(
                                i.getCodigo(), i.getNombre(), i.getPrograma(), i.isEsLider()))
                        .toList());
    }
}
