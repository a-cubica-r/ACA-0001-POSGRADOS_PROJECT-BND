package ufps.edu.co.services.core.crud;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ufps.edu.co.maps.specific.DocumentosrequisitoprogramaMap;
import ufps.edu.co.records.input.entity.DocumentosrequisitoprogramaInput.*;
import ufps.edu.co.records.output.entity.DocumentosrequisitoprogramaOutput;
import ufps.edu.co.rest.dto.DocumentosrequisitoprogramaDTO;
import ufps.edu.co.rest.services.DocumentosrequisitoprogramaService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
@Primary
public class DocumentosrequisitoprogramaCoreService implements
        GlobalUseCase<
            DOCUMENTOSREQUISITOPROGRAMA_CREATE, 
            DOCUMENTOSREQUISITOPROGRAMA_UPDATE, 
            DOCUMENTOSREQUISITOPROGRAMA_DELETE, 
            DOCUMENTOSREQUISITOPROGRAMA_PATCH, 
            DOCUMENTOSREQUISITOPROGRAMA_FIND, 
            DocumentosrequisitoprogramaOutput> {

    @Autowired
    private DocumentosrequisitoprogramaService service;

    @Autowired
    private DocumentosrequisitoprogramaMap map;

    @Override
    public DocumentosrequisitoprogramaOutput create(DOCUMENTOSREQUISITOPROGRAMA_CREATE input) {
        try {
            DocumentosrequisitoprogramaDTO dto = map.toDto(input);
            return map.toOutput(service.create(dto));
        } catch (Exception e) {
            throw new RuntimeException("Error creating Documentosrequisitoprograma: " + e.getMessage(), e);
        }
    }

    public DocumentosrequisitoprogramaOutput create(DOCUMENTOSREQUISITOPROGRAMA_CREATEDOCUMENT input, Integer idPrograma) {
        try {
            DocumentosrequisitoprogramaDTO dto = DocumentosrequisitoprogramaDTO.builder()
                    .nombre(input.nombre())
                    .tamanomaximo(input.tamanomaximo())
                    .urlformato(input.urlformato())
                    .id_programa(idPrograma)
                    .build();
            dto.setId_programa(idPrograma);
            return map.toOutput(service.create(dto));
        } catch (Exception e) {
            throw new RuntimeException("Error creating Documentosrequisitoprograma: " + e.getMessage(), e);
        }
    }

    public DocumentosrequisitoprogramaOutput update(DOCUMENTOSREQUISITOPROGRAMA_CREATEDOCUMENT input, Integer idDocumento,
            Integer idPrograma) {
        try {
            DocumentosrequisitoprogramaDTO dto = DocumentosrequisitoprogramaDTO.builder()
                    .nombre(input.nombre())
                    .tamanomaximo(input.tamanomaximo())
                    .urlformato(input.urlformato())
                    .id_programa(idPrograma)
                    .build();
            dto.setId(idDocumento);
            return map.toOutput(service.update(idDocumento, dto));
        } catch (Exception e) {
            throw new RuntimeException("Error updating Documentosrequisitoprograma: " + e.getMessage(), e);
        }
    }

    @Override
    public DocumentosrequisitoprogramaOutput update(DOCUMENTOSREQUISITOPROGRAMA_UPDATE input) {
        try {
            DocumentosrequisitoprogramaDTO dto = map.toDto(input);
            return map.toOutput(service.update(input.id(), dto));
        } catch (Exception e) {
            throw new RuntimeException("Error updating Documentosrequisitoprograma: " + e.getMessage(), e);
        }
    }

    @Override
    public DocumentosrequisitoprogramaOutput patch(DOCUMENTOSREQUISITOPROGRAMA_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Documentosrequisitoprograma");
    }

    @Override
    public DocumentosrequisitoprogramaOutput findById(DOCUMENTOSREQUISITOPROGRAMA_FIND input) {
        try {
            return map.toOutput(service.findById(input.id()));
        } catch (Exception e) {
            throw new RuntimeException("Error finding Documentosrequisitoprograma by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DocumentosrequisitoprogramaOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Documentosrequisitoprograma: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(DOCUMENTOSREQUISITOPROGRAMA_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Documentosrequisitoprograma by ID: " + e.getMessage(), e);
        }
    }
}
