package ufps.edu.co.services.core.crud;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufps.edu.co.maps.specific.PlazoMap;
import ufps.edu.co.records.input.entity.PlazoInput.*;
import ufps.edu.co.records.output.entity.PlazoOutput;
import ufps.edu.co.rest.dto.PlazoDTO;
import ufps.edu.co.rest.services.PlazoService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class PlazoCoreService implements
        GlobalUseCase<PLAZO_CREATE, PLAZO_UPDATE, PLAZO_DELETE, PLAZO_PATCH, PLAZO_FIND, PlazoOutput> {

    @Autowired
    private PlazoService service;

    @Autowired
    private PlazoMap map;

    @Override
    public PlazoOutput create(PLAZO_CREATE input) {
        try {
            PlazoDTO dto = map.toDto(input);
            PlazoDTO created = service.create(dto);
            return map.toOutput(created);
        } catch (Exception e) {
            throw new RuntimeException("Error creating Plazo: " + e.getMessage(), e);
        }
    }

    @Override
    public PlazoOutput update(PLAZO_UPDATE input) {
        try {
            PlazoDTO dto = map.toDto(input);
            PlazoDTO updated = service.update(input.id(), dto);
            return map.toOutput(updated);
        } catch (Exception e) {
            throw new RuntimeException("Error updating Plazo: " + e.getMessage(), e);
        }
    }

    @Override
    public PlazoOutput patch(PLAZO_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Plazo");
    }

    @Override
    public PlazoOutput findById(PLAZO_FIND input) {
        try {
            PlazoDTO dto = service.findById(input.id());
            return map.toOutput(dto);
        } catch (Exception e) {
            throw new RuntimeException("Error finding Plazo by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PlazoOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Plazos: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(PLAZO_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Plazo by ID: " + e.getMessage(), e);
        }
    }
}
