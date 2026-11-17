package ufps.edu.co.services.core.crud;

import java.time.LocalDate;

/**
 * Regla única del periodo académico a partir de la fecha de inicio.
 *
 * <p>Decisión de Adolfo (2026-11-16): el número de periodo no se guarda en
 * {@code semestre.numero} (está en NULL); se deriva de la fecha. Enero a junio
 * es periodo 1; julio a diciembre, periodo 2. El año es el de la fecha de
 * inicio. Un semestre intersemestral que empiece en junio queda en el periodo
 * 1.</p>
 *
 * @param anio    año de la fecha de inicio
 * @param periodo 1 (enero-junio) o 2 (julio-diciembre)
 */
public record SemestrePeriodo(int anio, int periodo) {

    public static SemestrePeriodo de(LocalDate fechaInicio) {
        if (fechaInicio == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria");
        }
        int periodo = fechaInicio.getMonthValue() <= 6 ? 1 : 2;
        return new SemestrePeriodo(fechaInicio.getYear(), periodo);
    }
}
