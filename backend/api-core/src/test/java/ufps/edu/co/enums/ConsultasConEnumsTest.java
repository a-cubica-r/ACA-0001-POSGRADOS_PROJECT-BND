package ufps.edu.co.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ufps.edu.co.persistence.repositories.AspiranteRepository;
import ufps.edu.co.persistence.repositories.CohorteRepository;
import ufps.edu.co.persistence.repositories.EstadoRepository;
import ufps.edu.co.persistence.repositories.PagoreciboinscripcionRepository;
import ufps.edu.co.persistence.repositories.PagorecibomatriculaRepository;
import ufps.edu.co.rest.services.AspiranteService;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.rest.services.EstadoService;
import ufps.edu.co.rest.services.PagoreciboinscripcionService;
import ufps.edu.co.rest.services.PagorecibomatriculaService;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * SC-05 - Verifica que, después de reemplazar los literales por enums, los
 * servicios envían a los repositorios exactamente los mismos textos que antes.
 */
@ExtendWith(MockitoExtension.class)
class ConsultasConEnumsTest {

    @Mock private EstadoRepository estadoRepository;
    @Mock private CohorteRepository cohorteRepository;
    @Mock private AspiranteRepository aspiranteRepository;
    @Mock private PagoreciboinscripcionRepository pagoreciboinscripcionRepository;
    @Mock private PagorecibomatriculaRepository pagorecibomatriculaRepository;

    @InjectMocks private EstadoService estadoService;
    @InjectMocks private CohorteService cohorteService;
    @InjectMocks private AspiranteService aspiranteService;
    @InjectMocks private PagoreciboinscripcionService pagoreciboinscripcionService;
    @InjectMocks private PagorecibomatriculaService pagorecibomatriculaService;

    // ─── EstadoService.findByTipoAndEntidad(EstadoCatalogo) ───────────────────

    @Test
    void estadoService_conEnum_consultaConTipoYEntidadOriginales() {
        estadoService.findByTipoAndEntidad(EstadoAspirante.POR_LEGALIZAR);
        estadoService.findByTipoAndEntidad(EstadoCohorte.CERRADA);
        estadoService.findByTipoAndEntidad(EstadoPagoInscripcion.EN_CURSO);
        estadoService.findByTipoAndEntidad(EstadoPagoMatricula.RECHAZADO);
        estadoService.findByTipoAndEntidad(EstadoEntrevista.PENDIENTE_DE_CONFIRMACION);
        estadoService.findByTipoAndEntidad(EstadoPrueba.SOLICITUD_DE_CAMBIO);
        estadoService.findByTipoAndEntidad(EstadoPago.REALIZADO);
        estadoService.findByTipoAndEntidad(EstadoAdministrativo.INACTIVO);

        verify(estadoRepository).findByTipoIgnoreCaseAndEntidadIgnoreCase("POR LEGALIZAR", "aspirante");
        verify(estadoRepository).findByTipoIgnoreCaseAndEntidadIgnoreCase("CERRADA", "cohorte");
        verify(estadoRepository).findByTipoIgnoreCaseAndEntidadIgnoreCase("EN CURSO", "pagoinscripcion");
        verify(estadoRepository).findByTipoIgnoreCaseAndEntidadIgnoreCase("RECHAZADO", "pagomatricula");
        verify(estadoRepository).findByTipoIgnoreCaseAndEntidadIgnoreCase("PENDIENTE DE CONFIRMACION", "entrevista");
        verify(estadoRepository).findByTipoIgnoreCaseAndEntidadIgnoreCase("SOLICITUD DE CAMBIO", "prueba");
        verify(estadoRepository).findByTipoIgnoreCaseAndEntidadIgnoreCase("REALIZADO", "pago");
        verify(estadoRepository).findByTipoIgnoreCaseAndEntidadIgnoreCase("INACTIVO", "administrativo");
    }

    // ─── CohorteRepository: consultas que antes tenían literales en @Query ─────

    @Test
    void cohorteService_findActivas_envia_cohorte_ABIERTA() {
        cohorteService.findActivasByIdPrograma(5);
        verify(cohorteRepository).findActivasByIdPrograma(5, "cohorte", "ABIERTA");
    }

    @Test
    void cohorteService_countEnProceso_envia_aspirante_EN_PROCESO() {
        cohorteService.countAspirantesEnProcesoByCohorteId(7);
        verify(cohorteRepository).countAspirantesEnProcesoByCohorteId(7, "aspirante", "EN PROCESO");
    }

    @Test
    void cohorteService_countEnProcesoEnCohorteAbierta_enviaLosCuatroValoresOriginales() {
        cohorteService.countAspirantesEnProcesoEnCohorteAbierta(9);
        verify(cohorteRepository).countAspirantesEnProcesoEnCohorteAbierta(9, "aspirante", "EN PROCESO",
                "cohorte", "ABIERTA");
    }

    // ─── AspiranteService: listas de estados ──────────────────────────────────

    @Test
    void aspiranteService_documentosAprobados_envia_APROBADO() {
        aspiranteService.findWithDocuments();
        aspiranteService.findCalificados();
        aspiranteService.findPorCalificar();
        verify(aspiranteRepository).findValidados("APROBADO");
        verify(aspiranteRepository).findCalificados("APROBADO");
        verify(aspiranteRepository).findPorCalificar("APROBADO");
    }

    @Test
    void aspiranteService_listasDeEstados_identicasALasOriginales() {
        aspiranteService.findValidadosByCohorte(1);
        aspiranteService.findAdmitidosByCohorte(1);
        aspiranteService.findAValidarByCohorte(1);
        verify(aspiranteRepository).findByIdCohorteAndEstadoTipoIn(1,
                List.of("VALIDADO_POR_CALIFICAR", "VALIDADO_EN_PROGRESO", "VALIDADO_CALIFICADO"));
        verify(aspiranteRepository).findByIdCohorteAndEstadoTipoIn(1, List.of("ADMITIDO"));
        verify(aspiranteRepository).findByIdCohorteAndEstadoTipoIn(1, List.of("PAZ Y SALVO", "VALIDADO_POR_CALIFICAR"));

        aspiranteService.countValidados();
        aspiranteService.countPorCalificar();
        aspiranteService.countCalificados();
        verify(aspiranteRepository).countByEstadoTipoIn(
                List.of("VALIDADO_POR_CALIFICAR", "VALIDADO_EN_PROGRESO", "VALIDADO_CALIFICADO"));
        verify(aspiranteRepository).countByEstadoTipoIn(List.of("VALIDADO_POR_CALIFICAR", "VALIDADO_EN_PROGRESO"));
        verify(aspiranteRepository).countByEstadoTipoIn(List.of("VALIDADO_CALIFICADO"));

        aspiranteService.countValidadosByCohorte(2);
        aspiranteService.countPorCalificarByCohorte(3);
        aspiranteService.countCalificadosByCohorte(4);
        aspiranteService.countPazYSalvoByCohorte(5);
        aspiranteService.countAdmitidosByCohorte(6);
        verify(aspiranteRepository).countByIdCohorteAndEstadoTipoIn(2,
                List.of("VALIDADO_POR_CALIFICAR", "VALIDADO_EN_PROGRESO"));
        verify(aspiranteRepository).countByIdCohorteAndEstadoTipoIn(3,
                List.of("VALIDADO_POR_CALIFICAR", "VALIDADO_EN_PROGRESO"));
        verify(aspiranteRepository).countByIdCohorteAndEstadoTipoIn(4, List.of("VALIDADO_CALIFICADO"));
        verify(aspiranteRepository).countByIdCohorteAndEstadoTipoIn(5, List.of("PAZ Y SALVO"));
        verify(aspiranteRepository).countByIdCohorteAndEstadoTipoIn(6,
                List.of("POR LEGALIZAR", "LEGALIZADO", "ADMITIDO"));
    }

    // ─── Recibos de pago ───────────────────────────────────────────────────────

    @Test
    void pagoreciboinscripcion_envia_EN_CURSO_y_RECHAZADO_de_pagoinscripcion() {
        pagoreciboinscripcionService.findCurrentByIdAspirante(11);
        pagoreciboinscripcionService.findLastRejectedByIdAspirante(11);
        pagoreciboinscripcionService.existsCurrentByIdAspirante(11);
        verify(pagoreciboinscripcionRepository)
                .findFirstByPago_IdAspiranteAndEstado_TipoIgnoreCaseAndEstado_EntidadIgnoreCaseOrderByIdDesc(
                        11, "EN CURSO", "pagoinscripcion");
        verify(pagoreciboinscripcionRepository)
                .findFirstByPago_IdAspiranteAndEstado_TipoIgnoreCaseAndEstado_EntidadIgnoreCaseOrderByIdDesc(
                        11, "RECHAZADO", "pagoinscripcion");
        verify(pagoreciboinscripcionRepository)
                .existsByPago_IdAspiranteAndEstado_TipoIgnoreCaseAndEstado_EntidadIgnoreCase(
                        11, "EN CURSO", "pagoinscripcion");
    }

    @Test
    void pagorecibomatricula_envia_EN_CURSO_y_RECHAZADO_de_pagomatricula() {
        pagorecibomatriculaService.findCurrentByIdAspirante(12);
        pagorecibomatriculaService.findLastRejectedByIdAspirante(12);
        pagorecibomatriculaService.existsCurrentByIdAspirante(12);
        verify(pagorecibomatriculaRepository)
                .findFirstByPago_IdAspiranteAndEstado_TipoIgnoreCaseAndEstado_EntidadIgnoreCaseOrderByIdDesc(
                        12, "EN CURSO", "pagomatricula");
        verify(pagorecibomatriculaRepository)
                .findFirstByPago_IdAspiranteAndEstado_TipoIgnoreCaseAndEstado_EntidadIgnoreCaseOrderByIdDesc(
                        12, "RECHAZADO", "pagomatricula");
        verify(pagorecibomatriculaRepository)
                .existsByPago_IdAspiranteAndEstado_TipoIgnoreCaseAndEstado_EntidadIgnoreCase(
                        12, "EN CURSO", "pagomatricula");
    }
}
