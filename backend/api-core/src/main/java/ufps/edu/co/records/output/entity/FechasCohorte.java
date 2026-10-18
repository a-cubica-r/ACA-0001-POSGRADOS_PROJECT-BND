package ufps.edu.co.records.output.entity;

import java.time.LocalDate;

import ufps.edu.co.rest.dto.CohorteDTO;
import ufps.edu.co.rest.dto.PlazoDTO;

/**
 * Las seis fechas de una cohorte, tomadas de sus tres plazos:
 * documentación ({@code plazo}), inscripción ({@code plazo2}) y pago ({@code plazo3}).
 */
public record FechasCohorte(
        LocalDate fechaInicioDocumentacion,
        LocalDate fechaFinDocumentacion,
        LocalDate fechaInicioInscripcion,
        LocalDate fechaFinInscripcion,
        LocalDate fechaInicioPago,
        LocalDate fechaFinPago) {

    /**
     * Construye las seis fechas desde los plazos de la cohorte. Es tolerante a
     * cohortes sin plazo o sin alguno de los tres plazos: en esos casos devuelve
     * {@code null} en las fechas que no se pueden resolver.
     */
    public static FechasCohorte desde(CohorteDTO cohorte) {
        if (cohorte == null) {
            return new FechasCohorte(null, null, null, null, null, null);
        }
        return new FechasCohorte(
                inicio(cohorte.getPlazo()),
                fin(cohorte.getPlazo()),
                inicio(cohorte.getPlazo2()),
                fin(cohorte.getPlazo2()),
                inicio(cohorte.getPlazo3()),
                fin(cohorte.getPlazo3()));
    }

    private static LocalDate inicio(PlazoDTO plazo) {
        return plazo != null ? plazo.getFechainicio() : null;
    }

    private static LocalDate fin(PlazoDTO plazo) {
        return plazo != null ? plazo.getFechafin() : null;
    }
}
