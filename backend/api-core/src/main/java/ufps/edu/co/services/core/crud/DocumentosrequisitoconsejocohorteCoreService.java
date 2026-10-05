package ufps.edu.co.services.core.crud;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufps.edu.co.maps.specific.DocumentosrequisitoconsejocohorteMap;
import ufps.edu.co.records.input.entity.DocumentosrequisitoconsejocohorteInput.*;
import ufps.edu.co.records.output.entity.DocumentosrequisitoconsejocohorteOutput;
import ufps.edu.co.rest.dto.DocumentosrequisitoconsejocohorteDTO;
import ufps.edu.co.rest.services.DocumentosrequisitoconsejocohorteService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class DocumentosrequisitoconsejocohorteCoreService implements
        GlobalUseCase<DOCUMENTOSREQUISITOCONSEJOCOHORTE_CREATE, DOCUMENTOSREQUISITOCONSEJOCOHORTE_UPDATE, DOCUMENTOSREQUISITOCONSEJOCOHORTE_DELETE, DOCUMENTOSREQUISITOCONSEJOCOHORTE_PATCH, DOCUMENTOSREQUISITOCONSEJOCOHORTE_FIND, DocumentosrequisitoconsejocohorteOutput> {

    @Autowired
    private DocumentosrequisitoconsejocohorteService service;

    @Autowired
    private DocumentosrequisitoconsejocohorteMap map;

    @Override
    public DocumentosrequisitoconsejocohorteOutput create(DOCUMENTOSREQUISITOCONSEJOCOHORTE_CREATE input) {
        try {
            DocumentosrequisitoconsejocohorteDTO dto = map.toDto(input);
            return map.toOutput(service.create(dto));
        } catch (Exception e) {
            throw new RuntimeException("Error creating Documentosrequisitoconsejocohorte: " + e.getMessage(), e);
        }
    }

    @Override
    public DocumentosrequisitoconsejocohorteOutput update(DOCUMENTOSREQUISITOCONSEJOCOHORTE_UPDATE input) {
        try {
            DocumentosrequisitoconsejocohorteDTO dto = map.toDto(input);
            return map.toOutput(service.update(input.id(), dto));
        } catch (Exception e) {
            throw new RuntimeException("Error updating Documentosrequisitoconsejocohorte: " + e.getMessage(), e);
        }
    }

    @Override
    public DocumentosrequisitoconsejocohorteOutput patch(DOCUMENTOSREQUISITOCONSEJOCOHORTE_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Documentosrequisitoconsejocohorte");
    }

    @Override
    public DocumentosrequisitoconsejocohorteOutput findById(DOCUMENTOSREQUISITOCONSEJOCOHORTE_FIND input) {
        try {
            return map.toOutput(service.findById(input.id()));
        } catch (Exception e) {
            throw new RuntimeException("Error finding Documentosrequisitoconsejocohorte by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DocumentosrequisitoconsejocohorteOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Documentosrequisitoconsejocohorte: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(DOCUMENTOSREQUISITOCONSEJOCOHORTE_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Documentosrequisitoconsejocohorte by ID: " + e.getMessage(), e);
        }
    }

    public List<DocumentosrequisitoconsejocohorteOutput> findByIdCohorte(Integer idCohorte) {
        try {
            return service.findByIdCohorte(idCohorte).stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding Documentosrequisitoconsejocohorte by cohorte: " + e.getMessage(), e);
        }
    }
}
