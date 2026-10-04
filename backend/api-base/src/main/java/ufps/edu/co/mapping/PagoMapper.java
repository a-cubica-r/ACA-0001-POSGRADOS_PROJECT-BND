package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import ufps.edu.co.persistence.entities.PagoEntity;
import ufps.edu.co.rest.dto.PagoDTO;

/**
 * Mapper MapStruct PagoEntity &lt;-&gt; PagoDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class, uses = { AspiranteMapper.class, EstadoMapper.class, PagoconceptoMapper.class })
public interface PagoMapper extends EntityMapper<PagoEntity, PagoDTO> {

    @Override
    PagoDTO toDto(PagoEntity entity);

    @Override
    PagoEntity toEntity(PagoDTO dto);
}
