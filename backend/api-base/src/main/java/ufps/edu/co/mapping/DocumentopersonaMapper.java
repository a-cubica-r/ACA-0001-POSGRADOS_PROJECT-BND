package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.DocumentopersonaEntity;
import ufps.edu.co.rest.dto.DocumentopersonaDTO;

/**
 * Mapper MapStruct DocumentopersonaEntity &lt;-&gt; DocumentopersonaDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class, uses = { TipodocumentopersonaMapper.class, UbicacionMapper.class })
public interface DocumentopersonaMapper extends EntityMapper<DocumentopersonaEntity, DocumentopersonaDTO> {

    @Override
    @Mapping(target = "personaList", ignore = true)
    DocumentopersonaDTO toDto(DocumentopersonaEntity entity);

    @Override
    @Mapping(target = "personaList", ignore = true)
    DocumentopersonaEntity toEntity(DocumentopersonaDTO dto);
}
