package ufps.edu.co.services.core.crud;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ufps.edu.co.maps.specific.FacultadMap;
import ufps.edu.co.records.input.entity.FacultadInput.*;
import ufps.edu.co.records.output.entity.FacultadOutput;
import ufps.edu.co.rest.dto.FacultadDTO;
import ufps.edu.co.rest.services.FacultadService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class FacultadCoreService implements GlobalUseCase<FACULTAD_CREATE, FACULTAD_UPDATE, FACULTAD_DELETE, FACULTAD_PATCH, FACULTAD_FIND, FacultadOutput> {

    @Autowired
    private FacultadService service;

    @Autowired
    private FacultadMap map;

    @Override
    public FacultadOutput create(FACULTAD_CREATE input) {
        try {
            FacultadDTO dto = map.toDto(input);
            FacultadDTO created = service.create(dto);
            return map.toOutput(created);
        } catch (Exception e) {
            throw new RuntimeException("Error creating Facultad: " + e.getMessage(), e);
        }
    }

    @Override
    public FacultadOutput update(FACULTAD_UPDATE input) {
        try {
            FacultadDTO dto = map.toDto(input);
            FacultadDTO updated = service.update(input.id(), dto);
            return map.toOutput(updated);
        } catch (Exception e) {
            throw new RuntimeException("Error updating Facultad: " + e.getMessage(), e);
        }
    }

    @Override
    public FacultadOutput patch(FACULTAD_PATCH input) {
        throw new UnsupportedOperationException("Patch not supported for Facultad");
    }

    @Override
    public FacultadOutput findById(FACULTAD_FIND input) {
        try {
            return map.toOutput(service.findById(input.id()));
        } catch (Exception e) {
            throw new RuntimeException("Error finding Facultad: " + e.getMessage(), e);
        }
    }

    @Override
    public List<FacultadOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Facultades: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(FACULTAD_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Facultad: " + e.getMessage(), e);
        }
    }
}
