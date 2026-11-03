package ufps.edu.co.controllers.cases.aspirante;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ufps.edu.co.config.EnlacesPublicos;
import ufps.edu.co.enums.EstadoAspirante;
import ufps.edu.co.records.output.entity.ProgramaOutput;
import ufps.edu.co.rest.dto.AspiranteDTO;
import ufps.edu.co.rest.dto.CohorteDTO;
import ufps.edu.co.rest.dto.DocumentopersonaDTO;
import ufps.edu.co.rest.dto.EstadoDTO;
import ufps.edu.co.rest.dto.PersonaDTO;
import ufps.edu.co.rest.dto.UbicacionDTO;
import ufps.edu.co.rest.services.AspiranteService;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.rest.services.DocumentopersonaService;
import ufps.edu.co.rest.services.EstadoService;
import ufps.edu.co.rest.services.PersonaService;
import ufps.edu.co.rest.services.UbicacionService;
import ufps.edu.co.rest.services.UsuarioService;
import ufps.edu.co.services.EmailConfirmationTokenService;
import ufps.edu.co.services.SESService;
import ufps.edu.co.services.core.crud.PagoCoreService;
import ufps.edu.co.services.core.crud.ProgramaCoreService;

@ExtendWith(MockitoExtension.class)
class InscripcionCorreoTest {

    @Mock private CohorteService cohorteService;
    @Mock private UbicacionService ubicacionService;
    @Mock private DocumentopersonaService documentopersonaService;
    @Mock private PersonaService personaService;
    @Mock private UsuarioService usuarioService;
    @Mock private AspiranteService aspiranteService;
    @Mock private PagoCoreService pagoProcessor;
    @Mock private EstadoService estadoService;
    @Mock private SESService sesService;
    @Mock private EmailConfirmationTokenService confirmationTokenService;
    @Mock private ProgramaCoreService programaProcessor;
    @Mock private EnlacesPublicos enlacesPublicos;

    @InjectMocks
    private InscripcionCase inscripcionCase;

    @Test
    void registrarFormularioGuardaElCorreoEnMinusculas() {
        when(personaService.existsByCorreo(anyString())).thenReturn(false);
        when(ubicacionService.create(any())).thenReturn(UbicacionDTO.builder().id(100).build());
        when(documentopersonaService.create(any())).thenReturn(DocumentopersonaDTO.builder().id(200).build());
        when(personaService.create(any())).thenAnswer(inv -> {
            PersonaDTO dto = inv.getArgument(0);
            return PersonaDTO.builder()
                    .id(300)
                    .nombres(dto.getNombres())
                    .apellidos(dto.getApellidos())
                    .correo(dto.getCorreo())
                    .build();
        });
        when(estadoService.findByTipoAndEntidad(EstadoAspirante.NO_CONFIRMADO))
                .thenReturn(EstadoDTO.builder().id(1).build());
        when(cohorteService.findById(10)).thenReturn(CohorteDTO.builder()
                .id(10).idPrograma(20).nombre("Cohorte").build());
        when(programaProcessor.findById(any())).thenReturn(ProgramaOutput.builder()
                .id(20).nombre("Maestría").build());
        when(aspiranteService.create(any())).thenReturn(AspiranteDTO.builder().id(400).build());
        when(confirmationTokenService.generateToken(400)).thenReturn("token");
        when(enlacesPublicos.confirmacionCorreo("token")).thenReturn("https://x/confirmar?token=token");

        inscripcionCase.registrarFormulario(request("  ANA@EXAMPLE.COM "));

        ArgumentCaptor<PersonaDTO> captor = ArgumentCaptor.forClass(PersonaDTO.class);
        verify(personaService).create(captor.capture());
        assertEquals("ana@example.com", captor.getValue().getCorreo());
        verify(sesService, org.mockito.Mockito.atLeastOnce())
                .enviarCorreo(eq("ana@example.com"), anyString(), anyString());
    }

    private InscripcionCase.FormularioInscripcionRequest request(String correo) {
        return new InscripcionCase.FormularioInscripcionRequest(
                "Ana", "Pérez", 1, "123456", 1, 1, LocalDate.of(2000, 1, 1), LocalDate.of(2018, 1, 1),
                1, 1, null, "Ingeniería", correo, null, "3001234567", new BigDecimal("4.25"),
                null, null, null, false, null, null,
                new InscripcionCase.UbicacionNacimientoRequest(1, 1),
                null,
                new InscripcionCase.UbicacionResidenciaRequest("URBANA", 1, 1, "Calle 1"),
                10, 1, "ana");
    }
}
