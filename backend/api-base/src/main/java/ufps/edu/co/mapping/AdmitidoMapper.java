package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import ufps.edu.co.persistence.entities.AdmitidoEntity;
import ufps.edu.co.rest.dto.AdmitidoDTO;

/**
 * Mapper MapStruct AdmitidoEntity &lt;-&gt; AdmitidoDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class, uses = { AspiranteMapper.class, CohorteMapper.class })
public interface AdmitidoMapper extends EntityMapper<AdmitidoEntity, AdmitidoDTO> {

    @Override
    AdmitidoDTO toDto(AdmitidoEntity entity);

    @Override
    AdmitidoEntity toEntity(AdmitidoDTO dto);
}
