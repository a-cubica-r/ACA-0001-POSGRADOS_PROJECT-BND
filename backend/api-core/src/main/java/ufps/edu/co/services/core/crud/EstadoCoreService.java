package ufps.edu.co.services.core.crud;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ufps.edu.co.maps.specific.EstadoMap;
import ufps.edu.co.records.input.entity.EstadoInput.*;
import ufps.edu.co.records.output.entity.EstadoOutput;
import ufps.edu.co.rest.dto.EstadoDTO;
import ufps.edu.co.rest.services.EstadoService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class EstadoCoreService
        implements GlobalUseCase<ESTADO_CREATE, ESTADO_UPDATE, ESTADO_DELETE, ESTADO_PATCH, ESTADO_FIND, EstadoOutput> {

    @Autowired
    private EstadoService service;

    @Autowired
    private EstadoMap map;

    @Override
    public EstadoOutput create(ESTADO_CREATE input) {
        EstadoDTO dto = map.toDto(input);
        EstadoDTO created = service.create(dto);
        return map.toOutput(created);


    }

    @Override
    public EstadoOutput update(ESTADO_UPDATE input) {
        EstadoDTO dto = map.toDto(input);
        EstadoDTO updated = service.update(input.id(), dto);
        return map.toOutput(updated);


    }

    @Override
    public EstadoOutput findById(ESTADO_FIND input) {
        EstadoDTO dto = service.findById(input.id());
        return map.toOutput(dto);

    }

    @Override
    public List<EstadoOutput> findAll() {
        return service.findAll().stream().map(map::toOutput).collect(Collectors.toList());

    }

    @Override
    public void deleteById(ESTADO_DELETE input) {
        service.deleteById(input.id());
    }

    @Override
    public EstadoOutput patch(ESTADO_PATCH input) {
        throw new UnsupportedOperationException("Unimplemented method 'patch'");
    }
}
