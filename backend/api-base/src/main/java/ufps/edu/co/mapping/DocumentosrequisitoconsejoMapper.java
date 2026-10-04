package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.DocumentosrequisitoconsejoEntity;
import ufps.edu.co.rest.dto.DocumentosrequisitoconsejoDTO;

/**
 * Mapper MapStruct DocumentosrequisitoconsejoEntity &lt;-&gt; DocumentosrequisitoconsejoDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface DocumentosrequisitoconsejoMapper extends EntityMapper<DocumentosrequisitoconsejoEntity, DocumentosrequisitoconsejoDTO> {

    @Override
    DocumentosrequisitoconsejoDTO toDto(DocumentosrequisitoconsejoEntity entity);

    @Override
    @Mapping(target = "documentosrequisitoconsejocohorteList", ignore = true)
    DocumentosrequisitoconsejoEntity toEntity(DocumentosrequisitoconsejoDTO dto);
}
