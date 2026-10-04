package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import ufps.edu.co.persistence.entities.PagorecibomatriculaEntity;
import ufps.edu.co.rest.dto.PagorecibomatriculaDTO;

/**
 * Mapper MapStruct PagorecibomatriculaEntity &lt;-&gt; PagorecibomatriculaDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class, uses = { EstadoMapper.class, PagoMapper.class })
public interface PagorecibomatriculaMapper extends EntityMapper<PagorecibomatriculaEntity, PagorecibomatriculaDTO> {

    @Override
    PagorecibomatriculaDTO toDto(PagorecibomatriculaEntity entity);

    @Override
    PagorecibomatriculaEntity toEntity(PagorecibomatriculaDTO dto);
}
