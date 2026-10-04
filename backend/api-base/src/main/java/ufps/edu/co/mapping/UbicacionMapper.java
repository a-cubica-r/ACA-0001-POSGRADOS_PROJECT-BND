package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.UbicacionEntity;
import ufps.edu.co.rest.dto.UbicacionDTO;

/**
 * Mapper MapStruct UbicacionEntity &lt;-&gt; UbicacionDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten municipio y las colecciones (antes emptyTypeMap).
 */
@Mapper(config = MapStructConfig.class, uses = { MunicipioMapper.class })
public interface UbicacionMapper extends EntityMapper<UbicacionEntity, UbicacionDTO> {

    @Override
    @Mapping(target = "documentopersonaList", ignore = true)
    @Mapping(target = "entrevistaList", ignore = true)
    @Mapping(target = "personaList", ignore = true)
    @Mapping(target = "personaList2", ignore = true)
    @Mapping(target = "personaList3", ignore = true)
    @Mapping(target = "pruebaList", ignore = true)
    @Mapping(target = "sedeList", ignore = true)
    UbicacionDTO toDto(UbicacionEntity entity);

    @Override
    @Mapping(target = "documentopersonaList", ignore = true)
    @Mapping(target = "entrevistaList", ignore = true)
    @Mapping(target = "municipio", ignore = true)
    @Mapping(target = "personaList", ignore = true)
    @Mapping(target = "personaList2", ignore = true)
    @Mapping(target = "personaList3", ignore = true)
    @Mapping(target = "pruebaList", ignore = true)
    @Mapping(target = "sedeList", ignore = true)
    UbicacionEntity toEntity(UbicacionDTO dto);
}
