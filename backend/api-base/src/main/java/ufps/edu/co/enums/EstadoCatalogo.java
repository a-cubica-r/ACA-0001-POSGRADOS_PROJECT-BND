package ufps.edu.co.enums;

/**
 * Valor de la tabla {@code estado}: cada elemento conoce su {@code tipo}
 * ({@link #getValor()}) y la {@code entidad} a la que pertenece.
 */
public interface EstadoCatalogo extends ValorCatalogo {

    EntidadEstado getEntidad();
}
