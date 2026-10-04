package ufps.edu.co.mapping;

/**
 * Contrato comun de los mappers Entity &lt;-&gt; DTO generados con MapStruct.
 * {@code GenericService} lo inyecta resolviendo los tipos genericos de cada servicio.
 *
 * @param <ENTITY> entidad JPA
 * @param <DTO>    DTO REST
 */
public interface EntityMapper<ENTITY, DTO> {

    DTO toDto(ENTITY entity);

    ENTITY toEntity(DTO dto);
}
