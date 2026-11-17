package ufps.edu.co.controllers.cases.aspirante;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import ufps.edu.co.services.core.crud.*;
// import ufps.edu.co.rest.services.TipodocumentoService;
import ufps.edu.co.auth.contract.PasswordHashService;
import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.AspiranteErrorCode;
import ufps.edu.co.domain.exceptions.errorcodes.CohorteErrorCode;
import ufps.edu.co.domain.exceptions.errorcodes.ProgramaErrorCode;
import ufps.edu.co.domain.exceptions.errorcodes.UsuarioErrorCode;
import ufps.edu.co.domain.validators.PromedioAcademico;
import ufps.edu.co.util.CorreoNormalizer;
import ufps.edu.co.config.EnlacesPublicos;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ufps.edu.co.records.input.entity.ProgramaInput;
import ufps.edu.co.records.output.entity.*;
import ufps.edu.co.rest.dto.*;
import ufps.edu.co.rest.services.*;
import ufps.edu.co.exception.SesEmailException;
import ufps.edu.co.services.EmailConfirmationTokenService;
import ufps.edu.co.services.SESService;
import ufps.edu.co.utils.EmailTemplates;
import ufps.edu.co.enums.EstadoAspirante;
import ufps.edu.co.enums.EstadoCohorte;
import ufps.edu.co.enums.ZonaResidencia;
import ufps.edu.co.enums.Rol;

@RestController
@RequestMapping(value = "/inscripciones", produces = MediaType.APPLICATION_JSON_VALUE)
public class InscripcionCase {

        private static final Logger log = LoggerFactory.getLogger(InscripcionCase.class);

        @Autowired
        private CohorteService cohorteService;

        @Autowired
        private EstadocivilService estadocivilService;

        @Autowired
        private GeneroCoreService generoProcessor;

        @Autowired
        private TipodocumentopersonaCoreService tipodocumentopersonaProcessor;

        @Autowired
        private PaisCoreService paisProcessor;

        @Autowired
        private DepartamentoService departamentoService;

        @Autowired
        private MunicipioService municipioService;

        @Autowired
        private CapacidadexepcionalService capacidadexepcionalService;

        @Autowired
        private DiscapacidadService discapacidadService;

        @Autowired
        private TipovinculacionService tipovinculacionService;

        @Autowired
        private GrupoetnicoService grupoetnicoService;

        @Autowired
        private PoblacionindigenaService poblacionindigenaService;

        @Autowired
        private UbicacionService ubicacionService;

        @Autowired
        private DocumentopersonaService documentopersonaService;

        @Autowired
        private PersonaService personaService;

        @Autowired
        private AspiranteService aspiranteService;

        @Autowired
        private PagoCoreService pagoProcessor;

        @Autowired
        private EstadoService estadoService;

        @Autowired
        private ClaveService claveService;

        @Autowired
        private PasswordHashService passwordHashService;

        @Autowired
        private UsuarioService usuarioService;

        @Autowired
        RolCoreService rolProcessor = new RolCoreService();

        @Autowired
        private SESService sesService;

        @Autowired
        private EmailConfirmationTokenService confirmationTokenService;

        @Autowired
        private EnlacesPublicos enlacesPublicos;

        // @Autowired
        // private EmailTemplates emailTemplates;

        // ─── Records de petición ────────────────────────────────────────────────

        public record ExperienciaLaboralItem(String experienciaLaboral) {
        }

        public record UbicacionNacimientoRequest(
                        Integer idDeptoNacimiento,
                        @NotNull(message = "El municipio de nacimiento es obligatorio")
                        Integer idMunicipioNacimiento) {
        }

        public record UbicacionTrabajoRequest(
                        Integer idDptoTrabajo,
                        @NotNull(message = "El municipio de trabajo es obligatorio")
                        Integer idMunicipioTrabajo,
                        String direccionTrabajo) {
        }

        public record UbicacionResidenciaRequest(
                        String zonaResidencia,
                        Integer idDeptoResidencia,
                        @NotNull(message = "El municipio de residencia es obligatorio")
                        Integer idMunicipioResidencia,
                        @NotBlank(message = "La dirección de residencia es obligatoria")
                        String direccionResidencia) {
        }

        public record RegistrarNuevoUsuarioRequest(
                        @NotBlank(message = "Los nombres son obligatorios")
                        @Size(max = 100, message = "Los nombres no pueden superar 100 caracteres")
                        String nombres,
                        @NotBlank(message = "Los apellidos son obligatorios")
                        @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres")
                        String apellidos,
                        @NotNull(message = "El tipo de documento es obligatorio")
                        Integer idTipoDoc,
                        @NotBlank(message = "El número de documento es obligatorio")
                        @Size(max = 100, message = "El número de documento no puede superar 100 caracteres")
                        String numeroDocumento,
                        Integer idEstadoCivil,
                        Integer idGenero,
                        LocalDate fechaNacimiento,
                        LocalDate fechaExpedicionDocumento,
                        Integer idDeptoExpedicionDoc,
                        @NotNull(message = "El municipio de expedición es obligatorio")
                        Integer idMunicipioExpedicionDoc,
                        String titulosPostgrado,
                        @Size(max = 100, message = "El título de pregrado no puede superar 100 caracteres")
                        String tituloPregrado,
                        @NotBlank(message = "El correo electrónico es obligatorio")
                        @Email(message = "El correo electrónico no tiene un formato válido")
                        @Size(max = 100, message = "El correo electrónico no puede superar 100 caracteres")
                        String email,
                        @Size(max = 100, message = "La empresa no puede superar 100 caracteres")
                        String empresa,
                        @NotBlank(message = "El teléfono de contacto es obligatorio")
                        String telefonoContacto,
                        BigDecimal promedioPonderadoAcumulado,
                        Integer idGrupoEtnico,
                        Integer idPuebloIndigena,
                        Integer idCapacidadExcepcional,
                        Boolean egresadoUfpsCucuta,
                        String experienciaLaboral,
                        Integer idDiscapacidad,
                        @NotNull(message = "La ubicación de nacimiento es obligatoria")
                        @Valid
                        UbicacionNacimientoRequest ubicacionNacimiento,
                        @Valid
                        UbicacionTrabajoRequest ubicacionTrabajo,
                        @NotNull(message = "La ubicación de residencia es obligatoria")
                        @Valid
                        UbicacionResidenciaRequest ubicacionResidencia,
                        @NotNull(message = "La cohorte es obligatoria")
                        Integer idCohorte,
                        @NotNull(message = "El tipo de vinculación es obligatorio")
                        Integer idTipoVinculacion,
                        @NotBlank(message = "El usuario es obligatorio")
                        @Size(max = 50, message = "El usuario no puede superar 50 caracteres")
                        String usuario,
                        @NotBlank(message = "La contraseña es obligatoria")
                        @Size(max = 72, message = "La contraseña no puede superar 72 caracteres")
                        String contrasena) {
            @Override
            public String toString() {
                return "RegistrarNuevoUsuarioRequest[usuario=" + usuario + ", contrasena=***]";
            }
        }

        public record RegistrarNuevoUsuarioOutput(Integer idPersona, Integer idAspirante, Integer idUsuario,
                        String nombreusuario) {
        }

        private void validarFormulario(RegistrarNuevoUsuarioRequest body) {
                if (body.telefonoContacto() == null || body.telefonoContacto().trim().length() != 10) {
                        throw new DomainException(AspiranteErrorCode.TELEFONO_INSCRIPCION_INVALIDO_CONFLICT,
                                        body.telefonoContacto());
                }

                if (body.promedioPonderadoAcumulado() != null
                                && !PromedioAcademico.esValido(body.promedioPonderadoAcumulado())) {
                        throw new DomainException(AspiranteErrorCode.PROMEDIO_INSCRIPCION_FUERA_DE_RANGO,
                                        body.promedioPonderadoAcumulado());
                }

                if (body.email() != null && personaService.existsByCorreo(CorreoNormalizer.normalizar(body.email()))) {
                        throw new DomainException(AspiranteErrorCode.CORREO_YA_REGISTRADO_CONFLICT,
                                        CorreoNormalizer.normalizar(body.email()));
                }

                if (body.usuario() != null && usuarioService.findByNombreusuario(body.usuario()) != null) {
                        throw new DomainException(UsuarioErrorCode.USUARIO_YA_EXISTE_CONFLICT, body.usuario());
                }

                boolean personaExistePorDocumento = false;
                if (body.numeroDocumento() != null && body.idTipoDoc() != null) {
                        // Mismo criterio que el índice UNIQUE de la BD:
                        // (numerodocumento, id_tipodocumento). Consulta directa, sin
                        // recorrer la tabla en memoria.
                        personaExistePorDocumento = documentopersonaService
                                        .existsByNumerodocumentoAndIdTipodocumento(body.numeroDocumento().trim(),
                                                        body.idTipoDoc());
                }

                if (personaExistePorDocumento) {
                        throw new DomainException(AspiranteErrorCode.PERSONA_INSCRIPCION_YA_EXISTE_CONFLICT,
                                        body.numeroDocumento());
                }
        }

        // ─── Endpoint Unificado: Registrar Formulario + Usuario ─────────────────
        @Transactional(rollbackFor = Exception.class)
        @PostMapping(value = "/formulario/registrar-nuevo-usuario", consumes = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<RegistrarNuevoUsuarioOutput> registrarNuevoUsuario(
                        @Valid @RequestBody RegistrarNuevoUsuarioRequest body) {

                validarFormulario(body);
                CohortePrograma cohortePrograma = validarCohorteParaRegistro(body.idCohorte());

                String experienciaLaboralJson = body.experienciaLaboral();
                String correo = CorreoNormalizer.normalizar(body.email());

                // 3. Ubicación del lugar de expedición del documento
                UbicacionDTO ubicExpedicion = ubicacionService.create(
                                UbicacionDTO.builder()
                                                .idMunicipio(body.idMunicipioExpedicionDoc())
                                                .direccion("")
                                                .build());

                // 5. Documento de identidad de la persona
                DocumentopersonaDTO docPersona = documentopersonaService.create(
                                DocumentopersonaDTO.builder()
                                                .numerodocumento(body.numeroDocumento())
                                                .idTipodocumento(body.idTipoDoc())
                                                .idLugarexpedicion(ubicExpedicion.getId())
                                                .build());

                // 6. Ubicación de nacimiento
                UbicacionDTO ubicNacimiento = ubicacionService.create(
                                UbicacionDTO.builder()
                                                .idMunicipio(body.ubicacionNacimiento().idMunicipioNacimiento())
                                                .direccion("")
                                                .build());

                // 7. Ubicación de trabajo (opcional)
                Integer idUbicacionTrabajo = null;
                if (body.ubicacionTrabajo() != null) {
                        String dirTrabajo = body.ubicacionTrabajo().direccionTrabajo() != null
                                        ? body.ubicacionTrabajo().direccionTrabajo()
                                        : "";
                        UbicacionDTO ubicTrabajo = ubicacionService.create(
                                        UbicacionDTO.builder()
                                                        .idMunicipio(body.ubicacionTrabajo().idMunicipioTrabajo())
                                                        .direccion(dirTrabajo)
                                                        .build());
                        idUbicacionTrabajo = ubicTrabajo.getId();
                }

                // 8. Ubicación de residencia
                boolean esUrbana = body.ubicacionResidencia() != null
                                && ZonaResidencia.URBANA.matches(body.ubicacionResidencia().zonaResidencia());
                UbicacionDTO ubicResidencia = ubicacionService.create(
                                UbicacionDTO.builder()
                                                .idMunicipio(body.ubicacionResidencia().idMunicipioResidencia())
                                                .direccion(body.ubicacionResidencia().direccionResidencia())
                                                .zonaurbana(esUrbana)
                                                .build());

                // 9. Persona
                PersonaDTO persona = personaService.create(
                                PersonaDTO.builder()
                                                .nombres(body.nombres())
                                                .apellidos(body.apellidos())
                                                .correo(correo)
                                                .empresa(body.empresa())
                                                .celular(body.telefonoContacto())
                                                .egresadoufps(body.egresadoUfpsCucuta())
                                                .experiencialaboral(experienciaLaboralJson)
                                                .fechanacimiento(body.fechaNacimiento())
                                                .promediopregrado(body.promedioPonderadoAcumulado())
                                                .titulopregrado(body.tituloPregrado())
                                                .titulosposgrados(body.titulosPostgrado())
                                                .idDocumentopersona(docPersona.getId())
                                                .idCapacidadexepcional(body.idCapacidadExcepcional())
                                                .idDiscapacidad(body.idDiscapacidad())
                                                .idEstadocivil(body.idEstadoCivil())
                                                .idGenero(body.idGenero())
                                                .idGrupoetnico(body.idGrupoEtnico())
                                                .idPoblacionindigena(body.idPuebloIndigena())
                                                .idUbicacionnacimiento(ubicNacimiento.getId())
                                                .idUbicaciontrabajo(idUbicacionTrabajo)
                                                .idUbicacionvivienda(ubicResidencia.getId())
                                                .build());

                // 10. Estado inicial del aspirante
                EstadoDTO estado = estadoService.findByTipoAndEntidad(EstadoAspirante.NO_CONFIRMADO);
                if (estado == null) {
                        throw new IllegalStateException(
                                        "Estado inicial 'NO CONFIRMADO' para ASPIRANTE no encontrado en la base de datos");
                }

                CohorteDTO cohorte = cohortePrograma.cohorte();
                ProgramaOutput programa = cohortePrograma.programa();

                // 11. Aspirante
                AspiranteDTO aspirante = aspiranteService.create(
                                AspiranteDTO.builder()
                                                .idPersona(persona.getId())
                                                .idEstado(estado.getId())
                                                .idCohorte(body.idCohorte())
                                                .idTipovinculacion(body.idTipoVinculacion())
                                                .build());

                pagoProcessor.ensureInitialPaymentsForAspirante(aspirante.getId());
                try {
                        sesService.enviarCorreo(correo, EmailTemplates.ASUNTO_INSCRIPCION,
                                        EmailTemplates.cuerpoInscripcion(persona.getNombres(), persona.getApellidos(),
                                                        cohorte.getNombre(), programa.nombre()));
                } catch (SesEmailException ex) {
                        log.error("[INSCRIPCION_EMAIL] Fallo al enviar correo de confirmación a '{}': {}",
                                        correo, ex.getMessage(), ex);
                }
                try {
                        String token = confirmationTokenService.generateToken(aspirante.getId());
                        String enlace = enlacesPublicos.confirmacionCorreo(token);
                        sesService.enviarCorreo(
                                        correo,
                                        EmailTemplates.ASUNTO_CONFIRMACION_CORREO,
                                        EmailTemplates.cuerpoConfirmacionCorreo(persona.getNombres(), enlace));
                } catch (Exception ex) {
                        log.error("[VERIFICACION_EMAIL] Fallo al enviar correo de verificación a '{}': {}",
                                        correo, ex.getMessage(), ex);
                }

                // 12. Crear clave y usuario
                ClaveDTO clave = claveService.create(ClaveDTO.builder()
                                .valor(passwordHashService.encode(body.contrasena()))
                                .build());

                UsuarioDTO usuario = usuarioService.create(UsuarioDTO.builder()
                                .idPersona(persona.getId())
                                .idClave(clave.getId())
                                .idRol(rolProcessor.findByNombre(Rol.ASPIRANTE.getValor()).id())
                                .nombreusuario(body.usuario())
                                .build());

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new RegistrarNuevoUsuarioOutput(persona.getId(), aspirante.getId(),
                                                usuario.getId(), usuario.getNombreusuario()));
        }

        /**
         * Valida la cohorte antes de crear persona, ubicaciones, documento y
         * usuario. Devuelve la cohorte y su programa para reutilizarlos en el
         * registro y en el correo de confirmación.
         */
        private CohortePrograma validarCohorteParaRegistro(Integer idCohorte) {
                if (idCohorte == null) {
                        throw new DomainException(AspiranteErrorCode.INSCRIPCION_COHORTE_REQUERIDA, "idCohorte");
                }
                CohorteDTO cohorte = cohorteService.findById(idCohorte);
                if (cohorte == null) {
                        throw new DomainException(CohorteErrorCode.COHORTE_NOT_FOUND, idCohorte);
                }
                if (cohorte.getEstado() != null && cohorte.getEstado().getTipo() != null
                                && !EstadoCohorte.ABIERTA.matches(cohorte.getEstado().getTipo())) {
                        throw new DomainException(AspiranteErrorCode.INSCRIPCION_COHORTE_NO_ABIERTA_CONFLICT, idCohorte);
                }
                if (cohorte.getIdPrograma() == null) {
                        throw new DomainException(ProgramaErrorCode.PROGRAMA_NOT_FOUND, idCohorte);
                }
                ProgramaOutput programa = programaProcessor
                                .findById(new ProgramaInput.PROGRAMA_FIND(cohorte.getIdPrograma()));
                if (programa == null) {
                        throw new DomainException(ProgramaErrorCode.PROGRAMA_NOT_FOUND, cohorte.getIdPrograma());
                }
                return new CohortePrograma(cohorte, programa);
        }

        private record CohortePrograma(CohorteDTO cohorte, ProgramaOutput programa) {
        }

        // ─── Endpoints GET existentes ────────────────────────────────────────────

        @GetMapping("/tipos-vinculacion")
        public ResponseEntity<List<TipovinculacionOutput>> getTiposVinculacion() {
                List<TipovinculacionOutput> tipos = tipovinculacionService.findAll().stream()
                                .map(dto -> TipovinculacionOutput.builder()
                                                .id(dto.getId())
                                                .nombre(dto.getTipo())
                                                .build())
                                .toList();
                return ResponseEntity.ok(tipos);
        }

        @GetMapping("/discapacidades")
        public ResponseEntity<List<DiscapacidadOutput>> getDiscapacidades() {
                List<DiscapacidadOutput> discapacidades = discapacidadService.findAll().stream()
                                .map(dto -> DiscapacidadOutput.builder()
                                                .id(dto.getId())
                                                .nombre(dto.getTipodiscapacidad())
                                                .build())
                                .toList();
                return ResponseEntity.ok(discapacidades);
        }

        @GetMapping("/capacidades-excepcionales")
        public ResponseEntity<List<CapacidadexepcionalOutput>> getCapacidadesExcepcionales() {
                List<CapacidadexepcionalOutput> capacidades = capacidadexepcionalService.findAll().stream()
                                .map(dto -> CapacidadexepcionalOutput.builder()
                                                .id(dto.getId())
                                                .nombre(dto.getTipocapacidad())
                                                .build())
                                .toList();
                return ResponseEntity.ok(capacidades);
        }

        @GetMapping("/pueblos-indigenas")
        public ResponseEntity<List<PoblacionindigenaOutput>> getPueblosIndigenas() {
                List<PoblacionindigenaOutput> pueblos = poblacionindigenaService.findAll().stream()
                                .map(dto -> PoblacionindigenaOutput.builder()
                                                .id(dto.getId())
                                                .nombre(dto.getPoblacion())
                                                .build())
                                .toList();
                return ResponseEntity.ok(pueblos);
        }

        @GetMapping("/grupos-etnicos")
        public ResponseEntity<List<GrupoetnicoOutput>> getGruposEtnicos() {
                List<GrupoetnicoOutput> grupos = grupoetnicoService.findAll().stream()
                                .map(dto -> GrupoetnicoOutput.builder()
                                                .id(dto.getId())
                                                .nombre(dto.getGrupo())
                                                .build())
                                .toList();
                return ResponseEntity.ok(grupos);
        }

        @GetMapping("/zonas-residencia")
        public ResponseEntity<List<Map<String, Object>>> getZonasResidencia() {
                return ResponseEntity.ok(Arrays.stream(ZonaResidencia.values())
                                .map(zona -> Map.<String, Object>of("id", zona.getId(), "nombre", zona.getValor()))
                                .toList());
        }

        @GetMapping("/tipos-documento")
        public ResponseEntity<List<TipodocumentopersonaOutput>> getTiposDocumento() {
                return ResponseEntity.ok(tipodocumentopersonaProcessor.findAll());
        }

        @GetMapping("/estados-civiles")
        public ResponseEntity<List<EstadocivilOutput>> getEstadosCiviles() {
                List<EstadocivilOutput> list = estadocivilService.findAll().stream()
                                .map(dto -> EstadocivilOutput.builder()
                                                .id(dto.getId())
                                                .tipo(dto.getEstado())
                                                .build())
                                .toList();
                return ResponseEntity.ok(list);
        }

        @GetMapping("/generos")
        public ResponseEntity<List<GeneroOutput>> getGeneros() {
                return ResponseEntity.ok(generoProcessor.findAll());
        }

        @GetMapping("/paises")
        public ResponseEntity<List<PaisOutput>> getPaises() {
                return ResponseEntity.ok(paisProcessor.findAll());
        }

        @GetMapping("/paises/{idPais}/departamentos")
        public ResponseEntity<List<DepartamentoOutput>> getDepartamentosByPais(@PathVariable Integer idPais) {
                List<DepartamentoOutput> departamentos = departamentoService.findByIdPais(idPais).stream()
                                .map(d -> DepartamentoOutput.builder()
                                                .id(d.getId())
                                                .nombre(d.getNombre())
                                                .build())
                                .toList();
                return ResponseEntity.ok(departamentos);
        }

        @GetMapping("/departamentos/{idDepartamento}/municipios")
        public ResponseEntity<List<MunicipioOutput>> getMunicipiosByDepartamento(@PathVariable Integer idDepartamento) {
                List<MunicipioOutput> municipios = municipioService.findByIdDepartamento(idDepartamento).stream()
                                .map(m -> MunicipioOutput.builder()
                                                .id(m.getId())
                                                .nombre(m.getNombre())
                                                .build())
                                .toList();
                return ResponseEntity.ok(municipios);
        }

        @GetMapping("/programa/{programaId}/cohortes")
        public ResponseEntity<List<CohorteActivaOutput>> getCohortesActivas(@PathVariable Integer programaId) {
                List<CohorteActivaOutput> cohortes = cohorteService.findActivasByIdPrograma(programaId).stream()
                                .map(c -> CohorteActivaOutput.builder()
                                                .id(c.getId())
                                                .nombre(c.getNombre())
                                                .build())
                                .toList();
                return ResponseEntity.ok(cohortes);
        }

        @Autowired
        private ProgramaCoreService programaProcessor;

        @GetMapping("/programas")
        public ResponseEntity<List<ProgramaListadoOutput>> getProgramas() {
                List<ProgramaListadoOutput> programas = programaProcessor.findAll().stream()
                                .map(this::toProgramaListadoOutput)
                                .toList();
                return ResponseEntity.ok(programas);
        }


        private ProgramaListadoOutput toProgramaListadoOutput(ProgramaOutput programa) {
                return ProgramaListadoOutput.builder()
                                .id(programa.id())
                                .nombre(programa.nombre())
                                .build();
        }

}