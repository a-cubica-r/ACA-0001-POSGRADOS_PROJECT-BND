package ufps.edu.co.persistence.repositories;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ufps.edu.co.persistence.entities.UltimocodigoprogramaEntity;

@Repository
public interface UltimocodigoprogramaRepository extends JpaRepository<UltimocodigoprogramaEntity, Integer> {

    Optional<UltimocodigoprogramaEntity> findByIdPrograma(Integer idPrograma);

    boolean existsByIdPrograma(Integer idPrograma);

    /**
     * Una fila por programa, aunque todavía no tenga consecutivo (LEFT JOIN),
     * ordenada por nombre del programa.
     *
     * <p>Columnas: {@code [0]} id de la fila del consecutivo ({@code null} si el
     * programa no tiene), {@code [1]} id del programa, {@code [2]} consecutivo
     * ({@code null} si no tiene), {@code [3]} nombre del programa.</p>
     */
    @Query("""
            SELECT u.id, p.id, u.codigo, p.nombre
              FROM ProgramaEntity p
              LEFT JOIN UltimocodigoprogramaEntity u ON u.idPrograma = p.id
             ORDER BY p.nombre
            """)
    List<Object[]> findAllConNombrePrograma();

    /**
     * Bloquea la fila del programa para reservar el siguiente secuencial. Solo
     * debe usarse dentro de una transacción de escritura.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UltimocodigoprogramaEntity u WHERE u.idPrograma = :idPrograma")
    Optional<UltimocodigoprogramaEntity> findByIdProgramaForUpdate(@Param("idPrograma") Integer idPrograma);
}
