package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.MunicipioEntity;
import ufps.edu.co.rest.dto.MunicipioDTO;

/**
 * Mapper MapStruct MunicipioEntity &lt;-&gt; MunicipioDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class, uses = { DepartamentoMapper.class })
public interface MunicipioMapper extends EntityMapper<MunicipioEntity, MunicipioDTO> {

    @Override
    @Mapping(target = "ubicacionList", ignore = true)
    MunicipioDTO toDto(MunicipioEntity entity);

    @Override
    @Mapping(target = "ubicacionList", ignore = true)
    MunicipioEntity toEntity(MunicipioDTO dto);
}
