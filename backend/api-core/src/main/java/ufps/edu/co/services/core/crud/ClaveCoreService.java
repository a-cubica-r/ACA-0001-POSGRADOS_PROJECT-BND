package ufps.edu.co.services.core.crud;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufps.edu.co.maps.specific.ClaveMap;
import ufps.edu.co.records.input.entity.ClaveInput.*;
import ufps.edu.co.records.output.entity.ClaveOutput;
import ufps.edu.co.rest.dto.ClaveDTO;
import ufps.edu.co.rest.services.ClaveService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class ClaveCoreService implements
        GlobalUseCase<CLAVE_CREATE, CLAVE_UPDATE, CLAVE_DELETE, CLAVE_PATCH, CLAVE_FIND, ClaveOutput> {

    @Autowired
    private ClaveService service;

    @Autowired
    private ClaveMap map;

    @Override
    public ClaveOutput create(CLAVE_CREATE input) {
        try {
            ClaveDTO dto = map.toDto(input);
            ClaveDTO created = service.create(dto);
            return map.toOutput(created);
        } catch (Exception e) {
            throw new RuntimeException("Error creating Clave: " + e.getMessage(), e);
        }
    }

    @Override
    public ClaveOutput update(CLAVE_UPDATE input) {
        try {
            ClaveDTO dto = map.toDto(input);
            ClaveDTO updated = service.update(input.id(), dto);
            return map.toOutput(updated);
        } catch (Exception e) {
            throw new RuntimeException("Error updating Clave: " + e.getMessage(), e);
        }
    }

    @Override
    public ClaveOutput patch(CLAVE_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Clave");
    }

    @Override
    public ClaveOutput findById(CLAVE_FIND input) {
        try {
            ClaveDTO dto = service.findById(input.id());
            return map.toOutput(dto);
        } catch (Exception e) {
            throw new RuntimeException("Error finding Clave by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ClaveOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Claves: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(CLAVE_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Clave by ID: " + e.getMessage(), e);
        }
    }
}
