package ufps.edu.co.records.output.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import ufps.edu.co.records.OutputResponse;

@Builder
public record CohorteDetalleOutput(
        Integer id,
        String nombre,
        boolean activa,
        long inscritos,
        long admitidos,
        Integer cupos,
        Integer idSemestre,
        String nombreSemestre,
        Integer idModalidad,
        String nombreModalidad,
        LocalDate fechaLimiteDocumentos,
        LocalDate fechaLimitePago,
        // Inicio del SEMESTRE, no de un plazo. Se conserva por compatibilidad.
        LocalDate fechaInicio,
        LocalDate fechaInicioDocumentacion,
        LocalDate fechaFinDocumentacion,
        LocalDate fechaInicioInscripcion,
        LocalDate fechaFinInscripcion,
        LocalDate fechaInicioPago,
        LocalDate fechaFinPago,
        List<CriterioInfo> criterios,
        DocumentosAsignadosInfo documentosAsignados,
        List<AspiranteInfo> inscritosData,
        List<AspiranteInfo> admitidosData
) implements OutputResponse {

    @Builder
    public record CriterioInfo(
            Integer id,
            Integer idCriterioevaluacion,
            String nombre,
            BigDecimal peso) {}

    @Builder
    public record AspiranteInfo(Integer id, String nombre, String cedula, String correo) {}

    @Builder
    public record DocumentoAsignadoInfo(Integer id, Integer idDocrequisito, Integer idCohorte, String nombre) {}

    @Builder
    public record DocumentosAsignadosInfo(
            List<DocumentoAsignadoInfo> documentosConsejo,
            List<DocumentoAsignadoInfo> documentosPrograma) {}
}
