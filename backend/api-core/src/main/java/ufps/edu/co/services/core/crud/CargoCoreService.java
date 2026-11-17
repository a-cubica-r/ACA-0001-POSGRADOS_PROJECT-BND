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
        CargoDTO dto = map.toDto(input);
        CargoDTO created = service.create(dto);
        return map.toOutput(created);

    }

    @Override
    public CargoOutput update(CARGO_UPDATE input) {
        CargoDTO dto = map.toDto(input);
        CargoDTO updated = service.update(input.id(), dto);
        return map.toOutput(updated);

    }

    @Override
    public CargoOutput patch(CARGO_PATCH input) {
        throw new UnsupportedOperationException("Patch not supported for Cargo");
    }

    @Override
    public CargoOutput findById(CARGO_FIND input) {
        return map.toOutput(service.findById(input.id()));

    }

    @Override
    public List<CargoOutput> findAll() {
        return service.findAll().stream().map(map::toOutput).toList();

    }

    @Override
    public void deleteById(CARGO_DELETE input) {
        service.deleteById(input.id());
    }
}
