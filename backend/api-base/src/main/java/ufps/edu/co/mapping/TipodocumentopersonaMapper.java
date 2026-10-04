package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.TipodocumentopersonaEntity;
import ufps.edu.co.rest.dto.TipodocumentopersonaDTO;

/**
 * Mapper MapStruct TipodocumentopersonaEntity &lt;-&gt; TipodocumentopersonaDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface TipodocumentopersonaMapper extends EntityMapper<TipodocumentopersonaEntity, TipodocumentopersonaDTO> {

    @Override
    @Mapping(target = "documentopersonaList", ignore = true)
    TipodocumentopersonaDTO toDto(TipodocumentopersonaEntity entity);

    @Override
    @Mapping(target = "documentopersonaList", ignore = true)
    TipodocumentopersonaEntity toEntity(TipodocumentopersonaDTO dto);
}
