package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.PoblacionindigenaEntity;
import ufps.edu.co.rest.dto.PoblacionindigenaDTO;

/**
 * Mapper MapStruct PoblacionindigenaEntity &lt;-&gt; PoblacionindigenaDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface PoblacionindigenaMapper extends EntityMapper<PoblacionindigenaEntity, PoblacionindigenaDTO> {

    @Override
    @Mapping(target = "personaList", ignore = true)
    PoblacionindigenaDTO toDto(PoblacionindigenaEntity entity);

    @Override
    @Mapping(target = "personaList", ignore = true)
    PoblacionindigenaEntity toEntity(PoblacionindigenaDTO dto);
}
