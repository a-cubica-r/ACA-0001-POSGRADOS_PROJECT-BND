package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import ufps.edu.co.persistence.entities.CalificacioncriterioEntity;
import ufps.edu.co.rest.dto.CalificacioncriterioDTO;

/**
 * Mapper MapStruct CalificacioncriterioEntity &lt;-&gt; CalificacioncriterioDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class, uses = { AspiranteMapper.class, CriteriocohorteMapper.class })
public interface CalificacioncriterioMapper extends EntityMapper<CalificacioncriterioEntity, CalificacioncriterioDTO> {

    @Override
    CalificacioncriterioDTO toDto(CalificacioncriterioEntity entity);

    @Override
    CalificacioncriterioEntity toEntity(CalificacioncriterioDTO dto);
}
