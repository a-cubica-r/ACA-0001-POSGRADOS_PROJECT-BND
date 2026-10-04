package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.TiporegistroEntity;
import ufps.edu.co.rest.dto.TiporegistroDTO;

/**
 * Mapper MapStruct TiporegistroEntity &lt;-&gt; TiporegistroDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface TiporegistroMapper extends EntityMapper<TiporegistroEntity, TiporegistroDTO> {

    @Override
    @Mapping(target = "programaList", ignore = true)
    TiporegistroDTO toDto(TiporegistroEntity entity);

    @Override
    @Mapping(target = "programaList", ignore = true)
    TiporegistroEntity toEntity(TiporegistroDTO dto);
}
