package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.FacultadEntity;
import ufps.edu.co.rest.dto.FacultadDTO;

/**
 * Mapper MapStruct FacultadEntity &lt;-&gt; FacultadDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface FacultadMapper extends EntityMapper<FacultadEntity, FacultadDTO> {

    @Override
    @Mapping(target = "cargoList", ignore = true)
    @Mapping(target = "programaList", ignore = true)
    FacultadDTO toDto(FacultadEntity entity);

    @Override
    @Mapping(target = "cargoList", ignore = true)
    @Mapping(target = "programaList", ignore = true)
    FacultadEntity toEntity(FacultadDTO dto);
}
