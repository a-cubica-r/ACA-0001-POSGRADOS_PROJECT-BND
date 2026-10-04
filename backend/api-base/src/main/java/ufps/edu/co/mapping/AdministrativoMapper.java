package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.AdministrativoEntity;
import ufps.edu.co.rest.dto.AdministrativoDTO;

/**
 * Mapper MapStruct AdministrativoEntity &lt;-&gt; AdministrativoDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: solo se copian columnas e ids; las relaciones se omiten (antes emptyTypeMap).
 */
@Mapper(config = MapStructConfig.class, uses = { CargoMapper.class, EstadoMapper.class, PersonaMapper.class })
public interface AdministrativoMapper extends EntityMapper<AdministrativoEntity, AdministrativoDTO> {

    @Override
    @Mapping(target = "documentoList", ignore = true)
    AdministrativoDTO toDto(AdministrativoEntity entity);

    @Override
    @Mapping(target = "cargo", ignore = true)
    @Mapping(target = "documentoList", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "persona", ignore = true)
    AdministrativoEntity toEntity(AdministrativoDTO dto);
}
