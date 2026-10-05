package ufps.edu.co.services.core.crud;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.OtrosvaloresErrorCode;
import ufps.edu.co.maps.specific.OtrosvaloresMap;
import ufps.edu.co.records.input.entity.OtrosvaloresInput.*;
import ufps.edu.co.records.output.entity.OtrosvaloresOutput;
import ufps.edu.co.rest.dto.OtrosvaloresDTO;
import ufps.edu.co.rest.services.OtrosvaloresService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class OtrosvaloresCoreService implements
        GlobalUseCase<OTROSVALORES_CREATE, OTROSVALORES_UPDATE, OTROSVALORES_DELETE, OTROSVALORES_PATCH, OTROSVALORES_FIND, OtrosvaloresOutput> {

    @Autowired
    private OtrosvaloresService service;

    @Autowired
    private OtrosvaloresMap map;

    @Override
    public OtrosvaloresOutput create(OTROSVALORES_CREATE input) {
        try {
            OtrosvaloresDTO dto = map.toDto(input);
            return map.toOutput(service.create(dto));
        } catch (Exception e) {
            throw new DomainException(OtrosvaloresErrorCode.OTROSVALORES_DUPLICADO, input);
        }
    }

    @Override
    public OtrosvaloresOutput update(OTROSVALORES_UPDATE input) {
        try {
            OtrosvaloresDTO dto = map.toDto(input);
            return map.toOutput(service.update(dto.getId(), dto));
        } catch (Exception e) {
            throw new DomainException(OtrosvaloresErrorCode.OTROSVALORES_NOT_FOUND, input.id());
        }
    }

    @Override
    public OtrosvaloresOutput findById(OTROSVALORES_FIND input) {
        try {
            return map.toOutput(service.findById(input.id()));
        } catch (Exception e) {
            throw new DomainException(OtrosvaloresErrorCode.OTROSVALORES_NOT_FOUND, input.id());
        }
    }

    @Override
    public List<OtrosvaloresOutput> findAll() {
        try {
            return service.findAll().stream()
                    .map(map::toOutput)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new DomainException(OtrosvaloresErrorCode.OTROSVALORES_NOT_FOUND, "findAll");
        }
    }

    @Override
    public void deleteById(OTROSVALORES_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new DomainException(OtrosvaloresErrorCode.OTROSVALORES_NOT_FOUND, input.id());
        }
    }

    @Override
    public OtrosvaloresOutput patch(OTROSVALORES_PATCH input) {
        throw new UnsupportedOperationException("Unimplemented method 'patch'");
    }
}