package ufps.edu.co.services.core.crud;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufps.edu.co.maps.specific.ModalidadMap;
import ufps.edu.co.records.input.entity.ModalidadInput.*;
import ufps.edu.co.records.output.entity.ModalidadOutput;
import ufps.edu.co.rest.dto.ModalidadDTO;
import ufps.edu.co.rest.services.ModalidadService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class ModalidadCoreService implements GlobalUseCase<MODALIDAD_CREATE, MODALIDAD_UPDATE, MODALIDAD_DELETE, MODALIDAD_PATCH, MODALIDAD_FIND, ModalidadOutput> {

    @Autowired
    private ModalidadService service;

    @Autowired
    private ModalidadMap map;

    @Override
    public ModalidadOutput create(MODALIDAD_CREATE input) {
        ModalidadDTO dto = map.toDtoCreate(input);
        ModalidadDTO created = service.create(dto);
        return map.toOutput(created);


    }

    @Override
    public ModalidadOutput update(MODALIDAD_UPDATE input) {
        ModalidadDTO dto = map.toDtoUpdate(input);
        ModalidadDTO updated = service.update(input.id(), dto);
        return map.toOutput(updated);


    }

    @Override
    public ModalidadOutput findById(MODALIDAD_FIND input) {
        ModalidadDTO dto = service.findById(input.id());
        return map.toOutput(dto);

    }

    @Override
    public List<ModalidadOutput> findAll() {
        return service.findAll().stream().map(map::toOutput).collect(Collectors.toList());

    }

    @Override
    public void deleteById(MODALIDAD_DELETE input) {
        service.deleteById(input.id());
    }

    @Override
    public ModalidadOutput patch(MODALIDAD_PATCH input) {
        throw new UnsupportedOperationException("Unimplemented method 'patch'");
    }
}
