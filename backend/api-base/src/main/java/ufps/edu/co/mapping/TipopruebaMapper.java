package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.TipopruebaEntity;
import ufps.edu.co.rest.dto.TipopruebaDTO;

/**
 * Mapper MapStruct TipopruebaEntity &lt;-&gt; TipopruebaDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface TipopruebaMapper extends EntityMapper<TipopruebaEntity, TipopruebaDTO> {

    @Override
    TipopruebaDTO toDto(TipopruebaEntity entity);

    @Override
    @Mapping(target = "pruebaList", ignore = true)
    TipopruebaEntity toEntity(TipopruebaDTO dto);
}
