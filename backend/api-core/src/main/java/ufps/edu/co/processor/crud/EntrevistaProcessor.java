package ufps.edu.co.processor.crud;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ufps.edu.co.maps.specific.EntrevistaMap;
import ufps.edu.co.records.input.entity.AspiranteInput.ASPIRANTE_FIND;
import ufps.edu.co.records.input.entity.EntrevistaInput.*;
import ufps.edu.co.records.output.entity.EntrevistaOutput;
import ufps.edu.co.records.output.entity.EntrevistaResumenOutput;
import ufps.edu.co.rest.dto.AspiranteDTO;
import ufps.edu.co.rest.dto.EntrevistaDTO;
import ufps.edu.co.rest.dto.EstadoDTO;
import ufps.edu.co.rest.dto.PersonaDTO;
import ufps.edu.co.rest.dto.TipoentrevistaDTO;
import ufps.edu.co.rest.dto.UbicacionDTO;
import ufps.edu.co.rest.services.AdministrativoService;
import ufps.edu.co.rest.services.AspiranteService;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.rest.services.EntrevistaService;
import ufps.edu.co.rest.services.EstadoService;
import ufps.edu.co.rest.services.PersonaService;
import ufps.edu.co.rest.services.TipoentrevistaService;
import ufps.edu.co.rest.services.UbicacionService;
import ufps.edu.co.services.*;
import ufps.edu.co.utils.*;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class EntrevistaProcessor implements
        GlobalUseCase<ENTREVISTA_CREATE, ENTREVISTA_UPDATE, ENTREVISTA_DELETE, ENTREVISTA_PATCH, ENTREVISTA_FIND, EntrevistaOutput> {

    private static final Logger logger = LoggerFactory.getLogger(EntrevistaProcessor.class);

    @Autowired
    private EntrevistaService service;

    @Autowired
    private AspiranteService aspiranteService;

    @Autowired
    private PersonaService personaService;

    @Autowired
    private UbicacionService ubicacionService;

    @Autowired
    private TipoentrevistaService tipoentrevistaService;

    @Autowired
    private EstadoService estadoService;

    @Autowired
    private CohorteService cohorteService;

    @Autowired
    private AdministrativoService administrativoService;

    @Autowired
    private EntrevistaMap map;

    @Autowired
    private SESService sesService;

    // @Autowired
    // private EmailTemplates emailTemplates;

    @Override
    public EntrevistaOutput create(ENTREVISTA_CREATE input) {
        try {
            TipoentrevistaDTO tipoentrevista = tipoentrevistaService.findById(input.idTipoentrevista());
            if (tipoentrevista == null) {
                throw new RuntimeException("Modalidad no encontrada: " + input.idTipoentrevista());
            }
            EstadoDTO estadoInicial = estadoService.findByTipoAndEntidad("PENDIENTE DE CONFIRMACION", "entrevista");
            if (estadoInicial == null) {
                throw new RuntimeException(
                        "Estado inicial 'PENDIENTE DE CONFIRMACION' no encontrado para entidad 'entrevista'");
            }
            UbicacionDTO ubicacion = ubicacionService.create(
                    UbicacionDTO.builder().direccion(input.ubicacion()).zonaurbana(true).build());
            EntrevistaDTO dto = map.toDto(input);
            dto.setIdTipoentrevista(tipoentrevista.getId());
            dto.setIdEstado(estadoInicial.getId());
            dto.setIdUbicacion(ubicacion.getId());
            EntrevistaDTO created = service.create(dto);
            EntrevistaOutput output = map.toOutput(created);
            notifyEntrevistaCreada(created, input.idAspirante());
            return output;
        } catch (Exception e) {
            throw new RuntimeException("Error creating Entrevista: " + e.getMessage(), e);
        }
    }

    @Override
    public EntrevistaOutput update(ENTREVISTA_UPDATE input) {
        try {
            UbicacionDTO ubicacion = ubicacionService
                    .create(UbicacionDTO.builder().direccion(input.ubicacion()).zonaurbana(true).build());
            EntrevistaDTO dto = map.toDto(input);
            dto.setIdUbicacion(ubicacion.getId());
            EntrevistaDTO updated = service.update(input.id(), dto);
            return map.toOutput(updated);
        } catch (Exception e) {
            throw new RuntimeException("Error updating Entrevista: " + e.getMessage(), e);
        }
    }

    @Override
    public EntrevistaOutput patch(ENTREVISTA_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Entrevista");
    }

    @Override
    @Transactional(readOnly = true)
    public EntrevistaOutput findById(ENTREVISTA_FIND input) {
        try {
            EntrevistaDTO dto = service.findById(input.id());
            return map.toOutput(dto);
        } catch (Exception e) {
            throw new RuntimeException("Error finding Entrevista by ID: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntrevistaOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Entrevistas: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(ENTREVISTA_DELETE input) {
        try {
            EntrevistaDTO entrevista = service.findById(input.id());
            service.deleteById(input.id());
            notifyEntrevistaEliminada(entrevista);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Entrevista by ID: " + e.getMessage(), e);
        }
    }

    public EntrevistaOutput completeInterview(ENTREVISTA_FIND input) {
        try {
            EstadoDTO estadoCompletada = estadoService.findByTipoAndEntidad("COMPLETADA", "entrevista");
            if (estadoCompletada == null) {
                throw new RuntimeException("Estado 'COMPLETADA' no encontrado para entidad 'entrevista'");
            }
            EntrevistaDTO updated = service.changeEstado(input.id(), estadoCompletada.getId(), "CONFIRMADA");
            notifyActividadCompletada(updated);
            return map.toOutput(updated);
        } catch (Exception e) {
            throw new RuntimeException("Error completing Entrevista: " + e.getMessage(), e);
        }
    }

    public EntrevistaOutput cancelInterview(ENTREVISTA_FIND input) {
        return cancelInterview(input.id(), null, "DIRECTOR");
    }

    public EntrevistaOutput cancelInterview(Integer id, String motivocambio, String canceladoPor) {
        try {
            EstadoDTO estadoCancelada = estadoService.findByTipoAndEntidad("CANCELADA", "entrevista");
            if (estadoCancelada == null) {
                throw new RuntimeException("Estado 'CANCELADA' no encontrado para entidad 'entrevista'");
            }
            EntrevistaDTO updated = service.changeEstadoWithMotivo(id, estadoCancelada.getId(), "CONFIRMADA",
                    motivocambio);
            notifyCancelacion(updated, canceladoPor, motivocambio);
            return map.toOutput(updated);
        } catch (Exception e) {
            throw new RuntimeException("Error cancelling Entrevista: " + e.getMessage(), e);
        }
    }

    public EntrevistaOutput confirmInterview(ENTREVISTA_FIND input) {
        try {
            EstadoDTO estadoConfirmada = estadoService.findByTipoAndEntidad("CONFIRMADA", "entrevista");
            if (estadoConfirmada == null) {
                throw new RuntimeException("Estado 'CONFIRMADA' no encontrado para entidad 'entrevista'");
            }
            EntrevistaDTO updated = service.changeEstado(input.id(), estadoConfirmada.getId(),
                    "PENDIENTE DE CONFIRMACION");
            notifyConfirmacionAlDirector(updated);
            return map.toOutput(updated);
        } catch (Exception e) {
            throw new RuntimeException("Error confirming Entrevista: " + e.getMessage(), e);
        }
    }

    public EntrevistaOutput requestChangeInterview(Integer id, String motivocambio) {
        try {
            EntrevistaDTO updated = service.requestChange(id, motivocambio);
            notifyCambioSolicitadoAlDirector(updated, motivocambio);
            return map.toOutput(updated);
        } catch (Exception e) {
            throw new RuntimeException("Error requesting change for Entrevista: " + e.getMessage(), e);
        }
    }

    public EntrevistaOutput reschedule(ENTREVISTA_RESCHEDULE input) {
        try {
            TipoentrevistaDTO tipoentrevista = tipoentrevistaService.findById(input.idTipoentrevista());
            if (tipoentrevista == null) {
                throw new RuntimeException("Modalidad no encontrada: " + input.idTipoentrevista());
            }
            UbicacionDTO ubicacion = ubicacionService.create(
                    UbicacionDTO.builder().direccion(input.ubicacion()).zonaurbana(true).build());
            EntrevistaDTO updated = service.reschedule(
                    input.id(), input.fecha(), input.tiempo(),
                    tipoentrevista.getId(), ubicacion.getId(), input.motivocambio());
            AspiranteDTO aspirante = aspiranteService.findById(updated.getIdAspirante());
            sesService.enviarCorreo(aspirante.getPersona().getCorreo(), EmailTemplates.ASUNTO_REAGENDAR_ENTREVISTA,
                    EmailTemplates.cuerpoReagendarEntrevista(aspirante.getPersona().getNombres(),
                            input.fecha(), input.tiempo(), tipoentrevista.getTipo(), input.ubicacion()));
            return map.toOutput(updated);
        } catch (Exception e) {
            throw new RuntimeException("Error rescheduling Entrevista: " + e.getMessage(), e);
        }
    }

    public List<EntrevistaResumenOutput> findByIdAspirante(ASPIRANTE_FIND input) {
        try {
            return service.findByIdAspirante(input.id()).stream().map(dto -> {
                String direccion = dto.getUbicacion() != null ? dto.getUbicacion().getDireccion() : null;
                String estadoNombre = dto.getEstado() != null ? dto.getEstado().getTipo() : null;
                String tipoNombre = dto.getTipoentrevista() != null ? dto.getTipoentrevista().getTipo() : null;
                return EntrevistaResumenOutput.builder()
                        .id(dto.getId())
                        .fecha(dto.getFecha())
                        .tiempo(dto.getTiempo())
                        .idEstado(dto.getIdEstado())
                        .estado(estadoNombre)
                        .idTipoentrevista(dto.getIdTipoentrevista())
                        .tipoentrevista(tipoNombre)
                        .ubicacion(direccion)
                        .motivocambio(dto.getMotivocambio())
                        .build();
            }).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding Entrevistas by Aspirante ID: " + e.getMessage(), e);
        }
    }

    public EntrevistaOutput editEntrevista(Integer idEntrevista, ENTREVISTA_REAGENDAR_REQUEST request) {
        try {
            TipoentrevistaDTO tipoentrevista = tipoentrevistaService.findById(request.idTipoentrevista());
            if (tipoentrevista == null) {
                throw new RuntimeException("Modalidad no encontrada: " + request.idTipoentrevista());
            }
            UbicacionDTO ubicacion = ubicacionService.create(
                    UbicacionDTO.builder().direccion(request.ubicacion()).zonaurbana(true).build());
            EntrevistaDTO updated = service.edit(idEntrevista, request.fecha(), request.tiempo(),
                    tipoentrevista.getId(), ubicacion.getId());
            return map.toOutput(updated);
        } catch (Exception e) {
            throw new RuntimeException("Error editing Entrevista: " + e.getMessage(), e);
        }
    }

    public EntrevistaOutput rateInterview(ENTREVISTA_RATE input) {
        try {
            // TODO: Actualmente el método rateInterview no asigna la calificación a la
            // entrevista. Habría que modificar el servicio para que lo haga, y luego este
            // método funcionaría correctamente.
            EntrevistaDTO updated = service.rateInterview(input.id(), input.calificacion());
            return map.toOutput(updated);
        } catch (Exception e) {
            throw new RuntimeException("Error rating Entrevista: " + e.getMessage(), e);
        }
    }

    private void notifyEntrevistaCreada(EntrevistaDTO entrevista, Integer idAspirante) {
        try {
            Integer idPersona = aspiranteService.findIdPersonaById(idAspirante);
            if (idPersona == null) {
                return;
            }

            PersonaDTO persona = personaService.findById(idPersona);
            if (persona == null) {
                return;
            }

            String nombre = persona.getNombres();
            String correoAspirante = persona.getCorreo();
            // String fecha = entrevista.getFecha() != null ? entrevista.getFecha().toString() : "No definida";
            // String hora = entrevista.getTiempo() != null ? entrevista.getTiempo().toString() : "No definida";
            String tipo = entrevista.getTipoentrevista() != null ? entrevista.getTipoentrevista().getTipo()
                    : "No definido";
            String lugar = entrevista.getUbicacion() != null ? entrevista.getUbicacion().getDireccion() : "No definido";

            sesService.enviarCorreo(correoAspirante, EmailTemplates.ASUNTO_AGENDAR_ENTREVISTA,
                    EmailTemplates.cuerpoAgendarEntrevista(nombre, entrevista.getFecha(), entrevista.getTiempo(), tipo, lugar));
        } catch (Exception e) {
            logger.warn("No se pudo notificar entrevista creada al aspirante {}: {}", idAspirante, e.getMessage());
        }
    }

    private void notifyEntrevistaEliminada(EntrevistaDTO entrevista) {
        notifyCancelacion(entrevista, "DIRECTOR", null);
    }

    private void notifyConfirmacionAlDirector(EntrevistaDTO entrevista) {
        try {
            AspiranteDTO aspirante = aspiranteService.findById(entrevista.getIdAspirante());
            if (aspirante == null || aspirante.getPersona() == null) return;
            ufps.edu.co.rest.dto.AdministrativoDTO director = findDirectorForAspirante(aspirante);
            if (director == null || director.getPersona() == null) return;
            String tipo = entrevista.getTipoentrevista() != null ? entrevista.getTipoentrevista().getTipo() : "No definido";
            String lugar = entrevista.getUbicacion() != null ? entrevista.getUbicacion().getDireccion() : "No definido";
            sesService.enviarCorreo(director.getPersona().getCorreo(), EmailTemplates.ASUNTO_ASPIRANTE_CONFIRMA,
                    EmailTemplates.cuerpoConfirmacionAlDirector(
                            aspirante.getPersona().getNombres(), "entrevista", tipo,
                            entrevista.getFecha(), entrevista.getTiempo(), lugar));
        } catch (Exception e) {
            logger.warn("No se pudo notificar confirmación de entrevista al director: {}", e.getMessage());
        }
    }

    private void notifyActividadCompletada(EntrevistaDTO entrevista) {
        try {
            AspiranteDTO aspirante = aspiranteService.findById(entrevista.getIdAspirante());
            if (aspirante == null || aspirante.getPersona() == null) return;
            String tipo = entrevista.getTipoentrevista() != null ? entrevista.getTipoentrevista().getTipo() : "No definido";
            String lugar = entrevista.getUbicacion() != null ? entrevista.getUbicacion().getDireccion() : "No definido";
            sesService.enviarCorreo(aspirante.getPersona().getCorreo(), EmailTemplates.ASUNTO_ACTIVIDAD_COMPLETADA,
                    EmailTemplates.cuerpoActividadCompletada(
                            aspirante.getPersona().getNombres(), "entrevista", tipo,
                            entrevista.getFecha(), entrevista.getTiempo(), lugar));
        } catch (Exception e) {
            logger.warn("No se pudo notificar entrevista completada al aspirante: {}", e.getMessage());
        }
    }

    private void notifyCambioSolicitadoAlDirector(EntrevistaDTO entrevista, String motivo) {
        try {
            AspiranteDTO aspirante = aspiranteService.findById(entrevista.getIdAspirante());
            if (aspirante == null || aspirante.getPersona() == null) return;
            ufps.edu.co.rest.dto.AdministrativoDTO director = findDirectorForAspirante(aspirante);
            if (director == null || director.getPersona() == null) return;
            String tipo = entrevista.getTipoentrevista() != null ? entrevista.getTipoentrevista().getTipo() : "No definido";
            sesService.enviarCorreo(director.getPersona().getCorreo(), EmailTemplates.ASUNTO_SOLICITUD_CAMBIO,
                    EmailTemplates.cuerpoCambioSolicitadoAlDirector(
                            aspirante.getPersona().getNombres(), "entrevista", tipo,
                            entrevista.getFecha(), entrevista.getTiempo(),
                            motivo != null ? motivo : "Sin motivo especificado"));
        } catch (Exception e) {
            logger.warn("No se pudo notificar solicitud de cambio al director: {}", e.getMessage());
        }
    }

    private void notifyCancelacion(EntrevistaDTO entrevista, String canceladoPor, String motivo) {
        try {
            AspiranteDTO aspirante = aspiranteService.findById(entrevista.getIdAspirante());
            if (aspirante == null || aspirante.getPersona() == null) return;
            String tipo = entrevista.getTipoentrevista() != null ? entrevista.getTipoentrevista().getTipo() : "No definido";
            // String lugar = entrevista.getUbicacion() != null ? entrevista.getUbicacion().getDireccion() : "No definido";
            String motivoTexto = motivo != null ? motivo : "Sin motivo especificado";

            if ("ASPIRANTE".equalsIgnoreCase(canceladoPor)) {
                ufps.edu.co.rest.dto.AdministrativoDTO director = findDirectorForAspirante(aspirante);
                if (director == null || director.getPersona() == null) return;
                sesService.enviarCorreo(director.getPersona().getCorreo(), EmailTemplates.ASUNTO_CANCELACION_POR_ASPIRANTE,
                        EmailTemplates.cuerpoCancelacionPorAspirante(
                                aspirante.getPersona().getNombres(), "entrevista", tipo,
                                entrevista.getFecha(), entrevista.getTiempo(), motivoTexto));
            } else {
                sesService.enviarCorreo(aspirante.getPersona().getCorreo(), EmailTemplates.ASUNTO_CANCELACION_POR_DIRECTOR,
                        EmailTemplates.cuerpoCancelacionPorDirector(
                                aspirante.getPersona().getNombres(), "entrevista", tipo,
                                entrevista.getFecha(), entrevista.getTiempo(), motivoTexto));
            }
        } catch (Exception e) {
            logger.warn("No se pudo notificar cancelación de entrevista {}: {}", entrevista.getId(), e.getMessage());
        }
    }

    private ufps.edu.co.rest.dto.AdministrativoDTO findDirectorForAspirante(AspiranteDTO aspirante) {
        try {
            if (aspirante.getIdCohorte() == null) return null;
            ufps.edu.co.rest.dto.CohorteDTO cohorte = cohorteService.findById(aspirante.getIdCohorte());
            if (cohorte == null || cohorte.getIdPrograma() == null) return null;
            return administrativoService.findDirectorByIdPrograma(cohorte.getIdPrograma());
        } catch (Exception e) {
            logger.warn("No se pudo obtener el director para aspirante {}: {}", aspirante.getId(), e.getMessage());
            return null;
        }
    }
}
