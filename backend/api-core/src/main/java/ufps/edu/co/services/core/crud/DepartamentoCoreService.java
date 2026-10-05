package ufps.edu.co.services.core.crud;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufps.edu.co.maps.specific.DepartamentoMap;
import ufps.edu.co.records.input.entity.DepartamentoInput.*;
import ufps.edu.co.records.output.entity.DepartamentoOutput;
import ufps.edu.co.rest.dto.DepartamentoDTO;
import ufps.edu.co.rest.services.DepartamentoService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class DepartamentoCoreService
        implements GlobalUseCase<DEPARTAMENTO_CREATE, DEPARTAMENTO_UPDATE, DEPARTAMENTO_DELETE, DEPARTAMENTO_PATCH, DEPARTAMENTO_FIND, DepartamentoOutput> {

    @Autowired
    private DepartamentoService service;

    @Autowired
    private DepartamentoMap map;

    @Override
    public DepartamentoOutput create(DEPARTAMENTO_CREATE input) {
        try {
            DepartamentoDTO dto = map.toDto(input);
            try {
                DepartamentoOutput output = map.toOutput(service.create(dto));
                return output;
            } catch (Exception e) {
                throw new RuntimeException("Error creating Departamento: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating Departamento: " + e.getMessage(), e);
        }
    }

    @Override
    public DepartamentoOutput update(DEPARTAMENTO_UPDATE input) {
        try {
            DepartamentoDTO dto = map.toDto(input);
            try {
                DepartamentoOutput output = map.toOutput(service.update(dto.getId(), dto));
                return output;
            } catch (Exception e) {
                throw new RuntimeException("Error updating Departamento: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error updating Departamento: " + e.getMessage(), e);
        }
    }

    @Override
    public DepartamentoOutput findById(DEPARTAMENTO_FIND input) {
        try {
            DepartamentoDTO dto = map.toDto(input);
            try {
                DepartamentoOutput output = map.toOutput(service.findById(dto.getId()));
                return output;
            } catch (Exception e) {
                throw new RuntimeException("Error finding Departamento: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error finding Departamento: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DepartamentoOutput> findAll() {
        try {
            List<DepartamentoDTO> dtoList = service.findAll();
            List<DepartamentoOutput> outputList = map.toOutputList(dtoList);
            return outputList;
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Departamentos: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(DEPARTAMENTO_DELETE input) {
        try {
            DepartamentoDTO dto = map.toDto(input);
            try {
                service.deleteById(dto.getId());
            } catch (Exception e) {
                throw new RuntimeException("Error deleting Departamento: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Departamento: " + e.getMessage(), e);
        }
    }

    @Override
    public DepartamentoOutput patch(DEPARTAMENTO_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Departamento");
    }
}
