package ufps.edu.co.controllers.cases.aspirante;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ufps.edu.co.exception.GlobalExceptionHandler;
import ufps.edu.co.rest.services.DocumentopersonaService;

/**
 * D-08 (CP-030) — La validación del documento del aspirante consulta por
 * (número, tipo) en vez de recorrer la tabla completa en memoria.
 */
@ExtendWith(MockitoExtension.class)
class InscripcionDocumentoTest {

    @Mock private DocumentopersonaService documentopersonaService;

    @InjectMocks private InscripcionCase inscripcionCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inscripcionCase)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Documento ya registrado → 409 PERSONA_INSCRIPCION_YA_EXISTE_CONFLICT sin findAll()")
    void documentoYaRegistrado_responde409SinRecorrerLaTabla() throws Exception {
        when(documentopersonaService.existsByNumerodocumentoAndIdTipodocumento("1098765432", 1)).thenReturn(true);

        mockMvc.perform(post("/inscripciones/formulario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telefonoContacto\":\"3001234567\",\"numeroDocumento\":\"1098765432\",\"idTipoDoc\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PERSONA_INSCRIPCION_YA_EXISTE_CONFLICT"));

        verify(documentopersonaService, never()).findAll();
    }

    @Test
    @DisplayName("El número se consulta recortado (trim)")
    void documentoConEspacios_seConsultaRecortado() throws Exception {
        when(documentopersonaService.existsByNumerodocumentoAndIdTipodocumento("1098765432", 1)).thenReturn(true);

        mockMvc.perform(post("/inscripciones/formulario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telefonoContacto\":\"3001234567\",\"numeroDocumento\":\"  1098765432 \",\"idTipoDoc\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PERSONA_INSCRIPCION_YA_EXISTE_CONFLICT"));

        verify(documentopersonaService, never()).findAll();
    }
}
