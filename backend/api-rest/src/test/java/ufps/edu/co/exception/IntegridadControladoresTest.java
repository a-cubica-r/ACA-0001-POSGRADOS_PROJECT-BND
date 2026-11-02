package ufps.edu.co.exception;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ufps.edu.co.controllers.cases.administrativo.DirectorProgramaCase;
import ufps.edu.co.controllers.cases.programa.Programadocumentos;
import ufps.edu.co.records.input.entity.DocumentosrequisitoprogramaInput.DOCUMENTOSREQUISITOPROGRAMA_DELETE;
import ufps.edu.co.services.S3Service;
import ufps.edu.co.services.core.cases.DocumentosrequisitoprogramaPE;
import ufps.edu.co.services.core.crud.CriterioevaluacionCoreService;

/**
 * PLN0010 T1 — Los controladores de documentos y criterios dejan de tragarse la
 * excepción: una violación de integridad de MySQL llega al
 * {@link GlobalExceptionHandler} y responde 409 con cuerpo, no 500 vacío.
 */
@ExtendWith(MockitoExtension.class)
class IntegridadControladoresTest {

    private static final String FK_IN_USO = "Cannot delete or update a parent row: a foreign key constraint fails "
            + "(`railway`.`documentosrequisitoprogramacohorte`, CONSTRAINT `documentosrequisitoprogramacohorte_ibfk_2` "
            + "FOREIGN KEY (`id_docrequisito`) REFERENCES `documentosrequisitoprograma` (`id`) ON DELETE RESTRICT)";

    private static final String DUPLICADO = "Duplicate entry 'Criterio' for key 'criterioevaluacion.criterioevaluacion_index_25'";

    @Mock
    private DocumentosrequisitoprogramaPE documentosProcessor;

    @Mock
    private S3Service s3Service;

    @Mock
    private CriterioevaluacionCoreService criterioevaluacionProcessor;

    @InjectMocks
    private Programadocumentos programadocumentos;

    @InjectMocks
    private DirectorProgramaCase directorProgramaCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(programadocumentos, directorProgramaCase)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static DataIntegrityViolationException conSql(int codigo, String mensaje) {
        return new DataIntegrityViolationException("fallo de integridad",
                new SQLException(mensaje, "23000", codigo));
    }

    @Test
    @DisplayName("DELETE documento asignado a cohorte (1451) → 409 REGISTRO_EN_USO_CONFLICT")
    void borrarDocumentoEnUsoDevuelve409() throws Exception {
        doThrow(conSql(1451, FK_IN_USO))
                .when(documentosProcessor).deleteById(any(DOCUMENTOSREQUISITOPROGRAMA_DELETE.class));

        mockMvc.perform(delete("/director-programa/programa/documento/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REGISTRO_EN_USO_CONFLICT"))
                .andExpect(jsonPath("$.param.restriccion").value("documentosrequisitoprogramacohorte_ibfk_2"));
    }

    @Test
    @DisplayName("DELETE criterio asignado a cohorte (1451) → 409 REGISTRO_EN_USO_CONFLICT")
    void borrarCriterioEnUsoDevuelve409() throws Exception {
        doThrow(conSql(1451, "Cannot delete or update a parent row: a foreign key constraint fails "
                + "(`railway`.`criteriocohorte`, CONSTRAINT `criteriocohorte_ibfk_2` FOREIGN KEY (`id_criterio`) "
                + "REFERENCES `criterioevaluacion` (`id`) ON DELETE RESTRICT)"))
                .when(criterioevaluacionProcessor).deleteForPrograma(16, 9);

        mockMvc.perform(delete("/director-programa/programa/16/criterios/9"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REGISTRO_EN_USO_CONFLICT"))
                .andExpect(jsonPath("$.param.restriccion").value("criteriocohorte_ibfk_2"));
    }

    @Test
    @DisplayName("POST criterio con nombre repetido (1062) → 409 REGISTRO_DUPLICADO_CONFLICT")
    void crearCriterioDuplicadoDevuelve409() throws Exception {
        doThrow(conSql(1062, DUPLICADO))
                .when(criterioevaluacionProcessor).createForPrograma(eq(16), any());

        mockMvc.perform(post("/director-programa/programa/16/criterios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Criterio repetido\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REGISTRO_DUPLICADO_CONFLICT"));
    }
}
