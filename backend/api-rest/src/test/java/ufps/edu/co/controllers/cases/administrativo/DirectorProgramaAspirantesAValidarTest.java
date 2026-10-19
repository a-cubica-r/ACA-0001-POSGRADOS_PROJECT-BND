package ufps.edu.co.controllers.cases.administrativo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ufps.edu.co.maps.specific.ListaadmitidosMap;
import ufps.edu.co.records.output.entity.AspiranteCohorteOutput;
import ufps.edu.co.rest.services.AdministrativoService;
import ufps.edu.co.rest.services.AspiranteService;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.rest.services.EstadoService;
import ufps.edu.co.rest.services.ListaadmitidosService;
import ufps.edu.co.rest.services.PagoreciboinscripcionService;
import ufps.edu.co.rest.services.PagorecibomatriculaService;
import ufps.edu.co.rest.services.PagoService;
import ufps.edu.co.rest.services.UsuarioService;
import ufps.edu.co.services.PdfGeneratorService;
import ufps.edu.co.services.S3Service;
import ufps.edu.co.services.SESService;
import ufps.edu.co.services.core.crud.AspiranteCoreService;
import ufps.edu.co.services.core.crud.CalificacioncriterioCoreService;
import ufps.edu.co.services.core.crud.CriteriocohorteCoreService;
import ufps.edu.co.services.core.crud.CriterioevaluacionCoreService;
import ufps.edu.co.services.core.crud.DocumentoCoreService;
import ufps.edu.co.services.core.crud.DocumentosrequisitoprogramacohorteCoreService;
import ufps.edu.co.services.core.crud.EntrevistaCoreService;
import ufps.edu.co.services.core.crud.ListaadmitidosCoreService;
import ufps.edu.co.services.core.crud.PagoCoreService;
import ufps.edu.co.services.core.crud.PruebaCoreService;
import ufps.edu.co.persistence.repositories.PersonaRepository;

/**
 * SC-07 — CP-SC07-13 a CP-SC07-15
 * Endpoint: GET /director-programa/cohorte/{idCohorte}/aspirantes-a-validar
 */
@ExtendWith(MockitoExtension.class)
class DirectorProgramaAspirantesAValidarTest {

    @Mock private AspiranteCoreService aspiranteProcessor;
    @Mock private DocumentoCoreService documentoProcessor;
    @Mock private EntrevistaCoreService entrevistaProcessor;
    @Mock private ListaadmitidosCoreService listaadmitidosProcessor;
    @Mock private ListaadmitidosMap listaadmitidosMap;
    @Mock private PdfGeneratorService pdfGeneratorService;
    @Mock private ListaadmitidosService listaadmitidosService;
    @Mock private AspiranteService aspiranteService;
    @Mock private EstadoService estadoService;
    @Mock private CalificacioncriterioCoreService calificacioncriterioProcessor;
    @Mock private PruebaCoreService pruebaProcessor;
    @Mock private CriterioevaluacionCoreService criterioevaluacionProcessor;
    @Mock private CriteriocohorteCoreService criteriocohorteProcessor;
    @Mock private DocumentosrequisitoprogramacohorteCoreService documentosrequisitoprogramacohorteProcessor;
    @Mock private UsuarioService usuarioService;
    @Mock private AdministrativoService administrativoService;
    @Mock private PagoreciboinscripcionService pagoreciboinscripcionService;
    @Mock private PagorecibomatriculaService pagorecibomatriculaService;
    @Mock private PagoService pagoService;
    @Mock private CohorteService cohorteService;
    @Mock private SESService sesService;
    @Mock private PersonaRepository personaRepository;
    @Mock private PagoCoreService pagoProcessor;
    @Mock private S3Service s3Service;

    @InjectMocks
    private DirectorProgramaCase controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // CP-SC07-13
    @Test
    @DisplayName("CP-SC07-13 — Sin params → defaults page=0, size=20 llegan al servicio con idCohorte=5")
    void sinParams_usaDefaults_page0_size20() throws Exception {
        Page<AspiranteCohorteOutput> paginaMock = new PageImpl<>(
                List.of(AspiranteCohorteOutput.builder().id(1).nombre("Ana").build()),
                PageRequest.of(0, 20), 1);
        when(aspiranteProcessor.findAValidarByCohorte(eq(5), any(Pageable.class))).thenReturn(paginaMock);

        mockMvc.perform(get("/director-programa/cohorte/5/aspirantes-a-validar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(aspiranteProcessor).findAValidarByCohorte(eq(5), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(20, captor.getValue().getPageSize());
    }

    // CP-SC07-14
    @Test
    @DisplayName("CP-SC07-14 — Con ?page=1&size=10 → Pageable(1,10) con idCohorte=5 llega al servicio")
    void conParams_propagaPageableYIdCohorteCorrectamente() throws Exception {
        Page<AspiranteCohorteOutput> paginaMock = new PageImpl<>(
                List.of(AspiranteCohorteOutput.builder().id(2).build()),
                PageRequest.of(1, 10), 30);
        when(aspiranteProcessor.findAValidarByCohorte(eq(5), any(Pageable.class))).thenReturn(paginaMock);

        mockMvc.perform(get("/director-programa/cohorte/5/aspirantes-a-validar")
                        .param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(30))
                .andExpect(jsonPath("$.totalPages").value(3));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(aspiranteProcessor).findAValidarByCohorte(eq(5), captor.capture());
        assertEquals(1, captor.getValue().getPageNumber());
        assertEquals(10, captor.getValue().getPageSize());
    }

    // CP-SC07-15
    @Test
    @DisplayName("CP-SC07-15 — Cohorte sin aspirantes a validar → 200 con content vacío, no 500")
    void cohorteSinAspirantesAValidar_devuelve200ConContentVacio() throws Exception {
        Page<AspiranteCohorteOutput> paginaVacia = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(aspiranteProcessor.findAValidarByCohorte(eq(7), any(Pageable.class))).thenReturn(paginaVacia);

        mockMvc.perform(get("/director-programa/cohorte/7/aspirantes-a-validar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
