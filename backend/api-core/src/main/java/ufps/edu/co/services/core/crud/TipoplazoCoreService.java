package ufps.edu.co.services.core.crud;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufps.edu.co.maps.specific.TipoplazoMap;
import ufps.edu.co.records.input.entity.TipoplazoInput.*;
import ufps.edu.co.records.output.entity.TipoplazoOutput;
import ufps.edu.co.rest.dto.TipoplazoDTO;
import ufps.edu.co.rest.services.TipoplazoService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class TipoplazoCoreService implements
        GlobalUseCase<TIPOPLAZO_CREATE, TIPOPLAZO_UPDATE, TIPOPLAZO_DELETE, TIPOPLAZO_PATCH, TIPOPLAZO_FIND, TipoplazoOutput> {

    @Autowired
    private TipoplazoService service;

    @Autowired
    private TipoplazoMap map;

    @Override
    public TipoplazoOutput create(TIPOPLAZO_CREATE input) {
        try {
            TipoplazoDTO dto = map.toDto(input);
            TipoplazoDTO created = service.create(dto);
            return map.toOutput(created);
        } catch (Exception e) {
            throw new RuntimeException("Error creating Tipoplazo: " + e.getMessage(), e);
        }
    }

    @Override
    public TipoplazoOutput update(TIPOPLAZO_UPDATE input) {
        try {
            TipoplazoDTO dto = map.toDto(input);
            TipoplazoDTO updated = service.update(input.id(), dto);
            return map.toOutput(updated);
        } catch (Exception e) {
            throw new RuntimeException("Error updating Tipoplazo: " + e.getMessage(), e);
        }
    }

    @Override
    public TipoplazoOutput patch(TIPOPLAZO_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Tipoplazo");
    }

    @Override
    public TipoplazoOutput findById(TIPOPLAZO_FIND input) {
        try {
            TipoplazoDTO dto = service.findById(input.id());
            return map.toOutput(dto);
        } catch (Exception e) {
            throw new RuntimeException("Error finding Tipoplazo by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<TipoplazoOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Tipoplazos: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(TIPOPLAZO_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Tipoplazo by ID: " + e.getMessage(), e);
        }
    }
}
