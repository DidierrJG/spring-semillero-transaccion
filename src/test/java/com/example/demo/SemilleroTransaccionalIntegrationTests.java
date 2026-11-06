package com.example.demo;

import com.example.demo.repository.DirectorRepository;
import com.example.demo.repository.IntegranteRepository;
import com.example.demo.repository.LineaRepository;
import com.example.demo.repository.SemilleroRepository;
import com.example.demo.service.SemilleroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SemilleroTransaccionalIntegrationTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SemilleroService semilleroService;

    @Autowired
    private DirectorRepository directorRepository;

    @Autowired
    private SemilleroRepository semilleroRepository;

    @Autowired
    private LineaRepository lineaRepository;

    @Autowired
    private IntegranteRepository integranteRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        semilleroService.setSimularFalloPostDirector(false);
        semilleroRepository.findBySigla("SAW-01").ifPresent(semilleroRepository::delete);
        semilleroRepository.findBySigla("SRT-99").ifPresent(semilleroRepository::delete);
        directorRepository.findByCorreo("docente@universidad.edu").ifPresent(directorRepository::delete);
        directorRepository.findByCorreo("docente_huerfano_t7@universidad.edu").ifPresent(directorRepository::delete);
    }

    @Test
    @Order(1)
    @DisplayName("T1: JSON válido -> 201 Created y todos los registros asociados persistidos")
    void t1_registroValido_retorna201YPersisteTodo() throws Exception {
        String jsonValido = """
                {
                  "nombre": "Semillero de Aplicaciones Web",
                  "sigla": "SAW-01",
                  "grupoInvestigacion": "Grupo de Ingeniería de Software",
                  "mision": "Promover la investigación aplicada",
                  "vision": "Desarrollar soluciones universitarias",
                  "objetivoGeneral": "Fortalecer competencias investigativas",
                  "director": {
                    "nombre": "Docente Ejemplo",
                    "correo": "docente@universidad.edu"
                  },
                  "lineas": ["Aplicaciones Web", "Sistemas Distribuidos"],
                  "integrantes": [
                    { "codigo": "2026001", "nombre": "Ana Ejemplo",  "programa": "Ingeniería de Sistemas", "esLider": true },
                    { "codigo": "2026002", "nombre": "Luis Ejemplo", "programa": "Ingeniería de Sistemas", "esLider": false }
                  ]
                }
                """;

        mockMvc.perform(post("/api/semilleros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.sigla").value("SAW-01"))
                .andExpect(jsonPath("$.totalIntegrantes").value(2))
                .andExpect(jsonPath("$.estado").value("REGISTRADO"));

        assertThat(semilleroRepository.existsBySigla("SAW-01")).isTrue();
        assertThat(directorRepository.findByCorreo("docente@universidad.edu")).isPresent();
        assertThat(lineaRepository.findByNombre("Aplicaciones Web")).isPresent();
        assertThat(lineaRepository.findByNombre("Sistemas Distribuidos")).isPresent();
    }

    @Test
    @Order(2)
    @DisplayName("T2: Sigla duplicada -> 409 Conflict y ninguna nueva fila")
    void t2_siglaDuplicada_retorna409YSinEscriturasNuevas() throws Exception {
        if (!semilleroRepository.existsBySigla("SAW-01")) {
            t1_registroValido_retorna201YPersisteTodo();
        }

        long conteoSemillerosAntes = semilleroRepository.count();
        long conteoDirectoresAntes = directorRepository.count();

        String jsonSiglaRepetida = """
                {
                  "nombre": "Semillero Duplicado",
                  "sigla": "SAW-01",
                  "grupoInvestigacion": "Grupo de IA",
                  "mision": "Otra mision",
                  "vision": "Otra vision",
                  "objetivoGeneral": "Otro objetivo",
                  "director": {
                    "nombre": "Otro Docente",
                    "correo": "otrodocente@universidad.edu"
                  },
                  "lineas": ["Inteligencia Artificial"],
                  "integrantes": [
                    { "codigo": "2026003", "nombre": "Carlos Ejemplo", "programa": "Ingeniería de Sistemas", "esLider": true }
                  ]
                }
                """;

        mockMvc.perform(post("/api/semilleros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSiglaRepetida))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        assertThat(semilleroRepository.count()).isEqualTo(conteoSemillerosAntes);
        assertThat(directorRepository.count()).isEqualTo(conteoDirectoresAntes);
    }

    @Test
    @Order(3)
    @DisplayName("T3: Sin integrantes -> 400 Bad Request y ninguna escritura")
    void t3_sinIntegrantes_retorna400YNingunaEscritura() throws Exception {
        long conteoSemillerosAntes = semilleroRepository.count();

        String jsonSinIntegrantes = """
                {
                  "nombre": "Semillero Sin Integrantes",
                  "sigla": "SSI-01",
                  "grupoInvestigacion": "Grupo Redes",
                  "director": {
                    "nombre": "Docente Redes",
                    "correo": "redes@universidad.edu"
                  },
                  "lineas": ["Redes"],
                  "integrantes": []
                }
                """;

        mockMvc.perform(post("/api/semilleros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSinIntegrantes))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        assertThat(semilleroRepository.count()).isEqualTo(conteoSemillerosAntes);
        assertThat(semilleroRepository.existsBySigla("SSI-01")).isFalse();
    }

    @Test
    @Order(4)
    @DisplayName("T4: Dos líderes -> 400 Bad Request y ninguna escritura")
    void t4_dosLideres_retorna400YNingunaEscritura() throws Exception {
        long conteoSemillerosAntes = semilleroRepository.count();

        String jsonDosLideres = """
                {
                  "nombre": "Semillero Dos Lideres",
                  "sigla": "SDL-01",
                  "grupoInvestigacion": "Grupo Móviles",
                  "director": {
                    "nombre": "Docente Movil",
                    "correo": "movil@universidad.edu"
                  },
                  "lineas": ["Desarrollo Móvil"],
                  "integrantes": [
                    { "codigo": "2026010", "nombre": "Pedro Uno", "programa": "Sistemas", "esLider": true },
                    { "codigo": "2026011", "nombre": "Maria Dos", "programa": "Sistemas", "esLider": true }
                  ]
                }
                """;

        mockMvc.perform(post("/api/semilleros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonDosLideres))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("líder")));

        assertThat(semilleroRepository.count()).isEqualTo(conteoSemillerosAntes);
        assertThat(semilleroRepository.existsBySigla("SDL-01")).isFalse();
    }

    @Test
    @Order(5)
    @DisplayName("T5: Código repetido dentro del semillero -> 400 Bad Request y ninguna escritura")
    void t5_codigoRepetido_retorna400YNingunaEscritura() throws Exception {
        long conteoSemillerosAntes = semilleroRepository.count();

        String jsonCodigoRepetido = """
                {
                  "nombre": "Semillero Codigos Repetidos",
                  "sigla": "SCR-01",
                  "grupoInvestigacion": "Grupo Datos",
                  "director": {
                    "nombre": "Docente Datos",
                    "correo": "datos@universidad.edu"
                  },
                  "lineas": ["Ciencia de Datos"],
                  "integrantes": [
                    { "codigo": "2026999", "nombre": "Estudiante A", "programa": "Sistemas", "esLider": true },
                    { "codigo": "2026999", "nombre": "Estudiante B", "programa": "Sistemas", "esLider": false }
                  ]
                }
                """;

        mockMvc.perform(post("/api/semilleros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCodigoRepetido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("repetidos")));

        assertThat(semilleroRepository.count()).isEqualTo(conteoSemillerosAntes);
        assertThat(semilleroRepository.existsBySigla("SCR-01")).isFalse();
    }

    @Test
    @Order(6)
    @DisplayName("T6: Correo inválido -> 400 Bad Request en validación del DTO")
    void t6_correoInvalido_retorna400() throws Exception {
        long conteoSemillerosAntes = semilleroRepository.count();

        String jsonCorreoInvalido = """
                {
                  "nombre": "Semillero Correo Mal",
                  "sigla": "SCM-01",
                  "grupoInvestigacion": "Grupo Seguridad",
                  "director": {
                    "nombre": "Docente Seguridad",
                    "correo": "correo-totalmente-invalido"
                  },
                  "lineas": ["Ciberseguridad"],
                  "integrantes": [
                    { "codigo": "2026111", "nombre": "Estudiante Ciber", "programa": "Sistemas", "esLider": true }
                  ]
                }
                """;

        mockMvc.perform(post("/api/semilleros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCorreoInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        assertThat(semilleroRepository.count()).isEqualTo(conteoSemillerosAntes);
        assertThat(semilleroRepository.existsBySigla("SCM-01")).isFalse();
    }

    @Test
    @Order(7)
    @DisplayName("T7: Fallo después de guardar director -> Rollback total (sin director huérfano)")
    void t7_falloDespuesDeDirector_haceRollbackCompleto() {
        long conteoDirectoresAntes = directorRepository.count();
        long conteoSemillerosAntes = semilleroRepository.count();
        String correoHuerfano = "docente_huerfano_t7@universidad.edu";

        // Activar la simulación de fallo posterior a la inserción del director
        semilleroService.setSimularFalloPostDirector(true);

        com.example.demo.dto.request.SemilleroRequest request = new com.example.demo.dto.request.SemilleroRequest(
                "Semillero Rollback Test",
                "SRT-99",
                "Grupo Transaccional",
                "Misión",
                "Visión",
                "Objetivo",
                new com.example.demo.dto.request.DirectorRequest("Docente Huérfano Test", correoHuerfano),
                java.util.List.of("Línea Transaccional"),
                java.util.List.of(new com.example.demo.dto.request.IntegranteRequest("2026888", "Estudiante T7", "Sistemas", true))
        );

        try {
            semilleroService.crear(request);
            org.junit.jupiter.api.Assertions.fail("Se esperaba una RuntimeException por simulación de fallo");
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).contains("Fallo simulado después de guardar el director");
        } finally {
            semilleroService.setSimularFalloPostDirector(false);
        }

        // Comprobación de Rollback en la base de datos
        assertThat(directorRepository.findByCorreo(correoHuerfano)).isEmpty();
        assertThat(directorRepository.count()).isEqualTo(conteoDirectoresAntes);
        assertThat(semilleroRepository.count()).isEqualTo(conteoSemillerosAntes);
    }
}
