package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.ClaveEntity;
import ufps.edu.co.rest.dto.ClaveDTO;

/**
 * Mapper MapStruct ClaveEntity &lt;-&gt; ClaveDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface ClaveMapper extends EntityMapper<ClaveEntity, ClaveDTO> {

    @Override
    @Mapping(target = "usuarioList", ignore = true)
    ClaveDTO toDto(ClaveEntity entity);

    @Override
    @Mapping(target = "usuarioList", ignore = true)
    ClaveEntity toEntity(ClaveDTO dto);
}
