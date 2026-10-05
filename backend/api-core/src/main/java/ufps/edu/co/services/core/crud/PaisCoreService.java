package ufps.edu.co.services.core.crud;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufps.edu.co.maps.specific.PaisMap;
import ufps.edu.co.records.input.entity.PaisInput.*;
import ufps.edu.co.records.output.entity.PaisOutput;
import ufps.edu.co.rest.dto.PaisDTO;
import ufps.edu.co.rest.services.PaisService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class PaisCoreService implements GlobalUseCase<PAIS_CREATE, PAIS_UPDATE, PAIS_DELETE, PAIS_PATCH, PAIS_FIND, PaisOutput> {

    @Autowired
    private PaisService service;

    @Autowired
    private PaisMap map;

    @Override
    public PaisOutput create(PAIS_CREATE input) {
        try {
            PaisDTO dto = map.toDto(input);
            try {
                PaisOutput output = map.toOutput(service.create(dto));
                return output;
            } catch (Exception e) {
                throw new RuntimeException("Error creating Pais: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating Pais: " + e.getMessage(), e);
        }
    }

    @Override
    public PaisOutput update(PAIS_UPDATE input) {
        try {
            PaisDTO dto = map.toDto(input);
            try {
                PaisOutput output = map.toOutput(service.update(dto.getId(), dto));
                return output;
            } catch (Exception e) {
                throw new RuntimeException("Error updating Pais: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error updating Pais: " + e.getMessage(), e);
        }
    }

    @Override
    public PaisOutput findById(PAIS_FIND input) {
        try {
            PaisOutput output = map.toOutput(service.findById(input.id()));
            return output;
        } catch (Exception e) {
            throw new RuntimeException("Error finding Pais by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PaisOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Paises: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(PAIS_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Pais by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public PaisOutput patch(PAIS_PATCH input) {
        throw new UnsupportedOperationException("Unimplemented method 'patch'");
    }
}
