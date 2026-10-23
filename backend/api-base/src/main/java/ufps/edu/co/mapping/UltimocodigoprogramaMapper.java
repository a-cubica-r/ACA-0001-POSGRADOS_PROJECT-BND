package ufps.edu.co.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ufps.edu.co.persistence.entities.UltimocodigoprogramaEntity;
import ufps.edu.co.rest.dto.UltimocodigoprogramaDTO;

/**
 * Mapper MapStruct UltimocodigoprogramaEntity &lt;-&gt; UltimocodigoprogramaDTO.
 * <p>
 * Entity -&gt; DTO: el nombre del programa se toma de la relación.
 * DTO -&gt; Entity: la relación no se escribe (la columna {@code id_programa} la
 * llena {@code idPrograma}).
 */
@Mapper(config = MapStructConfig.class)
public interface UltimocodigoprogramaMapper extends EntityMapper<UltimocodigoprogramaEntity, UltimocodigoprogramaDTO> {

    @Override
    @Mapping(source = "programa.nombre", target = "nombrePrograma")
    UltimocodigoprogramaDTO toDto(UltimocodigoprogramaEntity entity);

    @Override
    @Mapping(target = "programa", ignore = true)
    UltimocodigoprogramaEntity toEntity(UltimocodigoprogramaDTO dto);
}
