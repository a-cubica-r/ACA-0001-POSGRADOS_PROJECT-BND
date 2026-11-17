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
        UbicacionDTO dto = map.toDto(input);
        UbicacionDTO created = service.create(dto);
        return map.toOutput(created);


    }

    @Override
    public UbicacionOutput update(UBICACION_UPDATE input) {
        UbicacionDTO dto = map.toDto(input);
        UbicacionDTO updated = service.update(input.id(), dto);
        return map.toOutput(updated);


    }

    @Override
    public UbicacionOutput findById(UBICACION_FIND input) {
        UbicacionDTO dto = service.findById(input.id());
        return map.toOutput(dto);

    }

    @Override
    public UbicacionOutput patch(UBICACION_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Ubicacion");
    }

    @Override
    public List<UbicacionOutput> findAll() {
        return service.findAll().stream().map(map::toOutput).toList();

    }

    @Override
    public void deleteById(UBICACION_DELETE id) {
        service.deleteById(id.id());
    }

}
