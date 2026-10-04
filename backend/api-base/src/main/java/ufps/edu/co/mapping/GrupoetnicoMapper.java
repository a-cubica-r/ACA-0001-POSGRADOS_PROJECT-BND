package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.GrupoetnicoEntity;
import ufps.edu.co.rest.dto.GrupoetnicoDTO;

/**
 * Mapper MapStruct GrupoetnicoEntity &lt;-&gt; GrupoetnicoDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface GrupoetnicoMapper extends EntityMapper<GrupoetnicoEntity, GrupoetnicoDTO> {

    @Override
    @Mapping(target = "personaList", ignore = true)
    GrupoetnicoDTO toDto(GrupoetnicoEntity entity);

    @Override
    @Mapping(target = "personaList", ignore = true)
    GrupoetnicoEntity toEntity(GrupoetnicoDTO dto);
}
