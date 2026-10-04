package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.ModalidadEntity;
import ufps.edu.co.rest.dto.ModalidadDTO;

/**
 * Mapper MapStruct ModalidadEntity &lt;-&gt; ModalidadDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface ModalidadMapper extends EntityMapper<ModalidadEntity, ModalidadDTO> {

    @Override
    @Mapping(target = "cohorteList", ignore = true)
    ModalidadDTO toDto(ModalidadEntity entity);

    @Override
    @Mapping(target = "cohorteList", ignore = true)
    ModalidadEntity toEntity(ModalidadDTO dto);
}
