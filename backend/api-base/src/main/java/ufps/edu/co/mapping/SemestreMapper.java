package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.SemestreEntity;
import ufps.edu.co.rest.dto.SemestreDTO;

/**
 * Mapper MapStruct SemestreEntity &lt;-&gt; SemestreDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten estado y las colecciones (antes emptyTypeMap).
 */
@Mapper(config = MapStructConfig.class, uses = { EstadoMapper.class })
public interface SemestreMapper extends EntityMapper<SemestreEntity, SemestreDTO> {

    @Override
    @Mapping(target = "cohorteList", ignore = true)
    SemestreDTO toDto(SemestreEntity entity);

    @Override
    @Mapping(target = "cohorteList", ignore = true)
    @Mapping(target = "estado", ignore = true)
    SemestreEntity toEntity(SemestreDTO dto);
}
