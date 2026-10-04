package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.CargoEntity;
import ufps.edu.co.rest.dto.CargoDTO;

/**
 * Mapper MapStruct CargoEntity &lt;-&gt; CargoDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class, uses = { FacultadMapper.class, ProgramaMapper.class })
public interface CargoMapper extends EntityMapper<CargoEntity, CargoDTO> {

    @Override
    @Mapping(target = "administrativoList", ignore = true)
    CargoDTO toDto(CargoEntity entity);

    @Override
    @Mapping(target = "administrativoList", ignore = true)
    CargoEntity toEntity(CargoDTO dto);
}
