package ufps.edu.co.controllers.cases.administrativo;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.CohorteErrorCode;
import ufps.edu.co.exception.GlobalExceptionHandler;
import ufps.edu.co.rest.dto.PagoreciboDirectorProjectionDTO;
import ufps.edu.co.rest.services.AdministrativoService;
import ufps.edu.co.rest.services.PagoreciboinscripcionService;
import ufps.edu.co.rest.services.PagorecibomatriculaService;
import ufps.edu.co.rest.services.UsuarioService;
import ufps.edu.co.services.core.crud.CohorteCoreService;

/**
 * D-00 T2 — GET /director-programa/pagos/{idCohorte}/inscripcion y /matricula:
 * lista plana sin {@code page}, {@code Page} con {@code page}, y traducción de
 * los errores de acceso a 403/404 (no a 500).
 */
@ExtendWith(MockitoExtension.class)
class DirectorProgramaPagosPorCohorteTest {

    private static final Integer ID_COHORTE = 11;
    private static final Integer PROGRAMA_COHORTE = 16;

    @Mock private PagoreciboinscripcionService pagoreciboinscripcionService;
    @Mock private PagorecibomatriculaService pagorecibomatriculaService;
    @Mock private CohorteCoreService cohorteCoreService;
    @Mock private UsuarioService usuarioService;
    @Mock private AdministrativoService administrativoService;

    @InjectMocks
    private DirectorProgramaCase controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticar(String... roles) {
        var authorities = java.util.Arrays.stream(roles)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("usuario", null, authorities));
    }

    private static PagoreciboDirectorProjectionDTO recibo(Integer id) {
        return new PagoreciboDirectorProjectionDTO(id, 100 + id, 200 + id, "Ana Perez",
                LocalDate.of(2026, 11, 30), "https://recibo/" + id, "https://factura/" + id,
                "REF-" + id, new BigDecimal("150000"), 5, "EN CURSO");
    }

    @Test
    @DisplayName("Posgrados sin page → arreglo plano con los recibos de la cohorte")
    void posgradosSinPageDevuelveListaPlana() throws Exception {
        autenticar("POSGRADOS");
        when(pagoreciboinscripcionService.findDirectorByCohorteId(ID_COHORTE))
                .thenReturn(List.of(recibo(1)));

        mockMvc.perform(get("/director-programa/pagos/" + ID_COHORTE + "/inscripcion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].aspirante").value("Ana Perez"))
                .andExpect(jsonPath("$[0].estado").value("EN CURSO"))
                .andExpect(jsonPath("$[0].idEstado").value(5));

        verify(cohorteCoreService).validarAccesoCohorte(ID_COHORTE, null);
        verify(usuarioService, never()).findIdPersonaByNombreusuario(eq("usuario"));
    }

    @Test
    @DisplayName("Con page=0&size=10 → objeto Page")
    void conPageDevuelvePage() throws Exception {
        autenticar("POSGRADOS");
        Page<PagoreciboDirectorProjectionDTO> pagina = new PageImpl<>(
                List.of(recibo(1)), PageRequest.of(0, 10), 1);
        when(pagoreciboinscripcionService.findDirectorByCohorteId(ID_COHORTE, PageRequest.of(0, 10)))
                .thenReturn(pagina);

        mockMvc.perform(get("/director-programa/pagos/" + ID_COHORTE + "/inscripcion")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Matrícula sin datos → 200 con arreglo vacío")
    void matriculaVaciaDevuelveArregloVacio() throws Exception {
        autenticar("POSGRADOS");
        when(pagorecibomatriculaService.findDirectorByCohorteId(ID_COHORTE)).thenReturn(List.of());

        mockMvc.perform(get("/director-programa/pagos/" + ID_COHORTE + "/matricula"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Director de Programa del mismo programa → consulta con su programa")
    void directorDelMismoPrograma() throws Exception {
        autenticar("DIRECTOR_DE_PROGRAMA");
        when(usuarioService.findIdPersonaByNombreusuario("usuario")).thenReturn(7);
        when(administrativoService.findIdProgramaByIdPersona(7)).thenReturn(PROGRAMA_COHORTE);
        when(pagoreciboinscripcionService.findDirectorByCohorteId(ID_COHORTE))
                .thenReturn(List.of(recibo(1)));

        mockMvc.perform(get("/director-programa/pagos/" + ID_COHORTE + "/inscripcion"))
                .andExpect(status().isOk());

        verify(cohorteCoreService).validarAccesoCohorte(ID_COHORTE, PROGRAMA_COHORTE);
    }

    @Test
    @DisplayName("Director de otro programa → 403 COHORTE_ACCESO_FORBIDDEN")
    void directorDeOtroProgramaDevuelve403() throws Exception {
        autenticar("DIRECTOR_DE_PROGRAMA");
        when(usuarioService.findIdPersonaByNombreusuario("usuario")).thenReturn(7);
        when(administrativoService.findIdProgramaByIdPersona(7)).thenReturn(99);
        doThrow(new DomainException(CohorteErrorCode.COHORTE_ACCESO_FORBIDDEN, ID_COHORTE))
                .when(cohorteCoreService).validarAccesoCohorte(ID_COHORTE, 99);

        mockMvc.perform(get("/director-programa/pagos/" + ID_COHORTE + "/inscripcion"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COHORTE_ACCESO_FORBIDDEN"));

        verify(pagoreciboinscripcionService, never()).findDirectorByCohorteId(ID_COHORTE);
    }

    @Test
    @DisplayName("Cohorte inexistente → 404 COHORTE_NOT_FOUND")
    void cohorteInexistenteDevuelve404() throws Exception {
        autenticar("SUPER_ADMINISTRADOR");
        doThrow(new DomainException(CohorteErrorCode.COHORTE_NOT_FOUND, 9999))
                .when(cohorteCoreService).validarAccesoCohorte(9999, null);

        mockMvc.perform(get("/director-programa/pagos/9999/matricula"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COHORTE_NOT_FOUND"));

        verify(pagorecibomatriculaService, never()).findDirectorByCohorteId(9999);
    }

    @Test
    @DisplayName("La ruta vieja por idPrograma sigue respondiendo igual")
    void rutaViejaSigueFuncionando() throws Exception {
        autenticar("POSGRADOS");
        when(pagoreciboinscripcionService.findDirectorByProgramaId(PROGRAMA_COHORTE))
                .thenReturn(List.of(recibo(1)));

        mockMvc.perform(get("/director-programa/pagos/inscripcion")
                        .param("idPrograma", String.valueOf(PROGRAMA_COHORTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
