package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.CapacidadexepcionalEntity;
import ufps.edu.co.rest.dto.CapacidadexepcionalDTO;

/**
 * Mapper MapStruct CapacidadexepcionalEntity &lt;-&gt; CapacidadexepcionalDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface CapacidadexepcionalMapper extends EntityMapper<CapacidadexepcionalEntity, CapacidadexepcionalDTO> {

    @Override
    @Mapping(target = "personaList", ignore = true)
    CapacidadexepcionalDTO toDto(CapacidadexepcionalEntity entity);

    @Override
    @Mapping(target = "personaList", ignore = true)
    CapacidadexepcionalEntity toEntity(CapacidadexepcionalDTO dto);
}
