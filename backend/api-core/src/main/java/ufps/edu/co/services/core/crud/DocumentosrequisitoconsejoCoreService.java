package ufps.edu.co.services.core.crud;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufps.edu.co.maps.specific.DocumentosrequisitoconsejoMap;
import ufps.edu.co.records.input.entity.DocumentosrequisitoconsejoInput.*;
import ufps.edu.co.records.output.entity.DocumentosrequisitoconsejoOutput;
import ufps.edu.co.rest.dto.DocumentosrequisitoconsejoDTO;
import ufps.edu.co.rest.services.DocumentosrequisitoconsejoService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class DocumentosrequisitoconsejoCoreService implements
        GlobalUseCase<DOCUMENTOSREQUISITOCONSEJO_CREATE, DOCUMENTOSREQUISITOCONSEJO_UPDATE, DOCUMENTOSREQUISITOCONSEJO_DELETE, DOCUMENTOSREQUISITOCONSEJO_PATCH, DOCUMENTOSREQUISITOCONSEJO_FIND, DocumentosrequisitoconsejoOutput> {

    @Autowired
    private DocumentosrequisitoconsejoService service;

    @Autowired
    private DocumentosrequisitoconsejoMap map;

    @Override
    public DocumentosrequisitoconsejoOutput create(DOCUMENTOSREQUISITOCONSEJO_CREATE input) {
        DocumentosrequisitoconsejoDTO dto = map.toDto(input);
        return map.toOutput(service.create(dto));

    }

    @Override
    public DocumentosrequisitoconsejoOutput update(DOCUMENTOSREQUISITOCONSEJO_UPDATE input) {
        DocumentosrequisitoconsejoDTO dto = map.toDto(input);
        return map.toOutput(service.update(input.id(), dto));

    }

    @Override
    public DocumentosrequisitoconsejoOutput patch(DOCUMENTOSREQUISITOCONSEJO_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Documentosrequisitoconsejo");
    }

    @Override
    public DocumentosrequisitoconsejoOutput findById(DOCUMENTOSREQUISITOCONSEJO_FIND input) {
        return map.toOutput(service.findById(input.id()));

    }

    @Override
    public List<DocumentosrequisitoconsejoOutput> findAll() {
        return service.findAll().stream().map(map::toOutput).toList();

    }

    @Override
    public void deleteById(DOCUMENTOSREQUISITOCONSEJO_DELETE input) {
        service.deleteById(input.id());
    }

    /**
     * Update only the urlformato field for a Documentosrequisitoconsejo record.
     * This is a small helper used by controllers that upload a formato to S3.
     */
    public void updateUrlFormato(Integer id, String urlformato) {
        DocumentosrequisitoconsejoDTO existing = service.findById(id);
        if (existing == null) {
            throw new RuntimeException("Documentosrequisitoconsejo no encontrado con id: " + id);
        }
        existing.setUrlformato(urlformato);
        service.update(id, existing);

    }
}
