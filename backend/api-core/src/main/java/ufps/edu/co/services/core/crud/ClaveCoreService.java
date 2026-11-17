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
        ClaveDTO dto = map.toDto(input);
        ClaveDTO created = service.create(dto);
        return map.toOutput(created);

    }

    @Override
    public ClaveOutput update(CLAVE_UPDATE input) {
        ClaveDTO dto = map.toDto(input);
        ClaveDTO updated = service.update(input.id(), dto);
        return map.toOutput(updated);

    }

    @Override
    public ClaveOutput patch(CLAVE_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Clave");
    }

    @Override
    public ClaveOutput findById(CLAVE_FIND input) {
        ClaveDTO dto = service.findById(input.id());
        return map.toOutput(dto);

    }

    @Override
    public List<ClaveOutput> findAll() {
        return service.findAll().stream().map(map::toOutput).toList();

    }

    @Override
    public void deleteById(CLAVE_DELETE input) {
        service.deleteById(input.id());
    }
}
