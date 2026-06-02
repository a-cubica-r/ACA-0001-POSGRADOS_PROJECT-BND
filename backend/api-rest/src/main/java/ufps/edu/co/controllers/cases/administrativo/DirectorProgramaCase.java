package ufps.edu.co.controllers.cases.administrativo;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.DuplicateAdmisionException;
import ufps.edu.co.processor.crud.AspiranteProcessor;
import ufps.edu.co.processor.crud.CalificacioncriterioProcessor;
import ufps.edu.co.processor.crud.CriterioevaluacionProcessor;
import ufps.edu.co.processor.crud.CriteriocohorteProcessor;
import ufps.edu.co.processor.crud.DocumentoProcessor;
import ufps.edu.co.processor.crud.DocumentosrequisitoprogramacohorteProcessor;
import ufps.edu.co.processor.crud.EntrevistaProcessor;
import ufps.edu.co.processor.crud.ListaadmitidosProcessor;
import ufps.edu.co.processor.crud.PruebaProcessor;
import ufps.edu.co.records.output.entity.DocumentosrequisitoprogramacohorteOutput;
import ufps.edu.co.records.input.entity.AspiranteInput.ASPIRANTE_FIND;
import ufps.edu.co.records.input.entity.CalificacioncriterioInput.CALIFICACION_PUNTAJE_REQUEST;
import ufps.edu.co.records.input.entity.CohorteInput.COHORTE_DIRECTOR_CREATE;
import ufps.edu.co.records.input.entity.CohorteInput.COHORTE_DIRECTOR_UPDATE;
import ufps.edu.co.records.input.entity.CriterioevaluacionInput.CRITERIO_BULK_SAVE;
import ufps.edu.co.records.input.entity.CriterioevaluacionInput.CRITERIO_CREATE_BODY;
import ufps.edu.co.records.input.entity.CriterioevaluacionInput.CRITERIO_UPDATE_BODY;
import ufps.edu.co.records.input.entity.CriteriocohorteInput.CRITERIOCOHORTE_ASSIGN_BODY;
import ufps.edu.co.records.input.entity.CriteriocohorteInput.CRITERIOCOHORTE_PESO_UPDATE;
import ufps.edu.co.records.output.entity.CriteriocohorteOutput;
import ufps.edu.co.records.input.entity.CalificacioncriterioInput.CALIFICACIONCRITERIO_FIND_BY_ASPIRANTE;
import ufps.edu.co.records.input.entity.CalificacioncriterioInput.CALIFICACIONCRITERIO_UPDATE;
import ufps.edu.co.records.output.entity.CalificacionCriterioSimpleOutput;
import ufps.edu.co.records.output.entity.CalificacioncriterioOutput;
import ufps.edu.co.records.output.entity.AspiranteCohorteOutput;
import ufps.edu.co.records.output.entity.AspiranteDocumentosOutput;
import ufps.edu.co.records.output.entity.CriterioevaluacionOutput;
import ufps.edu.co.records.output.entity.DocumentoEstadoOutput;
import ufps.edu.co.records.output.entity.SuccessOutput;
import ufps.edu.co.rest.dto.AdmitidoDTO;
import ufps.edu.co.rest.dto.EstadoDTO;
import ufps.edu.co.rest.services.AdministrativoService;
import ufps.edu.co.rest.services.UsuarioService;
import ufps.edu.co.rest.services.ListaadmitidosService;
import ufps.edu.co.rest.services.AspiranteService;
import ufps.edu.co.rest.services.EstadoService;
import ufps.edu.co.records.input.entity.DocumentoInput.DOCUMENTO_FIND;
import ufps.edu.co.records.input.entity.DocumentoInput.DOCUMENTO_REJECT;
import ufps.edu.co.records.input.entity.EntrevistaInput.ENTREVISTA_CANCELAR_REQUEST;
import ufps.edu.co.records.input.entity.EntrevistaInput.ENTREVISTA_CREATE;
import ufps.edu.co.records.input.entity.EntrevistaInput.ENTREVISTA_FIND;
import ufps.edu.co.records.input.entity.EntrevistaInput.ENTREVISTA_RATE;
import ufps.edu.co.records.input.entity.EntrevistaInput.ENTREVISTA_REAGENDAR_REQUEST;
import ufps.edu.co.records.input.entity.EntrevistaInput.ENTREVISTA_RESCHEDULE;
import ufps.edu.co.records.input.entity.EntrevistaInput.ENTREVISTA_SCHEDULE_REQUEST;
import ufps.edu.co.records.input.entity.ListaadmitidosInput.GENERATE_LISTA;
import ufps.edu.co.records.input.entity.ListaadmitidosInput.ADMITIR_ASPIRANTE;
import ufps.edu.co.records.input.entity.ListaadmitidosInput.RECHAZAR_ASPIRANTE;
import ufps.edu.co.records.output.entity.AprobarDocumentoOutput;
import ufps.edu.co.records.output.entity.CohorteDetalleOutput;
import ufps.edu.co.records.output.entity.CohorteListadoOutput;
import ufps.edu.co.records.output.entity.CohorteResumenOutput;
import ufps.edu.co.records.output.entity.CriteriosCohorteOutput;
import ufps.edu.co.records.output.entity.ProgramaInicioOutput;
import ufps.edu.co.records.output.entity.RankingAdmitidosOutput;
import ufps.edu.co.records.output.entity.AspiranteCalificacionOutput;
import ufps.edu.co.records.output.entity.AspiranteCriteriosOutput;
import ufps.edu.co.records.output.entity.AspiranteOutput;
import ufps.edu.co.records.output.entity.DocumentoOutput;
import ufps.edu.co.records.output.entity.EntrevistaOutput;
import ufps.edu.co.records.output.entity.EntrevistaResumenOutput;
import ufps.edu.co.records.output.entity.EntrevistaSimpleOutput;
import ufps.edu.co.records.output.entity.ListaadmitidosOutput;
import ufps.edu.co.records.input.entity.PruebaInput.PRUEBA_CANCELAR_REQUEST;
import ufps.edu.co.records.input.entity.PruebaInput.PRUEBA_CREAR_REQUEST;
import ufps.edu.co.records.input.entity.PruebaInput.PRUEBA_EDITAR_REQUEST;
import ufps.edu.co.records.input.entity.PruebaInput.PRUEBA_REAGENDAR_REQUEST;
import ufps.edu.co.records.output.entity.PruebaResumenOutput;
import ufps.edu.co.records.output.entity.PruebaSimpleOutput;
import ufps.edu.co.services.S3Service;
import ufps.edu.co.records.output.entity.PagoreciboDirectorOutput;
import ufps.edu.co.records.input.entity.PagoEstadoInput;
import ufps.edu.co.rest.dto.PagoreciboinscripcionDTO;
import ufps.edu.co.rest.dto.PagorecibomatriculaDTO;
import ufps.edu.co.rest.dto.PagoDTO;
import ufps.edu.co.rest.dto.AspiranteDTO;
import ufps.edu.co.rest.dto.CohorteDTO;
import ufps.edu.co.rest.dto.PersonaDTO;
import ufps.edu.co.rest.services.PagoreciboinscripcionService;
import ufps.edu.co.rest.services.PagorecibomatriculaService;
import ufps.edu.co.rest.services.PagoService;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.processor.crud.PagoProcessor;
import ufps.edu.co.records.output.entity.PagoOutput;

@RestController
@RequestMapping("/director-programa")
public class DirectorProgramaCase {

    private static final Logger logger = LoggerFactory.getLogger(DirectorProgramaCase.class);

    @Autowired
    private S3Service s3Service;

    @Autowired
    private DocumentoProcessor documentoProcessor;

    @Autowired
    private AspiranteProcessor aspiranteProcessor;

    @Autowired
    private EntrevistaProcessor entrevistaProcessor;

    @Autowired
    private ListaadmitidosProcessor listaadmitidosProcessor;

    @Autowired
    private ListaadmitidosService listaadmitidosService;

    @Autowired
    private AspiranteService aspiranteService;

    @Autowired
    private EstadoService estadoService;

    @Autowired
    private CalificacioncriterioProcessor calificacioncriterioProcessor;

    @Autowired
    private PruebaProcessor pruebaProcessor;

    @Autowired
    private CriterioevaluacionProcessor criterioevaluacionProcessor;

    @Autowired
    private CriteriocohorteProcessor criteriocohorteProcessor;

    @Autowired
    private DocumentosrequisitoprogramacohorteProcessor documentosrequisitoprogramacohorteProcessor;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AdministrativoService administrativoService;

    @Autowired
    private PagoreciboinscripcionService pagoreciboinscripcionService;

    @Autowired
    private PagorecibomatriculaService pagorecibomatriculaService;

    @Autowired
    private PagoService pagoService;

    @Autowired
    private CohorteService cohorteService;

    @Autowired
    private PagoProcessor pagoProcessor;

    @GetMapping(value = "/cohortes")
    public ResponseEntity<List<CohorteResumenOutput>> getCohortesByPrograma() {
        try {
            Integer programaId = resolvePrograma();
            return ResponseEntity.ok(aspiranteProcessor.getCohortesByProgramaResumen(programaId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/cohortes/{idCohorte}/aspirantes")
    public ResponseEntity<List<AspiranteCohorteOutput>> getAspirantesByCohorte(@PathVariable Integer idCohorte) {
        try {
            return ResponseEntity.ok(aspiranteProcessor.findByCohorteConResumen(idCohorte));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/aspirantes/{idAspirante}/documentos")
    public ResponseEntity<AspiranteDocumentosOutput> getDocumentosDeAspirante(@PathVariable Integer idAspirante) {
        try {
            return ResponseEntity.ok(documentoProcessor.getDocumentosDeAspiranteParaDirector(idAspirante));
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error obteniendo documentos del aspirante {}", idAspirante, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/pagos/inscripcion")
    public ResponseEntity<List<PagoreciboDirectorOutput>> listPagosInscripcion() {
        try {
            Integer programaId = resolvePrograma();
            List<PagoreciboinscripcionDTO> recibos = pagoreciboinscripcionService.findByProgramaId(programaId);
            var outputs = recibos.stream().map(r -> {
                try {
                    if (r == null) return null;
                    PagoDTO pago = r.getPago() != null ? r.getPago() : (r.getIdPago() != null ? pagoService.findById(r.getIdPago()) : null);
                    if (pago == null) return null;
                    AspiranteDTO aspirante = pago.getAspirante() != null ? pago.getAspirante() : (pago.getIdAspirante() != null ? aspiranteService.findById(pago.getIdAspirante()) : null);
                    if (aspirante == null) return null;

                    PersonaDTO persona = aspirante.getPersona();
                    String nombre = persona != null ? (persona.getNombres() + " " + persona.getApellidos()).trim() : null;
                    String estadoName = null;
                    if (r.getEstado() != null && r.getEstado().getTipo() != null) {
                        estadoName = r.getEstado().getTipo();
                    } else if (r.getIdEstado() != null) {
                        var est = estadoService.findById(r.getIdEstado());
                        if (est != null) estadoName = est.getTipo();
                    }

                    return PagoreciboDirectorOutput.builder()
                            .id(r.getId())
                            .idPago(r.getIdPago())
                            .idAspirante(pago.getIdAspirante())
                            .aspirante(nombre)
                            .fechavencimiento(r.getFechavencimiento())
                            .urlrecibo(r.getUrlrecibo())
                            .urlfactura(r.getUrlfactura())
                            .referenciapago(r.getReferenciapago())
                            .valorpago(r.getValorpago())
                            .idEstado(r.getIdEstado())
                            .estado(estadoName)
                            .build();
                } catch (Exception ex) {
                    logger.error("Error mapeando recibo inscripcion {}", r != null ? r.getId() : null, ex);
                    return null;
                }
            }).filter(Objects::nonNull).toList();

            return ResponseEntity.ok(outputs);
        } catch (Exception e) {
            logger.error("Error listando pagos de inscripción", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/pagos/matricula")
    public ResponseEntity<List<PagoreciboDirectorOutput>> listPagosMatricula() {
        try {
            Integer programaId = resolvePrograma();
            List<PagorecibomatriculaDTO> recibos = pagorecibomatriculaService.findByProgramaId(programaId);
            var outputs = recibos.stream().map(r -> {
                try {
                    if (r == null) return null;
                    PagoDTO pago = r.getPago() != null ? r.getPago() : (r.getIdPago() != null ? pagoService.findById(r.getIdPago()) : null);
                    if (pago == null) return null;
                    AspiranteDTO aspirante = pago.getAspirante() != null ? pago.getAspirante() : (pago.getIdAspirante() != null ? aspiranteService.findById(pago.getIdAspirante()) : null);
                    if (aspirante == null) return null;

                    PersonaDTO persona = aspirante.getPersona();
                    String nombre = persona != null ? (persona.getNombres() + " " + persona.getApellidos()).trim() : null;
                    String estadoName = null;
                    if (r.getEstado() != null && r.getEstado().getTipo() != null) {
                        estadoName = r.getEstado().getTipo();
                    } else if (r.getIdEstado() != null) {
                        var est = estadoService.findById(r.getIdEstado());
                        if (est != null) estadoName = est.getTipo();
                    }

                    return PagoreciboDirectorOutput.builder()
                            .id(r.getId())
                            .idPago(r.getIdPago())
                            .idAspirante(pago.getIdAspirante())
                            .aspirante(nombre)
                            .fechavencimiento(r.getFechavencimiento())
                            .urlrecibo(r.getUrlrecibo())
                            .urlfactura(r.getUrlfactura())
                            .referenciapago(r.getReferenciapago())
                            .valorpago(r.getValorpago())
                            .idEstado(r.getIdEstado())
                            .estado(estadoName)
                            .build();
                } catch (Exception ex) {
                    logger.error("Error mapeando recibo matricula {}", r != null ? r.getId() : null, ex);
                    return null;
                }
            }).filter(Objects::nonNull).toList();

            return ResponseEntity.ok(outputs);
        } catch (Exception e) {
            logger.error("Error listando pagos de matrícula", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping(value = "/pagos/inscripcion/{idRecibo}/aprobar")
    public ResponseEntity<PagoOutput> aprobarReciboInscripcion(@PathVariable Integer idRecibo) {
        try {
            PagoOutput out = pagoProcessor.approveReciboInscripcion(idRecibo);
            return ResponseEntity.ok(out);
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error aprobando recibo inscripción {}", idRecibo, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping(value = "/pagos/matricula/{idRecibo}/aprobar")
    public ResponseEntity<PagoOutput> aprobarReciboMatricula(@PathVariable Integer idRecibo) {
        try {
            PagoOutput out = pagoProcessor.approveReciboMatricula(idRecibo);
            return ResponseEntity.ok(out);
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error aprobando recibo matrícula {}", idRecibo, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping(value = "/pagos/inscripcion/{idRecibo}/estado", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagoreciboDirectorOutput> cambiarEstadoReciboInscripcion(@PathVariable Integer idRecibo,
            @RequestBody(required = false) PagoEstadoInput.PAGO_ESTADO_UPDATE request) {
        try {
            Integer nuevoIdEstado = request != null ? request.idEstado() : null;
            if (nuevoIdEstado == null) {
                var est = estadoService.findByTipoAndEntidad("RECHAZADO", "pagoinscripcion");
                if (est == null) est = estadoService.findByTipoAndEntidad("RECHAZADO", "PAGOINSCRIPCION");
                if (est == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                nuevoIdEstado = est.getId();
            }

            var pag = pagoProcessor.updateEstadoReciboInscripcion(idRecibo, nuevoIdEstado);
            if (pag == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

            PagoDTO pago = pag.getPago() != null && pag.getPago().getId() != null ? pagoService.findById(pag.getPago().getId()) : null;
            AspiranteDTO aspirante = pago != null && pago.getIdAspirante() != null ? aspiranteService.findById(pago.getIdAspirante()) : null;
            PersonaDTO persona = aspirante != null ? aspirante.getPersona() : null;
            String nombre = persona != null ? (persona.getNombres() + " " + persona.getApellidos()).trim() : null;

            String estadoName = null;
            if (pag.getEstado() != null && pag.getEstado().getTipo() != null) {
                estadoName = pag.getEstado().getTipo();
            } else if (pag.getIdEstado() != null) {
                var est = estadoService.findById(pag.getIdEstado());
                if (est != null) estadoName = est.getTipo();
            }

            PagoreciboDirectorOutput out = PagoreciboDirectorOutput.builder()
                    .id(pag.getId())
                    .idPago(pag.getIdPago())
                    .idAspirante(pago != null ? pago.getIdAspirante() : null)
                    .aspirante(nombre)
                    .fechavencimiento(pag.getFechavencimiento())
                    .urlrecibo(pag.getUrlrecibo())
                    .urlfactura(pag.getUrlfactura())
                    .referenciapago(pag.getReferenciapago())
                    .valorpago(pag.getValorpago())
                    .idEstado(pag.getIdEstado())
                    .estado(estadoName)
                    .build();

            return ResponseEntity.ok(out);
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error cambiando estado recibo inscripción {}", idRecibo, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping(value = "/pagos/matricula/{idRecibo}/estado", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagoreciboDirectorOutput> cambiarEstadoReciboMatricula(@PathVariable Integer idRecibo,
            @RequestBody(required = false) PagoEstadoInput.PAGO_ESTADO_UPDATE request) {
        try {
            Integer nuevoIdEstado = request != null ? request.idEstado() : null;
            if (nuevoIdEstado == null) {
                var est = estadoService.findByTipoAndEntidad("RECHAZADO", "pagomatricula");
                if (est == null) est = estadoService.findByTipoAndEntidad("RECHAZADO", "PAGOMATRICULA");
                if (est == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                nuevoIdEstado = est.getId();
            }

            var pag = pagoProcessor.updateEstadoReciboMatricula(idRecibo, nuevoIdEstado);
            if (pag == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

            PagoDTO pago = pag.getPago() != null && pag.getPago().getId() != null ? pagoService.findById(pag.getPago().getId()) : null;
            AspiranteDTO aspirante = pago != null && pago.getIdAspirante() != null ? aspiranteService.findById(pago.getIdAspirante()) : null;
            PersonaDTO persona = aspirante != null ? aspirante.getPersona() : null;
            String nombre = persona != null ? (persona.getNombres() + " " + persona.getApellidos()).trim() : null;

            String estadoName = null;
            if (pag.getEstado() != null && pag.getEstado().getTipo() != null) {
                estadoName = pag.getEstado().getTipo();
            } else if (pag.getIdEstado() != null) {
                var est = estadoService.findById(pag.getIdEstado());
                if (est != null) estadoName = est.getTipo();
            }

            PagoreciboDirectorOutput out = PagoreciboDirectorOutput.builder()
                    .id(pag.getId())
                    .idPago(pag.getIdPago())
                    .idAspirante(pago != null ? pago.getIdAspirante() : null)
                    .aspirante(nombre)
                    .fechavencimiento(pag.getFechavencimiento())
                    .urlrecibo(pag.getUrlrecibo())
                    .urlfactura(pag.getUrlfactura())
                    .referenciapago(pag.getReferenciapago())
                    .valorpago(pag.getValorpago())
                    .idEstado(pag.getIdEstado())
                    .estado(estadoName)
                    .build();

            return ResponseEntity.ok(out);
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error cambiando estado recibo matrícula {}", idRecibo, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping(value = "/documentos/{idDocumento}/aprobar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AprobarDocumentoOutput> aprobarDocumento(@PathVariable Integer idDocumento) {
        try {
            return ResponseEntity.ok(documentoProcessor.approveDocument(DOCUMENTO_FIND.builder().id(idDocumento).build()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping(value = "/documentos/{idDocumento}/rechazar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DocumentoEstadoOutput> rechazarDocumento(@PathVariable Integer idDocumento, @RequestBody String motivoRechazo){
        try{
            return ResponseEntity.ok(documentoProcessor.rejectDocument(DOCUMENTO_REJECT.builder().id(idDocumento).motivoRechazo(motivoRechazo).build()));
        }catch(Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/downloadByDocumentId", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> download(@RequestBody DOCUMENTO_FIND request) {
        try {
            byte[] fileContent = s3Service.downloadDocument(request);
            DocumentoOutput fileInfo = documentoProcessor.findById(request);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileInfo.keyfile() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(fileContent);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping(value = "/documentos/{idDocumento}/archivo")
    public ResponseEntity<byte[]> verDocumentoInline(@PathVariable Integer idDocumento) {
        try {
            DOCUMENTO_FIND req = DOCUMENTO_FIND.builder().id(idDocumento).build();
            var result = s3Service.downloadDocumentWithMetadata(req);
            DocumentoOutput fileInfo = documentoProcessor.findById(req);

            String filename = fileInfo != null ? fileInfo.keyfile() : result != null ? result.keyfile() : "file";
            String disposition = "inline";
            if (filename != null && !filename.isBlank()) {
                disposition = disposition + "; filename=\"" + filename.replace('"', '_') + "\"";
            }

            org.springframework.http.MediaType mediaType = org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
            if (result != null && result.contentType() != null && !result.contentType().isBlank()) {
                try {
                    mediaType = org.springframework.http.MediaType.parseMediaType(result.contentType());
                } catch (Exception e) {
                    // fallback to octet-stream
                }
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                    .contentType(mediaType)
                    .body(result != null ? result.content() : new byte[0]);
        } catch (Exception e) {
            logger.error("Error visualizando documento inline {}: {}", idDocumento, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/aspirantsWithDocuments")
    public ResponseEntity<List<AspiranteOutput>> findAspirantsWithDocuments() {
        try {
            List<AspiranteOutput> outputs = aspiranteProcessor.findWithDocuments();
            return ResponseEntity.ok(outputs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/aspirantes/{idAspirante}/criterios")
    public ResponseEntity<AspiranteCriteriosOutput> getCriteriosCalificacionByAspirantId(
            @PathVariable Integer idAspirante) {
        try {
            return ResponseEntity.ok(aspiranteProcessor.findCriteriosCalificacion(new ASPIRANTE_FIND(idAspirante)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/calificacion/programa/cohorte/{idCohorte}/aspirante-validados")
    public ResponseEntity<List<AspiranteCalificacionOutput>> findAllValidadosCalificacion(
            @PathVariable Integer idCohorte) {
        try {
            return ResponseEntity.ok(aspiranteProcessor.findAllValidadosCalificacion(idCohorte));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/calificacion/programa/cohorte/{idCohorte}/aspirante-validados/count")
    public ResponseEntity<Long> countValidados(@PathVariable Integer idCohorte) {
        try {
            return ResponseEntity.ok(aspiranteProcessor.countValidados(idCohorte));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/calificacion/programa/cohorte/{idCohorte}/aspirante-validados/count/por-calificar")
    public ResponseEntity<Long> countPorCalificar(@PathVariable Integer idCohorte) {
        try {
            return ResponseEntity.ok(aspiranteProcessor.countPorCalificar(idCohorte));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/calificacion/programa/cohorte/{idCohorte}/aspirante-validados/count/calificados")
    public ResponseEntity<Long> countCalificados(@PathVariable Integer idCohorte) {
        try {
            return ResponseEntity.ok(aspiranteProcessor.countCalificados(idCohorte));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/programa/director/{idUsuario}")
    public ResponseEntity<Map<String, Integer>> findProgramaByUsuarioDirector(@PathVariable Integer idUsuario) {
        try {
            Integer idPersona = usuarioService.findIdPersonaById(idUsuario);
            if (idPersona == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            Integer idPrograma = administrativoService.findIdProgramaByIdPersona(idPersona);
            if (idPrograma == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(Map.of("idPrograma", idPrograma));
        } catch (Exception e) {
            logger.error("Error obteniendo el id del programa del director para el usuario {}", idUsuario, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/cohorte/{cohorteId}/criterios")
    public ResponseEntity<CriteriosCohorteOutput> getCriteriosByCohorte(@PathVariable Integer cohorteId) {
        try {
            return ResponseEntity.ok(aspiranteProcessor.getCriteriosByCohorte(cohorteId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/programa/{programaId}/cohortes", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CohorteListadoOutput> createCohorte(
            @PathVariable Integer programaId,
            @Valid @RequestBody COHORTE_DIRECTOR_CREATE body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(aspiranteProcessor.createCohorte(programaId, body));
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/programa/{programaId}/cohortes")
    public ResponseEntity<List<CohorteResumenOutput>> listCohorteResumen(@PathVariable Integer programaId) {
        try {
            return ResponseEntity.ok(aspiranteProcessor.getCohortesByProgramaResumen(programaId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/cohorte/{cohorteId}/abrir")
    public ResponseEntity<CohorteListadoOutput> abrirCohorte(@PathVariable Integer cohorteId) {
        try {
            return ResponseEntity.ok(aspiranteProcessor.abrirCohorte(cohorteId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/cohorte/{cohorteId}/cerrar")
    public ResponseEntity<CohorteListadoOutput> cerrarCohorte(@PathVariable Integer cohorteId) {
        try {
            return ResponseEntity.ok(aspiranteProcessor.cerrarCohorte(cohorteId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping(value = "/cohorte/{cohorteId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CohorteListadoOutput> updateCohorte(
            @PathVariable Integer cohorteId,
            @RequestBody COHORTE_DIRECTOR_UPDATE body) {
        try {
            return ResponseEntity.ok(aspiranteProcessor.updateCohorte(cohorteId, body));
        } catch (DomainException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            logger.warn("Solicitud invalida para actualizar cohorte {}: {}", cohorteId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/cohorte/{cohorteId}/admitidos/ranking")
    public ResponseEntity<RankingAdmitidosOutput> getRankingAdmitidos(@PathVariable Integer cohorteId) {
        try {
            return ResponseEntity.ok(aspiranteProcessor.getRankingAdmitidos(cohorteId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/programa/{programaId}/inicio")
    public ResponseEntity<List<ProgramaInicioOutput>> getProgramaInicio(@PathVariable Integer programaId) {
        try {
            return ResponseEntity.ok(aspiranteProcessor.getProgramaInicioByPrograma(programaId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/calificacion/criterio/aspirante", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CalificacioncriterioOutput>> findCalificacionesByAspirante(
            @RequestBody CALIFICACIONCRITERIO_FIND_BY_ASPIRANTE request) {
        try {
            return ResponseEntity.ok(calificacioncriterioProcessor.findByIdAspirante(request.idAspirante()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping(value = "/aspirantes/{idAspirante}/criterios/{idCriterio}/calificar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CalificacionCriterioSimpleOutput> calificarCriterio(
            @PathVariable Integer idAspirante,
            @PathVariable Integer idCriterio,
            @RequestBody CALIFICACION_PUNTAJE_REQUEST request) {
        try {
            return ResponseEntity.ok(
                    calificacioncriterioProcessor.calificarCriterio(idAspirante, idCriterio,
                            request.puntajeObtenido()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping(value = "/calificacion/criterio/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CalificacioncriterioOutput> updateCalificacionCriterio(
            @RequestBody CALIFICACIONCRITERIO_UPDATE request) {
        try {
            return ResponseEntity.ok(calificacioncriterioProcessor.update(request));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/cohorte/{cohorteId}")
    public ResponseEntity<CohorteDetalleOutput> getCohorteDetalle(@PathVariable Integer cohorteId) {
        try {
            return ResponseEntity.ok(aspiranteProcessor.getCohorteDetalle(cohorteId));
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/cohorte/{cohorteId}/aspirantes/paz-y-salvo")
    public ResponseEntity<List<AspiranteOutput>> findPazYSalvoByCohorte(@PathVariable Integer cohorteId) {
        try {
            return ResponseEntity.ok(aspiranteProcessor.findPazYSalvoByCohorte(cohorteId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/programa/{programaId}/cohorte/{cohorteId}/criterios", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CriterioevaluacionOutput> createCriterio(
            @PathVariable Integer programaId,
            @PathVariable Integer cohorteId,
            @RequestBody CRITERIO_CREATE_BODY body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(criterioevaluacionProcessor.createForCohorte(programaId, cohorteId, body));
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping(value = "/programa/{programaId}/cohorte/{cohorteId}/criterios/{criterioId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CriterioevaluacionOutput> updateCriterio(
            @PathVariable Integer programaId,
            @PathVariable Integer cohorteId,
            @PathVariable Integer criterioId,
            @RequestBody CRITERIO_UPDATE_BODY body) {
        try {
            return ResponseEntity.ok(
                    criterioevaluacionProcessor.updateForCohorte(programaId, cohorteId, criterioId, body));
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/programa/{programaId}/cohorte/{cohorteId}/criterios/{criterioId}")
    public ResponseEntity<SuccessOutput> deleteCriterio(
            @PathVariable Integer programaId,
            @PathVariable Integer cohorteId,
            @PathVariable Integer criterioId) {
        try {
            criterioevaluacionProcessor.deleteForCohorte(programaId, cohorteId, criterioId);
            return ResponseEntity.ok(SuccessOutput.builder().success(true).build());
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/programa/{programaId}/cohorte/{cohorteId}/criterios/save", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuccessOutput> bulkSaveCriterios(
            @PathVariable Integer programaId,
            @PathVariable Integer cohorteId,
            @RequestBody CRITERIO_BULK_SAVE body) {
        try {
            criterioevaluacionProcessor.bulkSaveForCohorte(programaId, cohorteId, body);
            return ResponseEntity.ok(SuccessOutput.builder().success(true).build());
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/cohorte/{idCohorte}/admitidos/lista")
    public ResponseEntity<List<ListaadmitidosOutput>> getAdmitidosByCohorte(@PathVariable Integer idCohorte) {
        try {
            return ResponseEntity.ok(listaadmitidosProcessor.findByIdCohorte(idCohorte));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("{idCohorte}/generateAdmittedList")
    public ResponseEntity<byte[]> generateAdmittedList(@PathVariable Integer idCohorte) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Integer idPersona = usuarioService.findIdPersonaByNombreusuario(username);
            var admin = administrativoService.findByIdPersona(idPersona);
            String directorNombre = (admin != null && admin.getPersona() != null)
                    ? admin.getPersona().getNombres() + " " + admin.getPersona().getApellidos()
                    : "Director de Programa";

            byte[] pdf = listaadmitidosProcessor.generarPdfAdmitidos(idCohorte, directorNombre);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"admitidos_cohorte_" + idCohorte + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/cohorte/{cohorteId}/admitidos/{aspiranteId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> admitAspirant(
            @PathVariable Integer cohorteId,
            @PathVariable Integer aspiranteId,
            @RequestBody ADMITIR_ASPIRANTE body) {
        try {
            Boolean admitido = body != null ? body.admitido() : Boolean.TRUE;
            if (admitido == null) {
                admitido = Boolean.TRUE;
            }

            Integer idCohorte = cohorteId;

            if (!admitido) {

                EstadoDTO estadoValidadoCalificado = estadoService.findByTipoAndEntidad("VALIDADO_CALIFICADO",
                        "aspirante");
                if (estadoValidadoCalificado == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }

                aspiranteService.updateEstado(aspiranteId, estadoValidadoCalificado.getId());

                //
                // Si el aspirante estaba previamente en la lista de admitidos, eliminar ese
                // registro
                try {
                    if (listaadmitidosService.existsByIdCohorteAndIdAspirante(idCohorte, aspiranteId)) {
                        listaadmitidosService.deleteByIdCohorteAndIdAspirante(idCohorte, aspiranteId);
                    }
                } catch (Exception ex) {
                    //
                    // No detener el flujo por errores al limpiar la lista de admitidos, solo
                    // loguear

                    logger.error("Error eliminando registro de lista de admitidos para cohorte {} aspirante {}",
                            idCohorte, aspiranteId, ex);
                }

                Map<String, Object> resp = Map.of(
                        "success", true,
                        "aspiranteId", aspiranteId.toString(),
                        "admitido", false);
                return ResponseEntity.ok(resp);
            }

            EstadoDTO estadoAdmitido = estadoService.findByTipoAndEntidad("ADMITIDO", "aspirante");
            if (estadoAdmitido == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            aspiranteService.updateEstado(aspiranteId, estadoAdmitido.getId());

            if (listaadmitidosService.existsByIdCohorteAndIdAspirante(idCohorte, aspiranteId)) {
                Map<String, Object> resp = Map.of(
                        "success", true,
                        "aspiranteId", aspiranteId.toString(),
                        "admitido", true);
                return ResponseEntity.ok(resp);
            }

            AdmitidoDTO dto = new AdmitidoDTO();
            dto.setIdCohorte(idCohorte);
            dto.setIdAspirante(aspiranteId);
            dto.setFechageneracion(LocalDate.now());
            listaadmitidosService.create(dto);

            Map<String, Object> resp = Map.of(
                    "success", true,
                    "aspiranteId", aspiranteId.toString(),
                    "admitido", true);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/admitAspirants", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ListaadmitidosOutput>> admitirAspirantes(@RequestBody GENERATE_LISTA request) {
        try {
            return ResponseEntity.ok(listaadmitidosProcessor.admitirAspirantes(request));
        } catch (DomainException e) {
            throw e;
        } catch (DuplicateAdmisionException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al admitir aspirantes para cohorte {}", request.idCohorte(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/rejectAspirant", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ListaadmitidosOutput>> rechazarAspirante(@RequestBody RECHAZAR_ASPIRANTE request) {
        try {
            List<ListaadmitidosOutput> outputs = listaadmitidosProcessor.rechazarAspirante(request);
            return ResponseEntity.ok(outputs);
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/aspirantes/{idAspirante}/entrevistas")
    public ResponseEntity<List<EntrevistaResumenOutput>> findInterviewsByAspirantId(
            @PathVariable Integer idAspirante) {
        try {
            return ResponseEntity.ok(entrevistaProcessor.findByIdAspirante(new ASPIRANTE_FIND(idAspirante)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/aspirantes/{idAspirante}/entrevistas/agendar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntrevistaResumenOutput> scheduleInterview(
            @PathVariable Integer idAspirante,
            @Valid @RequestBody ENTREVISTA_SCHEDULE_REQUEST request) {
        try {
            ENTREVISTA_CREATE create = new ENTREVISTA_CREATE(
                    request.fecha(), request.tiempo(), request.idTipoentrevista(),
                    idAspirante, request.ubicacion(), null);
            return ResponseEntity.ok(toResumen(entrevistaProcessor.create(create)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping(value = "/entrevistas/{idEntrevista}/reagendar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntrevistaResumenOutput> rescheduleInterview(
            @PathVariable Integer idEntrevista,
            @RequestBody ENTREVISTA_REAGENDAR_REQUEST request) {
        try {
            ENTREVISTA_RESCHEDULE reschedule = new ENTREVISTA_RESCHEDULE(
                    idEntrevista, request.fecha(), request.tiempo(), request.idTipoentrevista(),
                    request.ubicacion(), null);
            return ResponseEntity.ok(toResumen(entrevistaProcessor.reschedule(reschedule)));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping(value = "/entrevistas/{idEntrevista}/editar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntrevistaResumenOutput> editarEntrevista(
            @PathVariable Integer idEntrevista,
            @Valid @RequestBody ENTREVISTA_REAGENDAR_REQUEST request) {
        try {
            return ResponseEntity.ok(toResumen(entrevistaProcessor.editEntrevista(idEntrevista, request)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping(value = "/entrevistas/{idEntrevista}/completar")
    public ResponseEntity<EntrevistaSimpleOutput> completeInterview(
            @PathVariable Integer idEntrevista) {
        try {
            return ResponseEntity
                    .ok(toSimple(entrevistaProcessor.completeInterview(new ENTREVISTA_FIND(idEntrevista))));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping(value = "/entrevistas/{idEntrevista}/cancelar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntrevistaSimpleOutput> cancelInterview(
            @PathVariable Integer idEntrevista,
            @RequestBody ENTREVISTA_CANCELAR_REQUEST request) {
        try {
            EntrevistaOutput o = entrevistaProcessor.cancelInterview(idEntrevista, request.motivocambio(), "DIRECTOR");
            return ResponseEntity.ok(EntrevistaSimpleOutput.builder()
                    .idEntrevista(o.id())
                    .idAspirante(o.idAspirante())
                    .estado(o.estado() != null ? o.estado().tipo() : null)
                    .motivocambio(o.motivocambio())
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // --- Pruebas ---

    @GetMapping("/aspirantes/{idAspirante}/pruebas")
    public ResponseEntity<List<PruebaResumenOutput>> findPruebasByAspiranteId(
            @PathVariable Integer idAspirante) {
        try {
            return ResponseEntity.ok(pruebaProcessor.findByIdAspirante(new ASPIRANTE_FIND(idAspirante)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/aspirantes/{idAspirante}/pruebas/crear", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PruebaResumenOutput> crearPrueba(
            @PathVariable Integer idAspirante,
            @RequestBody PRUEBA_CREAR_REQUEST request) {
        try {
            return ResponseEntity.ok(pruebaProcessor.crearPrueba(idAspirante, request));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping(value = "/pruebas/{idPrueba}/reagendar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PruebaResumenOutput> reagendarPrueba(
            @PathVariable Integer idPrueba,
            @Valid @RequestBody PRUEBA_REAGENDAR_REQUEST request) {
        try {
            return ResponseEntity.ok(pruebaProcessor.reagendarPrueba(idPrueba, request));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping(value = "/pruebas/{idPrueba}/editar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PruebaResumenOutput> editarPrueba(
            @PathVariable Integer idPrueba,
            @RequestBody PRUEBA_EDITAR_REQUEST request) {
        try {
            return ResponseEntity.ok(pruebaProcessor.editPrueba(idPrueba, request));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/pruebas/{idPrueba}/completar")
    public ResponseEntity<PruebaSimpleOutput> completarPrueba(@PathVariable Integer idPrueba) {
        try {
            return ResponseEntity.ok(pruebaProcessor.completarPrueba(idPrueba));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping(value = "/pruebas/{idPrueba}/cancelar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PruebaSimpleOutput> cancelarPrueba(
            @PathVariable Integer idPrueba,
            @RequestBody PRUEBA_CANCELAR_REQUEST request) {
        try {
            return ResponseEntity.ok(pruebaProcessor.cancelarPrueba(idPrueba, request.motivocambio(), "DIRECTOR"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    private EntrevistaResumenOutput toResumen(EntrevistaOutput o) {
        return EntrevistaResumenOutput.builder()
                .id(o.id())
                .fecha(o.fecha())
                .tiempo(o.tiempo())
                .idEstado(o.idEstado())
                .estado(o.estado() != null ? o.estado().tipo() : null)
                .idTipoentrevista(o.idTipoentrevista())
                .tipoentrevista(o.tipoentrevista() != null ? o.tipoentrevista().tipo() : null)
                .ubicacion(o.ubicacion() != null ? o.ubicacion().direccion() : null)
                .motivocambio(o.motivocambio())
                .build();
    }

    private EntrevistaSimpleOutput toSimple(EntrevistaOutput o) {
        return EntrevistaSimpleOutput.builder()
                .idEntrevista(o.id())
                .idAspirante(o.idAspirante())
                .estado(o.estado() != null ? o.estado().tipo() : null)
                .motivocambio(null)
                .build();
    }

    private Integer resolvePrograma() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Integer idPersona = usuarioService.findIdPersonaByNombreusuario(username);
        if (idPersona == null) {
            throw new RuntimeException("No se pudo derivar el administrativo desde el usuario autenticado");
        }
        Integer idPrograma = administrativoService.findIdProgramaByIdPersona(idPersona);
        if (idPrograma == null) {
            throw new RuntimeException("El usuario autenticado no tiene un programa asignado");
        }
        return idPrograma;
    }

    @PutMapping("/interview/rate")
    public ResponseEntity<EntrevistaOutput> rateInterview(@RequestBody ENTREVISTA_RATE request) {
        try {
            EntrevistaOutput update = entrevistaProcessor.rateInterview(request);
            return ResponseEntity.ok(update);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/cohorte/{cohorteId}/documentosrequisitoprogramacohorte")
    public ResponseEntity<List<DocumentosrequisitoprogramacohorteOutput>> listDocumentosrequisitoprogramacohorteByCohorte(
            @PathVariable Integer cohorteId) {
        try {
            return ResponseEntity.ok(documentosrequisitoprogramacohorteProcessor.findByIdCohorte(cohorteId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // --- Criterios de evaluacion (programa) ---

    @GetMapping("/programa/{programaId}/criterios")
    public ResponseEntity<List<CriterioevaluacionOutput>> listCriteriosByPrograma(@PathVariable Integer programaId) {
        try {
            return ResponseEntity.ok(criterioevaluacionProcessor.findByIdPrograma(programaId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/programa/{programaId}/criterios", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CriterioevaluacionOutput> createCriterioForPrograma(
            @PathVariable Integer programaId,
            @RequestBody CRITERIO_CREATE_BODY body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(criterioevaluacionProcessor.createForPrograma(programaId, body));
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping(value = "/programa/{programaId}/criterios/{criterioId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CriterioevaluacionOutput> updateCriterioForPrograma(
            @PathVariable Integer programaId,
            @PathVariable Integer criterioId,
            @RequestBody CRITERIO_UPDATE_BODY body) {
        try {
            return ResponseEntity.ok(criterioevaluacionProcessor.updateForPrograma(programaId, criterioId, body));
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/programa/{programaId}/criterios/{criterioId}")
    public ResponseEntity<SuccessOutput> deleteCriterioForPrograma(
            @PathVariable Integer programaId,
            @PathVariable Integer criterioId) {
        try {
            criterioevaluacionProcessor.deleteForPrograma(programaId, criterioId);
            return ResponseEntity.ok(SuccessOutput.builder().success(true).build());
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // --- Criteriocohorte ---

    @GetMapping("/cohorte/{cohorteId}/criteriocohorte")
    public ResponseEntity<List<CriteriocohorteOutput>> listCriteriocohorteByCohorte(@PathVariable Integer cohorteId) {
        try {
            return ResponseEntity.ok(criteriocohorteProcessor.findByIdCohorte(cohorteId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/cohorte/{cohorteId}/criteriocohorte", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CriteriocohorteOutput> assignCriterioToCohorte(
            @PathVariable Integer cohorteId,
            @RequestBody CRITERIOCOHORTE_ASSIGN_BODY body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(criteriocohorteProcessor.assign(cohorteId, body.idCriterio(), body.pesoSnapshot()));
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping(value = "/cohorte/{cohorteId}/criteriocohorte/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CriteriocohorteOutput> updateCriteriocohorte(
            @PathVariable Integer cohorteId,
            @PathVariable Integer id,
            @RequestBody CRITERIOCOHORTE_PESO_UPDATE body) {
        try {
            return ResponseEntity.ok(criteriocohorteProcessor.updatePeso(id, body.pesoSnapshot()));
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/programa/{programaId}/criterios/{criterioId}/desactivar")
    public ResponseEntity<CriterioevaluacionOutput> desactivarCriterio(
            @PathVariable Integer programaId,
            @PathVariable Integer criterioId) {
        try {
            return ResponseEntity.ok(criterioevaluacionProcessor.desactivarCriterio(programaId, criterioId));
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
