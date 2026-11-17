package ufps.edu.co.services.core.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import ufps.edu.co.records.output.entity.ProgramaOutput;
import ufps.edu.co.rest.dto.ProgramaDTO;
import ufps.edu.co.rest.services.ProgramaService;
import ufps.edu.co.rest.services.UltimocodigoprogramaService;

@ExtendWith(MockitoExtension.class)
class ProgramaRegistroSniesTest {

    @Mock
    private ProgramaService service;

    @Mock
    private ProgramaMap map;

    @Mock
    private UltimocodigoprogramaService ultimocodigoprogramaService;

    @InjectMocks
    private ProgramaCoreService coreService;

    private PROGRAMA_CREATE create(String snies) {
        return new PROGRAMA_CREATE("P-01", "Maestría", 2, "correo@ufps.edu.co", snies,
                "Maestría", "Título", "RC", 40, "Semestral", null, 1, 1, 1, null, 1);
    }

    @Test
    @DisplayName("create con SNIES existente lanza PROGRAMA_REGISTROSNIES_DUPLICADO")
    void createSniesDuplicado_lanzaConflicto() {
        ProgramaDTO dto = new ProgramaDTO();
        dto.setRegistrosnies("12345");
        when(map.toDto(any(PROGRAMA_CREATE.class))).thenReturn(dto);
        when(service.existsByRegistrosnies("12345")).thenReturn(true);

        DomainException ex = assertThrows(DomainException.class, () -> coreService.create(create("12345")));

        assertEquals(ProgramaErrorCode.PROGRAMA_REGISTROSNIES_DUPLICADO, ex.getCode());
        verify(service, never()).create(any());
    }

    @Test
    @DisplayName("update con el propio SNIES se guarda")
    void updateMismoSnies_seGuarda() {
        ProgramaDTO dto = new ProgramaDTO();
        dto.setRegistrosnies("12345");
        when(map.toDto(any(PROGRAMA_UPDATE.class))).thenReturn(dto);
        when(service.existsByRegistrosniesAndIdNot("12345", 7)).thenReturn(false);
        when(service.update(eq(7), eq(dto))).thenReturn(dto);
        when(map.toOutput(dto)).thenReturn(ProgramaOutput.builder().id(7).build());

        ProgramaOutput result = coreService.update(new PROGRAMA_UPDATE(7, "P-01", "Maestría", 2,
                "correo@ufps.edu.co", "12345", "Maestría", "Título", "RC", 40, "Semestral",
                null, 1, 1, 1, null, 1));

        assertEquals(7, result.id());
        verify(service).update(7, dto);
    }

    @Test
    @DisplayName("un SNIES en blanco se guarda como null sin consultar duplicados")
    void createSniesEnBlanco_seGuardaNull() {
        ProgramaDTO dto = new ProgramaDTO();
        dto.setRegistrosnies("  ");
        ProgramaDTO created = new ProgramaDTO();
        created.setId(9);
        when(map.toDto(any(PROGRAMA_CREATE.class))).thenReturn(dto);
        when(service.create(dto)).thenReturn(created);
        when(ultimocodigoprogramaService.existsByIdPrograma(9)).thenReturn(true);
        when(map.toOutput(created)).thenReturn(ProgramaOutput.builder().id(9).build());

        coreService.create(create("  "));

        assertNull(dto.getRegistrosnies());
        verify(service, never()).existsByRegistrosnies(any());
    }
}
