package ufps.edu.co.records.output.cases;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;

/**
 * SC-03 - Respuesta de GET /integracion/v1/estudiantes/{cedula}, que consume
 * el sistema de Trámites. Los nombres siguen el contrato acordado con ese
 * equipo; no cambiarlos sin publicar una /v2.
 */
@Builder
public record SituacionAcademicaOutput(
    String cedula,
    String codigo,
    String nombres,
    String apellidos,
    String email,
    String estado,
    Programa programa,
    Admision admision,
    Matricula matricula,
    Creditos creditos
) {

    @Builder
    public record Programa(Integer id, String codigo, String nombre, Integer creditosRequeridos) {}

    @Builder
    public record Admision(Integer cohorteId, String cohorte, String semestre,
                           LocalDate fechaAdmision, LocalDate fechaIngreso) {}

    @Builder
    public record Matricula(String estado, BigDecimal valor, LocalDate fechaVencimiento) {}

    /** {@code aprobados} va en null: Matrícula no registra notas. */
    @Builder
    public record Creditos(Integer requeridos, Integer aprobados) {}
}
