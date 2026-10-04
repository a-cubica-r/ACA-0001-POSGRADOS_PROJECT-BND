package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.DiscapacidadEntity;
import ufps.edu.co.rest.dto.DiscapacidadDTO;

/**
 * Mapper MapStruct DiscapacidadEntity &lt;-&gt; DiscapacidadDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface DiscapacidadMapper extends EntityMapper<DiscapacidadEntity, DiscapacidadDTO> {

    @Override
    @Mapping(target = "personaList", ignore = true)
    DiscapacidadDTO toDto(DiscapacidadEntity entity);

    @Override
    @Mapping(target = "personaList", ignore = true)
    DiscapacidadEntity toEntity(DiscapacidadDTO dto);
}
