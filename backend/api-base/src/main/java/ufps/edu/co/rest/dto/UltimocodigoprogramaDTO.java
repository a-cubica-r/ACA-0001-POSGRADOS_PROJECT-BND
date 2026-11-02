package ufps.edu.co.rest.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de "Ultimocodigoprograma": el consecutivo del código de estudiante.
 *
 * <p>{@code codigo} es el próximo secuencial a asignar. {@code nombrePrograma}
 * solo viaja de la entidad al DTO (viene de {@code programa.nombre}).</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UltimocodigoprogramaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer idPrograma;

    private Integer codigo;

    private String nombrePrograma;
}
