package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import ufps.edu.co.persistence.entities.PagoreciboinscripcionEntity;
import ufps.edu.co.rest.dto.PagoreciboinscripcionDTO;

/**
 * Mapper MapStruct PagoreciboinscripcionEntity &lt;-&gt; PagoreciboinscripcionDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class, uses = { EstadoMapper.class, PagoMapper.class })
public interface PagoreciboinscripcionMapper extends EntityMapper<PagoreciboinscripcionEntity, PagoreciboinscripcionDTO> {

    @Override
    PagoreciboinscripcionDTO toDto(PagoreciboinscripcionEntity entity);

    @Override
    PagoreciboinscripcionEntity toEntity(PagoreciboinscripcionDTO dto);
}
