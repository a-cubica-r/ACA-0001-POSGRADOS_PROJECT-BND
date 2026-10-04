package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.RolEntity;
import ufps.edu.co.rest.dto.RolDTO;

/**
 * Mapper MapStruct RolEntity &lt;-&gt; RolDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface RolMapper extends EntityMapper<RolEntity, RolDTO> {

    @Override
    @Mapping(target = "usuarioList", ignore = true)
    RolDTO toDto(RolEntity entity);

    @Override
    @Mapping(target = "usuarioList", ignore = true)
    RolEntity toEntity(RolDTO dto);
}
