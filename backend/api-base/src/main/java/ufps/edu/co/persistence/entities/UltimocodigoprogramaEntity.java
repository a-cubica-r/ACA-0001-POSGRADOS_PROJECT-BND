package ufps.edu.co.persistence.entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Consecutivo del código de estudiante de cada programa.
 *
 * <p>{@code codigo} es el <strong>próximo</strong> secuencial a asignar
 * (arranca en 0): el estudiante siguiente recibe
 * {@code programas.codigo} + {@code codigo} con 4 dígitos, y el consecutivo
 * avanza en 1. Un valor de 10000 significa que el programa agotó sus códigos.</p>
 *
 * <p>La tabla la crea {@code db/scripts/2026-10-D00-ultimocodigoprograma.sql};
 * Hibernate no la gestiona ({@code ddl-auto = none}).</p>
 */
@Entity
@Table(name = "ultimocodigoprograma")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UltimocodigoprogramaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "id_programa", nullable = false)
    private Integer idPrograma;

    @Column(name = "codigo", nullable = false)
    private Integer codigo;

    @ManyToOne
    @JoinColumn(name = "id_programa", referencedColumnName = "id", insertable = false, updatable = false)
    private ProgramaEntity programa;
}
