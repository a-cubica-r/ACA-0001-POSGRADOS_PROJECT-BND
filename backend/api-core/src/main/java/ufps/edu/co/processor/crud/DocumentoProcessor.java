package ufps.edu.co.processor.crud;

import java.util.*;
import java.util.stream.*;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ufps.edu.co.domain.exceptions.*;
import ufps.edu.co.domain.exceptions.errorcodes.*;
import ufps.edu.co.maps.specific.*;
import ufps.edu.co.records.input.entity.AspiranteInput.*;
import ufps.edu.co.records.input.entity.DocumentoInput.*;
import ufps.edu.co.records.output.entity.*;
import ufps.edu.co.records.output.entity.AspiranteDocumentosOutput.DocumentoResumenOutput;
import ufps.edu.co.rest.dto.*;
import ufps.edu.co.rest.services.*;
import ufps.edu.co.services.*;
import ufps.edu.co.usecase.*;
import ufps.edu.co.utils.*;

@Service
public class DocumentoProcessor implements
        GlobalUseCase<DOCUMENTO_CREATE, DOCUMENTO_UPDATE, DOCUMENTO_DELETE, DOCUMENTO_PATCH, DOCUMENTO_FIND, DocumentoOutput> {

    private static final Logger logger = LoggerFactory.getLogger(DocumentoProcessor.class);

    @Autowired
    private DocumentoService service;

    @Autowired
    private DocumentoMap map;

    @Autowired
    private EstadodocumentoService estadodocumentoService;

    @Autowired
    private PersonaMap personaMap;

    @Autowired
    private AspiranteService aspiranteService;

    @Autowired
    private EstadoService estadoService;

    @Autowired
    private DocumentosrequisitoconsejocohorteService documentosrequisitoconsejocohorteService;

    @Autowired
    private DocumentosrequisitoprogramacohorteService documentosrequisitoprogramacohorteService;

    @Autowired
    private DocumentosrequisitoprogramaService documentosrequisitoprogramaService;

    @Autowired
    private DocumentosrequisitoconsejoService documentosrequisitoconsejoService;

    @Autowired
    private SESService sesService;

    // @Autowired
    // private EmailTemplates emailTemplates;

    @Override
    public DocumentoOutput create(DOCUMENTO_CREATE input) {
        try {
            DocumentoDTO dto = map.toDto(input);
            DocumentoDTO created = service.create(dto);
            return map.toOutput(created);
        } catch (Exception e) {
            throw new RuntimeException("Error creating Documento: " + e.getMessage(), e);
        }
    }

    @Override
    public DocumentoOutput update(DOCUMENTO_UPDATE input) {
        try {
            DocumentoDTO dto = map.toDto(input);
            DocumentoDTO updated = service.update(input.id(), dto);
            return map.toOutput(updated);
        } catch (Exception e) {
            throw new RuntimeException("Error updating Documento: " + e.getMessage(), e);
        }
    }

    @Override
    public DocumentoOutput patch(DOCUMENTO_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Documento");
    }

    @Override
    public DocumentoOutput findById(DOCUMENTO_FIND input) {
        try {
            DocumentoDTO dto = service.findById(input.id());
            return map.toOutput(dto);
        } catch (Exception e) {
            throw new RuntimeException("Error finding Documento by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DocumentoOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Documentos: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(DOCUMENTO_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Documento by ID: " + e.getMessage(), e);
        }
    }

    public AprobarDocumentoOutput approveDocument(DOCUMENTO_FIND input) {
        try {
            DocumentoDTO dto = service.findById(input.id());
            EstadodocumentoDTO estadodocumentoDTO = estadodocumentoService.findByEstado("APROBADO");
            dto.setEstadodocumento(estadodocumentoDTO);
            dto.setIdEstadodocumento(estadodocumentoDTO.getId());
            DocumentoDTO approve = service.update(input.id(), dto);
            checkAndUpdateEstadoValidacion(dto.getIdAspirante());
            String nombreDocumento = resolverNombreTitulo(dto);
            AspiranteDTO aspirante = aspiranteService.findById(dto.getIdAspirante());
            PersonaDTO persona = aspirante != null ? aspirante.getPersona() : null;
            if (persona != null) {
                sesService.enviarCorreoAsync(persona.getCorreo(), EmailTemplates.ASUNTO_APROBACION_DOCUMENTO,
                        EmailTemplates.cuerpoAprobacionDocumento(persona.getNombres(), nombreDocumento));
            }
            return AprobarDocumentoOutput.builder()
                    .id(approve.getId())
                    .nombre(approve.getKeyfile())
                    .estado(approve.getEstadodocumento().getEstado())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error approving Documento: " + e.getMessage(), e);
        }
    }

    public DocumentoEstadoOutput rejectDocument(DOCUMENTO_REJECT input) {
        try {
            DocumentoDTO dto = service.findById(input.id());
            EstadodocumentoDTO estadodocumentoDTO = estadodocumentoService.findByEstado("RECHAZADO");
            dto.setEstadodocumento(estadodocumentoDTO);
            dto.setObservaciones(input.motivoRechazo());
            dto.setIdEstadodocumento(estadodocumentoDTO.getId());
            DocumentoDTO reject = service.update(input.id(), dto);
            String nombreDocumento = resolverNombreTitulo(dto);
            AspiranteDTO aspirante = aspiranteService.findById(dto.getIdAspirante());
            PersonaDTO persona = aspirante != null ? aspirante.getPersona() : null;
            if (persona != null) {
                sesService.enviarCorreoAsync(persona.getCorreo(), EmailTemplates.ASUNTO_RECHAZO_DOCUMENTO,
                        EmailTemplates.cuerpoRechazoDocumento(persona.getNombres(), nombreDocumento, input.motivoRechazo()));
            }
            return DocumentoEstadoOutput.builder()
                    .id(reject.getId())
                    .nombre(reject.getKeyfile())
                    .estado(reject.getEstadodocumento().getEstado())
                    .motivoRechazo(reject.getObservaciones())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error rejecting Documento: " + e.getMessage(), e);
        }
    }

    public List<DocumentoOutput> findByAspiranteId(ASPIRANTE_FIND input) {
        try {
            return service.findAll().stream()
                    .filter(dto -> dto.getAspirante() != null && dto.getAspirante().getId().equals(input.id()))
                    .map(map::toOutput)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding Documentos by Aspirante ID: " + e.getMessage(), e);
        }
    }

    private void checkAndUpdateEstadoValidacion(Integer idAspirante) {
        try {
            AspiranteDTO aspirante = aspiranteService.findById(idAspirante);
            if (aspirante == null || aspirante.getIdCohorte() == null) {
                return;
            }
            Integer idCohorte = aspirante.getIdCohorte();

            List<DocumentosrequisitoconsejocohorteDTO> requisitosConsejo = documentosrequisitoconsejocohorteService
                    .findByIdCohorte(idCohorte);
            List<DocumentosrequisitoprogramacohorteDTO> requisitosPrograma = documentosrequisitoprogramacohorteService
                    .findByIdCohorte(idCohorte);

            if (requisitosConsejo.isEmpty() && requisitosPrograma.isEmpty()) {
                throw new RuntimeException(
                        "La cohorte con id " + idCohorte + " no tiene documentos requisito configurados.");
            }

            // Cargar todos los documentos del aspirante en una sola query y verificar en memoria
            List<DocumentoDTO> todosLosDocs = service.findByIdAspirante(idAspirante);

            java.util.Set<Integer> aprobadosConsejo = todosLosDocs.stream()
                    .filter(d -> d.getIdDocumentosrequisitoconsejocohorte() != null
                            && d.getEstadodocumento() != null
                            && "APROBADO".equalsIgnoreCase(d.getEstadodocumento().getEstado()))
                    .map(DocumentoDTO::getIdDocumentosrequisitoconsejocohorte)
                    .collect(Collectors.toSet());

            java.util.Set<Integer> aprobadosPrograma = todosLosDocs.stream()
                    .filter(d -> d.getIdDocumentosrequisitoprogramacohorte() != null
                            && d.getEstadodocumento() != null
                            && "APROBADO".equalsIgnoreCase(d.getEstadodocumento().getEstado()))
                    .map(DocumentoDTO::getIdDocumentosrequisitoprogramacohorte)
                    .collect(Collectors.toSet());

            for (DocumentosrequisitoconsejocohorteDTO requisito : requisitosConsejo) {
                if (!aprobadosConsejo.contains(requisito.getId())) return;
            }
            for (DocumentosrequisitoprogramacohorteDTO requisito : requisitosPrograma) {
                if (!aprobadosPrograma.contains(requisito.getId())) return;
            }

            EstadoDTO estadoValidado = estadoService.findByTipoAndEntidad("VALIDADO_POR_CALIFICAR", "aspirante");
            if (estadoValidado != null) {
                aspiranteService.updateEstado(idAspirante, estadoValidado.getId());
            }
        } catch (Exception e) {
            logger.warn("No se pudo actualizar estado de validación del aspirante {}: {}", idAspirante, e.getMessage());
        }
    }

    public PersonaOutput findPersonByDocument(DOCUMENTO_FIND input) {
        DocumentoDTO documento = service.findById(input.id());
        PersonaDTO persona = documento.getAspirante().getPersona();
        return personaMap.toOutput(persona);
    }

    public AspiranteDocumentosOutput getDocumentosDeAspirante(Integer aspiranteId) {
        try {
            AspiranteDTO aspirante = aspiranteService.findById(aspiranteId);
            PersonaDTO p = aspirante != null ? aspirante.getPersona() : null;
            String nombre = p != null
                    ? ((p.getNombres() != null ? p.getNombres() : "") + " "
                            + (p.getApellidos() != null ? p.getApellidos() : "")).trim()
                    : "";
            String cedula = p != null && p.getDocumentopersona() != null
                    && p.getDocumentopersona().getNumerodocumento() != null
                            ? p.getDocumentopersona().getNumerodocumento().toString()
                            : null;

            List<DocumentoDTO> docs = service.findByIdAspirante(aspiranteId);
            long total = docs.size();
            long validados = docs.stream()
                    .filter(d -> d.getEstadodocumento() != null
                            && "APROBADO".equalsIgnoreCase(d.getEstadodocumento().getEstado()))
                    .count();

            String estadoGeneral;
            if (total > 0 && validados == total) {
                estadoGeneral = "validados";
            } else if (validados > 0) {
                estadoGeneral = "en progreso";
            } else {
                estadoGeneral = "pendiente";
            }

            Integer idCohorte = aspirante != null ? aspirante.getIdCohorte() : null;
            Map<Integer, String> nombresPorConsejoCohorte = buildNombreMapConsejo(idCohorte);
            Map<Integer, String> nombresPorProgramaCohorte = buildNombreMapPrograma(idCohorte);

            List<DocumentoResumenOutput> documentosResumen = docs.stream()
                    .map(doc -> DocumentoResumenOutput.builder()
                            .idDocumento(doc.getId())
                            .idDocumentosrequisitoconsejocohorte(doc.getIdDocumentosrequisitoconsejocohorte())
                            .idDocumentosrequisitoprogramacohorte(doc.getIdDocumentosrequisitoprogramacohorte())
                            .nombreTitulo(resolverNombreTituloDesdeMap(doc, nombresPorConsejoCohorte, nombresPorProgramaCohorte))
                            .estado(doc.getEstadodocumento() != null ? doc.getEstadodocumento().getEstado()
                                    : "PENDIENTE")
                            .motivoRechazo(doc.getObservaciones())
                            .linkArchivo(doc.getEnlaceurl())
                            .build())
                    .toList();

            return AspiranteDocumentosOutput.builder()
                    .idAspirante(aspiranteId)
                    .nombreAspirante(nombre)
                    .cedula(cedula)
                    .estadoGeneral(estadoGeneral)
                    .documentos(documentosResumen)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo documentos del aspirante: " + e.getMessage(), e);
        }
    }

    public String resolverNombreTitulo(DocumentoDTO doc) {
        if (doc.getIdDocumentosrequisitoconsejocohorte() != null) {
            Integer idReferencia = doc.getIdDocumentosrequisitoconsejocohorte();
            DocumentosrequisitoconsejoDTO requisito = documentosrequisitoconsejoService.findById(idReferencia);
            if (requisito != null) {
                return requisito.getNombre();
            }

            DocumentosrequisitoconsejocohorteDTO puente = documentosrequisitoconsejocohorteService
                    .findById(idReferencia);
            if (puente != null && puente.getIdDocrequisito() != null) {
                requisito = documentosrequisitoconsejoService.findById(puente.getIdDocrequisito());
                if (requisito != null) {
                    return requisito.getNombre();
                }
            }

            throw new DomainException(AspiranteErrorCode.DOCUMENTO_REQUISITO_NOT_FOUND, idReferencia);
        }
        if (doc.getIdDocumentosrequisitoprogramacohorte() != null) {
            Integer idReferencia = doc.getIdDocumentosrequisitoprogramacohorte();
            DocumentosrequisitoprogramaDTO requisito = documentosrequisitoprogramaService.findById(idReferencia);
            if (requisito != null) {
                return requisito.getNombre();
            }

            DocumentosrequisitoprogramacohorteDTO puente = documentosrequisitoprogramacohorteService
                    .findById(idReferencia);
            if (puente != null && puente.getIdDocrequisito() != null) {
                requisito = documentosrequisitoprogramaService.findById(puente.getIdDocrequisito());
                if (requisito != null) {
                    return requisito.getNombre();
                }
            }

            throw new DomainException(AspiranteErrorCode.DOCUMENTO_REQUISITO_NOT_FOUND, idReferencia);
        }
        return null;
    }

    public AspiranteDocumentosOutput getDocumentosDeAspiranteParaDirector(Integer aspiranteId) {
        try {
            return buildAspiranteDocumentosOutputForDirector(aspiranteId);
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo documentos del aspirante para director: " + e.getMessage(), e);
        }
    }

        private AspiranteDocumentosOutput buildAspiranteDocumentosOutputForDirector(Integer aspiranteId) {
        AspiranteDTO aspirante = aspiranteService.findById(aspiranteId);
        if (aspirante == null) {
            throw new DomainException(AspiranteErrorCode.ASPIRANTE_NOT_FOUND, aspiranteId);
        }
        PersonaDTO p = aspirante != null ? aspirante.getPersona() : null;
        String nombre = p != null
            ? ((p.getNombres() != null ? p.getNombres() : "") + " "
                + (p.getApellidos() != null ? p.getApellidos() : "")).trim()
            : "";
        String cedula = p != null && p.getDocumentopersona() != null
            && p.getDocumentopersona().getNumerodocumento() != null
                ? p.getDocumentopersona().getNumerodocumento().toString()
                : null;

        List<DocumentoDTO> docs = service.findByIdAspirante(aspiranteId);
        long total = docs.size();
        long validados = docs.stream()
            .filter(d -> d.getEstadodocumento() != null
                && "APROBADO".equalsIgnoreCase(d.getEstadodocumento().getEstado()))
            .count();

        String estadoGeneral;
        if (total > 0 && validados == total) {
            estadoGeneral = "validados";
        } else if (validados > 0) {
            estadoGeneral = "en progreso";
        } else {
            estadoGeneral = "pendiente";
        }

        Map<Integer, String> nombresPorConsejoCohorte = buildNombreMapConsejo(aspirante.getIdCohorte());
        Map<Integer, String> nombresPorProgramaCohorte = buildNombreMapPrograma(aspirante.getIdCohorte());

        List<DocumentoResumenOutput> documentosResumen = docs.stream()
            .map(doc -> DocumentoResumenOutput.builder()
                .idDocumento(doc.getId())
                .idDocumentosrequisitoconsejocohorte(doc.getIdDocumentosrequisitoconsejocohorte())
                .idDocumentosrequisitoprogramacohorte(doc.getIdDocumentosrequisitoprogramacohorte())
                .nombreTitulo(resolverNombreTituloDesdeMap(doc, nombresPorConsejoCohorte, nombresPorProgramaCohorte))
                .estado(doc.getEstadodocumento() != null ? doc.getEstadodocumento().getEstado()
                    : "PENDIENTE")
                .motivoRechazo(doc.getObservaciones())
                .linkArchivo(doc.getEnlaceurl())
                .build())
            .toList();

        return AspiranteDocumentosOutput.builder()
            .idAspirante(aspiranteId)
            .nombreAspirante(nombre)
            .cedula(cedula)
            .estadoGeneral(estadoGeneral)
            .documentos(documentosResumen)
            .build();
        }

    @SuppressWarnings("unused")
    private AspiranteDocumentosOutput buildAspiranteDocumentosOutput(Integer aspiranteId) {
        AspiranteDTO aspirante = aspiranteService.findById(aspiranteId);
        if (aspirante == null) {
            throw new DomainException(AspiranteErrorCode.ASPIRANTE_NOT_FOUND, aspiranteId);
        }
        PersonaDTO p = aspirante != null ? aspirante.getPersona() : null;
        String nombre = p != null
                ? ((p.getNombres() != null ? p.getNombres() : "") + " "
                        + (p.getApellidos() != null ? p.getApellidos() : "")).trim()
                : "";
        String cedula = p != null && p.getDocumentopersona() != null
                && p.getDocumentopersona().getNumerodocumento() != null
                        ? p.getDocumentopersona().getNumerodocumento().toString()
                        : null;

        List<DocumentoDTO> docs = service.findByIdAspirante(aspiranteId);
        long total = docs.size();
        long validados = docs.stream()
                .filter(d -> d.getEstadodocumento() != null
                        && "APROBADO".equalsIgnoreCase(d.getEstadodocumento().getEstado()))
                .count();

        String estadoGeneral;
        if (total > 0 && validados == total) {
            estadoGeneral = "validados";
        } else if (validados > 0) {
            estadoGeneral = "en progreso";
        } else {
            estadoGeneral = "pendiente";
        }

        List<DocumentoResumenOutput> documentosResumen = docs.stream()
                .map(doc -> DocumentoResumenOutput.builder()
                        .idDocumento(doc.getId())
                        .idDocumentosrequisitoconsejocohorte(doc.getIdDocumentosrequisitoconsejocohorte())
                        .idDocumentosrequisitoprogramacohorte(doc.getIdDocumentosrequisitoprogramacohorte())
                        .nombreTitulo(resolverNombreTitulo(doc))
                        .estado(doc.getEstadodocumento() != null ? doc.getEstadodocumento().getEstado()
                                : "PENDIENTE")
                        .motivoRechazo(doc.getObservaciones())
                        .linkArchivo(doc.getEnlaceurl())
                        .build())
                .toList();

        return AspiranteDocumentosOutput.builder()
                .idAspirante(aspiranteId)
                .nombreAspirante(nombre)
                .cedula(cedula)
                .estadoGeneral(estadoGeneral)
                .documentos(documentosResumen)
                .build();
    }

    public DocumentoEstadoOutput updateEstadoDocumento(Integer docId, DOCUMENTO_ESTADO_UPDATE input) {
        try {
            DocumentoDTO dto = service.findById(docId);
            EstadodocumentoDTO estadodocumentoDTO = estadodocumentoService.findByEstado(input.estado());
            dto.setEstadodocumento(estadodocumentoDTO);
            dto.setIdEstadodocumento(estadodocumentoDTO.getId());
            dto.setObservaciones(input.motivoRechazo());
            service.update(docId, dto);
            if ("APROBADO".equalsIgnoreCase(input.estado())) {
                checkAndUpdateEstadoValidacion(dto.getIdAspirante());
            }

            // TODO: Ya no se mapea el nombre del documento, habría que agregarlo al output
            // o eliminarlo si no es necesario
            return DocumentoEstadoOutput.builder()
                    .id(docId)
                    // .nombre(nombreDoc)
                    .estado(input.estado())
                    .motivoRechazo(input.motivoRechazo())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error actualizando estado del documento: " + e.getMessage(), e);
        }
    }

    private Map<Integer, String> buildNombreMapConsejo(Integer idCohorte) {
        if (idCohorte == null) return Map.of();
        List<DocumentosrequisitoconsejocohorteDTO> puentes = documentosrequisitoconsejocohorteService.findByIdCohorte(idCohorte);
        List<Integer> idRequisitos = puentes.stream()
                .map(DocumentosrequisitoconsejocohorteDTO::getIdDocrequisito)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Integer, String> nombrePorIdRequisito = documentosrequisitoconsejoService.findAllByIds(idRequisitos)
                .stream()
                .collect(Collectors.toMap(DocumentosrequisitoconsejoDTO::getId, DocumentosrequisitoconsejoDTO::getNombre));
        return puentes.stream()
                .filter(p -> p.getIdDocrequisito() != null && nombrePorIdRequisito.containsKey(p.getIdDocrequisito()))
                .collect(Collectors.toMap(
                        DocumentosrequisitoconsejocohorteDTO::getId,
                        p -> nombrePorIdRequisito.get(p.getIdDocrequisito())));
    }

    private Map<Integer, String> buildNombreMapPrograma(Integer idCohorte) {
        if (idCohorte == null) return Map.of();
        List<DocumentosrequisitoprogramacohorteDTO> puentes = documentosrequisitoprogramacohorteService.findByIdCohorte(idCohorte);
        List<Integer> idRequisitos = puentes.stream()
                .map(DocumentosrequisitoprogramacohorteDTO::getIdDocrequisito)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Integer, String> nombrePorIdRequisito = documentosrequisitoprogramaService.findAllByIds(idRequisitos)
                .stream()
                .collect(Collectors.toMap(DocumentosrequisitoprogramaDTO::getId, DocumentosrequisitoprogramaDTO::getNombre));
        return puentes.stream()
                .filter(p -> p.getIdDocrequisito() != null && nombrePorIdRequisito.containsKey(p.getIdDocrequisito()))
                .collect(Collectors.toMap(
                        DocumentosrequisitoprogramacohorteDTO::getId,
                        p -> nombrePorIdRequisito.get(p.getIdDocrequisito())));
    }

    private String resolverNombreTituloDesdeMap(DocumentoDTO doc,
            Map<Integer, String> nombresPorConsejoCohorte,
            Map<Integer, String> nombresPorProgramaCohorte) {
        if (doc.getIdDocumentosrequisitoconsejocohorte() != null) {
            return nombresPorConsejoCohorte.get(doc.getIdDocumentosrequisitoconsejocohorte());
        }
        if (doc.getIdDocumentosrequisitoprogramacohorte() != null) {
            return nombresPorProgramaCohorte.get(doc.getIdDocumentosrequisitoprogramacohorte());
        }
        return null;
    }

    public DocumentoEstadoOutput updateEstadoDocumentoParaDirector(Integer docId, DOCUMENTO_ESTADO_UPDATE input) {
        try {
            EstadodocumentoDTO estadodocumentoDTO = estadodocumentoService.findByEstado(input.estado());
            if (estadodocumentoDTO == null) {
                throw new RuntimeException("Estado de documento no encontrado: " + input.estado());
            }

            int updatedRows = service.updateEstadoDocumentoById(docId, estadodocumentoDTO.getId(),
                    input.motivoRechazo());
            if (updatedRows == 0) {
                throw new RuntimeException("Documento no encontrado con id: " + docId);
            }

            return DocumentoEstadoOutput.builder()
                    .id(docId)
                    .estado(estadodocumentoDTO.getEstado())
                    .motivoRechazo(input.motivoRechazo())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error actualizando estado del documento para director: " + e.getMessage(), e);
        }
    }
}
