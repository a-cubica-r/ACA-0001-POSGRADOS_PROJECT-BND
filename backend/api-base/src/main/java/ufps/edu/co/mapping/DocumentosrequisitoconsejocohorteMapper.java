package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ufps.edu.co.persistence.entities.CohorteEntity;
import ufps.edu.co.persistence.entities.DocumentosrequisitoconsejocohorteEntity;
import ufps.edu.co.rest.dto.DocumentosrequisitoconsejocohorteDTO;

/**
 * Mapper MapStruct DocumentosrequisitoconsejocohorteEntity &lt;-&gt; DocumentosrequisitoconsejocohorteDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface DocumentosrequisitoconsejocohorteMapper extends EntityMapper<DocumentosrequisitoconsejocohorteEntity, DocumentosrequisitoconsejocohorteDTO> {

    @Override
    DocumentosrequisitoconsejocohorteDTO toDto(DocumentosrequisitoconsejocohorteEntity entity);

    @Override
    // ModelMapper creaba la cohorte solo con el id a partir de idCohorte
    @Mapping(target = "cohorte", source = "idCohorte", qualifiedByName = "cohorteSoloId")
    @Mapping(target = "documentoList", ignore = true)
    @Mapping(target = "documentosrequisitoconsejo", ignore = true)
    DocumentosrequisitoconsejocohorteEntity toEntity(DocumentosrequisitoconsejocohorteDTO dto);

    @Named("cohorteSoloId")
    default CohorteEntity cohorteSoloId(Integer id) {
        return id == null ? null : CohorteEntity.builder().id(id).build();
    }
}
