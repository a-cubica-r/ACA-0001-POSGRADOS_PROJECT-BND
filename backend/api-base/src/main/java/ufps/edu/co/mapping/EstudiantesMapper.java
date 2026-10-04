package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ufps.edu.co.persistence.entities.EstudiantesEntity;
import ufps.edu.co.persistence.entities.ProgramaEntity;
import ufps.edu.co.persistence.entities.UsuarioEntity;
import ufps.edu.co.rest.dto.EstudiantesDTO;

/**
 * Mapper MapStruct EstudiantesEntity &lt;-&gt; EstudiantesDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class)
public interface EstudiantesMapper extends EntityMapper<EstudiantesEntity, EstudiantesDTO> {

    @Override
    // ModelMapper lo descartaba por ambiguedad (programaId vs programa.id); se conserva igual
    @Mapping(target = "programaId", ignore = true)
    // ModelMapper lo descartaba por ambiguedad (usuarioId vs usuario.id); se conserva igual
    @Mapping(target = "usuarioId", ignore = true)
    EstudiantesDTO toDto(EstudiantesEntity entity);

    @Override
    // ModelMapper creaba el programa solo con el id a partir de programaId
    @Mapping(target = "programa", source = "programaId", qualifiedByName = "programaSoloId")
    // ModelMapper creaba el usuario solo con el id a partir de usuarioId
    @Mapping(target = "usuario", source = "usuarioId", qualifiedByName = "usuarioSoloId")
    EstudiantesEntity toEntity(EstudiantesDTO dto);

    @Named("programaSoloId")
    default ProgramaEntity programaSoloId(Integer id) {
        return id == null ? null : ProgramaEntity.builder().id(id).build();
    }

    @Named("usuarioSoloId")
    default UsuarioEntity usuarioSoloId(Integer id) {
        return id == null ? null : UsuarioEntity.builder().id(id).build();
    }
}
