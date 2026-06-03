package ufps.edu.co.processor.crud;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.CohorteErrorCode;
import ufps.edu.co.domain.exceptions.errorcodes.CriteriocohorteErrorCode;
import ufps.edu.co.domain.exceptions.errorcodes.CriterioevaluacionErrorCode;
import ufps.edu.co.maps.specific.AspiranteMap;
import ufps.edu.co.maps.specific.EstadoMap;
import ufps.edu.co.records.input.entity.AspiranteInput.*;
import ufps.edu.co.records.input.entity.CohorteInput;
import ufps.edu.co.records.input.entity.CohorteInput.*;
import ufps.edu.co.records.output.entity.AspiranteCalificacionOutput;
import ufps.edu.co.records.output.entity.RankingAdmitidosOutput;
import ufps.edu.co.records.output.entity.AspiranteCohorteOutput;
import ufps.edu.co.records.output.entity.AspiranteCriteriosOutput;
import ufps.edu.co.records.output.entity.AspiranteOutput;
import ufps.edu.co.records.output.entity.CohorteDetalleOutput;
import ufps.edu.co.records.output.entity.CohorteListadoOutput;
import ufps.edu.co.records.output.entity.CohorteResumenOutput;
import ufps.edu.co.records.output.entity.CriterioFilaOutput;
import ufps.edu.co.records.output.entity.CriteriosCohorteOutput;
import ufps.edu.co.records.output.entity.EstadoOutput;
import ufps.edu.co.records.output.entity.PasoProcesoOutput;
import ufps.edu.co.records.output.entity.ProgramaInicioOutput;
import ufps.edu.co.rest.dto.CohorteDTO;
import ufps.edu.co.rest.dto.AspiranteDTO;
import ufps.edu.co.rest.dto.CriteriocohorteDTO;
import ufps.edu.co.rest.dto.DocumentoDTO;
import ufps.edu.co.rest.dto.DocumentosrequisitoconsejocohorteDTO;
import ufps.edu.co.rest.dto.DocumentosrequisitoconsejoDTO;
import ufps.edu.co.rest.dto.DocumentosrequisitoprogramacohorteDTO;
import ufps.edu.co.rest.dto.DocumentosrequisitoprogramaDTO;
import ufps.edu.co.rest.services.DocumentoService;
import ufps.edu.co.rest.dto.CriterioevaluacionDTO;
import ufps.edu.co.rest.dto.EstadoDTO;
import ufps.edu.co.rest.dto.PersonaDTO;
import ufps.edu.co.rest.dto.PlazoDTO;
import ufps.edu.co.rest.dto.SemestreDTO;
import ufps.edu.co.rest.dto.TipoplazoDTO;
import ufps.edu.co.rest.services.AdmitidoService;
import ufps.edu.co.rest.services.AspiranteService;
import ufps.edu.co.rest.services.CalificacioncriterioService;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.rest.services.CriterioevaluacionService;
import ufps.edu.co.rest.services.CriteriocohorteService;
import ufps.edu.co.rest.services.DocumentosrequisitoconsejocohorteService;
import ufps.edu.co.rest.services.DocumentosrequisitoconsejoService;
import ufps.edu.co.rest.services.DocumentosrequisitoprogramacohorteService;
import ufps.edu.co.rest.services.DocumentosrequisitoprogramaService;
import ufps.edu.co.rest.services.EstadoService;
import ufps.edu.co.rest.services.PlazoService;
import ufps.edu.co.rest.services.SemestreService;
import ufps.edu.co.rest.services.TipoplazoService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class AspiranteProcessor implements
        GlobalUseCase<ASPIRANTE_CREATE, ASPIRANTE_UPDATE, ASPIRANTE_DELETE, ASPIRANTE_PATCH, ASPIRANTE_FIND, AspiranteOutput> {

    @Autowired
    private AspiranteService service;

    @Autowired
    private AspiranteMap map;

    @Autowired
    private EstadoMap estadoMap;

    @Autowired
    private CriterioevaluacionService criterioevaluacionService;

    @Autowired
    private CriteriocohorteService criteriocohorteService;

    @Autowired
    private CalificacioncriterioService calificacioncriterioService;

    @Autowired
    private CohorteService cohorteService;

    @Autowired
    private AdmitidoService admitidoService;

    @Autowired
    private PlazoService plazoService;

    @Autowired
    private SemestreService semestreService;

    @Autowired
    private EstadoService estadoService;

    @Autowired
    private TipoplazoService tipoplazoService;

    @Autowired
    private DocumentoService documentoService;

    @Autowired
    private DocumentosrequisitoconsejocohorteService documentosrequisitoconsejocohorteService;

    @Autowired
    private DocumentosrequisitoprogramacohorteService documentosrequisitoprogramacohorteService;

    @Autowired
    private PagoProcessor pagoProcessor;

    @Autowired
    private DocumentosrequisitoconsejoService documentosrequisitoconsejoService;

    @Autowired
    private DocumentosrequisitoprogramaService documentosrequisitoprogramaService;

    @Override
    public AspiranteOutput create(ASPIRANTE_CREATE input) {
        try {
            AspiranteDTO dto = map.toDto(input);
            AspiranteDTO created = service.create(dto);
            pagoProcessor.ensureInitialPaymentsForAspirante(created.getId());
            return map.toOutput(created);
        } catch (Exception e) {
            throw new RuntimeException("Error creating Aspirante: " + e.getMessage(), e);
        }
    }

    @Override
    public AspiranteOutput update(ASPIRANTE_UPDATE input) {
        try {
            AspiranteDTO dto = map.toDto(input);
            AspiranteDTO updated = service.update(input.id(), dto);
            return map.toOutput(updated);
        } catch (Exception e) {
            throw new RuntimeException("Error updating Aspirante: " + e.getMessage(), e);
        }
    }

    @Override
    public AspiranteOutput patch(ASPIRANTE_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Aspirante");
    }

    @Override
    public AspiranteOutput findById(ASPIRANTE_FIND input) {
        try {
            AspiranteDTO dto = service.findById(input.id());
            return map.toOutput(dto);
        } catch (Exception e) {
            throw new RuntimeException("Error finding Aspirante by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<AspiranteOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Aspirantes: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(ASPIRANTE_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Aspirante by ID: " + e.getMessage(), e);
        }
    }

    public List<AspiranteOutput> findWithDocuments() {
        try {
            return service.findWithDocuments().stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding Aspirantes with documents: " + e.getMessage(), e);
        }
    }

    public long countValidados() {
        try {
            return service.countValidados();
        } catch (Exception e) {
            throw new RuntimeException("Error counting validados: " + e.getMessage(), e);
        }
    }

    public long countPorCalificar() {
        try {
            return service.countPorCalificar();
        } catch (Exception e) {
            throw new RuntimeException("Error counting por calificar: " + e.getMessage(), e);
        }
    }

    public long countCalificados() {
        try {
            return service.countCalificados();
        } catch (Exception e) {
            throw new RuntimeException("Error counting calificados: " + e.getMessage(), e);
        }
    }

    public List<AspiranteCalificacionOutput> findAllValidadosCalificacion() {
        List<AspiranteDTO> validados = service.findWithDocuments();

        return validados.stream().map(aspirante -> {
            PersonaDTO persona = aspirante.getPersona();

            String nombreCompleto = persona != null
                    ? ((persona.getNombres() != null ? persona.getNombres() : "") + " "
                            + (persona.getApellidos() != null ? persona.getApellidos() : "")).trim()
                    : "";

            return AspiranteCalificacionOutput.builder()
                    .id(aspirante.getId())
                    .nombreCompleto(nombreCompleto)
                    .idEstado(aspirante.getIdEstado())
                    .estado(aspirante.getEstado() != null ? aspirante.getEstado().getTipo() : null)
                    .correo(persona != null ? persona.getCorreo() : null)
                    .puntajeTotal(aspirante.getPuntuacion())
                    .build();
        }).toList();
    }

    public AspiranteCriteriosOutput findCriteriosCalificacion(ASPIRANTE_FIND input) {
        try {
            Integer idCohorte = service.findIdCohorteById(input.id());
            if (idCohorte == null) {
                return AspiranteCriteriosOutput.builder()
                        .criterios(List.of())
                        .puntajeTotal(null)
                        .build();
            }
            BigDecimal puntuacion = service.findPuntuacionById(input.id());

            List<CriterioFilaOutput> filas = criteriocohorteService
                    .findCriteriosConCalificacion(idCohorte, input.id())
                    .stream()
                    .map(v -> CriterioFilaOutput.builder()
                            .id(v.getId())
                            .nombreCriterio(v.getNombreCriterio())
                            .peso(v.getPesoSnapshot())
                            .puntajeObtenido(v.getPuntajeObtenido())
                            .build())
                    .toList();

            return AspiranteCriteriosOutput.builder()
                    .criterios(filas)
                    .puntajeTotal(puntuacion)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error finding criterios for Aspirante: " + e.getMessage(), e);
        }
    }

    public CriteriosCohorteOutput getCriteriosByCohorte(Integer cohorteId) {
        CohorteDTO cohorte = cohorteService.findById(cohorteId);
        if (cohorte == null) {
            throw new RuntimeException("Cohorte no encontrada: " + cohorteId);
        }
        boolean activa = cohorte.getEstado() != null
                && "ABIERTA".equalsIgnoreCase(cohorte.getEstado().getTipo());
        List<CriteriosCohorteOutput.CriterioInfo> criterios = criteriocohorteService
                .findByIdCohorte(cohorteId).stream()
                .map(cc -> {
                    CriterioevaluacionDTO ce = criterioevaluacionService.findById(cc.getIdCriterio());
                    return CriteriosCohorteOutput.CriterioInfo.builder()
                            .id(cc.getId())
                            .nombre(ce != null ? ce.getNombre() : null)
                            .descripcion(ce != null ? ce.getDescripcion() : null)
                            .peso(cc.getPesoSnapshot())
                            .build();
                })
                .toList();
        return CriteriosCohorteOutput.builder()
                .cohorteActual(CriteriosCohorteOutput.CohorteInfo.builder()
                        .id(cohorte.getId())
                        .nombre(cohorte.getNombre())
                        .activa(activa)
                        .build())
                .criterios(criterios)
                .build();
    }

    public List<CohorteListadoOutput> getCohortesByPrograma(Integer programaId) {
        return cohorteService.findByIdPrograma(programaId).stream().map(cohorte -> {
            boolean activa = cohorte.getEstado() != null
                    && "ABIERTA".equalsIgnoreCase(cohorte.getEstado().getTipo());
            long inscritos = service.countByCohorte(cohorte.getId());
            long admitidos = admitidoService.countByCohorte(cohorte.getId());
            return CohorteListadoOutput.builder()
                    .id(cohorte.getId())
                    .nombre(cohorte.getNombre())
                    .activa(activa)
                    .inscritos(inscritos)
                    .admitidos(admitidos)
                    .cupos(cohorte.getCupos())
                    .fechaLimiteDocumentos(cohorte.getPlazo() != null ? cohorte.getPlazo().getFechafin() : null)
                    .fechaLimitePago(cohorte.getPlazo3() != null ? cohorte.getPlazo3().getFechafin() : null)
                    .fechaInicio(cohorte.getSemestre() != null ? cohorte.getSemestre().getFechaInicio() : null)
                    .build();
        }).toList();
    }

    public ProgramaInicioOutput getProgramaInicio(Integer cohorteId) {
        CohorteDTO cohorte = cohorteService.findById(cohorteId);
        if (cohorte == null) {
            throw new RuntimeException("Cohorte no encontrada: " + cohorteId);
        }

        long totalInscritos = service.countByCohorte(cohorte.getId());
        long validados = service.countValidadosByCohorte(cohorte.getId());
        long calificados = service.countCalificadosByCohorte(cohorte.getId());

        return ProgramaInicioOutput.builder()
                .cohorteActual(ProgramaInicioOutput.CohorteResumen.builder()
                        .id(cohorte.getId())
                        .nombre(cohorte.getNombre())
                        .activa(true)
                        .fechaLimiteDocumentos(cohorte.getPlazo() != null ? cohorte.getPlazo().getFechafin() : null)
                        .fechaLimitePago(cohorte.getPlazo3() != null ? cohorte.getPlazo3().getFechafin() : null)
                        .build())
                .validacion(ProgramaInicioOutput.ValidacionResumen.builder()
                        .totalInscritos(totalInscritos)
                        .aspirantesValidados(validados)
                        .build())
                .calificacion(ProgramaInicioOutput.CalificacionResumen.builder()
                        .totalValidados(validados)
                        .aspirantesCalificados(calificados)
                        .build())
                .build();
    }

    public List<ProgramaInicioOutput> getProgramaInicioByPrograma(Integer programaId) {
        try {
            return cohorteService.findResumenDataByIdPrograma(programaId).stream()
                    .filter(cohorte -> cohorte.getEstado() != null
                            && "ABIERTA".equalsIgnoreCase(cohorte.getEstado().getTipo()))
                    .map(cohorte -> {
                        Integer cohorteId = cohorte.getId();
                        String nombre = cohorte.getNombre();
                        LocalDate fechaLimiteDocumentos = cohorte.getPlazo() != null
                                ? cohorte.getPlazo().getFechafin()
                                : null;
                        LocalDate fechaLimitePago = cohorte.getPlazo3() != null
                                ? cohorte.getPlazo3().getFechafin()
                                : null;

                        long totalInscritos = service.countByCohorte(cohorteId);
                        long validados = service.countValidadosByCohorte(cohorteId);
                        long calificados = service.countCalificadosByCohorte(cohorteId);

                        return ProgramaInicioOutput.builder()
                                .cohorteActual(ProgramaInicioOutput.CohorteResumen.builder()
                                        .id(cohorteId)
                                        .nombre(nombre)
                                        .activa(true)
                                        .fechaLimiteDocumentos(fechaLimiteDocumentos)
                                        .fechaLimitePago(fechaLimitePago)
                                        .build())
                                .validacion(ProgramaInicioOutput.ValidacionResumen.builder()
                                        .totalInscritos(totalInscritos)
                                        .aspirantesValidados(validados)
                                        .build())
                                .calificacion(ProgramaInicioOutput.CalificacionResumen.builder()
                                        .totalValidados(validados)
                                        .aspirantesCalificados(calificados)
                                        .build())
                                .build();
                    })
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding programa inicio for programa: " + e.getMessage(), e);
        }
    }

    public EstadoOutput findEstadoById(ASPIRANTE_FIND input) {
        try {
            AspiranteDTO dto = service.findById(input.id());
            if (dto == null || dto.getEstado() == null) {
                return null;
            }
            return estadoMap.toOutput(dto.getEstado());
        } catch (Exception e) {
            throw new RuntimeException("Error finding estado for Aspirante: " + e.getMessage(), e);
        }
    }

    public CohorteDetalleOutput getCohorteDetalle(Integer cohorteId) {
        CohorteDTO cohorte = cohorteService.findById(cohorteId);
        if (cohorte == null) {
            throw new DomainException(CohorteErrorCode.COHORTE_NOT_FOUND, cohorteId);
        }
        boolean activa = cohorte.getEstado() != null
                && "ABIERTA".equalsIgnoreCase(cohorte.getEstado().getTipo());

        List<CohorteDetalleOutput.CriterioInfo> criterios = criteriocohorteService
                .findByIdCohorte(cohorteId).stream()
                .map(cc -> {
                    CriterioevaluacionDTO ce = criterioevaluacionService.findById(cc.getIdCriterio());
                    return CohorteDetalleOutput.CriterioInfo.builder()
                            .id(cc.getId())
                            .idCriterioevaluacion(cc.getIdCriterio())
                            .nombre(ce != null ? ce.getNombre() : null)
                            .peso(cc.getPesoSnapshot())
                            .build();
                })
                .toList();

        List<CohorteDetalleOutput.DocumentoAsignadoInfo> documentosConsejo = documentosrequisitoconsejocohorteService
                .findByIdCohorte(cohorteId).stream()
                .filter(doc -> doc.getIdCohorte() != null && doc.getIdCohorte().equals(cohorteId)
                        && doc.getIdDocrequisito() != null)
                .map(this::mapDocumentoConsejo)
                .toList();

        List<CohorteDetalleOutput.DocumentoAsignadoInfo> documentosPrograma = documentosrequisitoprogramacohorteService
                .findByIdCohorte(cohorteId).stream()
                .filter(doc -> doc.getIdCohorte() != null && doc.getIdCohorte().equals(cohorteId)
                        && doc.getIdDocrequisito() != null)
                .map(this::mapDocumentoPrograma)
                .toList();

        List<AspiranteDTO> aspirantes = service.findByCohorte(cohorteId);

        List<CohorteDetalleOutput.AspiranteInfo> inscritosData = aspirantes.stream()
                .map(a -> {
                    PersonaDTO p = a.getPersona();
                    String nombre = p != null
                            ? ((p.getNombres() != null ? p.getNombres() : "") + " "
                                    + (p.getApellidos() != null ? p.getApellidos() : "")).trim()
                            : "";
                    String cedula = p != null && p.getDocumentopersona() != null
                            && p.getDocumentopersona().getNumerodocumento() != null
                                    ? p.getDocumentopersona().getNumerodocumento().toString()
                                    : null;
                    return CohorteDetalleOutput.AspiranteInfo.builder()
                            .id(a.getId())
                            .nombre(nombre)
                            .cedula(cedula)
                            .correo(p != null ? p.getCorreo() : null)
                            .build();
                }).toList();

        List<CohorteDetalleOutput.AspiranteInfo> admitidosData = admitidoService
                .findByCohorte(cohorteId).stream()
                .map(admitido -> {
                    AspiranteDTO a = admitido.getAspirante();
                    if (a == null)
                        return null;
                    PersonaDTO p = a.getPersona();
                    String nombre = p != null
                            ? ((p.getNombres() != null ? p.getNombres() : "") + " "
                                    + (p.getApellidos() != null ? p.getApellidos() : "")).trim()
                            : "";
                    String cedula = p != null && p.getDocumentopersona() != null
                            && p.getDocumentopersona().getNumerodocumento() != null
                                    ? p.getDocumentopersona().getNumerodocumento().toString()
                                    : null;
                    return CohorteDetalleOutput.AspiranteInfo.builder()
                            .id(a.getId())
                            .nombre(nombre)
                            .cedula(cedula)
                            .correo(p != null ? p.getCorreo() : null)
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .toList();

        return CohorteDetalleOutput.builder()
                .id(cohorte.getId())
                .nombre(cohorte.getNombre())
                .activa(activa)
                .inscritos(aspirantes.size())
                .admitidos(admitidosData.size())
                .cupos(cohorte.getCupos())
                .idSemestre(cohorte.getSemestre() != null ? cohorte.getSemestre().getId() : null)
                .nombreSemestre(cohorte.getSemestre() != null ? cohorte.getSemestre().getNombre() : null)
                .idModalidad(cohorte.getModalidad() != null ? cohorte.getModalidad().getId() : null)
                .nombreModalidad(cohorte.getModalidad() != null ? cohorte.getModalidad().getNombre() : null)
                .fechaLimiteDocumentos(cohorte.getPlazo() != null ? cohorte.getPlazo().getFechafin() : null)
                .fechaLimitePago(cohorte.getPlazo3() != null ? cohorte.getPlazo3().getFechafin() : null)
                .fechaInicio(cohorte.getSemestre() != null ? cohorte.getSemestre().getFechaInicio() : null)
                .criterios(criterios)
                .documentosAsignados(CohorteDetalleOutput.DocumentosAsignadosInfo.builder()
                        .documentosConsejo(documentosConsejo)
                        .documentosPrograma(documentosPrograma)
                        .build())
                .inscritosData(inscritosData)
                .admitidosData(admitidosData)
                .build();
    }

    private CohorteDetalleOutput.DocumentoAsignadoInfo mapDocumentoConsejo(
            ufps.edu.co.rest.dto.DocumentosrequisitoconsejocohorteDTO dto) {
        String nombre = null;
        if (dto.getIdDocrequisito() != null) {
            DocumentosrequisitoconsejoDTO documento = documentosrequisitoconsejoService
                    .findById(dto.getIdDocrequisito());
            nombre = documento != null ? documento.getNombre() : null;
        }
        return CohorteDetalleOutput.DocumentoAsignadoInfo.builder()
                .id(dto.getId())
                .idDocrequisito(dto.getIdDocrequisito())
                .idCohorte(dto.getIdCohorte())
                .nombre(nombre)
                .build();
    }

    private CohorteDetalleOutput.DocumentoAsignadoInfo mapDocumentoPrograma(
            ufps.edu.co.rest.dto.DocumentosrequisitoprogramacohorteDTO dto) {
        String nombre = null;
        if (dto.getIdDocrequisito() != null) {
            DocumentosrequisitoprogramaDTO documento = documentosrequisitoprogramaService
                    .findById(dto.getIdDocrequisito());
            nombre = documento != null ? documento.getNombre() : null;
        }
        return CohorteDetalleOutput.DocumentoAsignadoInfo.builder()
                .id(dto.getId())
                .idDocrequisito(dto.getIdDocrequisito())
                .idCohorte(dto.getIdCohorte())
                .nombre(nombre)
                .build();
    }

    public List<AspiranteOutput> findPazYSalvoByCohorte(Integer cohorteId) {
        try {
            return service.findByCohorte(cohorteId).stream()
                    .filter(a -> a.getEstado() != null
                            && "PAZ Y SALVO".equalsIgnoreCase(a.getEstado().getTipo()))
                    .map(map::toOutput)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding aspirantes PAZ Y SALVO: " + e.getMessage(), e);
        }
    }

    public List<PasoProcesoOutput> getPasosProceso(Integer idAspirante) {
        String estadoTipo = service.findEstadoTipoById(idAspirante);
        String estado = estadoTipo != null ? estadoTipo.toUpperCase() : "";

        /**
         * Lógica de asignación de estados a cada paso del proceso según el estado del
         * aspirante.
         * s1 = estado del paso 1 (Inscripción)
         * s2 = estado del paso 2 (Pago)
         * s3 = estado del paso 3 (Documentos)
         * s4 = estado del paso 4 (Calificación)
         * s5 = estado del paso 5 (Resultado)
         * s6 = estado del paso 6 (Legalización)
         */
        String s1, s2, s3, s4, s5, s6;
        switch (estado) {
            case "NO CONFIRMADO" -> {
                s1 = "en progreso";
                s2 = "pendiente";
                s3 = "pendiente";
                s4 = "pendiente";
                s5 = "pendiente";
                s6 = "pendiente";
            }
            case "INSCRITO" -> {
                s1 = "completado";
                s2 = "en progreso";
                s3 = "pendiente";
                s4 = "pendiente";
                s5 = "pendiente";
                s6 = "pendiente";
            }
            case "PAZ Y SALVO" -> {
                s1 = "completado";
                s2 = "completado";
                s3 = "en progreso";
                s4 = "pendiente";
                s5 = "pendiente";
                s6 = "pendiente";
            }
            case "VALIDADO_POR_CALIFICAR" -> {
                s1 = "completado";
                s2 = "completado";
                s3 = "completado";
                s4 = "pendiente";
                s5 = "pendiente";
                s6 = "pendiente";
            }
            case "VALIDADO_EN_PROGRESO" -> {
                s1 = "completado";
                s2 = "completado";
                s3 = "completado";
                s4 = "en progreso";
                s5 = "pendiente";
                s6 = "pendiente";
            }
            case "VALIDADO_CALIFICADO" -> {
                s1 = "completado";
                s2 = "completado";
                s3 = "completado";
                s4 = "completado";
                s5 = "en progreso";
                s6 = "pendiente";
            }
            case "ADMITIDO" -> {
                s1 = "completado";
                s2 = "completado";
                s3 = "completado";
                s4 = "completado";
                s5 = "en progreso";
                s6 = "pendiente";
            }
            case "POR LEGALIZAR" -> {
                s1 = "completado";
                s2 = "completado";
                s3 = "completado";
                s4 = "completado";
                s5 = "completado";
                s6 = "en progreso";
            }
            case "LEGALIZADO" -> {
                s1 = "completado";
                s2 = "completado";
                s3 = "completado";
                s4 = "completado";
                s5 = "completado";
                s6 = "completado";
            }
            default -> {
                s1 = "completado";
                s2 = "pendiente";
                s3 = "pendiente";
                s4 = "pendiente";
                s5 = "pendiente";
                s6 = "pendiente";
            }
        }

        return List.of(
                PasoProcesoOutput.builder().id(1).name("Inscripción").status(s1).build(),
                PasoProcesoOutput.builder().id(2).name("Pago").status(s2).build(),
                PasoProcesoOutput.builder().id(3).name("Documentos").status(s3).build(),
                PasoProcesoOutput.builder().id(4).name("Calificación").status(s4).build(),
                PasoProcesoOutput.builder().id(5).name("Resultado").status(s5).build(),
                PasoProcesoOutput.builder().id(6).name("Legalización").status(s6).build());
    }

    public List<AspiranteCalificacionOutput> findAllValidadosCalificacion(Integer cohorteId) {
        return service.findValidadosByCohorte(cohorteId).stream()
                .map(aspirante -> {
                    PersonaDTO persona = aspirante.getPersona();
                    String nombreCompleto = persona != null
                            ? ((persona.getNombres() != null ? persona.getNombres() : "") + " "
                                    + (persona.getApellidos() != null ? persona.getApellidos() : "")).trim()
                            : "";
                    String numerodocumento = (persona != null && persona.getDocumentopersona() != null)
                            ? persona.getDocumentopersona().getNumerodocumento()
                            : null;
                    return AspiranteCalificacionOutput.builder()
                            .id(aspirante.getId())
                            .nombreCompleto(nombreCompleto)
                            .idEstado(aspirante.getIdEstado())
                            .estado(aspirante.getEstado() != null ? aspirante.getEstado().getTipo() : null)
                            .correo(persona != null ? persona.getCorreo() : null)
                            .puntajeTotal(aspirante.getPuntuacion())
                            .numerodocumento(numerodocumento)
                            .build();
                }).toList();
    }

    public long countValidados(Integer cohorteId) {
        return service.countValidadosByCohorte(cohorteId);
    }

    public long countPorCalificar(Integer cohorteId) {
        return service.countPorCalificarByCohorte(cohorteId);
    }

    public long countCalificados(Integer cohorteId) {
        return service.countCalificadosByCohorte(cohorteId);
    }

    @Transactional
    public CohorteListadoOutput createCohorte(Integer programaId, COHORTE_DIRECTOR_CREATE body) {
        LocalDate fechaInicio = body.fechaInicio();
        String nombre = body.nombre();

        List<TipoplazoDTO> tipoplazos = tipoplazoService.findAll();
        if (tipoplazos.isEmpty()) {
            throw new RuntimeException("No hay tipos de plazo configurados");
        }
        Integer tipoplazoId = tipoplazos.get(0).getId();

        PlazoDTO plazoDoc = plazoService.create(PlazoDTO.builder()
                .fechainicio(fechaInicio)
                .fechafin(body.fechaLimiteDocumentos())
                .idTipoplazo(tipoplazoId)
                .build());

        PlazoDTO plazoPago = plazoService.create(PlazoDTO.builder()
                .fechainicio(fechaInicio)
                .fechafin(body.fechaLimitePago())
                .idTipoplazo(tipoplazoId)
                .build());

        SemestreDTO semestre = resolverSemestreHabilitado(body.idSemestre(), fechaInicio);

        EstadoDTO estadoCohorte = estadoService.findByTipoAndEntidad("CERRADA", "cohorte");
        if (estadoCohorte == null) {
            throw new RuntimeException("No hay estado CERRADA configurado para cohorte");
        }

        Integer cohorteId = cohorteService.createAndGetId(CohorteDTO.builder()
                .nombre(nombre)
                .cupos(body.cupos())
                .idEstado(estadoCohorte.getId())
                .idSemestre(semestre.getId())
                .idModalidad(body.idModalidad())
                .idPlazodocumentacion(plazoDoc.getId())
                .idPlazoinscripcion(plazoDoc.getId())
                .idPlazopago(plazoPago.getId())
                .idPrograma(programaId)
                .build());

        if (body.documentosConsejo() != null) {
            body.documentosConsejo().stream()
                    .map(DOCUMENTO_ASIGNADO_CREATE::idDocrequisito)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .forEach(idDocrequisito -> documentosrequisitoconsejocohorteService.create(
                            DocumentosrequisitoconsejocohorteDTO.builder()
                                    .idDocrequisito(idDocrequisito)
                                    .idCohorte(cohorteId)
                                    .build()));
        }

        if (body.documentosPrograma() != null) {
            body.documentosPrograma().stream()
                    .map(DOCUMENTO_ASIGNADO_CREATE::idDocrequisito)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .forEach(idDocrequisito -> documentosrequisitoprogramacohorteService.create(
                            DocumentosrequisitoprogramacohorteDTO.builder()
                                    .idDocrequisito(idDocrequisito)
                                    .idCohorte(cohorteId)
                                    .build()));
        }

        if (body.criteriosCohorte() != null) {
            body.criteriosCohorte().stream()
                    .filter(java.util.Objects::nonNull)
                    .filter(criterio -> criterio.idCriterio() != null)
                    .distinct()
                    .forEach(criterio -> criteriocohorteService.create(
                            CriteriocohorteDTO.builder()
                                    .idCohorte(cohorteId)
                                    .idCriterio(criterio.idCriterio())
                                    .pesoSnapshot(criterio.pesoSnapshot())
                                    .build()));
        }

        return CohorteListadoOutput.builder()
                .id(cohorteId)
                .nombre(nombre)
                .activa(false)
                .inscritos(0)
                .admitidos(0)
                .cupos(body.cupos())
                .fechaLimiteDocumentos(body.fechaLimiteDocumentos())
                .fechaLimitePago(body.fechaLimitePago())
                .fechaInicio(fechaInicio)
                .build();
    }

    private SemestreDTO resolverSemestreHabilitado(Integer idSemestre, LocalDate fechaInicio) {
        SemestreDTO semestre = semestreService.findById(idSemestre);
        if (semestre == null) {
            throw new DomainException(CohorteErrorCode.COHORTE_NOT_FOUND, idSemestre);
        }

        String tipoEstado = semestre.getEstado() != null ? semestre.getEstado().getTipo() : null;
        boolean estadoPermitido = "EN CURSO".equalsIgnoreCase(tipoEstado) || "PROGRAMADO".equalsIgnoreCase(tipoEstado);
        if (!estadoPermitido) {
            throw new DomainException(CohorteErrorCode.COHORTE_SEMESTRE_NO_VALIDO_CONFLICT,
                    idSemestre + " / " + tipoEstado);
        }

        LocalDate fechaInicioSemestre = semestre.getFechaInicio();
        if (fechaInicioSemestre == null) {
            throw new DomainException(CohorteErrorCode.COHORTE_SEMESTRE_NO_VALIDO_CONFLICT, idSemestre);
        }

        LocalDate limiteSuperior = fechaInicioSemestre.plusMonths(2);
        if (fechaInicio.isBefore(fechaInicioSemestre) || fechaInicio.isAfter(limiteSuperior)) {
            throw new DomainException(CohorteErrorCode.COHORTE_SEMESTRE_FECHA_INVALIDA_CONFLICT,
                    "fechaInicio=" + fechaInicio + ", semestre=" + idSemestre + ", rango=" + fechaInicioSemestre + ".."
                            + limiteSuperior);
        }

        return semestre;
    }

    public List<CohorteResumenOutput> getCohortesByProgramaResumen(Integer programaId) {
        return cohorteService.findResumenDataByIdPrograma(programaId).stream().map(row -> {
            Integer id = row.getId();
            String nombre = row.getNombre();
            Integer cuposRaw = row.getCupos();
            String estadoTipo = row.getEstado() != null ? row.getEstado().getTipo() : null;
            String semNombre = row.getSemestre() != null ? row.getSemestre().getNombre() : null;
            LocalDate plazoDocFin = row.getPlazo() != null ? row.getPlazo().getFechafin() : null;
            LocalDate plazoInsFin = row.getPlazo2() != null ? row.getPlazo2().getFechafin() : null;
            LocalDate plazoPagoFin = row.getPlazo3() != null ? row.getPlazo3().getFechafin() : null;

            boolean activa = "ABIERTA".equalsIgnoreCase(estadoTipo);
            int cupos = cuposRaw != null ? cuposRaw : 0;
            long inscritos = service.countByCohorte(id);
            long pazYSalvo = service.countPazYSalvoByCohorte(id);
            long validados = service.countValidadosByCohorte(id);
            long calificados = service.countCalificadosByCohorte(id);
            long admitidos = service.countAdmitidosByCohorte(id);

            return CohorteResumenOutput.builder()
                    .id(id)
                    .nombre(nombre)
                    .activa(activa)
                    .semestre(semNombre)
                    .cupos(cupos)
                    .fechaLimitePago(plazoPagoFin)
                    .fechaLimiteDocs(plazoDocFin)
                    .fechaLimiteInscripcion(plazoInsFin)
                    .totalInscritos(inscritos)
                    .totalPazysalvo(pazYSalvo)
                    .totalValidados(validados)
                    .totalCalificados(calificados)
                    .totalAdmitidos(admitidos)
                    .build();
        }).toList();
    }

    public List<AspiranteCohorteOutput> findByCohorteConResumen(Integer cohorteId) {
        return service.findByCohorte(cohorteId).stream().map(aspirante -> {
            PersonaDTO p = aspirante.getPersona();
            String nombre = p != null
                    ? ((p.getNombres() != null ? p.getNombres() : "") + " "
                            + (p.getApellidos() != null ? p.getApellidos() : "")).trim()
                    : "";
            String cedula = p != null && p.getDocumentopersona() != null
                    && p.getDocumentopersona().getNumerodocumento() != null
                            ? p.getDocumentopersona().getNumerodocumento().toString()
                            : null;

            List<DocumentoDTO> docs = documentoService.findByIdAspirante(aspirante.getId());
            long total = docs.size();
            long validados = docs.stream()
                    .filter(d -> d.getEstadodocumento() != null
                            && "APROBADO".equalsIgnoreCase(d.getEstadodocumento().getEstado()))
                    .count();

            return AspiranteCohorteOutput.builder()
                    .id(aspirante.getId())
                    .nombre(nombre)
                    .cedula(cedula)
                    .correo(p != null ? p.getCorreo() : null)
                    .documentosValidados(validados)
                    .totalDocumentos(total)
                    .estadoGeneral(aspirante.getEstado().getTipo())
                    .build();
        }).toList();
    }

    public List<AspiranteCohorteOutput> findAValidarByCohorte(Integer cohorteId) {
        return service.findAValidarByCohorte(cohorteId).stream().map(aspirante -> {
            PersonaDTO p = aspirante.getPersona();
            String nombre = p != null
                    ? ((p.getNombres() != null ? p.getNombres() : "") + " "
                            + (p.getApellidos() != null ? p.getApellidos() : "")).trim()
                    : "";
            String cedula = p != null && p.getDocumentopersona() != null
                    && p.getDocumentopersona().getNumerodocumento() != null
                            ? p.getDocumentopersona().getNumerodocumento().toString()
                            : null;

            List<DocumentoDTO> docs = documentoService.findByIdAspirante(aspirante.getId());
            long total = docs.size();
            long validados = docs.stream()
                    .filter(d -> d.getEstadodocumento() != null
                            && "APROBADO".equalsIgnoreCase(d.getEstadodocumento().getEstado()))
                    .count();

            return AspiranteCohorteOutput.builder()
                    .id(aspirante.getId())
                    .nombre(nombre)
                    .cedula(cedula)
                    .correo(p != null ? p.getCorreo() : null)
                    .documentosValidados(validados)
                    .totalDocumentos(total)
                    .estadoGeneral(aspirante.getEstado().getTipo())
                    .build();
        }).toList();
    }

    public RankingAdmitidosOutput getRankingAdmitidos(Integer cohorteId) {
        CohorteDTO cohorte = cohorteService.findById(cohorteId);
        if (cohorte == null) {
            throw new RuntimeException("Cohorte no encontrada: " + cohorteId);
        }
        boolean activa = cohorte.getEstado() != null
                && "ABIERTA".equalsIgnoreCase(cohorte.getEstado().getTipo());

        var admitidosList = admitidoService.findByCohorte(cohorte.getId());
        Set<Integer> admitidosIds = admitidosList.stream()
                .map(a -> a.getIdAspirante())
                .collect(Collectors.toSet());

        long totalAdmitidos = admitidosList.size();
        int cuposDisponibles = Math.max(0, cohorte.getCupos() - (int) totalAdmitidos);

        List<RankingAdmitidosOutput.AspiranteResumen> aspirantesResumen = service.findByCohorte(cohorte.getId())
                .stream()
                .filter(a -> a.getEstado() != null
                        && ("VALIDADO_CALIFICADO".equalsIgnoreCase(a.getEstado().getTipo())
                                || "ADMITIDO".equalsIgnoreCase(a.getEstado().getTipo())))
                .sorted(Comparator.comparing(AspiranteDTO::getPuntuacion,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(a -> {
                    PersonaDTO persona = a.getPersona();
                    String nombre = persona != null
                            ? ((persona.getNombres() != null ? persona.getNombres() : "") + " "
                                    + (persona.getApellidos() != null ? persona.getApellidos() : "")).trim()
                            : "";
                    return RankingAdmitidosOutput.AspiranteResumen.builder()
                            .id(a.getId())
                            .nombre(nombre)
                            .correo(persona != null ? persona.getCorreo() : null)
                            .puntaje(a.getPuntuacion())
                            .admitido(admitidosIds.contains(a.getId()))
                            .build();
                })
                .toList();

        return RankingAdmitidosOutput.builder()
                .cohorteActual(RankingAdmitidosOutput.CohorteResumen.builder()
                        .id(cohorte.getId())
                        .nombre(cohorte.getNombre())
                        .activa(activa)
                        .cuposDisponibles(cuposDisponibles)
                        .totalAdmitidos(totalAdmitidos)
                        .build())
                .aspirantes(aspirantesResumen)
                .build();
    }

    public CohorteListadoOutput abrirCohorte(Integer cohorteId) {
        return cambiarEstadoCohorte(cohorteId, "ABIERTA");
    }

    public CohorteListadoOutput cerrarCohorte(Integer cohorteId) {
        return cambiarEstadoCohorte(cohorteId, "CERRADA");
    }

    private CohorteListadoOutput cambiarEstadoCohorte(Integer cohorteId, String nuevoEstado) {
        CohorteDTO cohorte = cohorteService.findById(cohorteId);
        if (cohorte == null) {
            throw new RuntimeException("Cohorte no encontrada: " + cohorteId);
        }
        EstadoDTO estado = estadoService.findByTipoAndEntidad(nuevoEstado, "cohorte");
        if (estado == null) {
            throw new RuntimeException("Estado '" + nuevoEstado + "' no configurado para cohorte");
        }
        cohorte.setIdEstado(estado.getId());
        cohorteService.update(cohorteId, cohorte);

        boolean activa = "ABIERTA".equalsIgnoreCase(nuevoEstado);
        return CohorteListadoOutput.builder()
                .id(cohorteId)
                .nombre(cohorte.getNombre())
                .activa(activa)
                .inscritos(service.countByCohorte(cohorteId))
                .admitidos(admitidoService.countByCohorte(cohorteId))
                .cupos(cohorte.getCupos())
                .fechaLimiteDocumentos(cohorte.getPlazo() != null ? cohorte.getPlazo().getFechafin() : null)
                .fechaLimitePago(cohorte.getPlazo3() != null ? cohorte.getPlazo3().getFechafin() : null)
                .fechaInicio(cohorte.getSemestre() != null ? cohorte.getSemestre().getFechaInicio() : null)
                .build();
    }

    @Transactional
    public CohorteListadoOutput updateCohorte(Integer cohorteId, COHORTE_DIRECTOR_UPDATE body) {
        Integer targetCohorteId = body.id() != null ? body.id() : cohorteId;
        if (targetCohorteId == null) {
            throw new RuntimeException("Debe enviar el id de la cohorte a actualizar");
        }
        if (cohorteId != null && !cohorteId.equals(targetCohorteId)) {
            throw new RuntimeException("El id de la ruta no coincide con el id del body");
        }

        CohorteDTO cohorte = cohorteService.findById(targetCohorteId);
        if (cohorte == null) {
            throw new RuntimeException("Cohorte no encontrada: " + targetCohorteId);
        }

        boolean cohorteChanged = false;
        if (body.nombre() != null && !body.nombre().isBlank()) {
            cohorte.setNombre(body.nombre());
            cohorteChanged = true;
        }
        if (body.cupos() != null) {
            cohorte.setCupos(body.cupos());
            cohorteChanged = true;
        }

        LocalDate fechaReferencia = body.fechaInicio();
        SemestreDTO semestreActual = cohorte.getSemestre();
        if (fechaReferencia == null && semestreActual != null) {
            fechaReferencia = semestreActual.getFechaInicio();
        }

        if (body.idSemestre() == null) {
            throw new IllegalArgumentException("Debe enviar el id del semestre");
        }

        if (body.idModalidad() == null) {
            throw new IllegalArgumentException("Debe enviar el id de la modalidad");
        }

        if (fechaReferencia == null) {
            throw new IllegalArgumentException("Debe enviar la fecha de inicio para validar el semestre");
        }

        SemestreDTO semestre = resolverSemestreHabilitado(body.idSemestre(), fechaReferencia);
        if (cohorte.getIdSemestre() == null || !cohorte.getIdSemestre().equals(semestre.getId())) {
            cohorte.setIdSemestre(semestre.getId());
            cohorteChanged = true;
        }

        if (cohorte.getIdModalidad() == null || !cohorte.getIdModalidad().equals(body.idModalidad())) {
            cohorte.setIdModalidad(body.idModalidad());
            cohorteChanged = true;
        }

        LocalDate fechaInicio = semestre.getFechaInicio();

        LocalDate fechaLimiteDocumentos = cohorte.getPlazo() != null ? cohorte.getPlazo().getFechafin() : null;
        if (body.fechaLimiteDocumentos() != null && cohorte.getPlazo() != null) {
            PlazoDTO plazo = cohorte.getPlazo();
            plazo.setFechafin(body.fechaLimiteDocumentos());
            plazoService.update(plazo.getId(), plazo);
            fechaLimiteDocumentos = body.fechaLimiteDocumentos();
        }

        LocalDate fechaLimitePago = cohorte.getPlazo3() != null ? cohorte.getPlazo3().getFechafin() : null;
        if (body.fechaLimitePago() != null && cohorte.getPlazo3() != null) {
            PlazoDTO plazo3 = cohorte.getPlazo3();
            plazo3.setFechafin(body.fechaLimitePago());
            plazoService.update(plazo3.getId(), plazo3);
            fechaLimitePago = body.fechaLimitePago();
        }

        if (cohorteChanged) {
            cohorteService.update(targetCohorteId, cohorte);
        }

        var existingConsejo = documentosrequisitoconsejocohorteService.findByIdCohorte(targetCohorteId);
        if (body.documentosConsejo() == null) {
            deleteAllConsejoDocuments(existingConsejo);
        } else if (!sameConsejoDocuments(body.documentosConsejo(), existingConsejo)) {
            if (body.documentosConsejo().stream().anyMatch(doc -> doc == null || doc.idDocrequisito() == null)) {
                throw new IllegalArgumentException(
                        "Todos los documentos de consejo deben incluir idDocrequisito");
            }

            List<Integer> invalidConsejoIds = body.documentosConsejo().stream()
                    .map(DOCUMENTO_ASIGNADO_CREATE::idDocrequisito)
                    .distinct()
                    .filter(idDocrequisito -> documentosrequisitoconsejoService.findById(idDocrequisito) == null)
                    .toList();

            if (!invalidConsejoIds.isEmpty()) {
                throw new IllegalArgumentException(
                        "No existen documentos de consejo con ids: " + invalidConsejoIds);
            }

            List<Integer> incomingConsejoIds = body.documentosConsejo().stream()
                    .map(DOCUMENTO_ASIGNADO_CREATE::idDocrequisito)
                    .distinct()
                    .toList();

            var toDeleteConsejo = existingConsejo.stream()
                    .filter(actual -> !incomingConsejoIds.contains(actual.getIdDocrequisito()))
                    .toList();

            var blockedConsejo = toDeleteConsejo.stream()
                    .filter(actual -> documentoService.countByIdDocumentosrequisitoconsejocohorte(actual.getId()) > 0)
                    .map(v -> v.getId())
                    .toList();

            if (!blockedConsejo.isEmpty()) {
                throw new DomainException(CohorteErrorCode.COHORTE_CON_ASIGNACIONES_BLOQUEADAS, blockedConsejo);
            }

            toDeleteConsejo.forEach(actual -> documentosrequisitoconsejocohorteService.deleteById(actual.getId()));

            var existingConsejoDocIds = existingConsejo.stream()
                    .map(v -> v.getIdDocrequisito())
                    .toList();

            incomingConsejoIds.stream()
                    .filter(idDocrequisito -> !existingConsejoDocIds.contains(idDocrequisito))
                    .forEach(idDocrequisito -> documentosrequisitoconsejocohorteService.create(
                            DocumentosrequisitoconsejocohorteDTO.builder()
                                    .idDocrequisito(idDocrequisito)
                                    .idCohorte(targetCohorteId)
                                    .build()));
        }

        var existingPrograma = documentosrequisitoprogramacohorteService.findByIdCohorte(targetCohorteId);
        if (body.documentosPrograma() == null) {
            deleteAllProgramaDocuments(existingPrograma);
        } else if (!sameProgramaDocuments(body.documentosPrograma(), existingPrograma)) {
            if (body.documentosPrograma().stream().anyMatch(doc -> doc == null || doc.idDocrequisito() == null)) {
                throw new IllegalArgumentException(
                        "Todos los documentos de programa deben incluir idDocrequisito");
            }

            List<Integer> invalidProgramaIds = body.documentosPrograma().stream()
                    .map(DOCUMENTO_ASIGNADO_CREATE::idDocrequisito)
                    .distinct()
                    .filter(idDocrequisito -> documentosrequisitoprogramaService.findById(idDocrequisito) == null)
                    .toList();

            if (!invalidProgramaIds.isEmpty()) {
                throw new IllegalArgumentException(
                        "No existen documentos de programa con ids: " + invalidProgramaIds);
            }

            List<Integer> incomingProgramaIds = body.documentosPrograma().stream()
                    .map(DOCUMENTO_ASIGNADO_CREATE::idDocrequisito)
                    .distinct()
                    .toList();

            var toDeletePrograma = existingPrograma.stream()
                    .filter(actual -> !incomingProgramaIds.contains(actual.getIdDocrequisito()))
                    .toList();

            var blockedPrograma = toDeletePrograma.stream()
                    .filter(actual -> documentoService.countByIdDocumentosrequisitoprogramacohorte(actual.getId()) > 0)
                    .map(v -> v.getId())
                    .toList();

            if (!blockedPrograma.isEmpty()) {
                throw new DomainException(CohorteErrorCode.COHORTE_CON_ASIGNACIONES_BLOQUEADAS, blockedPrograma);
            }

            toDeletePrograma.forEach(actual -> documentosrequisitoprogramacohorteService.deleteById(actual.getId()));

            var existingProgramaDocIds = existingPrograma.stream()
                    .map(v -> v.getIdDocrequisito())
                    .toList();

            incomingProgramaIds.stream()
                    .filter(idDocrequisito -> !existingProgramaDocIds.contains(idDocrequisito))
                    .forEach(idDocrequisito -> documentosrequisitoprogramacohorteService.create(
                            DocumentosrequisitoprogramacohorteDTO.builder()
                                    .idDocrequisito(idDocrequisito)
                                    .idCohorte(targetCohorteId)
                                    .build()));
        }

        List<CriteriocohorteDTO> criteriosExistentes = criteriocohorteService.findByIdCohorte(targetCohorteId);
        if (body.criteriosCohorte() == null) {
            deleteAllCriterios(criteriosExistentes);
        } else if (!sameCriterios(body.criteriosCohorte(), criteriosExistentes)) {
            Map<Integer, CriteriocohorteDTO> criteriosPorId = criteriosExistentes.stream()
                    .collect(Collectors.toMap(CriteriocohorteDTO::getId, criterio -> criterio));
            Map<Integer, CriteriocohorteDTO> criteriosPorIdCriterio = criteriosExistentes.stream()
                    .collect(Collectors.toMap(CriteriocohorteDTO::getIdCriterio, criterio -> criterio,
                            (primero, segundo) -> primero));

            Set<Integer> idsRecibidos = body.criteriosCohorte().stream()
                    .filter(java.util.Objects::nonNull)
                    .map(CohorteInput.CRITERIOCOHORTE_DIRECTOR_UPDATE::id)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());

            criteriosExistentes.stream()
                    .filter(actual -> !idsRecibidos.contains(actual.getId()))
                    .forEach(actual -> {
                        if (calificacioncriterioService.existsByCriterio(actual.getId())) {
                            throw new DomainException(CriterioevaluacionErrorCode.CRITERIO_CON_CALIFICACIONES_BLOQUEADO,
                                    actual.getIdCriterio());
                        }
                        criteriocohorteService.deleteById(actual.getId());
                    });

            body.criteriosCohorte().stream()
                    .filter(java.util.Objects::nonNull)
                    .forEach(criterio -> {
                        if (criterio.idCriterio() == null) {
                            throw new DomainException(CriteriocohorteErrorCode.CRITERIOCOHORTE_IDCRITERIO_OBLIGATORIO,
                                    criterio);
                        }

                        CriterioevaluacionDTO criterioEvaluacion = criterioevaluacionService
                                .findById(criterio.idCriterio());
                        if (criterioEvaluacion == null) {
                            throw new DomainException(CriterioevaluacionErrorCode.CRITERIOEVALUACION_NOT_FOUND,
                                    criterio.idCriterio());
                        }
                        if (!java.util.Objects.equals(criterioEvaluacion.getIdprograma(), cohorte.getIdPrograma())) {
                            throw new DomainException(CriteriocohorteErrorCode.CRITERIO_NO_PERTENECE_AL_PROGRAMA,
                                    criterio.idCriterio());
                        }
                        if (Boolean.FALSE.equals(criterioEvaluacion.getActivo())) {
                            throw new DomainException(CriteriocohorteErrorCode.CRITERIO_INACTIVO,
                                    criterio.idCriterio());
                        }

                        if (criterio.id() != null) {
                            CriteriocohorteDTO existente = criteriosPorId.get(criterio.id());
                            if (existente == null) {
                                throw new DomainException(CriteriocohorteErrorCode.CRITERIOCOHORTE_NOT_FOUND,
                                        criterio.id());
                            }
                            if (!java.util.Objects.equals(existente.getIdCriterio(), criterio.idCriterio())) {
                                throw new DomainException(CriteriocohorteErrorCode.CRITERIOCOHORTE_MISMATCH,
                                        criterio.id());
                            }
                            if (calificacioncriterioService.existsByCriterio(existente.getId())) {
                                throw new DomainException(
                                        CriterioevaluacionErrorCode.CRITERIO_CON_CALIFICACIONES_BLOQUEADO,
                                        existente.getIdCriterio());
                            }

                            criteriocohorteService.update(existente.getId(), CriteriocohorteDTO.builder()
                                    .idCohorte(existente.getIdCohorte())
                                    .idCriterio(existente.getIdCriterio())
                                    .pesoSnapshot(criterio.pesoSnapshot())
                                    .build());
                            return;
                        }

                        if (criteriosPorIdCriterio.containsKey(criterio.idCriterio())) {
                            throw new DomainException(CriteriocohorteErrorCode.CRITERIO_YA_ASIGNADO_A_COHORTE,
                                    criterio.idCriterio());
                        }

                        criteriocohorteService.create(CriteriocohorteDTO.builder()
                                .idCohorte(targetCohorteId)
                                .idCriterio(criterio.idCriterio())
                                .pesoSnapshot(criterio.pesoSnapshot())
                                .build());
                    });
        }

        boolean activa = cohorte.getEstado() != null && "ABIERTA".equalsIgnoreCase(cohorte.getEstado().getTipo());

        return CohorteListadoOutput.builder()
                .id(targetCohorteId)
                .nombre(cohorte.getNombre())
                .activa(activa)
                .inscritos(service.countByCohorte(targetCohorteId))
                .admitidos(admitidoService.countByCohorte(targetCohorteId))
                .cupos(cohorte.getCupos())
                .fechaLimiteDocumentos(fechaLimiteDocumentos)
                .fechaLimitePago(fechaLimitePago)
                .fechaInicio(fechaInicio)
                .build();
    }

    public AspiranteCriteriosOutput getCriteriosAspirante(Integer idAspirante) {
        return findCriteriosCalificacion(new ASPIRANTE_FIND(idAspirante));
    }

    private Boolean sameConsejoDocuments(List<DOCUMENTO_ASIGNADO_CREATE> incoming,
            List<DocumentosrequisitoconsejocohorteDTO> existing) {
        Set<Integer> incomingIds = incoming.stream()
                .map(documento -> documento != null ? documento.idDocrequisito() : null)
                .collect(Collectors.toSet());
        if (incomingIds.contains(null)) {
            return false;
        }

        Set<Integer> existingIds = existing.stream()
                .map(DocumentosrequisitoconsejocohorteDTO::getIdDocrequisito)
                .collect(Collectors.toSet());
        return incomingIds.equals(existingIds);
    }

    private Boolean sameProgramaDocuments(List<DOCUMENTO_ASIGNADO_CREATE> incoming,
            List<DocumentosrequisitoprogramacohorteDTO> existing) {
        Set<Integer> incomingIds = incoming.stream()
                .map(documento -> documento != null ? documento.idDocrequisito() : null)
                .collect(Collectors.toSet());
        if (incomingIds.contains(null)) {
            return false;
        }

        Set<Integer> existingIds = existing.stream()
                .map(DocumentosrequisitoprogramacohorteDTO::getIdDocrequisito)
                .collect(Collectors.toSet());
        return incomingIds.equals(existingIds);
    }

    private Boolean sameCriterios(List<CRITERIOCOHORTE_DIRECTOR_UPDATE> incoming,
            List<CriteriocohorteDTO> existing) {
        Set<String> incomingSignatures = incoming.stream()
                .map(criterio -> criterio != null ? criterioSignature(criterio.idCriterio(), criterio.pesoSnapshot())
                        : null)
                .collect(Collectors.toSet());
        if (incomingSignatures.contains(null)) {
            return false;
        }

        Set<String> existingSignatures = existing.stream()
                .map(criterio -> criterioSignature(criterio.getIdCriterio(), criterio.getPesoSnapshot()))
                .collect(Collectors.toSet());
        return incomingSignatures.equals(existingSignatures);
    }

    private String criterioSignature(Integer idCriterio, BigDecimal pesoSnapshot) {
        String peso = pesoSnapshot != null ? pesoSnapshot.stripTrailingZeros().toPlainString() : "null";
        return idCriterio + "::" + peso;
    }

    private void deleteAllConsejoDocuments(List<DocumentosrequisitoconsejocohorteDTO> existingConsejo) {
        var toDeleteConsejo = existingConsejo;
        var blockedConsejo = toDeleteConsejo.stream()
                .filter(actual -> documentoService.countByIdDocumentosrequisitoconsejocohorte(actual.getId()) > 0)
                .map(DocumentosrequisitoconsejocohorteDTO::getId)
                .toList();

        if (!blockedConsejo.isEmpty()) {
            throw new DomainException(CohorteErrorCode.COHORTE_CON_ASIGNACIONES_BLOQUEADAS, blockedConsejo);
        }

        toDeleteConsejo.forEach(actual -> documentosrequisitoconsejocohorteService.deleteById(actual.getId()));
    }

    private void deleteAllProgramaDocuments(List<DocumentosrequisitoprogramacohorteDTO> existingPrograma) {
        var toDeletePrograma = existingPrograma;
        var blockedPrograma = toDeletePrograma.stream()
                .filter(actual -> documentoService.countByIdDocumentosrequisitoprogramacohorte(actual.getId()) > 0)
                .map(DocumentosrequisitoprogramacohorteDTO::getId)
                .toList();

        if (!blockedPrograma.isEmpty()) {
            throw new DomainException(CohorteErrorCode.COHORTE_CON_ASIGNACIONES_BLOQUEADAS, blockedPrograma);
        }

        toDeletePrograma.forEach(actual -> documentosrequisitoprogramacohorteService.deleteById(actual.getId()));
    }

    private void deleteAllCriterios(List<CriteriocohorteDTO> criteriosExistentes) {
        criteriosExistentes.forEach(actual -> {
            if (calificacioncriterioService.existsByCriterio(actual.getId())) {
                throw new DomainException(CriterioevaluacionErrorCode.CRITERIO_CON_CALIFICACIONES_BLOQUEADO,
                        actual.getIdCriterio());
            }
            criteriocohorteService.deleteById(actual.getId());
        });
    }
}
