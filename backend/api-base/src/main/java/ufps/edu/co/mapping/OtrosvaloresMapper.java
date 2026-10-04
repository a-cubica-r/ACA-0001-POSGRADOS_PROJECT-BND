package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.OtrosvaloresEntity;
import ufps.edu.co.rest.dto.OtrosvaloresDTO;

/**
 * Mapper MapStruct OtrosvaloresEntity &lt;-&gt; OtrosvaloresDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface OtrosvaloresMapper extends EntityMapper<OtrosvaloresEntity, OtrosvaloresDTO> {

    @Override
    @Mapping(target = "programaList", ignore = true)
    OtrosvaloresDTO toDto(OtrosvaloresEntity entity);

    @Override
    @Mapping(target = "programaList", ignore = true)
    OtrosvaloresEntity toEntity(OtrosvaloresDTO dto);
}
