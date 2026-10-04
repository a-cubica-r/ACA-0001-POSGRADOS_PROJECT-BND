package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.CambiodocumentoEntity;
import ufps.edu.co.rest.dto.CambiodocumentoDTO;

/**
 * Mapper MapStruct CambiodocumentoEntity &lt;-&gt; CambiodocumentoDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class, uses = { DocumentoMapper.class })
public interface CambiodocumentoMapper extends EntityMapper<CambiodocumentoEntity, CambiodocumentoDTO> {

    @Override
    CambiodocumentoDTO toDto(CambiodocumentoEntity entity);

    @Override
    @Mapping(target = "motivo", ignore = true)
    @Mapping(target = "tiempocambio", ignore = true)
    CambiodocumentoEntity toEntity(CambiodocumentoDTO dto);
}
