package ufps.edu.co.services.core.crud;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufps.edu.co.maps.specific.CambiodocumentoMap;
import ufps.edu.co.records.input.entity.CambiodocumentoInput.*;
import ufps.edu.co.records.output.entity.CambiodocumentoOutput;
import ufps.edu.co.rest.dto.CambiodocumentoDTO;
import ufps.edu.co.rest.services.CambiodocumentoService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class CambiodocumentoCoreService implements
        GlobalUseCase<CAMBIODOCUMENTO_CREATE, CAMBIODOCUMENTO_UPDATE, CAMBIODOCUMENTO_DELETE, CAMBIODOCUMENTO_PATCH, CAMBIODOCUMENTO_FIND, CambiodocumentoOutput> {

    @Autowired
    private CambiodocumentoService service;

    @Autowired
    private CambiodocumentoMap map;

    @Override
    public CambiodocumentoOutput create(CAMBIODOCUMENTO_CREATE input) {
        CambiodocumentoDTO dto = map.toDto(input);
        CambiodocumentoDTO created = service.create(dto);
        return map.toOutput(created);

    }

    @Override
    public CambiodocumentoOutput update(CAMBIODOCUMENTO_UPDATE input) {
        CambiodocumentoDTO dto = map.toDto(input);
        CambiodocumentoDTO updated = service.update(input.id(), dto);
        return map.toOutput(updated);

    }

    @Override
    public CambiodocumentoOutput patch(CAMBIODOCUMENTO_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Cambiodocumento");
    }

    @Override
    public CambiodocumentoOutput findById(CAMBIODOCUMENTO_FIND input) {
        CambiodocumentoDTO dto = service.findById(input.id());
        return map.toOutput(dto);

    }

    @Override
    public List<CambiodocumentoOutput> findAll() {
        return service.findAll().stream().map(map::toOutput).toList();

    }

    @Override
    public void deleteById(CAMBIODOCUMENTO_DELETE input) {
        service.deleteById(input.id());
    }
}
