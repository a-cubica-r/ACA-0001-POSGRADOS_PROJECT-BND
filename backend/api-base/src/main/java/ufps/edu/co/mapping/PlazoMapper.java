package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.PlazoEntity;
import ufps.edu.co.rest.dto.PlazoDTO;

/**
 * Mapper MapStruct PlazoEntity &lt;-&gt; PlazoDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class, uses = { TipoplazoMapper.class })
public interface PlazoMapper extends EntityMapper<PlazoEntity, PlazoDTO> {

    @Override
    @Mapping(target = "cohorteList", ignore = true)
    @Mapping(target = "cohorteList2", ignore = true)
    @Mapping(target = "cohorteList3", ignore = true)
    @Mapping(target = "documentoList", ignore = true)
    PlazoDTO toDto(PlazoEntity entity);

    @Override
    @Mapping(target = "cohorteList", ignore = true)
    @Mapping(target = "cohorteList2", ignore = true)
    @Mapping(target = "cohorteList3", ignore = true)
    @Mapping(target = "documentoList", ignore = true)
    PlazoEntity toEntity(PlazoDTO dto);
}
