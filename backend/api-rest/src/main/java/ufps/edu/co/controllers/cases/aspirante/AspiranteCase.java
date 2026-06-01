package ufps.edu.co.controllers.cases.aspirante;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.AspiranteErrorCode;
// import ufps.edu.co.persistence.entities.PersonaEntity;
import ufps.edu.co.processor.cases.DocumentosrequisitoprogramaPE;
import ufps.edu.co.processor.crud.AspiranteProcessor;
import ufps.edu.co.processor.crud.DocumentoProcessor;
import ufps.edu.co.processor.crud.EntrevistaProcessor;
import ufps.edu.co.processor.crud.PruebaProcessor;
import ufps.edu.co.records.input.entity.AspiranteInput.ASPIRANTE_FIND;
// import ufps.edu.co.records.input.entity.DocumentoInput.DOCUMENTO_FIND;
import ufps.edu.co.records.input.entity.EntrevistaInput.ENTREVISTA_CANCELAR_REQUEST;
import ufps.edu.co.records.input.entity.EntrevistaInput.ENTREVISTA_FIND;
import ufps.edu.co.records.input.entity.EntrevistaInput.ENTREVISTA_REQUEST_CHANGE;
import ufps.edu.co.records.input.entity.PruebaInput.PRUEBA_CANCELAR_REQUEST;
import ufps.edu.co.records.output.entity.AspiranteCriteriosOutput;
import ufps.edu.co.records.output.entity.AspiranteDocumentosOutput;
import ufps.edu.co.records.output.entity.AspiranteIdOutput;
import ufps.edu.co.records.output.entity.DocumentoOutput;
import ufps.edu.co.records.output.entity.DocumentoRequeridoOutput;
import ufps.edu.co.rest.dto.DocumentoDTO;
import ufps.edu.co.rest.dto.PersonaDTO;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.rest.services.DocumentoService;
import ufps.edu.co.rest.services.EstadodocumentoService;
import ufps.edu.co.rest.services.PersonaService;
import ufps.edu.co.services.S3Service;
import ufps.edu.co.services.SESService;
import ufps.edu.co.utils.EmailTemplates;
import ufps.edu.co.records.output.entity.EntrevistaOutput;
import ufps.edu.co.records.output.entity.EntrevistaResumenOutput;
import ufps.edu.co.records.output.entity.EntrevistaSimpleOutput;
import ufps.edu.co.records.output.entity.PasoProcesoOutput;
import ufps.edu.co.records.output.entity.PruebaResumenOutput;
import ufps.edu.co.records.output.entity.PruebaSimpleOutput;
import ufps.edu.co.rest.dto.UsuarioDTO;
import ufps.edu.co.rest.services.AspiranteService;
import ufps.edu.co.rest.services.UsuarioService;

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
    private EntrevistaProcessor entrevistaProcessor;

    @Autowired
    private PruebaProcessor pruebaProcessor;

    @Autowired
    private SESService sesService;

    // @Autowired
    // private EmailTemplates emailTemplates;

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
        EntrevistaOutput o = entrevistaProcessor.cancelInterview(idEntrevista, body.motivocambio());
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
        return ResponseEntity.ok(pruebaProcessor.cancelarPrueba(idPrueba, body.motivocambio()));
    }
}
