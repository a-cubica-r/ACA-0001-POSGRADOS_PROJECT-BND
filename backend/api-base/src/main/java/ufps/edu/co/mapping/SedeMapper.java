package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.SedeEntity;
import ufps.edu.co.rest.dto.SedeDTO;

/**
 * Mapper MapStruct SedeEntity &lt;-&gt; SedeDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class, uses = { UbicacionMapper.class })
public interface SedeMapper extends EntityMapper<SedeEntity, SedeDTO> {

    @Override
    @Mapping(target = "programaList", ignore = true)
    SedeDTO toDto(SedeEntity entity);

    @Override
    @Mapping(target = "programaList", ignore = true)
    SedeEntity toEntity(SedeDTO dto);
}
