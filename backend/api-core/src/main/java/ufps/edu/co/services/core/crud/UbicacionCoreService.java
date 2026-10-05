package ufps.edu.co.services.core.crud;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ufps.edu.co.maps.specific.UbicacionMap;
import ufps.edu.co.records.input.entity.UbicacionInput.*;
import ufps.edu.co.records.output.entity.UbicacionOutput;
import ufps.edu.co.rest.dto.UbicacionDTO;
import ufps.edu.co.rest.services.UbicacionService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class UbicacionCoreService implements
        GlobalUseCase<UBICACION_CREATE, UBICACION_UPDATE, UBICACION_DELETE, UBICACION_PATCH, UBICACION_FIND, UbicacionOutput> {

    @Autowired
    private UbicacionService service;

    @Autowired
    private UbicacionMap map;

    @Override
    public UbicacionOutput create(UBICACION_CREATE input) {
        try {
            UbicacionDTO dto = map.toDto(input);
            try {
                UbicacionDTO created = service.create(dto);
                return map.toOutput(created);
            } catch (Exception e) {
                throw new RuntimeException("Error creating Ubicacion: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating Ubicacion: " + e.getMessage(), e);
        }
    }

    @Override
    public UbicacionOutput update(UBICACION_UPDATE input) {
        try {
            UbicacionDTO dto = map.toDto(input);
            try {
                UbicacionDTO updated = service.update(input.id(), dto);
                return map.toOutput(updated);
            } catch (Exception e) {
                throw new RuntimeException("Error updating Ubicacion: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error updating Ubicacion: " + e.getMessage(), e);
        }
    }

    @Override
    public UbicacionOutput findById(UBICACION_FIND input) {
        try {
            UbicacionDTO dto = service.findById(input.id());
            return map.toOutput(dto);
        } catch (Exception e) {
            throw new RuntimeException("Error finding Ubicacion by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public UbicacionOutput patch(UBICACION_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Ubicacion");
    }

    @Override
    public List<UbicacionOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Ubicaciones: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(UBICACION_DELETE id) {
        try {
            service.deleteById(id.id());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Ubicacion by ID: " + e.getMessage(), e);
        }
    }

}
