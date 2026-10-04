package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.CriteriocohorteEntity;
import ufps.edu.co.rest.dto.CriteriocohorteDTO;

/**
 * Mapper MapStruct CriteriocohorteEntity &lt;-&gt; CriteriocohorteDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class, uses = { CohorteMapper.class, CriterioevaluacionMapper.class })
public interface CriteriocohorteMapper extends EntityMapper<CriteriocohorteEntity, CriteriocohorteDTO> {

    @Override
    @Mapping(target = "calificacioncriterioList", ignore = true)
    CriteriocohorteDTO toDto(CriteriocohorteEntity entity);

    @Override
    @Mapping(target = "calificacioncriterioList", ignore = true)
    CriteriocohorteEntity toEntity(CriteriocohorteDTO dto);
}
