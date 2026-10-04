package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.EstadodocumentoEntity;
import ufps.edu.co.rest.dto.EstadodocumentoDTO;

/**
 * Mapper MapStruct EstadodocumentoEntity &lt;-&gt; EstadodocumentoDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface EstadodocumentoMapper extends EntityMapper<EstadodocumentoEntity, EstadodocumentoDTO> {

    @Override
    @Mapping(target = "documentoList", ignore = true)
    EstadodocumentoDTO toDto(EstadodocumentoEntity entity);

    @Override
    @Mapping(target = "documentoList", ignore = true)
    EstadodocumentoEntity toEntity(EstadodocumentoDTO dto);
}
