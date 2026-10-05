package ufps.edu.co.services.core.crud;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.GeneroErrorCode;
import ufps.edu.co.maps.specific.GeneroMap;
import ufps.edu.co.records.input.entity.GeneroInput.*;
import ufps.edu.co.records.output.entity.GeneroOutput;
import ufps.edu.co.rest.dto.GeneroDTO;
import ufps.edu.co.rest.services.GeneroService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class GeneroCoreService implements GlobalUseCase<GENERO_CREATE, GENERO_UPDATE, GENERO_DELETE, GENERO_PATCH, GENERO_FIND, GeneroOutput> {

    @Autowired
    private GeneroService service;

    @Autowired
    private GeneroMap map;

    @Override
    public GeneroOutput create(GENERO_CREATE input) {
        try {
            GeneroDTO dto = map.toDto(input);
            return map.toOutput(service.create(dto));
        } catch (Exception e) {
            throw new DomainException(GeneroErrorCode.GENERO_DUPLICADO, input);
        }
    }

    @Override
    public GeneroOutput update(GENERO_UPDATE input) {
        try {
            GeneroDTO dto = map.toDto(input);
            return map.toOutput(service.update(input.id(), dto));
        } catch (Exception e) {
            throw new DomainException(GeneroErrorCode.GENERO_NOT_FOUND, input.id());
        }
    }

    @Override
    public GeneroOutput findById(GENERO_FIND input) {
        try {
            return map.toOutput(service.findById(input.id()));
        } catch (Exception e) {
            throw new DomainException(GeneroErrorCode.GENERO_NOT_FOUND, input.id());
        }
    }

    @Override
    public List<GeneroOutput> findAll() {
        try {
            return service.findAll().stream()
                    .map(map::toOutput)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new DomainException(GeneroErrorCode.GENERO_NOT_FOUND, "findAll");
        }
    }

    @Override
    public void deleteById(GENERO_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new DomainException(GeneroErrorCode.GENERO_NOT_FOUND, input.id());
        }
    }

    @Override
    public GeneroOutput patch(GENERO_PATCH input) {
        return null;
    }
}