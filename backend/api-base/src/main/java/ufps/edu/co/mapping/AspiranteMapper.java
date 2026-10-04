package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.AspiranteEntity;
import ufps.edu.co.rest.dto.AspiranteDTO;

/**
 * Mapper MapStruct AspiranteEntity &lt;-&gt; AspiranteDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class, uses = {
        CohorteMapper.class,
        EstadoMapper.class,
        PersonaMapper.class,
        TipovinculacionMapper.class
})
public interface AspiranteMapper extends EntityMapper<AspiranteEntity, AspiranteDTO> {

    @Override
    @Mapping(target = "admitidoList", ignore = true)
    @Mapping(target = "calificacioncriterioList", ignore = true)
    @Mapping(target = "documentoList", ignore = true)
    @Mapping(target = "entrevistaList", ignore = true)
    @Mapping(target = "pagoList", ignore = true)
    @Mapping(target = "pruebaList", ignore = true)
    AspiranteDTO toDto(AspiranteEntity entity);

    @Override
    @Mapping(target = "admitidoList", ignore = true)
    @Mapping(target = "calificacioncriterioList", ignore = true)
    @Mapping(target = "documentoList", ignore = true)
    @Mapping(target = "entrevistaList", ignore = true)
    @Mapping(target = "pagoList", ignore = true)
    @Mapping(target = "pruebaList", ignore = true)
    AspiranteEntity toEntity(AspiranteDTO dto);
}
