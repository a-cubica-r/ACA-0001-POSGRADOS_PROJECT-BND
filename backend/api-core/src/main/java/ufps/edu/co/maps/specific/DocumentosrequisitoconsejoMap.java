package ufps.edu.co.maps.specific;

import org.springframework.stereotype.Component;
import ufps.edu.co.domain.annotations.UniversalMapping;
import ufps.edu.co.maps.UniversalMapper;
import ufps.edu.co.records.input.entity.DocumentosrequisitoconsejoInput.*;
import ufps.edu.co.records.output.entity.DocumentosrequisitoconsejoOutput;
import ufps.edu.co.rest.dto.DocumentosrequisitoconsejoDTO;

@Component
@UniversalMapping(
    create = DOCUMENTOSREQUISITOCONSEJO_CREATE.class,
    update = DOCUMENTOSREQUISITOCONSEJO_UPDATE.class,
    delete = DOCUMENTOSREQUISITOCONSEJO_DELETE.class,
    patch = DOCUMENTOSREQUISITOCONSEJO_PATCH.class,
    find = DOCUMENTOSREQUISITOCONSEJO_FIND.class)
public class DocumentosrequisitoconsejoMap extends UniversalMapper<DocumentosrequisitoconsejoOutput, DocumentosrequisitoconsejoDTO> {

    @Override
    public DocumentosrequisitoconsejoOutput toOutput(DocumentosrequisitoconsejoDTO dto) {
        return DocumentosrequisitoconsejoOutput.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .tamanomaximo(dto.getTamanomaximo())
                .urlformato(dto.getUrlformato())
                .build();
    }
}
