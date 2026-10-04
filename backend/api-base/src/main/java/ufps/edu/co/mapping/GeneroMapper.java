package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.GeneroEntity;
import ufps.edu.co.rest.dto.GeneroDTO;

/**
 * Mapper MapStruct GeneroEntity &lt;-&gt; GeneroDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface GeneroMapper extends EntityMapper<GeneroEntity, GeneroDTO> {

    @Override
    @Mapping(target = "personaList", ignore = true)
    GeneroDTO toDto(GeneroEntity entity);

    @Override
    @Mapping(target = "personaList", ignore = true)
    GeneroEntity toEntity(GeneroDTO dto);
}
