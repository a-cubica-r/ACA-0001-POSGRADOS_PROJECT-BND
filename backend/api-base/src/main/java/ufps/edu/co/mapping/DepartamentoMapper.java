package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.DepartamentoEntity;
import ufps.edu.co.rest.dto.DepartamentoDTO;

/**
 * Mapper MapStruct DepartamentoEntity &lt;-&gt; DepartamentoDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class, uses = { PaisMapper.class })
public interface DepartamentoMapper extends EntityMapper<DepartamentoEntity, DepartamentoDTO> {

    @Override
    @Mapping(target = "municipioList", ignore = true)
    DepartamentoDTO toDto(DepartamentoEntity entity);

    @Override
    @Mapping(target = "municipioList", ignore = true)
    DepartamentoEntity toEntity(DepartamentoDTO dto);
}
