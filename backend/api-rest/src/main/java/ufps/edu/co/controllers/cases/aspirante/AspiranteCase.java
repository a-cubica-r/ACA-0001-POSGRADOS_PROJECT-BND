package ufps.edu.co.controllers.cases.aspirante;

import java.time.*;
import java.util.*;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import ufps.edu.co.domain.exceptions.*;
import ufps.edu.co.domain.exceptions.errorcodes.*;
import ufps.edu.co.processor.cases.*;
import ufps.edu.co.processor.crud.*;
import ufps.edu.co.records.input.entity.AspiranteInput.*;
import ufps.edu.co.records.input.entity.EntrevistaInput.*;
import ufps.edu.co.records.input.entity.PruebaInput.*;
import ufps.edu.co.records.output.entity.*;
import ufps.edu.co.rest.dto.*;
import ufps.edu.co.rest.services.*;
import ufps.edu.co.services.*;
import ufps.edu.co.utils.*;

@RestController
@RequestMapping(value = "/aspirantes", produces = MediaType.APPLICATION_JSON_VALUE)
public class AspiranteCase {

    private static final Logger log = LoggerFactory.getLogger(AspiranteCase.class);

    @Autowired
    private AspiranteProcessor processor;

    @Autowired
    private DocumentoProcessor documentoProcessor;

    @Autowired
    private AspiranteService aspiranteService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private DocumentosrequisitoprogramaPE documentosRequeridosPE;

    @Autowired
    private DocumentoService documentoService;

    @Autowired
    private EstadodocumentoService estadodocumentoService;

    @Autowired
    private CohorteService cohorteService;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private ufps.edu.co.rest.services.PagoreciboinscripcionService pagoreciboinscripcionService;

    @Autowired
    private ufps.edu.co.rest.services.PagorecibomatriculaService pagorecibomatriculaService;

    @Autowired
    private EntrevistaProcessor entrevistaProcessor;

    @Autowired
    private PruebaProcessor pruebaProcessor;

    @Autowired
    private SESService sesService;

    // @Autowired
    // private EmailTemplates emailTemplates;

    @Autowired
    private EstadoService estadoService;

    @Autowired
    private EmailConfirmationTokenService confirmationTokenService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.frontend-login-url}")
    private String frontendLoginUrl;

    @Autowired
    private PersonaService personaService;

    @GetMapping("/aspirante/{idUsuario}")
    public ResponseEntity<AspiranteIdOutput> getIdAspiranteByUsuario(@PathVariable Integer idUsuario) {
        UsuarioDTO usuario = usuarioService.findById(idUsuario);
        if (usuario == null || usuario.getIdPersona() == null) {
            return ResponseEntity.notFound().build();
        }
        Integer idAspirante = aspiranteService.findIdByIdPersona(usuario.getIdPersona());
        if (idAspirante == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(AspiranteIdOutput.builder().idAspirante(idAspirante).build());
    }

    @GetMapping("/{idAspirante}/criterios")
    public ResponseEntity<AspiranteCriteriosOutput> getCriteriosAspirante(@PathVariable Integer idAspirante) {
        return ResponseEntity.ok(processor.getCriteriosAspirante(idAspirante));
    }

    @GetMapping("/{idAspirante}/estado-proceso")
    public ResponseEntity<List<PasoProcesoOutput>> getEstadoProceso(@PathVariable Integer idAspirante) {
        List<PasoProcesoOutput> pasos = processor.getPasosProceso(idAspirante);
        return ResponseEntity.ok(pasos);
    }

    @GetMapping("/{idAspirante}/documentos")
    public ResponseEntity<AspiranteDocumentosOutput> getDocumentosDeAspirante(
            @PathVariable Integer idAspirante) {
        return ResponseEntity.ok(documentoProcessor.getDocumentosDeAspirante(idAspirante));
    }

    @GetMapping("/{idAspirante}/documentos/requeridos")
    public ResponseEntity<List<DocumentoRequeridoOutput>> getDocumentosRequeridos(
            @PathVariable Integer idAspirante) {
        try {
            Integer idCohorte = aspiranteService.findById(idAspirante).getIdCohorte();
            return ResponseEntity.ok(documentosRequeridosPE.findByIdCohorte(idCohorte, idAspirante));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @PostMapping(value = "/{idAspirante}/documentos/requeridos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoOutput> subirDocumentoRequerido(
            @PathVariable Integer idAspirante,
            @RequestParam(required = false) Integer idDocumentosrequisitoconsejocohorte,
            @RequestParam(required = false) Integer idDocumentosrequisitoprogramacohorte,
            @RequestParam("file") MultipartFile file) {

        boolean tieneConsejo = idDocumentosrequisitoconsejocohorte != null;
        boolean tienePrograma = idDocumentosrequisitoprogramacohorte != null;
        if (tieneConsejo == tienePrograma) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe indicar exactamente un tipo de requisito.");
        }

        Integer idCohorte = aspiranteService.findById(idAspirante).getIdCohorte();
        LocalDate fechafin = cohorteService.findById(idCohorte).getPlazo().getFechafin();
        if (LocalDate.now().isAfter(fechafin)) {
            throw new DomainException(
                    AspiranteErrorCode.DOCUMENTACION_FUERA_DE_PLAZO_FORBIDDEN,
                    java.util.Map.of("idAspirante", idAspirante, "idCohorte", idCohorte, "fechaLimite", fechafin));
        }

        Optional<DocumentoDTO> existing = tieneConsejo
                ? documentoService.findByIdAspiranteAndIdDocumentosrequisitoconsejocohorte(
                        idAspirante, idDocumentosrequisitoconsejocohorte)
                : documentoService.findByIdAspiranteAndIdDocumentosrequisitoprogramacohorte(
                        idAspirante, idDocumentosrequisitoprogramacohorte);

        if (existing.isPresent()) {
            throw new DomainException(
                    AspiranteErrorCode.DOCUMENTO_REQUERIDO_YA_EXISTE_CONFLICT,
                    buildDocumentoRequeridoDetails(idAspirante,
                            idDocumentosrequisitoconsejocohorte,
                            idDocumentosrequisitoprogramacohorte));
        }

        S3Service.UploadResult upload = s3Service.uploadFile(file);
        DocumentoDTO doc = buildDocumentoRequeridoDto(idAspirante,
                idDocumentosrequisitoconsejocohorte,
                idDocumentosrequisitoprogramacohorte,
                upload);
        String nombreDocumento = documentoProcessor.resolverNombreTitulo(doc);
        PersonaDTO persona = personaService.findById(aspiranteService.findById(idAspirante).getIdPersona());
        try {
            sesService.enviarCorreo(persona.getCorreo(), EmailTemplates.ASUNTO_SUBIDA_DOCUMENTO,
                    EmailTemplates.cuerpoSubidaDocumento(persona.getNombres(), nombreDocumento, LocalDate.now()));
        } catch (Exception ex) {
            log.error("[DOCUMENTO_EMAIL] Fallo al enviar correo de subida de documento a '{}': {}",
                    persona.getCorreo(), ex.getMessage(), ex);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(toDocumentoOutput(documentoService.create(doc)));
    }

    @PatchMapping(value = "/{idAspirante}/documentos/requeridos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoOutput> actualizarDocumentoRequerido(
            @PathVariable Integer idAspirante,
            @RequestParam(required = false) Integer idDocumentosrequisitoconsejocohorte,
            @RequestParam(required = false) Integer idDocumentosrequisitoprogramacohorte,
            @RequestParam("file") MultipartFile file) {

        boolean tieneConsejo = idDocumentosrequisitoconsejocohorte != null;
        boolean tienePrograma = idDocumentosrequisitoprogramacohorte != null;
        if (tieneConsejo == tienePrograma) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe indicar exactamente un tipo de requisito.");
        }

        Integer idCohorte = aspiranteService.findById(idAspirante).getIdCohorte();
        LocalDate fechafin = cohorteService.findById(idCohorte).getPlazo().getFechafin();
        if (LocalDate.now().isAfter(fechafin)) {
            throw new DomainException(
                    AspiranteErrorCode.DOCUMENTACION_FUERA_DE_PLAZO_FORBIDDEN,
                    java.util.Map.of("idAspirante", idAspirante, "idCohorte", idCohorte, "fechaLimite", fechafin));
        }

        Optional<DocumentoDTO> existing = tieneConsejo
                ? documentoService.findByIdAspiranteAndIdDocumentosrequisitoconsejocohorte(
                        idAspirante, idDocumentosrequisitoconsejocohorte)
                : documentoService.findByIdAspiranteAndIdDocumentosrequisitoprogramacohorte(
                        idAspirante, idDocumentosrequisitoprogramacohorte);

        S3Service.UploadResult upload = s3Service.uploadFile(file);
        DocumentoDTO doc = buildDocumentoRequeridoDto(idAspirante,
                idDocumentosrequisitoconsejocohorte,
                idDocumentosrequisitoprogramacohorte,
                upload);

        DocumentoDTO saved = existing
                .map(documento -> documentoService.update(documento.getId(), doc))
                .orElseGet(() -> documentoService.create(doc));

        return ResponseEntity.ok(toDocumentoOutput(saved));
    }

    @PatchMapping(value = "/{idAspirante}/pagos/inscripcion/factura", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> corregirFacturaInscripcion(@PathVariable Integer idAspirante,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Archivo inválido");
            }
            var recibo = pagoreciboinscripcionService.findLastRejectedByIdAspirante(idAspirante);
            if (recibo == null) {
                throw new DomainException(PagoErrorCode.PAGO_NOT_FOUND, idAspirante);
            }
            String tipoEstado = recibo.getEstado() != null ? recibo.getEstado().getTipo() : null;
            if (!"RECHAZADO".equalsIgnoreCase(tipoEstado)) {
                throw new DomainException(PagoErrorCode.PAGO_NOT_FOUND, recibo.getId());
            }
            var upload = s3Service.uploadFile(file);
            recibo.setUrlfactura(upload.enlaceurl());
            pagoreciboinscripcionService.update(recibo.getId(), recibo);
            return ResponseEntity.ok().build();
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error corrigiendo factura de inscripción: " + e.getMessage(), e);
        }
    }

    @PatchMapping(value = "/{idAspirante}/pagos/matricula/factura", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> corregirFacturaMatricula(@PathVariable Integer idAspirante,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Archivo inválido");
            }
            var recibo = pagorecibomatriculaService.findLastRejectedByIdAspirante(idAspirante);
            if (recibo == null) {
                throw new DomainException(PagoErrorCode.PAGO_NOT_FOUND, idAspirante);
            }
            String tipoEstado = recibo.getEstado() != null ? recibo.getEstado().getTipo() : null;
            if (!"RECHAZADO".equalsIgnoreCase(tipoEstado)) {
                throw new DomainException(PagoErrorCode.PAGO_NOT_FOUND, recibo.getId());
            }
            var upload = s3Service.uploadFile(file);
            recibo.setUrlfactura(upload.enlaceurl());
            pagorecibomatriculaService.update(recibo.getId(), recibo);
            return ResponseEntity.ok().build();
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error corrigiendo factura de matrícula: " + e.getMessage(), e);
        }
    }

    private java.util.Map<String, Object> buildDocumentoRequeridoDetails(
            Integer idAspirante,
            Integer idDocumentosrequisitoconsejocohorte,
            Integer idDocumentosrequisitoprogramacohorte) {
        java.util.Map<String, Object> details = new HashMap<>();
        details.put("idAspirante", idAspirante);
        details.put("idDocumentosrequisitoconsejocohorte", idDocumentosrequisitoconsejocohorte);
        details.put("idDocumentosrequisitoprogramacohorte", idDocumentosrequisitoprogramacohorte);
        return details;
    }

    private DocumentoDTO buildDocumentoRequeridoDto(
            Integer idAspirante,
            Integer idDocumentosrequisitoconsejocohorte,
            Integer idDocumentosrequisitoprogramacohorte,
            S3Service.UploadResult upload) {
        Integer idEstadoPendiente = estadodocumentoService.findByEstado("PENDIENTE").getId();
        return DocumentoDTO.builder()
                .enlaceurl(upload.enlaceurl())
                .keyfile(upload.keyfile())
                .fechacargue(LocalDate.now())
                .idAspirante(idAspirante)
                .idEstadodocumento(idEstadoPendiente)
                .idDocumentosrequisitoconsejocohorte(idDocumentosrequisitoconsejocohorte)
                .idDocumentosrequisitoprogramacohorte(idDocumentosrequisitoprogramacohorte)
                .build();
    }

    private DocumentoOutput toDocumentoOutput(DocumentoDTO dto) {
        return DocumentoOutput.builder()
                .id(dto.getId())
                .enlaceurl(dto.getEnlaceurl())
                .keyfile(dto.getKeyfile())
                .fechacargue(dto.getFechacargue())
                .idAspirante(dto.getIdAspirante())
                .idEstadodocumento(dto.getIdEstadodocumento())
                .idDocumentosrequisitoconsejocohorte(dto.getIdDocumentosrequisitoconsejocohorte())
                .idDocumentosrequisitoprogramacohorte(dto.getIdDocumentosrequisitoprogramacohorte())
                .build();
    }

    @GetMapping("/{idAspirante}/entrevistas")
    public ResponseEntity<List<EntrevistaResumenOutput>> getEntrevistasByAspirante(
            @PathVariable Integer idAspirante) {
        return ResponseEntity.ok(entrevistaProcessor.findByIdAspirante(new ASPIRANTE_FIND(idAspirante)));
    }

    @PatchMapping("/entrevistas/{idEntrevista}/aceptar")
    public ResponseEntity<EntrevistaSimpleOutput> aceptarEntrevista(
            @PathVariable Integer idEntrevista) {
        EntrevistaOutput o = entrevistaProcessor.confirmInterview(new ENTREVISTA_FIND(idEntrevista));
        return ResponseEntity.ok(toSimpleEntrevista(o));
    }

    @PatchMapping(value = "/entrevistas/{idEntrevista}/solicitar-cambio", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntrevistaSimpleOutput> solicitarCambioEntrevista(
            @PathVariable Integer idEntrevista,
            @RequestBody ENTREVISTA_REQUEST_CHANGE body) {
        EntrevistaOutput o = entrevistaProcessor.requestChangeInterview(idEntrevista, body.motivocambio());
        return ResponseEntity.ok(EntrevistaSimpleOutput.builder()
                .idEntrevista(o.id())
                .idAspirante(o.idAspirante())
                .estado(o.estado() != null ? o.estado().tipo() : null)
                .motivocambio(o.motivocambio())
                .build());
    }

    @PatchMapping(value = "/entrevistas/{idEntrevista}/cancelar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntrevistaSimpleOutput> cancelarEntrevista(
            @PathVariable Integer idEntrevista,
            @RequestBody ENTREVISTA_CANCELAR_REQUEST body) {
        EntrevistaOutput o = entrevistaProcessor.cancelInterview(idEntrevista, body.motivocambio(), "ASPIRANTE");
        return ResponseEntity.ok(EntrevistaSimpleOutput.builder()
                .idEntrevista(o.id())
                .idAspirante(o.idAspirante())
                .estado(o.estado() != null ? o.estado().tipo() : null)
                .motivocambio(o.motivocambio())
                .build());
    }

    private EntrevistaSimpleOutput toSimpleEntrevista(EntrevistaOutput o) {
        return EntrevistaSimpleOutput.builder()
                .idEntrevista(o.id())
                .idAspirante(o.idAspirante())
                .estado(o.estado() != null ? o.estado().tipo() : null)
                .motivocambio(null)
                .build();
    }

    @GetMapping("/{idAspirante}/pruebas")
    public ResponseEntity<List<PruebaResumenOutput>> getPruebasByAspirante(
            @PathVariable Integer idAspirante) {
        return ResponseEntity.ok(pruebaProcessor.findByIdAspirante(new ASPIRANTE_FIND(idAspirante)));
    }

    @PatchMapping("/pruebas/{idPrueba}/aceptar")
    public ResponseEntity<PruebaSimpleOutput> aceptarPrueba(@PathVariable Integer idPrueba) {
        return ResponseEntity.ok(pruebaProcessor.confirmarPrueba(idPrueba));
    }

    @PatchMapping(value = "/pruebas/{idPrueba}/solicitar-cambio", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PruebaSimpleOutput> solicitarCambioPrueba(
            @PathVariable Integer idPrueba,
            @RequestBody PRUEBA_CANCELAR_REQUEST body) {
        return ResponseEntity.ok(pruebaProcessor.solicitarCambioPrueba(idPrueba, body.motivocambio()));
    }

    @PatchMapping(value = "/pruebas/{idPrueba}/cancelar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PruebaSimpleOutput> cancelarPrueba(
            @PathVariable Integer idPrueba,
            @RequestBody PRUEBA_CANCELAR_REQUEST body) {
        return ResponseEntity.ok(pruebaProcessor.cancelarPrueba(idPrueba, body.motivocambio(), "ASPIRANTE"));
    }

    @PatchMapping("/{idAspirante}/confirmar-correo")
    public ResponseEntity<Void> confirmarCorreo(@PathVariable Integer idAspirante) {
        AspiranteDTO aspirante = aspiranteService.findById(idAspirante);
        if (aspirante == null) {
            throw new DomainException(AspiranteErrorCode.ASPIRANTE_NOT_FOUND, idAspirante);
        }
        String estadoActual = aspirante.getEstado() != null ? aspirante.getEstado().getTipo() : null;
        if (!"NO CONFIRMADO".equalsIgnoreCase(estadoActual)) {
            throw new DomainException(AspiranteErrorCode.ESTADO_TRANSICION_INVALIDA_CONFLICT, estadoActual);
        }
        EstadoDTO estadoNuevo = estadoService.findByTipoAndEntidad("INSCRITO", "aspirante");
        if (estadoNuevo == null) {
            throw new RuntimeException("Estado 'INSCRITO' para aspirante no encontrado en la base de datos");
        }
        aspiranteService.updateEstado(idAspirante, estadoNuevo.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{idAspirante}/enviar-confirmacion-correo")
    public ResponseEntity<Void> enviarConfirmacionCorreo(@PathVariable Integer idAspirante) {
        AspiranteDTO aspirante = aspiranteService.findById(idAspirante);
        if (aspirante == null) {
            throw new DomainException(AspiranteErrorCode.ASPIRANTE_NOT_FOUND, idAspirante);
        }
        String estadoActual = aspirante.getEstado() != null ? aspirante.getEstado().getTipo() : null;
        if (!"NO CONFIRMADO".equalsIgnoreCase(estadoActual)) {
            throw new DomainException(AspiranteErrorCode.ESTADO_TRANSICION_INVALIDA_CONFLICT, estadoActual);
        }
        String token = confirmationTokenService.generateToken(idAspirante);
        String enlace = baseUrl + "/api/application/case/aspirantes/confirmar-correo?token=" + token;
        PersonaDTO persona = personaService.findById(aspirante.getIdPersona());
        try {
            sesService.enviarCorreo(
                    persona.getCorreo(),
                    EmailTemplates.ASUNTO_CONFIRMACION_CORREO,
                    EmailTemplates.cuerpoConfirmacionCorreo(persona.getNombres(), enlace));
        } catch (Exception ex) {
            log.error("[CONFIRMACION_EMAIL] Fallo al enviar correo de confirmación a '{}': {}",
                    persona.getCorreo(), ex.getMessage(), ex);
            throw new RuntimeException("No se pudo enviar el correo de confirmación", ex);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{idAspirante}/correo")
    public ResponseEntity<java.util.Map<String, String>> getCorreo(@PathVariable Integer idAspirante) {
        AspiranteDTO aspirante = aspiranteService.findById(idAspirante);
        if (aspirante == null) {
            throw new DomainException(AspiranteErrorCode.ASPIRANTE_NOT_FOUND, idAspirante);
        }
        PersonaDTO persona = personaService.findById(aspirante.getIdPersona());
        return ResponseEntity.ok(java.util.Map.of("correo", persona.getCorreo()));
    }

    @PatchMapping(value = "/{idAspirante}/correo")
    public ResponseEntity<Void> actualizarCorreo(
            @PathVariable Integer idAspirante,
            @RequestParam("correoNuevo") String correoNuevo) {
        AspiranteDTO aspirante = aspiranteService.findById(idAspirante);
        if (aspirante == null) {
            throw new DomainException(AspiranteErrorCode.ASPIRANTE_NOT_FOUND, idAspirante);
        }
        if (correoNuevo == null || correoNuevo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El campo correoNuevo es requerido");
        }
        String correo = correoNuevo.trim().toLowerCase(java.util.Locale.ROOT);
        personaService.updateCorreo(aspirante.getIdPersona(), correo);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/confirmar-correo")
    public ResponseEntity<java.util.Map<String, String>> confirmarCorreoPorToken(@RequestParam String token) {
        Integer idAspirante;
        try {
            idAspirante = confirmationTokenService.validateAndExtract(token);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", ex.getMessage()));
        }
        AspiranteDTO aspirante = aspiranteService.findById(idAspirante);
        if (aspirante == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("error", "Aspirante no encontrado"));
        }
        String estadoActual = aspirante.getEstado() != null ? aspirante.getEstado().getTipo() : null;
        if (!"NO CONFIRMADO".equalsIgnoreCase(estadoActual)) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error",
                            "El correo ya fue confirmado o el aspirante no está en estado NO CONFIRMADO"));
        }
        EstadoDTO estadoNuevo = estadoService.findByTipoAndEntidad("INSCRITO", "aspirante");
        if (estadoNuevo == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Estado 'INSCRITO' no configurado en la base de datos"));
        }
        aspiranteService.updateEstado(idAspirante, estadoNuevo.getId());
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(java.net.URI.create(frontendLoginUrl))
                .build();
    }
}
