package ufps.edu.co.services.core.crud;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.CohorteErrorCode;
import ufps.edu.co.maps.specific.CohorteMap;
import ufps.edu.co.records.input.entity.CohorteInput.*;
import ufps.edu.co.records.output.entity.CohorteOutput;
import ufps.edu.co.rest.dto.CohorteDTO;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class CohorteCoreService implements GlobalUseCase<COHORTE_CREATE, COHORTE_UPDATE, COHORTE_DELETE, COHORTE_PATCH, COHORTE_FIND, CohorteOutput> {

    @Autowired
    private CohorteService service;

    @Autowired
    private CohorteMap map;

    @Override
    public CohorteOutput create(COHORTE_CREATE input) {
        try {
            CohorteDTO dto = map.toDto(input);
            return map.toOutput(service.create(dto));
        } catch (Exception e) {
            throw new DomainException(CohorteErrorCode.COHORTE_DUPLICADO, input);
        }
    }

    @Override
    public CohorteOutput update(COHORTE_UPDATE input) {
        try {
            CohorteDTO dto = map.toDto(input);
            return map.toOutput(service.update(input.id(), dto));
        } catch (Exception e) {
            throw new DomainException(CohorteErrorCode.COHORTE_NOT_FOUND, input.id());
        }
    }

    @Override
    public CohorteOutput findById(COHORTE_FIND input) {
        try {
            return map.toOutput(service.findById(input.id()));
        } catch (Exception e) {
            throw new DomainException(CohorteErrorCode.COHORTE_NOT_FOUND, input.id());
        }
    }

    @Override
    public List<CohorteOutput> findAll() {
        try {
            return service.findAll().stream()
                    .map(map::toOutput)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new DomainException(CohorteErrorCode.COHORTE_NOT_FOUND, "findAll");
        }
    }

    @Override
    public void deleteById(COHORTE_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new DomainException(CohorteErrorCode.COHORTE_NOT_FOUND, input.id());
        }
    }

    @Override
    public CohorteOutput patch(COHORTE_PATCH input) {
        return null;
    }

    public CohorteOutput createWithPlazo(COHORTE_WITHPLAZO_CREATE input) {
        throw new UnsupportedOperationException("No se puede crear una cohorte con plazo desde este endpoint");
    }

    public long countAspirantesEnProcesoEnCohorteAbierta(Integer cohorteId) {
        try {
            return service.countAspirantesEnProcesoEnCohorteAbierta(cohorteId);
        } catch (Exception e) {
            throw new DomainException(CohorteErrorCode.COHORTE_NOT_FOUND, cohorteId);
        }
    }

    public List<CohorteOutput> findActivasByIdPrograma(Integer idPrograma) {
        try {
            return service.findActivasByIdPrograma(idPrograma).stream()
                    .map(map::toOutput)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new DomainException(CohorteErrorCode.COHORTE_NOT_FOUND, idPrograma);
        }
    }

    public long countAspirantesEnProcesoByCohorteId(Integer cohorteId) {
        try {
            return service.countAspirantesEnProcesoByCohorteId(cohorteId);
        } catch (Exception e) {
            throw new DomainException(CohorteErrorCode.COHORTE_NOT_FOUND, cohorteId);
        }
    }

}
