package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import ufps.edu.co.persistence.entities.PruebaEntity;
import ufps.edu.co.rest.dto.PruebaDTO;

/**
 * Mapper MapStruct PruebaEntity &lt;-&gt; PruebaDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class, uses = {
        AspiranteMapper.class,
        CohorteMapper.class,
        EstadoMapper.class,
        TipopruebaMapper.class,
        UbicacionMapper.class
})
public interface PruebaMapper extends EntityMapper<PruebaEntity, PruebaDTO> {

    @Override
    PruebaDTO toDto(PruebaEntity entity);

    @Override
    PruebaEntity toEntity(PruebaDTO dto);
}
