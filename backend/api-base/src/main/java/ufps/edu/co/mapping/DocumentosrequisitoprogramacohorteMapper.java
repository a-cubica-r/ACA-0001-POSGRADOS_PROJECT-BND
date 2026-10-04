package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.DocumentosrequisitoprogramacohorteEntity;
import ufps.edu.co.rest.dto.DocumentosrequisitoprogramacohorteDTO;

/**
 * Mapper MapStruct DocumentosrequisitoprogramacohorteEntity &lt;-&gt; DocumentosrequisitoprogramacohorteDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class, uses = { CohorteMapper.class })
public interface DocumentosrequisitoprogramacohorteMapper extends EntityMapper<DocumentosrequisitoprogramacohorteEntity, DocumentosrequisitoprogramacohorteDTO> {

    @Override
    DocumentosrequisitoprogramacohorteDTO toDto(DocumentosrequisitoprogramacohorteEntity entity);

    @Override
    @Mapping(target = "documentoList", ignore = true)
    @Mapping(target = "documentosrequisitoprograma", ignore = true)
    DocumentosrequisitoprogramacohorteEntity toEntity(DocumentosrequisitoprogramacohorteDTO dto);
}
