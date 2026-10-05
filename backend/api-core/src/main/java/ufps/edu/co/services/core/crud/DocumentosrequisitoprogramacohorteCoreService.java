package ufps.edu.co.services.core.crud;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufps.edu.co.maps.specific.DocumentosrequisitoprogramacohorteMap;
import ufps.edu.co.records.input.entity.DocumentosrequisitoprogramacohorteInput.*;
import ufps.edu.co.records.output.entity.DocumentosrequisitoprogramacohorteOutput;
import ufps.edu.co.rest.dto.DocumentosrequisitoprogramacohorteDTO;
import ufps.edu.co.rest.services.DocumentosrequisitoprogramacohorteService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class DocumentosrequisitoprogramacohorteCoreService implements
        GlobalUseCase<DOCUMENTOSREQUISITOPROGRAMACOHORTE_CREATE, DOCUMENTOSREQUISITOPROGRAMACOHORTE_UPDATE, DOCUMENTOSREQUISITOPROGRAMACOHORTE_DELETE, DOCUMENTOSREQUISITOPROGRAMACOHORTE_PATCH, DOCUMENTOSREQUISITOPROGRAMACOHORTE_FIND, DocumentosrequisitoprogramacohorteOutput> {

    @Autowired
    private DocumentosrequisitoprogramacohorteService service;

    @Autowired
    private DocumentosrequisitoprogramacohorteMap map;

    @Override
    public DocumentosrequisitoprogramacohorteOutput create(DOCUMENTOSREQUISITOPROGRAMACOHORTE_CREATE input) {
        try {
            DocumentosrequisitoprogramacohorteDTO dto = map.toDto(input);
            return map.toOutput(service.create(dto));
        } catch (Exception e) {
            throw new RuntimeException("Error creating Documentosrequisitoprogramacohorte: " + e.getMessage(), e);
        }
    }

    @Override
    public DocumentosrequisitoprogramacohorteOutput update(DOCUMENTOSREQUISITOPROGRAMACOHORTE_UPDATE input) {
        try {
            DocumentosrequisitoprogramacohorteDTO dto = map.toDto(input);
            return map.toOutput(service.update(input.id(), dto));
        } catch (Exception e) {
            throw new RuntimeException("Error updating Documentosrequisitoprogramacohorte: " + e.getMessage(), e);
        }
    }

    @Override
    public DocumentosrequisitoprogramacohorteOutput patch(DOCUMENTOSREQUISITOPROGRAMACOHORTE_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Documentosrequisitoprogramacohorte");
    }

    @Override
    public DocumentosrequisitoprogramacohorteOutput findById(DOCUMENTOSREQUISITOPROGRAMACOHORTE_FIND input) {
        try {
            return map.toOutput(service.findById(input.id()));
        } catch (Exception e) {
            throw new RuntimeException("Error finding Documentosrequisitoprogramacohorte by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DocumentosrequisitoprogramacohorteOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Documentosrequisitoprogramacohorte: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(DOCUMENTOSREQUISITOPROGRAMACOHORTE_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Documentosrequisitoprogramacohorte by ID: " + e.getMessage(), e);
        }
    }

    public List<DocumentosrequisitoprogramacohorteOutput> findByIdCohorte(Integer idCohorte) {
        try {
            return service.findByIdCohorte(idCohorte).stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding Documentosrequisitoprogramacohorte by cohorte: " + e.getMessage(), e);
        }
    }
}
