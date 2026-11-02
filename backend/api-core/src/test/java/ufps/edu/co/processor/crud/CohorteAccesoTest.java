package ufps.edu.co.processor.crud;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.CohorteErrorCode;
import ufps.edu.co.rest.dto.CohorteDTO;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.services.core.crud.CohorteCoreService;

/**
 * D-00 T2 — Regla de acceso a una cohorte: Posgrados y Super Administrador
 * (sin programa) acceden a cualquiera; el Director de Programa solo a las de
 * su programa. La cohorte inexistente se informa como 404 para todos.
 */
@ExtendWith(MockitoExtension.class)
class CohorteAccesoTest {

    private static final Integer ID_COHORTE = 11;
    private static final Integer PROGRAMA_COHORTE = 16;

    @Mock
    private CohorteService cohorteService;

    @InjectMocks
    private CohorteCoreService cohorteCoreService;

    private CohorteDTO cohorteDelPrograma16;

    @BeforeEach
    void setUp() {
        cohorteDelPrograma16 = CohorteDTO.builder()
                .id(ID_COHORTE)
                .nombre("Cohorte 11")
                .idPrograma(PROGRAMA_COHORTE)
                .build();
    }

    @Test
    @DisplayName("Cohorte inexistente → COHORTE_NOT_FOUND para cualquier rol")
    void cohorteInexistente() {
        when(cohorteService.findById(9999)).thenReturn(null);

        DomainException ex = assertThrows(DomainException.class,
                () -> cohorteCoreService.validarAccesoCohorte(9999, null));

        assertEquals(CohorteErrorCode.COHORTE_NOT_FOUND, ex.getCode());
        assertEquals(9999, ex.getParam());
    }

    @Test
    @DisplayName("Sin programa (Posgrados / Super Administrador) → acceso a cualquier cohorte")
    void accesoTotalSinPrograma() {
        when(cohorteService.findById(ID_COHORTE)).thenReturn(cohorteDelPrograma16);

        assertDoesNotThrow(() -> cohorteCoreService.validarAccesoCohorte(ID_COHORTE, null));
    }

    @Test
    @DisplayName("Director con el mismo programa de la cohorte → sin excepción")
    void directorDelMismoPrograma() {
        when(cohorteService.findById(ID_COHORTE)).thenReturn(cohorteDelPrograma16);

        assertDoesNotThrow(() -> cohorteCoreService.validarAccesoCohorte(ID_COHORTE, PROGRAMA_COHORTE));
    }

    @Test
    @DisplayName("Director de otro programa → COHORTE_ACCESO_FORBIDDEN")
    void directorDeOtroPrograma() {
        when(cohorteService.findById(ID_COHORTE)).thenReturn(cohorteDelPrograma16);

        DomainException ex = assertThrows(DomainException.class,
                () -> cohorteCoreService.validarAccesoCohorte(ID_COHORTE, 99));

        assertEquals(CohorteErrorCode.COHORTE_ACCESO_FORBIDDEN, ex.getCode());
        assertEquals(ID_COHORTE, ex.getParam());
    }
}
