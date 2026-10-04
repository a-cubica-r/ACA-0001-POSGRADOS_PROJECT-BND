package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.PagoconceptoEntity;
import ufps.edu.co.rest.dto.PagoconceptoDTO;

/**
 * Mapper MapStruct PagoconceptoEntity &lt;-&gt; PagoconceptoDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface PagoconceptoMapper extends EntityMapper<PagoconceptoEntity, PagoconceptoDTO> {

    @Override
    @Mapping(target = "pagoList", ignore = true)
    PagoconceptoDTO toDto(PagoconceptoEntity entity);

    @Override
    @Mapping(target = "pagoList", ignore = true)
    PagoconceptoEntity toEntity(PagoconceptoDTO dto);
}
