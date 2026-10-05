package ufps.edu.co.services.core.crud;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufps.edu.co.maps.specific.DocumentopersonaMap;
import ufps.edu.co.records.input.entity.DocumentopersonaInput.*;
import ufps.edu.co.records.output.entity.DocumentopersonaOutput;
import ufps.edu.co.rest.dto.DocumentopersonaDTO;
import ufps.edu.co.rest.services.DocumentopersonaService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class DocumentopersonaCoreService implements
        GlobalUseCase<DOCUMENTOPERSONA_CREATE, DOCUMENTOPERSONA_UPDATE, DOCUMENTOPERSONA_DELETE, DOCUMENTOPERSONA_PATCH, DOCUMENTOPERSONA_FIND, DocumentopersonaOutput> {

    @Autowired
    private DocumentopersonaService service;

    @Autowired
    private DocumentopersonaMap map;

    @Override
    public DocumentopersonaOutput create(DOCUMENTOPERSONA_CREATE input) {
        try {
            DocumentopersonaDTO dto = map.toDto(input);
            return map.toOutput(service.create(dto));
        } catch (Exception e) {
            throw new RuntimeException("Error creating Documentopersona: " + e.getMessage(), e);
        }
    }

    @Override
    public DocumentopersonaOutput update(DOCUMENTOPERSONA_UPDATE input) {
        try {
            DocumentopersonaDTO dto = map.toDto(input);
            return map.toOutput(service.update(input.id(), dto));
        } catch (Exception e) {
            throw new RuntimeException("Error updating Documentopersona: " + e.getMessage(), e);
        }
    }

    @Override
    public DocumentopersonaOutput patch(DOCUMENTOPERSONA_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Documentopersona");
    }

    @Override
    public DocumentopersonaOutput findById(DOCUMENTOPERSONA_FIND input) {
        try {
            return map.toOutput(service.findById(input.id()));
        } catch (Exception e) {
            throw new RuntimeException("Error finding Documentopersona by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DocumentopersonaOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Documentopersonas: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(DOCUMENTOPERSONA_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Documentopersona by ID: " + e.getMessage(), e);
        }
    }
}
