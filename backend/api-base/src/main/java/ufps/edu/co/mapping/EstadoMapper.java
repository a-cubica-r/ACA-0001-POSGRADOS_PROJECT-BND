package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.EstadoEntity;
import ufps.edu.co.rest.dto.EstadoDTO;

/**
 * Mapper MapStruct EstadoEntity &lt;-&gt; EstadoDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface EstadoMapper extends EntityMapper<EstadoEntity, EstadoDTO> {

    @Override
    @Mapping(target = "administrativoList", ignore = true)
    @Mapping(target = "aspiranteList", ignore = true)
    @Mapping(target = "cohorteList", ignore = true)
    @Mapping(target = "entrevistaList", ignore = true)
    @Mapping(target = "pagoList", ignore = true)
    @Mapping(target = "semestreList", ignore = true)
    EstadoDTO toDto(EstadoEntity entity);

    @Override
    @Mapping(target = "administrativoList", ignore = true)
    @Mapping(target = "aspiranteList", ignore = true)
    @Mapping(target = "cohorteList", ignore = true)
    @Mapping(target = "entrevistaList", ignore = true)
    @Mapping(target = "pagoList", ignore = true)
    @Mapping(target = "pruebaList", ignore = true)
    @Mapping(target = "semestreList", ignore = true)
    EstadoEntity toEntity(EstadoDTO dto);
}
