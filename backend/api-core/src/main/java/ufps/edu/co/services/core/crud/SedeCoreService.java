package ufps.edu.co.services.core.crud;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.SedeErrorCode;
import ufps.edu.co.maps.specific.SedeMap;
import ufps.edu.co.records.input.entity.SedeInput.*;
import ufps.edu.co.records.input.entity.UbicacionInput.UBICACION_CREATE;
import ufps.edu.co.records.output.entity.SedeOutput;
import ufps.edu.co.records.output.entity.UbicacionOutput;
import ufps.edu.co.rest.dto.SedeDTO;
import ufps.edu.co.rest.services.SedeService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class SedeCoreService
        implements GlobalUseCase<SEDE_CREATE, SEDE_UPDATE, SEDE_DELETE, SEDE_PATCH, SEDE_FIND, SedeOutput> {

    @Autowired
    private SedeService service;

    @Autowired
    private SedeMap map;

    @Autowired
    private UbicacionCoreService ubicacionProcessor;

    @Override
    public SedeOutput create(SEDE_CREATE input) {
        try {
            SedeDTO dto = map.toDto(input);
            return map.toOutput(service.create(dto));
        } catch (Exception e) {
            throw new DomainException(SedeErrorCode.SEDE_DUPLICADA, input);
        }
    }

    public SedeOutput createWithUbicacion(SEDE_CREATE_WITH_UBICACION input) {
        try {
            UBICACION_CREATE ubiCreate = new UBICACION_CREATE(input.ubicacion());
            UbicacionOutput ubicacionOutput = ubicacionProcessor.create(ubiCreate);
            SedeDTO dto = map.toDto(new SEDE_CREATE(input.nombre(), ubicacionOutput.id()));
            return map.toOutput(service.create(dto));
        } catch (Exception e) {
            throw new DomainException(SedeErrorCode.SEDE_DUPLICADA, input);
        }
    }

    @Override
    public SedeOutput update(SEDE_UPDATE input) {
        try {
            SedeDTO dto = map.toDto(input);
            return map.toOutput(service.update(input.id(), dto));
        } catch (Exception e) {
            throw new DomainException(SedeErrorCode.SEDE_NOT_FOUND, input.id());
        }
    }

    @Override
    public SedeOutput findById(SEDE_FIND input) {
        try {
            return map.toOutput(service.findById(input.id()));
        } catch (Exception e) {
            throw new DomainException(SedeErrorCode.SEDE_NOT_FOUND, input.id());
        }
    }

    @Override
    public List<SedeOutput> findAll() {
        try {
            return service.findAll().stream()
                    .map(map::toOutput)
                    .toList();
        } catch (Exception e) {
            throw new DomainException(SedeErrorCode.SEDE_NOT_FOUND, "findAll");
        }
    }

    @Override
    public void deleteById(SEDE_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new DomainException(SedeErrorCode.SEDE_NOT_FOUND, input.id());
        }
    }

    @Override
    public SedeOutput patch(SEDE_PATCH input) {
        return null;
    }
}
