package ufps.edu.co.controllers.cases.aspirante;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ufps.edu.co.config.EnlacesPublicos;
import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.AspiranteErrorCode;
import ufps.edu.co.enums.EstadoAspirante;
import ufps.edu.co.exception.SesEmailException;
import ufps.edu.co.rest.dto.AspiranteDTO;
import ufps.edu.co.rest.dto.EstadoDTO;
import ufps.edu.co.rest.services.AspiranteService;
import ufps.edu.co.rest.services.PersonaService;
import ufps.edu.co.services.EmailConfirmationTokenService;
import ufps.edu.co.services.SESService;

@ExtendWith(MockitoExtension.class)
class AspiranteCorreoTest {

    @Mock
    private AspiranteService aspiranteService;

    @Mock
    private PersonaService personaService;

    @Mock
    private SESService sesService;

    @Mock
    private EmailConfirmationTokenService confirmationTokenService;

    @Mock
    private EnlacesPublicos enlacesPublicos;

    @InjectMocks
    private AspiranteCase aspiranteCase;

    @Test
    void actualizarCorreoNormalizaYGuardaEnMinusculas() {
        when(aspiranteService.findById(1)).thenReturn(aspirante());
        when(personaService.existsByCorreoAndIdNot("adolfo@hotmail.com", 5)).thenReturn(false);

        aspiranteCase.actualizarCorreo(1, "ADOLFO@HOTMAIL.COM");

        verify(personaService).updateCorreo(5, "adolfo@hotmail.com");
    }

    @Test
    void actualizarCorreoRechazaFormatoInvalido() {
        when(aspiranteService.findById(1)).thenReturn(aspirante());

        DomainException ex = assertThrows(DomainException.class,
                () -> aspiranteCase.actualizarCorreo(1, "no-es-correo"));

        assertEquals(AspiranteErrorCode.CORREO_FORMATO_INVALIDO, ex.getCode());
    }

    @Test
    void actualizarCorreoRechazaCorreoDeOtraPersona() {
        when(aspiranteService.findById(1)).thenReturn(aspirante());
        when(personaService.existsByCorreoAndIdNot("adolfo@hotmail.com", 5)).thenReturn(true);

        DomainException ex = assertThrows(DomainException.class,
                () -> aspiranteCase.actualizarCorreo(1, "ADOLFO@HOTMAIL.COM"));

        assertEquals(AspiranteErrorCode.CORREO_YA_REGISTRADO_CONFLICT, ex.getCode());
    }

    @Test
    void enviarConfirmacionConSesCaidoDevuelveErrorDeCorreoNoDisponible() {
        when(aspiranteService.findById(1)).thenReturn(aspirante());
        when(confirmationTokenService.generateToken(1)).thenReturn("token");
        when(enlacesPublicos.confirmacionCorreo("token")).thenReturn("https://x/confirmar?token=token");
        when(personaService.findNombreById(5)).thenReturn("Ana");
        when(personaService.findCorreoById(5)).thenReturn("ana@example.com");
        doThrow(new SesEmailException("fallo ses", new RuntimeException("detalle")))
                .when(sesService).enviarCorreo(anyString(), anyString(), anyString());

        DomainException ex = assertThrows(DomainException.class,
                () -> aspiranteCase.enviarConfirmacionCorreo(1));

        assertEquals(AspiranteErrorCode.CORREO_ENVIO_UNAVAILABLE, ex.getCode());
    }

    private AspiranteDTO aspirante() {
        return AspiranteDTO.builder()
                .id(1)
                .idPersona(5)
                .estado(EstadoDTO.builder().tipo(EstadoAspirante.NO_CONFIRMADO.getValor()).build())
                .build();
    }
}
