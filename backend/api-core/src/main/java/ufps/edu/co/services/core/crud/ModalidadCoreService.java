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
        try {
            ModalidadDTO dto = map.toDtoCreate(input);
            try {
                ModalidadDTO created = service.create(dto);
                return map.toOutput(created);
            } catch (Exception e) {
                throw new RuntimeException("Error creating Modalidad: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating Modalidad: " + e.getMessage(), e);
        }
    }

    @Override
    public ModalidadOutput update(MODALIDAD_UPDATE input) {
        try {
            ModalidadDTO dto = map.toDtoUpdate(input);
            try {
                ModalidadDTO updated = service.update(input.id(), dto);
                return map.toOutput(updated);
            } catch (Exception e) {
                throw new RuntimeException("Error updating Modalidad: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error updating Modalidad: " + e.getMessage(), e);
        }
    }

    @Override
    public ModalidadOutput findById(MODALIDAD_FIND input) {
        try {
            ModalidadDTO dto = service.findById(input.id());
            return map.toOutput(dto);
        } catch (Exception e) {
            throw new RuntimeException("Error finding Modalidad by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ModalidadOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Modalidades: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(MODALIDAD_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Modalidad by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public ModalidadOutput patch(MODALIDAD_PATCH input) {
        throw new UnsupportedOperationException("Unimplemented method 'patch'");
    }
}
