package ufps.edu.co.services.core.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.CohorteErrorCode;
import ufps.edu.co.maps.specific.CohorteMap;
import ufps.edu.co.records.input.entity.CohorteInput.COHORTE_CREATE;
import ufps.edu.co.records.input.entity.CohorteInput.COHORTE_UPDATE;
import ufps.edu.co.rest.dto.CohorteDTO;
import ufps.edu.co.rest.exceptions.RegistroNoEncontradoException;
import ufps.edu.co.rest.services.CohorteService;

@ExtendWith(MockitoExtension.class)
class CohorteCoreServiceTest {

    @Mock
    private CohorteService service;

    @Mock
    private CohorteMap map;

    @InjectMocks
    private CohorteCoreService coreService;

    private COHORTE_UPDATE updateInput() {
        return new COHORTE_UPDATE(1, "2026-1", 10, 1, 1, 1, 1, 1, 1, 1);
    }

    @Test
    @DisplayName("update propaga DataIntegrityViolationException, no la convierte en 404")
    void updateViolacionDeIntegridad_sePropaga() {
        when(map.toDto(any(COHORTE_UPDATE.class))).thenReturn(new CohorteDTO());
        when(service.update(eq(1), any())).thenThrow(new DataIntegrityViolationException("1062"));

        assertThrows(DataIntegrityViolationException.class, () -> coreService.update(updateInput()));
    }

    @Test
    @DisplayName("update traduce RegistroNoEncontradoException a COHORTE_NOT_FOUND")
    void updateNoEncontrado_lanzaCohorteNotFound() {
        when(map.toDto(any(COHORTE_UPDATE.class))).thenReturn(new CohorteDTO());
        when(service.update(eq(1), any())).thenThrow(new RegistroNoEncontradoException("no existe"));

        DomainException ex = assertThrows(DomainException.class, () -> coreService.update(updateInput()));

        assertEquals(CohorteErrorCode.COHORTE_NOT_FOUND, ex.getCode());
    }

    @Test
    @DisplayName("create traduce la violación de unicidad a COHORTE_DUPLICADO")
    void createDuplicado_lanzaCohorteDuplicado() {
        when(map.toDto(any(COHORTE_CREATE.class))).thenReturn(new CohorteDTO());
        when(service.create(any())).thenThrow(new DataIntegrityViolationException("1062",
                new SQLException("Duplicate entry", "23000", 1062)));

        COHORTE_CREATE input = new COHORTE_CREATE("2026-1", 10, 1, 1, 1, 1, 1, 1, 1);
        DomainException ex = assertThrows(DomainException.class, () -> coreService.create(input));

        assertEquals(CohorteErrorCode.COHORTE_DUPLICADO, ex.getCode());
    }
}
