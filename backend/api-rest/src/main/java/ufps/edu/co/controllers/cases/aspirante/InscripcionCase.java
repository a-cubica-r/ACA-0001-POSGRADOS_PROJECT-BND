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

import ufps.edu.co.processor.crud.*;
// import ufps.edu.co.rest.services.TipodocumentoService;
import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.AspiranteErrorCode;
import ufps.edu.co.domain.exceptions.errorcodes.UsuarioErrorCode;
import ufps.edu.co.records.input.entity.ProgramaInput;
import ufps.edu.co.records.output.entity.*;
import ufps.edu.co.rest.dto.*;
import ufps.edu.co.rest.services.*;
import ufps.edu.co.exception.SesEmailException;
import ufps.edu.co.services.SESService;
import ufps.edu.co.utils.EmailTemplates;

@RestController
@RequestMapping(value = "/inscripciones", produces = MediaType.APPLICATION_JSON_VALUE)
public class InscripcionCase {

        private static final Logger log = LoggerFactory.getLogger(InscripcionCase.class);

        @Autowired
        private CohorteService cohorteService;

        @Autowired
        private EstadocivilService estadocivilService;

        @Autowired
        private GeneroProcessor generoProcessor;

        @Autowired
        private TipodocumentopersonaProcessor tipodocumentopersonaProcessor;

        @Autowired
        private PaisProcessor paisProcessor;

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
        private PagoProcessor pagoProcessor;

        @Autowired
        private EstadoService estadoService;

        @Autowired
        private ClaveService claveService;

        @Autowired
        private UsuarioService usuarioService;

        @Autowired
        RolProcessor rolProcessor = new RolProcessor();

        @Autowired
        private SESService sesService;

        // @Autowired
        // private EmailTemplates emailTemplates;

        // ─── Records de petición ────────────────────────────────────────────────

        public record ExperienciaLaboralItem(String experienciaLaboral) {
        }

        public record UbicacionNacimientoRequest(
                        Integer idDeptoNacimiento,
                        Integer idMunicipioNacimiento) {
        }

        public record UbicacionTrabajoRequest(
                        Integer idDptoTrabajo,
                        Integer idMunicipioTrabajo,
                        String direccionTrabajo) {
        }

        public record UbicacionResidenciaRequest(
                        String zonaResidencia,
                        Integer idDeptoResidencia,
                        Integer idMunicipioResidencia,
                        String direccionResidencia) {
        }

        public record FormularioInscripcionRequest(
                        String nombres,
                        String apellidos,
                        Integer idTipoDoc,
                        String numeroDocumento,
                        Integer idEstadoCivil,
                        Integer idGenero,
                        LocalDate fechaNacimiento,
                        LocalDate fechaExpedicionDocumento,
                        Integer idDeptoExpedicionDoc,
                        Integer idMunicipioExpedicionDoc,
                        String titulosPostgrado,
                        String tituloPregrado,
                        String email,
                        String telefonoContacto,
                        BigDecimal promedioPonderadoAcumulado,
                        Integer idGrupoEtnico,
                        Integer idPuebloIndigena,
                        Integer idCapacidadExcepcional,
                        Boolean egresadoUfpsCucuta,
                        String experienciaLaboral,
                        Integer idDiscapacidad,
                        UbicacionNacimientoRequest ubicacionNacimiento,
                        UbicacionTrabajoRequest ubicacionTrabajo,
                        UbicacionResidenciaRequest ubicacionResidencia,
                        Integer idCohorte,
                        Integer idTipoVinculacion) {
        }

        public record FormularioInscripcionOutput(Integer idPersona, Integer idAspirante) {
        }

        public record RegistrarUsuarioRequest(Integer idPersona, String usuario, String contrasena) {
        }

        public record RegistrarUsuarioOutput(Integer idUsuario, String nombreusuario, Integer idPersona) {
        }

        // ─── Endpoint 14: Registrar Formulario Completo ─────────────────────────

        @Transactional(rollbackFor = Exception.class)
        @PostMapping(value = "/formulario", consumes = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<FormularioInscripcionOutput> registrarFormulario(
                        @RequestBody FormularioInscripcionRequest body) {

                validarFormulario(body);

                if (body.promedioPonderadoAcumulado() != null) {
                        BigDecimal promedio = body.promedioPonderadoAcumulado();
                        if (promedio.compareTo(BigDecimal.ZERO) < 0) {
                                throw new RuntimeException(
                                                "El promedio ponderado acumulado debe ser un valor positivo");
                        }
                }

                String experienciaLaboralJson = body.experienciaLaboral();

                // 3. Ubicación del lugar de expedición del documento
                UbicacionDTO ubicExpedicion = ubicacionService.create(
                                UbicacionDTO.builder()
                                                .idMunicipio(body.idMunicipioExpedicionDoc())
                                                .direccion("")
                                                .build());

                // 5. Documento de identidad de la persona
                DocumentopersonaDTO docPersona = documentopersonaService.create(
                                DocumentopersonaDTO.builder()
                                                .numerodocumento(Integer.parseInt(body.numeroDocumento()))
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
                boolean esUrbana = "Urbana".equalsIgnoreCase(body.ubicacionResidencia().zonaResidencia());
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
                                                .correo(body.email())
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
                EstadoDTO estado = estadoService.findByTipoAndEntidad("NO CONFIRMADO", "ASPIRANTE");
                if (estado == null) {
                        throw new RuntimeException(
                                        "Estado inicial 'NO CONFIRMADO' para ASPIRANTE no encontrado en la base de datos");
                }

                // Validar cohorte enviada por el front
                if (body.idCohorte() == null) {
                        throw new RuntimeException("Se debe enviar idCohorte en el formulario de inscripción");
                }
                CohorteDTO cohorte = cohorteService.findById(body.idCohorte());
                if (cohorte == null) {
                        throw new RuntimeException("Cohorte no encontrada con id: " + body.idCohorte());
                }
                if (cohorte.getEstado() != null && cohorte.getEstado().getTipo() != null
                                && !"ABIERTA".equalsIgnoreCase(cohorte.getEstado().getTipo())) {
                        throw new RuntimeException("La cohorte indicada no está abierta");
                }

                if (cohorte.getIdPrograma() == null) {
                        throw new RuntimeException("La cohorte indicada no tiene programa asociado");
                }
                ProgramaOutput programa = programaProcessor.findById(new ProgramaInput.PROGRAMA_FIND(cohorte.getIdPrograma()));
                if (programa == null) {
                        throw new RuntimeException("Programa no encontrado con id: " + cohorte.getIdPrograma());
                }

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
                        sesService.enviarCorreo(persona.getCorreo(), EmailTemplates.ASUNTO_INSCRIPCION,
                                        EmailTemplates.cuerpoInscripcion(persona.getNombres(), persona.getApellidos(),
                                                        cohorte.getNombre(), programa.nombre()));
                } catch (SesEmailException ex) {
                        log.error("[INSCRIPCION_EMAIL] Fallo al enviar correo de confirmación a '{}': {}",
                                persona.getCorreo(), ex.getMessage(), ex);
                }
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new FormularioInscripcionOutput(persona.getId(), aspirante.getId()));
        }

        private void validarFormulario(FormularioInscripcionRequest body) {
                if (body.telefonoContacto() == null || body.telefonoContacto().trim().length() != 10) {
                        throw new DomainException(AspiranteErrorCode.TELEFONO_INSCRIPCION_INVALIDO_CONFLICT,
                                        body.telefonoContacto());
                }

                boolean personaExistePorCorreo = body.email() != null && personaService.findAll().stream()
                                .anyMatch(persona -> persona.getCorreo() != null
                                                && persona.getCorreo().equalsIgnoreCase(body.email()));

                boolean personaExistePorDocumento = false;
                if (body.numeroDocumento() != null && body.idTipoDoc() != null) {
                        personaExistePorDocumento = documentopersonaService.findAll().stream()
                                        .anyMatch(doc -> doc.getNumerodocumento() != null
                                                        && body.numeroDocumento().equals(String.valueOf(doc.getNumerodocumento()))
                                                        && java.util.Objects.equals(doc.getIdTipodocumento(), body.idTipoDoc()));
                }

                if (personaExistePorCorreo || personaExistePorDocumento) {
                        throw new DomainException(AspiranteErrorCode.PERSONA_INSCRIPCION_YA_EXISTE_CONFLICT,
                                        body.email() != null ? body.email() : body.numeroDocumento());
                }
        }

        // ─── Endpoint 15: Registrar Usuario del Aspirante ───────────────────────

        @PostMapping(value = "/usuario", consumes = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<RegistrarUsuarioOutput> registrarUsuario(
                        @RequestBody RegistrarUsuarioRequest body) {

                        // Verify username uniqueness before creating key/usuario
                        if (body.usuario() != null && usuarioService.findByNombreusuario(body.usuario()) != null) {
                                throw new DomainException(UsuarioErrorCode.USUARIO_YA_EXISTE_CONFLICT, body.usuario());
                        }

                        ClaveDTO clave = claveService.create(ClaveDTO.builder()
                                        .valor(body.contrasena())
                                        .build());

                        UsuarioDTO usuario = usuarioService.create(UsuarioDTO.builder()
                                        .idPersona(body.idPersona())
                                        .idClave(clave.getId())
                                        .idRol(
                                                rolProcessor.findByNombre("ASPIRANTE").id()
                                        )
                                        .nombreusuario(body.usuario())
                                        .build());

                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(new RegistrarUsuarioOutput(usuario.getId(), usuario.getNombreusuario(),
                                                        usuario.getIdPersona()));
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
                return ResponseEntity.ok(List.of(
                                Map.of("id", 1, "nombre", "Urbana"),
                                Map.of("id", 2, "nombre", "Rural")));
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
        private ProgramaProcessor programaProcessor;

        @GetMapping("/programas")
        public ResponseEntity<List<ProgramaListadoOutput>> getProgramas() {
                List<ProgramaListadoOutput> programas = programaProcessor.findAll().stream()
                                .map(this::toProgramaListadoOutput)
                                .toList();
                return ResponseEntity.ok(programas);
        }

        @GetMapping("/requisitos")
        public ResponseEntity<List<RequisitoDocumentoOutput>> getRequisitos() {
                throw new UnsupportedOperationException("Operación no soportada: reimplementación pendiente.");
        }

        private ProgramaListadoOutput toProgramaListadoOutput(ProgramaOutput programa) {
                return ProgramaListadoOutput.builder()
                                .id(programa.id())
                                .nombre(programa.nombre())
                                .build();
        }

}