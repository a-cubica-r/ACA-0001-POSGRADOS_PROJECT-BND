package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.TipoentrevistaEntity;
import ufps.edu.co.rest.dto.TipoentrevistaDTO;

/**
 * Mapper MapStruct TipoentrevistaEntity &lt;-&gt; TipoentrevistaDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface TipoentrevistaMapper extends EntityMapper<TipoentrevistaEntity, TipoentrevistaDTO> {

    @Override
    @Mapping(target = "entrevistaList", ignore = true)
    TipoentrevistaDTO toDto(TipoentrevistaEntity entity);

    @Override
    @Mapping(target = "entrevistaList", ignore = true)
    TipoentrevistaEntity toEntity(TipoentrevistaDTO dto);
}
