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
        try {
            EstadodocumentoDTO dto = map.toDto(input);
            EstadodocumentoDTO created = service.create(dto);
            return map.toOutput(created);
        } catch (Exception e) {
            throw new RuntimeException("Error creating Estadodocumento: " + e.getMessage(), e);
        }
    }

    @Override
    public EstadodocumentoOutput update(ESTADODOCUMENTO_UPDATE input) {
        try {
            EstadodocumentoDTO dto = map.toDto(input);
            EstadodocumentoDTO updated = service.update(input.id(), dto);
            return map.toOutput(updated);
        } catch (Exception e) {
            throw new RuntimeException("Error updating Estadodocumento: " + e.getMessage(), e);
        }
    }

    @Override
    public EstadodocumentoOutput patch(ESTADODOCUMENTO_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Estadodocumento");
    }

    @Override
    public EstadodocumentoOutput findById(ESTADODOCUMENTO_FIND input) {
        try {
            EstadodocumentoDTO dto = service.findById(input.id());
            return map.toOutput(dto);
        } catch (Exception e) {
            throw new RuntimeException("Error finding Estadodocumento by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<EstadodocumentoOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Estadodocumentos: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(ESTADODOCUMENTO_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Estadodocumento by ID: " + e.getMessage(), e);
        }
    }
}
