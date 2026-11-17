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
        DocumentopersonaDTO dto = map.toDto(input);
        return map.toOutput(service.create(dto));

    }

    @Override
    public DocumentopersonaOutput update(DOCUMENTOPERSONA_UPDATE input) {
        DocumentopersonaDTO dto = map.toDto(input);
        return map.toOutput(service.update(input.id(), dto));

    }

    @Override
    public DocumentopersonaOutput patch(DOCUMENTOPERSONA_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Documentopersona");
    }

    @Override
    public DocumentopersonaOutput findById(DOCUMENTOPERSONA_FIND input) {
        return map.toOutput(service.findById(input.id()));

    }

    @Override
    public List<DocumentopersonaOutput> findAll() {
        return service.findAll().stream().map(map::toOutput).toList();

    }

    @Override
    public void deleteById(DOCUMENTOPERSONA_DELETE input) {
        service.deleteById(input.id());
    }
}
