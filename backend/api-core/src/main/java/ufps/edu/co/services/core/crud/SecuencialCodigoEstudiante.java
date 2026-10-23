package ufps.edu.co.services.core.crud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Regla única del secuencial que se usa en el código de estudiante.
 *
 * <p>El código de estudiante es {@code programas.codigo} + un secuencial de 4
 * dígitos. Este es el cálculo que decide cuál es el secuencial siguiente a
 * partir del último código del programa, y lo usan tanto la generación (al
 * aprobar un aspirante) como la edición manual del consecutivo, para que no se
 * repitan códigos.</p>
 *
 * <p>El secuencial no da la vuelta: después de 9999 el programa queda agotado y
 * el siguiente vale {@link #AGOTADO}.</p>
 */
public final class SecuencialCodigoEstudiante {

    private static final Logger log = LoggerFactory.getLogger(SecuencialCodigoEstudiante.class);

    /** Cantidad de dígitos del secuencial dentro del código de estudiante. */
    private static final int LONGITUD = 4;

    /** Valor que indica que el programa ya no tiene secuenciales disponibles. */
    public static final int AGOTADO = 10000;

    private SecuencialCodigoEstudiante() {
    }

    /**
     * Secuencial que le corresponde al siguiente estudiante del programa.
     *
     * @param ultimoCodigo código completo del último estudiante del programa, o
     *                     {@code null} si el programa no tiene estudiantes
     * @return el secuencial siguiente; {@code 0} si no hay último código o si no
     *         se puede leer, y {@link #AGOTADO} si el último llegó a 9999
     */
    public static int siguiente(String ultimoCodigo) {
        if (ultimoCodigo == null || ultimoCodigo.length() < LONGITUD) {
            return 0;
        }
        try {
            int ultimo = Integer.parseInt(ultimoCodigo.substring(ultimoCodigo.length() - LONGITUD));
            return ultimo + 1;
        } catch (NumberFormatException e) {
            log.warn("No se pudo parsear el secuencial del código '{}', comenzando en 0000", ultimoCodigo);
            return 0;
        }
    }
}
