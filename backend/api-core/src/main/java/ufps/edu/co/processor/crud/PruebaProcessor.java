package ufps.edu.co.processor.crud;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.PruebaErrorCode;
import ufps.edu.co.maps.specific.PruebaMap;
import ufps.edu.co.records.input.entity.AspiranteInput.ASPIRANTE_FIND;
import ufps.edu.co.records.input.entity.PruebaInput.*;
import ufps.edu.co.records.output.entity.PruebaOutput;
import ufps.edu.co.records.output.entity.PruebaResumenOutput;
import ufps.edu.co.records.output.entity.PruebaSimpleOutput;
import ufps.edu.co.rest.dto.AspiranteDTO;
import ufps.edu.co.rest.dto.EstadoDTO;
import ufps.edu.co.rest.dto.PruebaDTO;
import ufps.edu.co.rest.dto.TipopruebaDTO;
import ufps.edu.co.rest.dto.UbicacionDTO;
import ufps.edu.co.rest.services.AdministrativoService;
import ufps.edu.co.rest.services.AspiranteService;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.rest.services.EstadoService;
import ufps.edu.co.rest.services.PruebaService;
import ufps.edu.co.rest.services.TipopruebaService;
import ufps.edu.co.rest.services.UbicacionService;
import ufps.edu.co.services.*;
import ufps.edu.co.utils.*;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class PruebaProcessor implements
        GlobalUseCase<PRUEBA_CREATE, PRUEBA_UPDATE, PRUEBA_DELETE, PRUEBA_PATCH, PRUEBA_FIND, PruebaOutput> {

    private static final Logger logger = LoggerFactory.getLogger(PruebaProcessor.class);

    @Autowired private PruebaService service;
    @Autowired private AspiranteService aspiranteService;
    @Autowired private CohorteService cohorteService;
    @Autowired private AdministrativoService administrativoService;
    @Autowired private UbicacionService ubicacionService;
    @Autowired private TipopruebaService tipopruebaService;
    @Autowired private EstadoService estadoService;
    @Autowired private PruebaMap map;
    @Autowired private SESService sesService;
    // @Autowired private EmailTemplates emailTemplates;

    @Override
    public PruebaOutput create(PRUEBA_CREATE input) {
        UbicacionDTO ubicacion = ubicacionService.create(UbicacionDTO.builder().direccion(input.ubicacion()).zonaurbana(true).build());
        PruebaDTO dto = map.toDto(input);
        dto.setIdUbicacion(ubicacion.getId());
        PruebaDTO created = service.create(dto);
        PruebaOutput output = map.toOutput(created);
        notifyPruebaCreada(created.getId(), input.idAspirante());
        return output;
    }

    @Override
    public PruebaOutput update(PRUEBA_UPDATE input) {
        try {
            UbicacionDTO ubicacion = ubicacionService.create(UbicacionDTO.builder().direccion(input.ubicacion()).zonaurbana(true).build());
            PruebaDTO dto = map.toDto(input);
            dto.setIdUbicacion(ubicacion.getId());
            return map.toOutput(service.update(input.id(), dto));
        } catch (Exception e) {
            throw new DomainException(PruebaErrorCode.PRUEBA_NOT_FOUND, input.id());
        }
    }

    @Override
    public PruebaOutput patch(PRUEBA_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Prueba");
    }

    @Override
    public PruebaOutput findById(PRUEBA_FIND input) {
        try {
            return map.toOutput(service.findById(input.id()));
        } catch (Exception e) {
            throw new DomainException(PruebaErrorCode.PRUEBA_NOT_FOUND, input.id());
        }
    }

    @Override
    public List<PruebaOutput> findAll() {
        return service.findAll().stream().map(map::toOutput).toList();
    }

    @Override
    public void deleteById(PRUEBA_DELETE input) {
        PruebaDTO prueba;
        try {
            prueba = service.findById(input.id());
        } catch (Exception e) {
            throw new DomainException(PruebaErrorCode.PRUEBA_NOT_FOUND, input.id());
        }
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new DomainException(PruebaErrorCode.PRUEBA_NOT_FOUND, input.id());
        }
        notifyPruebaEliminada(prueba);
    }

    public List<PruebaResumenOutput> findByIdAspirante(ASPIRANTE_FIND input) {
        try {
            return service.findByIdAspirante(input.id()).stream().map(dto -> {
                String estadoNombre = dto.getEstado() != null ? dto.getEstado().getTipo() : null;
                String tipoNombre = dto.getTipoprueba() != null ? dto.getTipoprueba().getTipo() : null;
                String direccion = dto.getUbicacion() != null ? dto.getUbicacion().getDireccion() : null;
                return PruebaResumenOutput.builder()
                        .id(dto.getId())
                        .nombre(dto.getNombre())
                        .descripcion(dto.getDescripcion())
                        .fecha(dto.getFecha())
                        .tiempo(dto.getTiempo())
                        .idEstado(dto.getIdEstado())
                        .estado(estadoNombre)
                        .idTipoprueba(dto.getIdTipoprueba())
                        .tipoprueba(tipoNombre)
                        .ubicacion(direccion)
                        .motivocambio(dto.getMotivocambio())
                        .build();
            }).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding Pruebas by Aspirante ID: " + e.getMessage(), e);
        }
    }

    public PruebaResumenOutput crearPrueba(Integer idAspirante, PRUEBA_CREAR_REQUEST request) {
        try {
            TipopruebaDTO tipoprueba = tipopruebaService.findById(request.idTipoprueba());
            if (tipoprueba == null) {
                throw new RuntimeException("Tipo de prueba no encontrado: " + request.idTipoprueba());
            }
            EstadoDTO estadoInicial = estadoService.findByTipoAndEntidad("PENDIENTE DE CONFIRMACION", "prueba");
            if (estadoInicial == null) {
                throw new RuntimeException("Estado 'PENDIENTE DE CONFIRMACION' no encontrado para entidad 'prueba'");
            }
            AspiranteDTO aspirante = aspiranteService.findById(idAspirante);
            UbicacionDTO ubicacion = ubicacionService.create(
                    UbicacionDTO.builder().direccion(request.ubicacion()).zonaurbana(true).build());
            PruebaDTO dto = PruebaDTO.builder()
                    .nombre(request.nombre())
                    .descripcion(request.descripcion())
                    .fecha(request.fecha())
                    .tiempo(request.tiempo())
                    .idAspirante(idAspirante)
                    .idCohorte(aspirante.getIdCohorte())
                    .idUbicacion(ubicacion.getId())
                    .idEstado(estadoInicial.getId())
                    .idTipoprueba(tipoprueba.getId())
                    .build();
            PruebaDTO created = service.create(dto);
            notifyPruebaCreada(created.getId(), idAspirante);
            return toPruebaResumen(service.findById(created.getId()));
        } catch (Exception e) {
            throw new RuntimeException("Error creating Prueba: " + e.getMessage(), e);
        }
    }

    public PruebaResumenOutput reagendarPrueba(Integer idPrueba, PRUEBA_REAGENDAR_REQUEST request) {
        try {
            TipopruebaDTO tipoprueba = tipopruebaService.findById(request.idTipoprueba());
            if (tipoprueba == null) {
                throw new RuntimeException("Tipo de prueba no encontrado: " + request.idTipoprueba());
            }
            UbicacionDTO ubicacion = ubicacionService.create(
                    UbicacionDTO.builder().direccion(request.ubicacion()).zonaurbana(true).build());
            PruebaDTO updated = service.reschedule(idPrueba, request.fecha(), request.tiempo(),
                    tipoprueba.getId(), ubicacion.getId());
            PruebaResumenOutput result = toPruebaResumen(service.findById(updated.getId()));
            AspiranteDTO aspirante = aspiranteService.findById(updated.getIdAspirante());
            sesService.enviarCorreo(aspirante.getPersona().getCorreo(), EmailTemplates.ASUNTO_REAGENDAR_PRUEBA,
                    EmailTemplates.cuerpoReagendarPrueba(aspirante.getPersona().getNombres(), result.nombre(),
                            request.fecha(), request.tiempo(), tipoprueba.getTipo(), request.ubicacion()));
            return result;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error rescheduling Prueba: " + e.getMessage(), e);
        }
    }

    public PruebaResumenOutput editPrueba(Integer idPrueba, PRUEBA_EDITAR_REQUEST request) {
        try {
            Integer idTipoprueba = null;
            if (request.idTipoprueba() != null) {
                TipopruebaDTO tipoprueba = tipopruebaService.findById(request.idTipoprueba());
                if (tipoprueba == null) {
                    throw new RuntimeException("Tipo de prueba no encontrado: " + request.idTipoprueba());
                }
                idTipoprueba = tipoprueba.getId();
            }
            Integer idUbicacion = null;
            if (request.ubicacion() != null) {
                UbicacionDTO ubicacion = ubicacionService.create(
                        UbicacionDTO.builder().direccion(request.ubicacion()).zonaurbana(true).build());
                idUbicacion = ubicacion.getId();
            }
            PruebaDTO updated = service.edit(idPrueba, request.nombre(), request.descripcion(),
                    request.fecha(), request.tiempo(), idTipoprueba, idUbicacion);
            return toPruebaResumen(updated);
        } catch (Exception e) {
            throw new RuntimeException("Error editing Prueba: " + e.getMessage(), e);
        }
    }

    public PruebaSimpleOutput completarPrueba(Integer idPrueba) {
        try {
            EstadoDTO estadoCompletada = estadoService.findByTipoAndEntidad("COMPLETADA", "prueba");
            if (estadoCompletada == null) {
                throw new RuntimeException("Estado 'COMPLETADA' no encontrado para entidad 'prueba'");
            }
            PruebaDTO updated = service.changeEstado(idPrueba, estadoCompletada.getId(), "CONFIRMADA");
            PruebaDTO full = service.findById(updated.getId());
            notifyPruebaCompletada(full);
            return toPruebaSimple(full);
        } catch (Exception e) {
            throw new RuntimeException("Error completing Prueba: " + e.getMessage(), e);
        }
    }

    public PruebaSimpleOutput confirmarPrueba(Integer idPrueba) {
        try {
            EstadoDTO estadoConfirmada = estadoService.findByTipoAndEntidad("CONFIRMADA", "prueba");
            if (estadoConfirmada == null) {
                throw new RuntimeException("Estado 'CONFIRMADA' no encontrado para entidad 'prueba'");
            }
            PruebaDTO updated = service.changeEstado(idPrueba, estadoConfirmada.getId(), "PENDIENTE DE CONFIRMACION");
            PruebaDTO full = service.findById(updated.getId());
            notifyConfirmacionPruebaAlDirector(full);
            return toPruebaSimple(full);
        } catch (Exception e) {
            throw new RuntimeException("Error confirming Prueba: " + e.getMessage(), e);
        }
    }

    public PruebaSimpleOutput solicitarCambioPrueba(Integer idPrueba, String motivocambio) {
        try {
            EstadoDTO estadoSolicitud = estadoService.findByTipoAndEntidad("SOLICITUD DE CAMBIO", "prueba");
            if (estadoSolicitud == null) {
                throw new RuntimeException("Estado 'SOLICITUD DE CAMBIO' no encontrado para entidad 'prueba'");
            }
            PruebaDTO updated = service.changeEstadoWithMotivo(idPrueba, estadoSolicitud.getId(), "PENDIENTE DE CONFIRMACION", motivocambio);
            PruebaDTO full = service.findById(updated.getId());
            notifyCambioPruebaAlDirector(full, motivocambio);
            return toPruebaSimple(full);
        } catch (Exception e) {
            throw new RuntimeException("Error requesting change for Prueba: " + e.getMessage(), e);
        }
    }

    public PruebaSimpleOutput cancelarPrueba(Integer idPrueba, String motivocambio, String canceladoPor) {
        try {
            EstadoDTO estadoCancelada = estadoService.findByTipoAndEntidad("CANCELADA", "prueba");
            if (estadoCancelada == null) {
                throw new RuntimeException("Estado 'CANCELADA' no encontrado para entidad 'prueba'");
            }
            PruebaDTO updated = service.changeEstadoWithMotivo(idPrueba, estadoCancelada.getId(), "CONFIRMADA", motivocambio);
            PruebaDTO full = service.findById(updated.getId());
            notifyCancelacionPrueba(full, canceladoPor, motivocambio);
            return toPruebaSimple(full);
        } catch (Exception e) {
            throw new RuntimeException("Error cancelling Prueba: " + e.getMessage(), e);
        }
    }

    private PruebaResumenOutput toPruebaResumen(PruebaDTO dto) {
        String estadoNombre = dto.getEstado() != null ? dto.getEstado().getTipo() : null;
        String tipoNombre = dto.getTipoprueba() != null ? dto.getTipoprueba().getTipo() : null;
        String direccion = dto.getUbicacion() != null ? dto.getUbicacion().getDireccion() : null;
        return PruebaResumenOutput.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .fecha(dto.getFecha())
                .tiempo(dto.getTiempo())
                .idEstado(dto.getIdEstado())
                .estado(estadoNombre)
                .idTipoprueba(dto.getIdTipoprueba())
                .tipoprueba(tipoNombre)
                .ubicacion(direccion)
                .motivocambio(dto.getMotivocambio())
                .build();
    }

    private PruebaSimpleOutput toPruebaSimple(PruebaDTO dto) {
        String estadoNombre = dto.getEstado() != null ? dto.getEstado().getTipo() : null;
        return PruebaSimpleOutput.builder()
                .idPrueba(dto.getId())
                .idAspirante(dto.getIdAspirante())
                .nombre(dto.getNombre())
                .estado(estadoNombre)
                .motivocambio(dto.getMotivocambio())
                .build();
    }

    private void notifyPruebaCreada(Integer idPrueba, Integer idAspirante) {
        try {
            PruebaDTO prueba = service.findById(idPrueba);
            AspiranteDTO aspirante = aspiranteService.findById(idAspirante);
            if (aspirante == null || aspirante.getPersona() == null) return;

            String nombre = aspirante.getPersona().getNombres();
            String correoAspirante = aspirante.getPersona().getCorreo();
            String nombrePrueba = prueba.getNombre() != null ? prueba.getNombre() : "No definido";
            // String descripcion = prueba.getDescripcion() != null ? prueba.getDescripcion() : "Sin descripción";
            String lugar = prueba.getUbicacion() != null ? prueba.getUbicacion().getDireccion() : "No definido";

            String tipoPrueba = prueba.getTipoprueba() != null ? prueba.getTipoprueba().getTipo() : "No definido";
            sesService.enviarCorreo(correoAspirante, EmailTemplates.ASUNTO_AGENDAR_PRUEBA,
                    EmailTemplates.cuerpoAgendarPrueba(nombre, nombrePrueba, prueba.getFecha(), prueba.getTiempo(), tipoPrueba, lugar));
        } catch (Exception e) {
            logger.warn("No se pudo notificar prueba creada al aspirante {}: {}", idAspirante, e.getMessage());
        }
    }

    private void notifyPruebaEliminada(PruebaDTO prueba) {
        notifyCancelacionPrueba(prueba, "DIRECTOR", null);
    }

    private void notifyConfirmacionPruebaAlDirector(PruebaDTO prueba) {
        try {
            AspiranteDTO aspirante = aspiranteService.findById(prueba.getIdAspirante());
            if (aspirante == null || aspirante.getPersona() == null) return;
            ufps.edu.co.rest.dto.AdministrativoDTO director = findDirectorForAspirante(aspirante);
            if (director == null || director.getPersona() == null) return;
            String nombrePrueba = prueba.getNombre() != null ? prueba.getNombre() : "No definido";
            String lugar = prueba.getUbicacion() != null ? prueba.getUbicacion().getDireccion() : "No definido";
            sesService.enviarCorreo(director.getPersona().getCorreo(), EmailTemplates.ASUNTO_ASPIRANTE_CONFIRMA,
                    EmailTemplates.cuerpoConfirmacionAlDirector(
                            aspirante.getPersona().getNombres(), "prueba", nombrePrueba,
                            prueba.getFecha(), prueba.getTiempo(), lugar));
        } catch (Exception e) {
            logger.warn("No se pudo notificar confirmación de prueba al director: {}", e.getMessage());
        }
    }

    private void notifyPruebaCompletada(PruebaDTO prueba) {
        try {
            AspiranteDTO aspirante = aspiranteService.findById(prueba.getIdAspirante());
            if (aspirante == null || aspirante.getPersona() == null) return;
            String nombrePrueba = prueba.getNombre() != null ? prueba.getNombre() : "No definido";
            String lugar = prueba.getUbicacion() != null ? prueba.getUbicacion().getDireccion() : "No definido";
            sesService.enviarCorreo(aspirante.getPersona().getCorreo(), EmailTemplates.ASUNTO_ACTIVIDAD_COMPLETADA,
                    EmailTemplates.cuerpoActividadCompletada(
                            aspirante.getPersona().getNombres(), "prueba", nombrePrueba,
                            prueba.getFecha(), prueba.getTiempo(), lugar));
        } catch (Exception e) {
            logger.warn("No se pudo notificar prueba completada al aspirante: {}", e.getMessage());
        }
    }

    private void notifyCambioPruebaAlDirector(PruebaDTO prueba, String motivo) {
        try {
            AspiranteDTO aspirante = aspiranteService.findById(prueba.getIdAspirante());
            if (aspirante == null || aspirante.getPersona() == null) return;
            ufps.edu.co.rest.dto.AdministrativoDTO director = findDirectorForAspirante(aspirante);
            if (director == null || director.getPersona() == null) return;
            String nombrePrueba = prueba.getNombre() != null ? prueba.getNombre() : "No definido";
            sesService.enviarCorreo(director.getPersona().getCorreo(), EmailTemplates.ASUNTO_SOLICITUD_CAMBIO,
                    EmailTemplates.cuerpoCambioSolicitadoAlDirector(
                            aspirante.getPersona().getNombres(), "prueba", nombrePrueba,
                            prueba.getFecha(), prueba.getTiempo(),
                            motivo != null ? motivo : "Sin motivo especificado"));
        } catch (Exception e) {
            logger.warn("No se pudo notificar solicitud de cambio de prueba al director: {}", e.getMessage());
        }
    }

    private void notifyCancelacionPrueba(PruebaDTO prueba, String canceladoPor, String motivo) {
        try {
            AspiranteDTO aspirante = aspiranteService.findById(prueba.getIdAspirante());
            if (aspirante == null || aspirante.getPersona() == null) return;
            String nombrePrueba = prueba.getNombre() != null ? prueba.getNombre() : "No definido";
            // String lugar = prueba.getUbicacion() != null ? prueba.getUbicacion().getDireccion() : "No definido";
            String motivoTexto = motivo != null ? motivo : "Sin motivo especificado";

            if ("ASPIRANTE".equalsIgnoreCase(canceladoPor)) {
                ufps.edu.co.rest.dto.AdministrativoDTO director = findDirectorForAspirante(aspirante);
                if (director == null || director.getPersona() == null) return;
                sesService.enviarCorreo(director.getPersona().getCorreo(), EmailTemplates.ASUNTO_CANCELACION_POR_ASPIRANTE,
                        EmailTemplates.cuerpoCancelacionPorAspirante(
                                aspirante.getPersona().getNombres(), "prueba", nombrePrueba,
                                prueba.getFecha(), prueba.getTiempo(), motivoTexto));
            } else {
                sesService.enviarCorreo(aspirante.getPersona().getCorreo(), EmailTemplates.ASUNTO_CANCELACION_POR_DIRECTOR,
                        EmailTemplates.cuerpoCancelacionPorDirector(
                                aspirante.getPersona().getNombres(), "prueba", nombrePrueba,
                                prueba.getFecha(), prueba.getTiempo(), motivoTexto));
            }
        } catch (Exception e) {
            logger.warn("No se pudo notificar cancelación de prueba {}: {}", prueba.getId(), e.getMessage());
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
