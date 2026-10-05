package ufps.edu.co.services.core.crud;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufps.edu.co.maps.specific.RolMap;
import ufps.edu.co.records.input.entity.RolInput.*;
import ufps.edu.co.records.output.entity.RolOutput;
import ufps.edu.co.rest.dto.RolDTO;
import ufps.edu.co.rest.services.RolService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class RolCoreService implements
        GlobalUseCase<ROL_CREATE, ROL_UPDATE, ROL_DELETE, ROL_PATCH, ROL_FIND, RolOutput> {

    @Autowired
    private RolService service;

    @Autowired
    private RolMap map;

    @Override
    public RolOutput create(ROL_CREATE input) {
        try {
            RolDTO dto = map.toDto(input);
            RolDTO created = service.create(dto);
            return map.toOutput(created);
        } catch (Exception e) {
            throw new RuntimeException("Error creating Rol: " + e.getMessage(), e);
        }
    }

    @Override
    public RolOutput update(ROL_UPDATE input) {
        try {
            RolDTO dto = map.toDto(input);
            RolDTO updated = service.update(input.id(), dto);
            return map.toOutput(updated);
        } catch (Exception e) {
            throw new RuntimeException("Error updating Rol: " + e.getMessage(), e);
        }
    }

    @Override
    public RolOutput patch(ROL_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Rol");
    }

    @Override
    public RolOutput findById(ROL_FIND input) {
        try {
            RolDTO dto = service.findById(input.id());
            return map.toOutput(dto);
        } catch (Exception e) {
            throw new RuntimeException("Error finding Rol by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<RolOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Roles: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(ROL_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Rol by ID: " + e.getMessage(), e);
        }
    }

    public RolOutput findByNombre(String nombre) {
        try {
            RolDTO dto = service.findByNombre(nombre);
            return map.toOutput(dto);
        } catch (Exception e) {
            throw new RuntimeException("Error finding Rol by nombre: " + e.getMessage(), e);
        }
    }
}
