package ufps.edu.co.services.core.crud;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ufps.edu.co.maps.specific.SemestreMap;
import ufps.edu.co.records.input.entity.SemestreInput.*;
import ufps.edu.co.records.output.entity.SemestreOutput;
import ufps.edu.co.rest.dto.SemestreDTO;
import ufps.edu.co.rest.services.SemestreService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class SemestreCoreService implements
        GlobalUseCase<SEMESTRE_CREATE, SEMESTRE_UPDATE, SEMESTRE_DELETE, SEMESTRE_PATCH, SEMESTRE_FIND, SemestreOutput> {

    @Autowired
    private SemestreService service;

    @Autowired
    private SemestreMap map;

    @Override
    public SemestreOutput create(SEMESTRE_CREATE input) {
        try {
            SemestreDTO dto = map.toDto(input);
            SemestreDTO created = service.create(dto);
            return map.toOutput(created);
        } catch (Exception e) {
            throw new RuntimeException("Error creating Semestre: " + e.getMessage(), e);
        }
    }

    @Override
    public SemestreOutput update(SEMESTRE_UPDATE input) {
        try {
            SemestreDTO dto = map.toDto(input);
            SemestreDTO updated = service.update(input.id(), dto);
            return map.toOutput(updated);
        } catch (Exception e) {
            throw new RuntimeException("Error updating Semestre: " + e.getMessage(), e);
        }
    }

    @Override
    public SemestreOutput patch(SEMESTRE_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Semestre");
    }

    @Override
    public SemestreOutput findById(SEMESTRE_FIND input) {
        try {
            SemestreDTO dto = service.findById(input.id());
            return map.toOutput(dto);
        } catch (Exception e) {
            throw new RuntimeException("Error finding Semestre by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SemestreOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Semestres: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(SEMESTRE_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Semestre by ID: " + e.getMessage(), e);
        }
    }
}

