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
        try {
            EstadoDTO dto = map.toDto(input);
            try {
                EstadoDTO created = service.create(dto);
                return map.toOutput(created);
            } catch (Exception e) {
                throw new RuntimeException("Error creating Estado: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating Estado: " + e.getMessage(), e);
        }
    }

    @Override
    public EstadoOutput update(ESTADO_UPDATE input) {
        try {
            EstadoDTO dto = map.toDto(input);
            try {
                EstadoDTO updated = service.update(input.id(), dto);
                return map.toOutput(updated);
            } catch (Exception e) {
                throw new RuntimeException("Error updating Estado: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error updating Estado: " + e.getMessage(), e);
        }
    }

    @Override
    public EstadoOutput findById(ESTADO_FIND input) {
        try {
            EstadoDTO dto = service.findById(input.id());
            return map.toOutput(dto);
        } catch (Exception e) {
            throw new RuntimeException("Error finding Estado by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<EstadoOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Estados: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(ESTADO_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Estado by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public EstadoOutput patch(ESTADO_PATCH input) {
        throw new UnsupportedOperationException("Unimplemented method 'patch'");
    }
}
