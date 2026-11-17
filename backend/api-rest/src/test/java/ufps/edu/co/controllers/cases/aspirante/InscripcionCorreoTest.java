package ufps.edu.co.controllers.cases.aspirante;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ufps.edu.co.auth.contract.PasswordHashService;
import ufps.edu.co.config.EnlacesPublicos;
import ufps.edu.co.enums.EstadoAspirante;
import ufps.edu.co.enums.Rol;
import ufps.edu.co.exception.GlobalExceptionHandler;
import ufps.edu.co.records.output.entity.ProgramaOutput;
import ufps.edu.co.records.output.entity.RolOutput;
import ufps.edu.co.rest.dto.AspiranteDTO;
import ufps.edu.co.rest.dto.ClaveDTO;
import ufps.edu.co.rest.dto.CohorteDTO;
import ufps.edu.co.rest.dto.DocumentopersonaDTO;
import ufps.edu.co.rest.dto.EstadoDTO;
import ufps.edu.co.rest.dto.PersonaDTO;
import ufps.edu.co.rest.dto.UbicacionDTO;
import ufps.edu.co.rest.dto.UsuarioDTO;
import ufps.edu.co.rest.services.AspiranteService;
import ufps.edu.co.rest.services.ClaveService;
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
import ufps.edu.co.services.core.crud.RolCoreService;

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
    @Mock private ClaveService claveService;
    @Mock private PasswordHashService passwordHashService;
    @Mock private RolCoreService rolProcessor;

    @InjectMocks
    private InscripcionCase inscripcionCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inscripcionCase)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void registrarNuevoUsuarioGuardaElCorreoEnMinusculas() {
        stubFlujo();

        inscripcionCase.registrarNuevoUsuario(request("  ANA@EXAMPLE.COM "));

        ArgumentCaptor<PersonaDTO> captor = ArgumentCaptor.forClass(PersonaDTO.class);
        verify(personaService).create(captor.capture());
        assertEquals("ana@example.com", captor.getValue().getCorreo());
        verify(sesService, org.mockito.Mockito.atLeastOnce())
                .enviarCorreo(eq("ana@example.com"), anyString(), anyString());
    }

    @Test
    void registrarNuevoUsuarioMockMvc_responde201() throws Exception {
        stubFlujo();
        String cuerpo = """
                {
                  "nombres": "Ana",
                  "apellidos": "Pérez",
                  "idTipoDoc": 1,
                  "numeroDocumento": "123456",
                  "idMunicipioExpedicionDoc": 1,
                  "email": "ana@example.com",
                  "telefonoContacto": "3001234567",
                  "ubicacionNacimiento": {"idMunicipioNacimiento": 1},
                  "ubicacionResidencia": {"idMunicipioResidencia": 1, "direccionResidencia": "Calle 1"},
                  "idCohorte": 10,
                  "idTipoVinculacion": 1,
                  "usuario": "ana",
                  "contrasena": "Clave123*"
                }
                """;

        mockMvc.perform(post("/inscripciones/formulario/registrar-nuevo-usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated());
    }

    private void stubFlujo() {
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
        when(claveService.create(any())).thenReturn(ClaveDTO.builder().id(500).build());
        when(rolProcessor.findByNombre(Rol.ASPIRANTE.getValor())).thenReturn(RolOutput.builder().id(4).build());
        when(usuarioService.create(any())).thenReturn(UsuarioDTO.builder()
                .id(600).nombreusuario("ana").idPersona(300).build());
    }

    private InscripcionCase.RegistrarNuevoUsuarioRequest request(String correo) {
        return new InscripcionCase.RegistrarNuevoUsuarioRequest(
                "Ana", "Pérez", 1, "123456", 1, 1, LocalDate.of(2000, 1, 1), LocalDate.of(2018, 1, 1),
                1, 1, null, "Ingeniería", correo, null, "3001234567", new BigDecimal("4.25"),
                null, null, null, false, null, null,
                new InscripcionCase.UbicacionNacimientoRequest(1, 1),
                null,
                new InscripcionCase.UbicacionResidenciaRequest("URBANA", 1, 1, "Calle 1"),
                10, 1, "ana", "Clave123*");
    }
}
