package ufps.edu.co.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Contrato común de los enums que representan un valor predefinido del sistema
 * (estado, tipo o categoría). {@link #getValor()} devuelve el texto exacto que se
 * guarda en la base de datos o que se expone en la API; nunca se debe usar
 * {@code name()} para consultar o comparar.
 */
public interface ValorCatalogo {

    String getValor();

    /**
     * Compara el valor del enum con un texto sin distinguir mayúsculas,
     * igual que el {@code equalsIgnoreCase} que se usaba antes.
     */
    default boolean matches(String valor) {
        return getValor().equalsIgnoreCase(valor);
    }

    /**
     * Busca el elemento del enum cuyo valor coincide (sin distinguir mayúsculas)
     * con el texto recibido.
     */
    static <E extends Enum<E> & ValorCatalogo> Optional<E> fromValor(Class<E> tipo, String valor) {
        if (valor == null) {
            return Optional.empty();
        }
        return Arrays.stream(tipo.getEnumConstants())
                .filter(item -> item.matches(valor))
                .findFirst();
    }

    /**
     * Lista con los valores (texto de la base de datos) de los elementos recibidos,
     * en el mismo orden.
     */
    static List<String> valores(ValorCatalogo... items) {
        return Arrays.stream(items).map(ValorCatalogo::getValor).toList();
    }
}
