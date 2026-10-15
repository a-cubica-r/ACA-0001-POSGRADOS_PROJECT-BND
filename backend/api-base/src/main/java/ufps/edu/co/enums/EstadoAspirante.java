package ufps.edu.co.enums;

import lombok.Getter;

/**
 * Estados de la entidad {@code aspirante} en la tabla {@code estado}.
 */
@Getter
public enum EstadoAspirante implements EstadoCatalogo {
    NO_CONFIRMADO("NO CONFIRMADO"),
    APROBADO("APROBADO"),
    MATRICULADO("MATRICULADO"),
    EN_PROCESO("EN PROCESO"),
    SUSPENDIDO("SUSPENDIDO"),
    ARCHIVADO("ARCHIVADO"),
    CANCELADO("CANCELADO"),
    INSCRITO("INSCRITO"),
    VALIDADO_POR_CALIFICAR("VALIDADO_POR_CALIFICAR"),
    VALIDADO_EN_PROGRESO("VALIDADO_EN_PROGRESO"),
    VALIDADO_CALIFICADO("VALIDADO_CALIFICADO"),
    ADMITIDO("ADMITIDO"),
    PAZ_Y_SALVO("PAZ Y SALVO"),
    POR_LEGALIZAR("POR LEGALIZAR"),
    LEGALIZADO("LEGALIZADO");

    private final String valor;

    EstadoAspirante(String valor) {
        this.valor = valor;
    }

    @Override
    public EntidadEstado getEntidad() {
        return EntidadEstado.ASPIRANTE;
    }
}
