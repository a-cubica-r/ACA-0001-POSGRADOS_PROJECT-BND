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
        RolDTO dto = map.toDto(input);
        RolDTO created = service.create(dto);
        return map.toOutput(created);

    }

    @Override
    public RolOutput update(ROL_UPDATE input) {
        RolDTO dto = map.toDto(input);
        RolDTO updated = service.update(input.id(), dto);
        return map.toOutput(updated);

    }

    @Override
    public RolOutput patch(ROL_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Rol");
    }

    @Override
    public RolOutput findById(ROL_FIND input) {
        RolDTO dto = service.findById(input.id());
        return map.toOutput(dto);

    }

    @Override
    public List<RolOutput> findAll() {
        return service.findAll().stream().map(map::toOutput).toList();

    }

    @Override
    public void deleteById(ROL_DELETE input) {
        service.deleteById(input.id());
    }

    public RolOutput findByNombre(String nombre) {
        RolDTO dto = service.findByNombre(nombre);
        return map.toOutput(dto);

    }
}
