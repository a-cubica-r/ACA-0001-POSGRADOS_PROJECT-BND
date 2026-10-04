package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ufps.edu.co.persistence.entities.ModalidadEntity;
import ufps.edu.co.persistence.entities.ProgramaEntity;
import ufps.edu.co.rest.dto.ProgramaDTO;

/**
 * Mapper MapStruct ProgramaEntity &lt;-&gt; ProgramaDTO (reemplaza ModelMapper).
 * <p>
 * Entity -&gt; DTO: no se mapean colecciones ({@code @OneToMany}) para evitar ciclos y cargas LAZY.
 * DTO -&gt; Entity: se omiten las colecciones; las relaciones se copian igual que con ModelMapper.
 */
@Mapper(config = MapStructConfig.class, uses = {
        FacultadMapper.class,
        OtrosvaloresMapper.class,
        SedeMapper.class,
        TiporegistroMapper.class
})
public interface ProgramaMapper extends EntityMapper<ProgramaEntity, ProgramaDTO> {

    @Override
    @Mapping(target = "cargoList", ignore = true)
    @Mapping(target = "cohorteList", ignore = true)
    ProgramaDTO toDto(ProgramaEntity entity);

    @Override
    @Mapping(target = "cargoList", ignore = true)
    @Mapping(target = "cohorteList", ignore = true)
    @Mapping(target = "criterioevaluacionList", ignore = true)
    @Mapping(target = "directorId", ignore = true)
    @Mapping(target = "documentosrequisitoprogramaList", ignore = true)
    @Mapping(target = "estudiantesList", ignore = true)
    @Mapping(target = "historicomoodleld", ignore = true)
    // ModelMapper creaba la modalidad solo con el id a partir de idModalidad
    @Mapping(target = "modalidad", source = "idModalidad", qualifiedByName = "modalidadSoloId")
    @Mapping(target = "moodleld", ignore = true)
    @Mapping(target = "semestreActual", ignore = true)
    @Mapping(target = "tipoProgramaId", ignore = true)
    ProgramaEntity toEntity(ProgramaDTO dto);

    @Named("modalidadSoloId")
    default ModalidadEntity modalidadSoloId(Integer id) {
        return id == null ? null : ModalidadEntity.builder().id(id).build();
    }
}
