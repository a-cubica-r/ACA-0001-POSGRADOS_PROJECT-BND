package ufps.edu.co.processor.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.FacultadErrorCode;
import ufps.edu.co.maps.specific.FacultadMap;
import ufps.edu.co.records.input.entity.FacultadInput.FACULTAD_CREATE;
import ufps.edu.co.records.input.entity.FacultadInput.FACULTAD_DELETE;
import ufps.edu.co.records.input.entity.FacultadInput.FACULTAD_UPDATE;
import ufps.edu.co.rest.dto.FacultadDTO;
import ufps.edu.co.rest.services.FacultadService;
import ufps.edu.co.services.core.crud.FacultadCoreService;

/**
 * D-08 (D-08.3 y D-08.2) — Facultad: errores propios en vez de 500/404
 * genéricos, y unicidad de nombre validada antes de guardar.
 */
@ExtendWith(MockitoExtension.class)
class FacultadCoreServiceTest {

    @Mock private FacultadService service;
    @Mock private FacultadMap map;

    @InjectMocks private FacultadCoreService processor;

    // ─── T1: existencia y parámetro ──────────────────────────────────────────

    @Test
    void updateDeIdInexistente_lanzaNotFound() {
        when(service.findById(99)).thenReturn(null);

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.update(new FACULTAD_UPDATE(99, "X", "x@ufps.edu.co", 1)));

        assertEquals(FacultadErrorCode.FACULTAD_NOT_FOUND, ex.getCode());
        verify(service, never()).update(any(), any());
    }

    @Test
    void deleteDeIdInexistente_lanzaNotFound() {
        when(service.findById(99)).thenReturn(null);

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.deleteById(new FACULTAD_DELETE(99)));

        assertEquals(FacultadErrorCode.FACULTAD_NOT_FOUND, ex.getCode());
        verify(service, never()).deleteById(any());
    }

    @Test
    void idNuloEnUpdate_lanzaParametroRequerido() {
        DomainException ex = assertThrows(DomainException.class,
                () -> processor.update(new FACULTAD_UPDATE(null, "X", null, 1)));

        assertEquals(FacultadErrorCode.FACULTAD_PARAMETRO_REQUERIDO, ex.getCode());
    }

    @Test
    void createPropagaDataIntegrityViolationSinEnvolverla() {
        when(map.toDto(any())).thenReturn(FacultadDTO.builder().nombre("Nueva").build());
        when(service.findIdByNombreIgnoreCase("Nueva")).thenReturn(null);
        when(service.create(any())).thenThrow(new DataIntegrityViolationException("dup"));

        DataIntegrityViolationException ex = assertThrows(DataIntegrityViolationException.class,
                () -> processor.create(new FACULTAD_CREATE("Nueva", "n@ufps.edu.co", 1)));

        assertEquals("dup", ex.getMessage());
    }

    // ─── T2: unicidad de nombre ──────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("Nombre repetido (con espacios y otra capitalización) → FACULTAD_NOMBRE_DUPLICADO")
    void createConNombreRepetido_lanzaNombreDuplicado() {
        when(map.toDto(any())).thenReturn(FacultadDTO.builder().nombre("  facultad de  DERECHO ").build());
        when(service.findIdByNombreIgnoreCase("facultad de DERECHO")).thenReturn(3);

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.create(new FACULTAD_CREATE("  facultad de  DERECHO ", "d@ufps.edu.co", 1)));

        assertEquals(FacultadErrorCode.FACULTAD_NOMBRE_DUPLICADO, ex.getCode());
        Map<String, Object> param = (Map<String, Object>) ex.getParam();
        assertEquals("nombre", param.get("campo"));
        assertEquals("facultad de DERECHO", param.get("valor"));
        assertEquals(3, param.get("idExistente"));
        verify(service, never()).create(any());
    }

    @Test
    void createConNombreNuevo_guardaElNombreNormalizado() {
        when(map.toDto(any())).thenReturn(FacultadDTO.builder().nombre("  facultad de  DERECHO ").build());
        when(service.findIdByNombreIgnoreCase("facultad de DERECHO")).thenReturn(null);
        when(service.create(any())).thenReturn(FacultadDTO.builder().id(9).build());
        when(map.toOutput(any())).thenReturn(null);

        processor.create(new FACULTAD_CREATE("  facultad de  DERECHO ", "d@ufps.edu.co", 1));

        ArgumentCaptor<FacultadDTO> captor = ArgumentCaptor.forClass(FacultadDTO.class);
        verify(service).create(captor.capture());
        assertEquals("facultad de DERECHO", captor.getValue().getNombre());
    }

    @Test
    void updateConSuPropioNombre_noLanza() {
        when(service.findById(3)).thenReturn(FacultadDTO.builder().id(3).build());
        when(map.toDto(any())).thenReturn(FacultadDTO.builder().id(3).nombre("Facultad de Derecho").build());
        when(service.findIdByNombreIgnoreCase("Facultad de Derecho")).thenReturn(3);
        when(service.update(eq(3), any())).thenReturn(FacultadDTO.builder().id(3).build());
        when(map.toOutput(any())).thenReturn(null);

        processor.update(new FACULTAD_UPDATE(3, "Facultad de Derecho", "d@ufps.edu.co", 1));

        verify(service).update(eq(3), any());
    }

    @Test
    void updateConNombreDeOtraFacultad_lanzaNombreDuplicado() {
        when(service.findById(4)).thenReturn(FacultadDTO.builder().id(4).build());
        when(map.toDto(any())).thenReturn(FacultadDTO.builder().id(4).nombre("Facultad de Derecho").build());
        when(service.findIdByNombreIgnoreCase("Facultad de Derecho")).thenReturn(3);

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.update(new FACULTAD_UPDATE(4, "Facultad de Derecho", "d@ufps.edu.co", 1)));

        assertEquals(FacultadErrorCode.FACULTAD_NOMBRE_DUPLICADO, ex.getCode());
        verify(service, never()).update(any(), any());
    }
}
