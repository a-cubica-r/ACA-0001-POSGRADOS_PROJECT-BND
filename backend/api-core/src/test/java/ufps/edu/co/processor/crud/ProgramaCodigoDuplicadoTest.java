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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.ProgramaErrorCode;
import ufps.edu.co.maps.specific.ProgramaMap;
import ufps.edu.co.records.input.entity.ProgramaInput.PROGRAMA_CREATE;
import ufps.edu.co.records.input.entity.ProgramaInput.PROGRAMA_UPDATE;
import ufps.edu.co.rest.dto.ProgramaDTO;
import ufps.edu.co.rest.services.ProgramaService;
import ufps.edu.co.services.core.crud.ProgramaCoreService;

/**
 * D-08 (D-08.2) — Unicidad del código de programa validada antes de guardar.
 */
@ExtendWith(MockitoExtension.class)
class ProgramaCodigoDuplicadoTest {

    @Mock private ProgramaService service;
    @Mock private ProgramaMap map;

    @InjectMocks private ProgramaCoreService processor;

    private static PROGRAMA_CREATE create(String codigo) {
        return new PROGRAMA_CREATE(codigo, "Programa nuevo", 4, null, null, null, null, null, null, null, null,
                1, 1, null, null, 1);
    }

    private static PROGRAMA_UPDATE update(Integer id, String codigo) {
        return new PROGRAMA_UPDATE(id, codigo, "Programa", 4, null, null, null, null, null, null, null, null,
                1, 1, null, null, 1);
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("Crear con un código existente (id 7) → PROGRAMA_CODIGO_DUPLICADO y no se crea")
    void createConCodigoExistente_lanzaCodigoDuplicado() {
        when(map.toDto(any())).thenReturn(ProgramaDTO.builder().codigo("2102").build());
        when(service.findIdByCodigo("2102")).thenReturn(7);

        DomainException ex = assertThrows(DomainException.class, () -> processor.create(create("2102")));

        assertEquals(ProgramaErrorCode.PROGRAMA_CODIGO_DUPLICADO, ex.getCode());
        Map<String, Object> param = (Map<String, Object>) ex.getParam();
        assertEquals("codigo", param.get("campo"));
        assertEquals("2102", param.get("valor"));
        assertEquals(7, param.get("idExistente"));
        verify(service, never()).create(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("El código se normaliza (trim) antes de validar")
    void createNormalizaElCodigoAntesDeValidar() {
        when(map.toDto(any())).thenReturn(ProgramaDTO.builder().codigo("  2102  ").build());
        when(service.findIdByCodigo("2102")).thenReturn(7);

        DomainException ex = assertThrows(DomainException.class, () -> processor.create(create("  2102  ")));

        assertEquals(ProgramaErrorCode.PROGRAMA_CODIGO_DUPLICADO, ex.getCode());
        Map<String, Object> param = (Map<String, Object>) ex.getParam();
        assertEquals("2102", param.get("valor"));
    }

    @Test
    @DisplayName("Editar el programa 7 conservando su propio código → no lanza")
    void updateConSuPropioCodigo_noLanza() {
        ProgramaDTO actual = ProgramaDTO.builder().id(7).codigo("2102").build();
        when(service.findById(7)).thenReturn(actual);
        when(map.toDto(any())).thenReturn(ProgramaDTO.builder().id(7).codigo("2102").build());
        when(service.findIdByCodigo("2102")).thenReturn(7);
        when(service.update(eq(7), any())).thenReturn(actual);
        when(map.toOutput(any())).thenReturn(null);

        processor.update(update(7, "2102"));

        verify(service).update(eq(7), any());
    }

    @Test
    @DisplayName("Editar el programa 8 con el código de otro → PROGRAMA_CODIGO_DUPLICADO")
    void updateConCodigoDeOtroPrograma_lanzaCodigoDuplicado() {
        when(service.findById(8)).thenReturn(ProgramaDTO.builder().id(8).build());
        when(map.toDto(any())).thenReturn(ProgramaDTO.builder().id(8).codigo("2102").build());
        when(service.findIdByCodigo("2102")).thenReturn(7);

        DomainException ex = assertThrows(DomainException.class, () -> processor.update(update(8, "2102")));

        assertEquals(ProgramaErrorCode.PROGRAMA_CODIGO_DUPLICADO, ex.getCode());
        verify(service, never()).update(any(), any());
    }

    @Test
    @DisplayName("Sin código no se valida unicidad")
    void createSinCodigo_noValidaUnicidad() {
        ProgramaDTO dto = ProgramaDTO.builder().nombre("Sin código").build();
        when(map.toDto(any())).thenReturn(dto);
        when(service.create(any())).thenReturn(dto);
        when(map.toOutput(any())).thenReturn(null);

        processor.create(create(null));

        verify(service, never()).findIdByCodigo(any());
        verify(service).create(any());
    }
}
