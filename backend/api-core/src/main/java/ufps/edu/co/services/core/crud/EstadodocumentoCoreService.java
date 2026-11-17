package ufps.edu.co.services.core.crud;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufps.edu.co.maps.specific.EstadodocumentoMap;
import ufps.edu.co.records.input.entity.EstadodocumentoInput.*;
import ufps.edu.co.records.output.entity.EstadodocumentoOutput;
import ufps.edu.co.rest.dto.EstadodocumentoDTO;
import ufps.edu.co.rest.services.EstadodocumentoService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class EstadodocumentoCoreService implements
        GlobalUseCase<ESTADODOCUMENTO_CREATE, ESTADODOCUMENTO_UPDATE, ESTADODOCUMENTO_DELETE, ESTADODOCUMENTO_PATCH, ESTADODOCUMENTO_FIND, EstadodocumentoOutput> {

    @Autowired
    private EstadodocumentoService service;

    @Autowired
    private EstadodocumentoMap map;

    @Override
    public EstadodocumentoOutput create(ESTADODOCUMENTO_CREATE input) {
        EstadodocumentoDTO dto = map.toDto(input);
        EstadodocumentoDTO created = service.create(dto);
        return map.toOutput(created);

    }

    @Override
    public EstadodocumentoOutput update(ESTADODOCUMENTO_UPDATE input) {
        EstadodocumentoDTO dto = map.toDto(input);
        EstadodocumentoDTO updated = service.update(input.id(), dto);
        return map.toOutput(updated);

    }

    @Override
    public EstadodocumentoOutput patch(ESTADODOCUMENTO_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Estadodocumento");
    }

    @Override
    public EstadodocumentoOutput findById(ESTADODOCUMENTO_FIND input) {
        EstadodocumentoDTO dto = service.findById(input.id());
        return map.toOutput(dto);

    }

    @Override
    public List<EstadodocumentoOutput> findAll() {
        return service.findAll().stream().map(map::toOutput).toList();

    }

    @Override
    public void deleteById(ESTADODOCUMENTO_DELETE input) {
        service.deleteById(input.id());
    }
}
