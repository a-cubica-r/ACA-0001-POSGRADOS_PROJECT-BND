package ufps.edu.co.controllers.rest;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.GeneroErrorCode;
import ufps.edu.co.exception.GlobalExceptionHandler;
import ufps.edu.co.rest.exceptions.RegistroNoEncontradoException;
import ufps.edu.co.services.core.crud.GeneroCoreService;
import ufps.edu.co.services.core.crud.PlazoCoreService;

/**
 * D-09 T2 — Los endpoints {@code /delete} del CRUD genérico ya no atrapan la
 * excepción: el {@link GlobalExceptionHandler} responde con cuerpo y con el
 * status correcto (404, 409 o 500), en lugar del 404 vacío de antes.
 */
@ExtendWith(MockitoExtension.class)
class DeleteEndpointErrorHandlingTest {

    @Mock private PlazoCoreService plazoProcessor;
    @InjectMocks private PlazoRestController plazoController;

    @Mock private GeneroCoreService generoProcessor;
    @InjectMocks private GeneroRestController generoController;

    private MockMvc plazoMvc;
    private MockMvc generoMvc;

    @BeforeEach
    void setUp() {
        plazoMvc = MockMvcBuilders.standaloneSetup(plazoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        generoMvc = MockMvcBuilders.standaloneSetup(generoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static DataIntegrityViolationException enUso() {
        return new DataIntegrityViolationException("fallo de integridad",
                new SQLException("Cannot delete or update a parent row: a foreign key constraint fails "
                        + "(`railway`.`plazo`, CONSTRAINT `plazo_ibfk_1` FOREIGN KEY (`id_tipoplazo`) "
                        + "REFERENCES `tipoplazo` (`id`) ON DELETE RESTRICT)", "23000", 1451));
    }

    @Test
    @DisplayName("Registro inexistente → 404 REGISTRO_NOT_FOUND con cuerpo")
    void registroNoEncontrado() throws Exception {
        doThrow(new RegistroNoEncontradoException("Plazo no encontrado con id: 7"))
                .when(plazoProcessor).deleteById(any());

        plazoMvc.perform(delete("/plazo/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":7}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("REGISTRO_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Plazo no encontrado con id: 7"));
    }

    @Test
    @DisplayName("Entidad con código propio → 404 con ese código")
    void noEncontradoConCodigoPropio() throws Exception {
        doThrow(new DomainException(GeneroErrorCode.GENERO_NOT_FOUND, 3))
                .when(generoProcessor).deleteById(any());

        generoMvc.perform(delete("/genero/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":3}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GENERO_NOT_FOUND"));
    }

    @Test
    @DisplayName("Registro en uso (MySQL 1451) → 409 REGISTRO_EN_USO_CONFLICT con la restricción")
    void registroEnUso() throws Exception {
        doThrow(enUso()).when(plazoProcessor).deleteById(any());

        plazoMvc.perform(delete("/plazo/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":7}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REGISTRO_EN_USO_CONFLICT"))
                .andExpect(jsonPath("$.param.restriccion").value("plazo_ibfk_1"));
    }

    @Test
    @DisplayName("Error no controlado → 500 ERROR_INTERNO sin traza en el cuerpo")
    void errorNoControlado() throws Exception {
        doThrow(new IllegalStateException("fallo interno con detalle sensible"))
                .when(plazoProcessor).deleteById(any());

        plazoMvc.perform(delete("/plazo/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":7}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("ERROR_INTERNO"))
                .andExpect(jsonPath("$.message").value("Error interno del servidor"))
                .andExpect(content().string(not(containsString("detalle sensible"))));
    }
}
