package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.PaisEntity;
import ufps.edu.co.rest.dto.PaisDTO;

/**
 * Mapper MapStruct PaisEntity &lt;-&gt; PaisDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface PaisMapper extends EntityMapper<PaisEntity, PaisDTO> {

    @Override
    @Mapping(target = "departamentoList", ignore = true)
    PaisDTO toDto(PaisEntity entity);

    @Override
    @Mapping(target = "departamentoList", ignore = true)
    PaisEntity toEntity(PaisDTO dto);
}
