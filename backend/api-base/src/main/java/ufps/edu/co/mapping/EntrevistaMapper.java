package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import ufps.edu.co.persistence.entities.EntrevistaEntity;
import ufps.edu.co.rest.dto.EntrevistaDTO;

/**
 * Mapper MapStruct EntrevistaEntity &lt;-&gt; EntrevistaDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class, uses = {
        AspiranteMapper.class,
        EstadoMapper.class,
        TipoentrevistaMapper.class,
        UbicacionMapper.class
})
public interface EntrevistaMapper extends EntityMapper<EntrevistaEntity, EntrevistaDTO> {

    @Override
    EntrevistaDTO toDto(EntrevistaEntity entity);

    @Override
    EntrevistaEntity toEntity(EntrevistaDTO dto);
}
