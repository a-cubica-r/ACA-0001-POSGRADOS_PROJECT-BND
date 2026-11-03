package ufps.edu.co.maps.specific;

import java.util.List;
import org.springframework.stereotype.Component;
import ufps.edu.co.maps.GlobalMapper;
import ufps.edu.co.records.input.entity.ClaveInput.*;
import ufps.edu.co.records.output.entity.ClaveOutput;
import ufps.edu.co.rest.dto.ClaveDTO;

@Component
public class ClaveMap extends
        GlobalMapper<CLAVE_CREATE, CLAVE_UPDATE, CLAVE_DELETE, CLAVE_PATCH, CLAVE_FIND, ClaveOutput, ClaveDTO> {

    public ClaveMap() {
        super(CLAVE_CREATE.class, CLAVE_UPDATE.class, CLAVE_DELETE.class, CLAVE_PATCH.class,
                CLAVE_FIND.class);
    }

    @Override
    protected ClaveDTO toDtoCreate(CLAVE_CREATE input) {
        ClaveDTO dto = ClaveDTO.builder()
                .valor(input.valor())
                .build();
        return dto;
    }

    @Override
    protected ClaveDTO toDtoUpdate(CLAVE_UPDATE input) {
        ClaveDTO dto = ClaveDTO.builder()
                .id(input.id())
                .valor(input.valor())
                .build();
        return dto;
    }

    @Override
    protected ClaveDTO toDtoDelete(CLAVE_DELETE input) {
        ClaveDTO dto = ClaveDTO.builder()
                .id(input.id())
                .build();
        return dto;
    }

    @Override
    protected ClaveDTO toDtoPatch(CLAVE_PATCH input) {
        ClaveDTO.ClaveDTOBuilder builder = ClaveDTO.builder()
                .id(input.id());

        return builder.build();
    }

    @Override
    protected ClaveDTO toDtoFind(CLAVE_FIND input) {
        ClaveDTO dto = ClaveDTO.builder()
                .id(input.id())
                .build();
        return dto;
    }

    @Override
    public ClaveOutput toOutput(ClaveDTO dto) {
        return ClaveOutput.builder()
                .id(dto.getId())
                .build();
    }

    public List<ClaveOutput> toOutputList(List<ClaveDTO> dtoList) {
        return dtoList.stream().map(this::toOutput).toList();
    }
}
