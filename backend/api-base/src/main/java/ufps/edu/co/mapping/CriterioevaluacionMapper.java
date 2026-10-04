package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.CriterioevaluacionEntity;
import ufps.edu.co.rest.dto.CriterioevaluacionDTO;

/**
 * Mapper MapStruct CriterioevaluacionEntity &lt;-&gt; CriterioevaluacionDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class, uses = { ProgramaMapper.class })
public interface CriterioevaluacionMapper extends EntityMapper<CriterioevaluacionEntity, CriterioevaluacionDTO> {

    @Override
    @Mapping(target = "calificacioncriterioList", ignore = true)
    // nombres distintos en Entity (idPrograma) y DTO (idprograma)
    @Mapping(target = "idprograma", source = "idPrograma")
    CriterioevaluacionDTO toDto(CriterioevaluacionEntity entity);

    @Override
    @Mapping(target = "criteriocohorteList", ignore = true)
    // ModelMapper tomaba idPrograma de programa.id (el DTO usa idprograma)
    @Mapping(target = "idPrograma", source = "programa.id")
    CriterioevaluacionEntity toEntity(CriterioevaluacionDTO dto);
}
