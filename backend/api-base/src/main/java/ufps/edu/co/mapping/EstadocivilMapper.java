package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.EstadocivilEntity;
import ufps.edu.co.rest.dto.EstadocivilDTO;

/**
 * Mapper MapStruct EstadocivilEntity &lt;-&gt; EstadocivilDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface EstadocivilMapper extends EntityMapper<EstadocivilEntity, EstadocivilDTO> {

    @Override
    @Mapping(target = "personaList", ignore = true)
    EstadocivilDTO toDto(EstadocivilEntity entity);

    @Override
    @Mapping(target = "personaList", ignore = true)
    EstadocivilEntity toEntity(EstadocivilDTO dto);
}
