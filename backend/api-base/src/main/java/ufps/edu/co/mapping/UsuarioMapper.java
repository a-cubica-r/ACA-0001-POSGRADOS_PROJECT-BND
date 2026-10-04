package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ufps.edu.co.persistence.entities.UsuarioEntity;
import ufps.edu.co.rest.dto.UsuarioDTO;

/**
 * Mapper MapStruct UsuarioEntity &lt;-&gt; UsuarioDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class, uses = { ClaveMapper.class, PersonaMapper.class, RolMapper.class })
public interface UsuarioMapper extends EntityMapper<UsuarioEntity, UsuarioDTO> {

    @Override
    UsuarioDTO toDto(UsuarioEntity entity);

    @Override
    @Mapping(target = "cedula", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "fotoUrl", ignore = true)
    @Mapping(target = "googleId", ignore = true)
    @Mapping(target = "moodleld", ignore = true)
    @Mapping(target = "nombrecompleto", ignore = true)
    @Mapping(target = "primerapellido", ignore = true)
    @Mapping(target = "primernombre", ignore = true)
    @Mapping(target = "segundoapellido", ignore = true)
    @Mapping(target = "segundonombre", ignore = true)
    @Mapping(target = "telefono", ignore = true)
    UsuarioEntity toEntity(UsuarioDTO dto);
}
