package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.CohorteEntity;
import ufps.edu.co.rest.dto.CohorteDTO;

/**
 * Mapper MapStruct CohorteEntity &lt;-&gt; CohorteDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: solo se copian columnas e ids; las relaciones se omiten (antes emptyTypeMap).
 */
@Mapper(config = MapStructConfig.class, uses = {
        EstadoMapper.class,
        ModalidadMapper.class,
        PlazoMapper.class,
        ProgramaMapper.class,
        SemestreMapper.class
})
public interface CohorteMapper extends EntityMapper<CohorteEntity, CohorteDTO> {

    @Override
    @Mapping(target = "admitidoList", ignore = true)
    @Mapping(target = "aspiranteList", ignore = true)
    @Mapping(target = "criterioevaluacionList", ignore = true)
    @Mapping(target = "pruebaList", ignore = true)
    CohorteDTO toDto(CohorteEntity entity);

    @Override
    @Mapping(target = "admitidoList", ignore = true)
    @Mapping(target = "aspiranteList", ignore = true)
    @Mapping(target = "criteriocohorteList", ignore = true)
    @Mapping(target = "documentosrequisitoconsejocohorteList", ignore = true)
    @Mapping(target = "documentosrequisitoprogramacohorteList", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "modalidad", ignore = true)
    @Mapping(target = "plazo", ignore = true)
    @Mapping(target = "plazo2", ignore = true)
    @Mapping(target = "plazo3", ignore = true)
    @Mapping(target = "programa", ignore = true)
    @Mapping(target = "pruebaList", ignore = true)
    @Mapping(target = "semestre", ignore = true)
    CohorteEntity toEntity(CohorteDTO dto);
}
