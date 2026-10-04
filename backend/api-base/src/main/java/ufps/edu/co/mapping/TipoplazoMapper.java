package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.TipoplazoEntity;
import ufps.edu.co.rest.dto.TipoplazoDTO;

/**
 * Mapper MapStruct TipoplazoEntity &lt;-&gt; TipoplazoDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface TipoplazoMapper extends EntityMapper<TipoplazoEntity, TipoplazoDTO> {

    @Override
    @Mapping(target = "plazoList", ignore = true)
    TipoplazoDTO toDto(TipoplazoEntity entity);

    @Override
    @Mapping(target = "plazoList", ignore = true)
    TipoplazoEntity toEntity(TipoplazoDTO dto);
}
