package ufps.edu.co.services.core.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.SemestreErrorCode;
import ufps.edu.co.maps.specific.SemestreMap;
import ufps.edu.co.records.input.entity.SemestreInput.SEMESTRE_CREATE;
import ufps.edu.co.records.input.entity.SemestreInput.SEMESTRE_UPDATE;
import ufps.edu.co.records.output.entity.SemestreOutput;
import ufps.edu.co.rest.dto.SemestreDTO;
import ufps.edu.co.rest.services.SemestreService;

@ExtendWith(MockitoExtension.class)
class SemestreCoreServiceTest {

    @Mock
    private SemestreService service;

    @Mock
    private SemestreMap map;

    @InjectMocks
    private SemestreCoreService coreService;

    private SemestreDTO dto(String nombre, LocalDate inicio, LocalDate fin) {
        SemestreDTO dto = new SemestreDTO();
        dto.setNombre(nombre);
        dto.setFechaInicio(inicio);
        dto.setFechaFin(fin);
        return dto;
    }

    @Test
    @DisplayName("create con el mismo año y periodo lanza SEMESTRE_PERIODO_DUPLICADO")
    void createPeriodoDuplicado_lanzaConflicto() {
        SemestreDTO dto = dto("2028-2", LocalDate.of(2028, 8, 15), LocalDate.of(2028, 12, 15));
        when(map.toDto(any(SEMESTRE_CREATE.class))).thenReturn(dto);
        when(service.existsByPeriodo(2028, 2, null)).thenReturn(true);

        DomainException ex = assertThrows(DomainException.class,
                () -> coreService.create(new SEMESTRE_CREATE(
                        LocalDate.of(2028, 8, 15), LocalDate.of(2028, 12, 15), 1, "2028-2")));

        assertEquals(SemestreErrorCode.SEMESTRE_PERIODO_DUPLICADO, ex.getCode());
        verify(service, never()).create(any());
    }

    @Test
    @DisplayName("create con nombre repetido y periodo libre lanza SEMESTRE_NOMBRE_DUPLICADO")
    void createNombreDuplicado_lanzaConflicto() {
        SemestreDTO dto = dto("2028-2", LocalDate.of(2028, 8, 15), LocalDate.of(2028, 12, 15));
        when(map.toDto(any(SEMESTRE_CREATE.class))).thenReturn(dto);
        when(service.existsByPeriodo(2028, 2, null)).thenReturn(false);
        when(service.existsByNombre("2028-2", null)).thenReturn(true);

        DomainException ex = assertThrows(DomainException.class,
                () -> coreService.create(new SEMESTRE_CREATE(
                        LocalDate.of(2028, 8, 15), LocalDate.of(2028, 12, 15), 1, "2028-2")));

        assertEquals(SemestreErrorCode.SEMESTRE_NOMBRE_DUPLICADO, ex.getCode());
        verify(service, never()).create(any());
    }

    @Test
    @DisplayName("update del mismo semestre sin cambios se guarda")
    void updateMismoSemestre_seGuarda() {
        SemestreDTO dto = dto("2028-2", LocalDate.of(2028, 8, 15), LocalDate.of(2028, 12, 15));
        when(map.toDto(any(SEMESTRE_UPDATE.class))).thenReturn(dto);
        when(service.existsByPeriodo(2028, 2, 3)).thenReturn(false);
        when(service.existsByNombre("2028-2", 3)).thenReturn(false);
        when(service.update(eq(3), eq(dto))).thenReturn(dto);
        when(map.toOutput(dto)).thenReturn(SemestreOutput.builder().id(3).build());

        SemestreOutput result = coreService.update(new SEMESTRE_UPDATE(3,
                LocalDate.of(2028, 8, 15), LocalDate.of(2028, 12, 15), 1, "2028-2"));

        assertEquals(3, result.id());
        verify(service).update(3, dto);
    }
}
