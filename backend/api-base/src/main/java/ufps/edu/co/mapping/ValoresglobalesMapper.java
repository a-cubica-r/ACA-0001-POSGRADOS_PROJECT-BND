package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import ufps.edu.co.persistence.entities.ValoresglobalesEntity;
import ufps.edu.co.rest.dto.ValoresglobalesDTO;

/**
 * Mapper MapStruct ValoresglobalesEntity &lt;-&gt; ValoresglobalesDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface ValoresglobalesMapper extends EntityMapper<ValoresglobalesEntity, ValoresglobalesDTO> {

    @Override
    ValoresglobalesDTO toDto(ValoresglobalesEntity entity);

    @Override
    ValoresglobalesEntity toEntity(ValoresglobalesDTO dto);
}
