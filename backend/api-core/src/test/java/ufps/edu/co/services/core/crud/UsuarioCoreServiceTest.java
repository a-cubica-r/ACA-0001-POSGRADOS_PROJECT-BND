package ufps.edu.co.services.core.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.springframework.dao.DataIntegrityViolationException;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.UsuarioErrorCode;
import ufps.edu.co.maps.specific.UsuarioMap;
import ufps.edu.co.records.input.entity.UsuarioInput.USUARIO_CREATE;
import ufps.edu.co.records.input.entity.UsuarioInput.USUARIO_UPDATE;
import ufps.edu.co.records.output.entity.UsuarioOutput;
import ufps.edu.co.rest.dto.UsuarioDTO;
import ufps.edu.co.rest.services.UsuarioService;

@ExtendWith(MockitoExtension.class)
class UsuarioCoreServiceTest {

    @Mock
    private UsuarioService service;

    @Mock
    private UsuarioMap map;

    @InjectMocks
    private UsuarioCoreService coreService;

    @Test
    @DisplayName("update rechaza el nombre de usuario de otro usuario")
    void updateNombreDeOtroUsuario_lanzaConflicto() {
        when(service.existsByNombreusuarioAndIdNot("repetido", 7)).thenReturn(true);

        DomainException ex = assertThrows(DomainException.class,
                () -> coreService.update(new USUARIO_UPDATE(7, " repetido ", 1, 1, 1)));

        assertEquals(UsuarioErrorCode.USUARIO_YA_EXISTE_CONFLICT, ex.getCode());
        verify(service, never()).update(any(), any());
    }

    @Test
    @DisplayName("update permite conservar el propio nombre de usuario")
    void updateMismoNombre_seGuarda() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(7);
        UsuarioOutput output = UsuarioOutput.builder().id(7).build();
        when(service.existsByNombreusuarioAndIdNot("propio", 7)).thenReturn(false);
        when(map.toDto(any(USUARIO_UPDATE.class))).thenReturn(dto);
        when(service.update(eq(7), eq(dto))).thenReturn(dto);
        when(map.toOutput(dto)).thenReturn(output);

        UsuarioOutput result = coreService.update(new USUARIO_UPDATE(7, " propio ", 1, 1, 1));

        assertEquals(7, result.id());
        verify(service).update(7, dto);
    }

    @Test
    @DisplayName("create rechaza un nombre de usuario ya usado")
    void createNombreRepetido_lanzaConflicto() {
        when(service.existsByNombreusuario("repetido")).thenReturn(true);

        DomainException ex = assertThrows(DomainException.class,
                () -> coreService.create(new USUARIO_CREATE(" repetido ", 1, 1, 1)));

        assertEquals(UsuarioErrorCode.USUARIO_YA_EXISTE_CONFLICT, ex.getCode());
        verify(service, never()).create(any());
    }

    @Test
    @DisplayName("update propaga DataIntegrityViolationException sin envolverla")
    void updateViolacionDeIntegridad_sePropaga() {
        UsuarioDTO dto = new UsuarioDTO();
        when(service.existsByNombreusuarioAndIdNot("propio", 7)).thenReturn(false);
        when(map.toDto(any(USUARIO_UPDATE.class))).thenReturn(dto);
        when(service.update(eq(7), eq(dto))).thenThrow(new DataIntegrityViolationException("1062"));

        assertThrows(DataIntegrityViolationException.class,
                () -> coreService.update(new USUARIO_UPDATE(7, "propio", 1, 1, 1)));
    }
}
