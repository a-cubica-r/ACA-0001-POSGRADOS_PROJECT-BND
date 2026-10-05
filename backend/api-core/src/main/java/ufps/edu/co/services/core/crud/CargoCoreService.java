package ufps.edu.co.services.core.crud;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ufps.edu.co.maps.specific.CargoMap;
import ufps.edu.co.records.input.entity.CargoInput.*;
import ufps.edu.co.records.output.entity.CargoOutput;
import ufps.edu.co.rest.dto.CargoDTO;
import ufps.edu.co.rest.services.CargoService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class CargoCoreService implements GlobalUseCase<CARGO_CREATE, CARGO_UPDATE, CARGO_DELETE, CARGO_PATCH, CARGO_FIND, CargoOutput> {

    @Autowired
    private CargoService service;

    @Autowired
    private CargoMap map;

    @Override
    public CargoOutput create(CARGO_CREATE input) {
        try {
            CargoDTO dto = map.toDto(input);
            CargoDTO created = service.create(dto);
            return map.toOutput(created);
        } catch (Exception e) {
            throw new RuntimeException("Error creating Cargo: " + e.getMessage(), e);
        }
    }

    @Override
    public CargoOutput update(CARGO_UPDATE input) {
        try {
            CargoDTO dto = map.toDto(input);
            CargoDTO updated = service.update(input.id(), dto);
            return map.toOutput(updated);
        } catch (Exception e) {
            throw new RuntimeException("Error updating Cargo: " + e.getMessage(), e);
        }
    }

    @Override
    public CargoOutput patch(CARGO_PATCH input) {
        throw new UnsupportedOperationException("Patch not supported for Cargo");
    }

    @Override
    public CargoOutput findById(CARGO_FIND input) {
        try {
            return map.toOutput(service.findById(input.id()));
        } catch (Exception e) {
            throw new RuntimeException("Error finding Cargo: " + e.getMessage(), e);
        }
    }

    @Override
    public List<CargoOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Cargos: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(CARGO_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Cargo: " + e.getMessage(), e);
        }
    }
}
