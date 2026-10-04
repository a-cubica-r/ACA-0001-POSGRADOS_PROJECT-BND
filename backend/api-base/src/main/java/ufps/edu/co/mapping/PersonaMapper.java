package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ufps.edu.co.persistence.entities.PersonaEntity;
import ufps.edu.co.rest.dto.PersonaDTO;
import ufps.edu.co.rest.dto.UbicacionDTO;

/**
 * Mapper MapStruct PersonaEntity &lt;-&gt; PersonaDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: solo se copian columnas e ids; las relaciones se omiten (antes emptyTypeMap).
 */
@Mapper(config = MapStructConfig.class, uses = {
        CapacidadexepcionalMapper.class,
        DiscapacidadMapper.class,
        DocumentopersonaMapper.class,
        EstadocivilMapper.class,
        GeneroMapper.class,
        GrupoetnicoMapper.class,
        PoblacionindigenaMapper.class
})
public interface PersonaMapper extends EntityMapper<PersonaEntity, PersonaDTO> {

    @Override
    @Mapping(target = "administrativoList", ignore = true)
    @Mapping(target = "aspiranteList", ignore = true)
    // ModelMapper construia la ubicacion solo con el id a partir de idUbicacionnacimiento
    @Mapping(target = "ubicacionNacimiento", source = "idUbicacionnacimiento", qualifiedByName = "ubicacionSoloId")
    // ModelMapper construia la ubicacion solo con el id a partir de idUbicaciontrabajo
    @Mapping(target = "ubicacionTrabajo", source = "idUbicaciontrabajo", qualifiedByName = "ubicacionSoloId")
    // ModelMapper construia la ubicacion solo con el id a partir de idUbicacionvivienda
    @Mapping(target = "ubicacionVivienda", source = "idUbicacionvivienda", qualifiedByName = "ubicacionSoloId")
    @Mapping(target = "usuarioList", ignore = true)
    PersonaDTO toDto(PersonaEntity entity);

    @Override
    @Mapping(target = "administrativoList", ignore = true)
    @Mapping(target = "aspiranteList", ignore = true)
    @Mapping(target = "capacidadexepcional", ignore = true)
    @Mapping(target = "discapacidad", ignore = true)
    @Mapping(target = "documentopersona", ignore = true)
    @Mapping(target = "estadocivil", ignore = true)
    @Mapping(target = "genero", ignore = true)
    @Mapping(target = "grupoetnico", ignore = true)
    @Mapping(target = "poblacionindigena", ignore = true)
    @Mapping(target = "ubicacion", ignore = true)
    @Mapping(target = "ubicacion2", ignore = true)
    @Mapping(target = "ubicacion3", ignore = true)
    @Mapping(target = "usuarioList", ignore = true)
    PersonaEntity toEntity(PersonaDTO dto);

    @Named("ubicacionSoloId")
    default UbicacionDTO ubicacionSoloId(Integer id) {
        return id == null ? null : UbicacionDTO.builder().id(id).build();
    }
}
