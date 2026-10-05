package ufps.edu.co.services.core.crud;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufps.edu.co.maps.specific.TipoentrevistaMap;
import ufps.edu.co.records.input.entity.TipoentrevistaInput.*;
import ufps.edu.co.records.output.entity.TipoentrevistaOutput;
import ufps.edu.co.rest.dto.TipoentrevistaDTO;
import ufps.edu.co.rest.services.TipoentrevistaService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class TipoentrevistaCoreService implements
        GlobalUseCase<TIPOENTREVISTA_CREATE, TIPOENTREVISTA_UPDATE, TIPOENTREVISTA_DELETE, TIPOENTREVISTA_PATCH, TIPOENTREVISTA_FIND, TipoentrevistaOutput> {

    @Autowired
    private TipoentrevistaService service;

    @Autowired
    private TipoentrevistaMap map;

    @Override
    public TipoentrevistaOutput create(TIPOENTREVISTA_CREATE input) {
        try {
            TipoentrevistaDTO dto = map.toDto(input);
            TipoentrevistaDTO created = service.create(dto);
            return map.toOutput(created);
        } catch (Exception e) {
            throw new RuntimeException("Error creating Tipoentrevista: " + e.getMessage(), e);
        }
    }

    @Override
    public TipoentrevistaOutput update(TIPOENTREVISTA_UPDATE input) {
        try {
            TipoentrevistaDTO dto = map.toDto(input);
            TipoentrevistaDTO updated = service.update(input.id(), dto);
            return map.toOutput(updated);
        } catch (Exception e) {
            throw new RuntimeException("Error updating Tipoentrevista: " + e.getMessage(), e);
        }
    }

    @Override
    public TipoentrevistaOutput patch(TIPOENTREVISTA_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Tipoentrevista");
    }

    @Override
    public TipoentrevistaOutput findById(TIPOENTREVISTA_FIND input) {
        try {
            TipoentrevistaDTO dto = service.findById(input.id());
            return map.toOutput(dto);
        } catch (Exception e) {
            throw new RuntimeException("Error finding Tipoentrevista by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<TipoentrevistaOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Tipoentrevistas: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(TIPOENTREVISTA_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Tipoentrevista by ID: " + e.getMessage(), e);
        }
    }
}
