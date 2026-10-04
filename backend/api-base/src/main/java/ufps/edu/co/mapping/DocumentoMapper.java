package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.DocumentoEntity;
import ufps.edu.co.rest.dto.DocumentoDTO;

/**
 * Mapper MapStruct DocumentoEntity &lt;-&gt; DocumentoDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class, uses = {
        AdministrativoMapper.class,
        AspiranteMapper.class,
        DocumentosrequisitoconsejocohorteMapper.class,
        DocumentosrequisitoprogramacohorteMapper.class,
        EstadodocumentoMapper.class,
        PlazoMapper.class
})
public interface DocumentoMapper extends EntityMapper<DocumentoEntity, DocumentoDTO> {

    @Override
    @Mapping(target = "cambiodocumentoList", ignore = true)
    @Mapping(target = "cambiodocumentoList2", ignore = true)
    DocumentoDTO toDto(DocumentoEntity entity);

    @Override
    @Mapping(target = "cambiodocumentoList", ignore = true)
    @Mapping(target = "cambiodocumentoList2", ignore = true)
    DocumentoEntity toEntity(DocumentoDTO dto);
}
