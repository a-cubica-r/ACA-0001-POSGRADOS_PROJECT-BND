package ufps.edu.co.services.core.crud;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ufps.edu.co.maps.specific.MunicipioMap;
import ufps.edu.co.records.input.entity.MunicipioInput.*;
import ufps.edu.co.records.output.entity.MunicipioOutput;
import ufps.edu.co.rest.dto.MunicipioDTO;
import ufps.edu.co.rest.services.MunicipioService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class MunicipioCoreService implements
        GlobalUseCase<MUNICIPIO_CREATE, MUNICIPIO_UPDATE, MUNICIPIO_DELETE, MUNICIPIO_PATCH, MUNICIPIO_FIND, MunicipioOutput> {

    @Autowired
    private MunicipioMap map;

    @Autowired
    private MunicipioService service;

    @Override
    public MunicipioOutput create(MUNICIPIO_CREATE input) {
        try {
            MunicipioDTO dto = map.toDto(input);
            try {
                return map.toOutput(service.create(dto));
            } catch (Exception e) {
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public MunicipioOutput update(MUNICIPIO_UPDATE input) {
        try {
            MunicipioDTO dto = map.toDto(input);
            try {
                return map.toOutput(service.update(input.id(), dto));
            } catch (Exception e) {
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public MunicipioOutput patch(MUNICIPIO_PATCH input) {
        throw new UnsupportedOperationException("Unimplemented method 'patch'");
    }

    @Override
    public MunicipioOutput findById(MUNICIPIO_FIND id) {
        try {
            return map.toOutput(service.findById(id.id()));
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public List<MunicipioOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).toList();
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public void deleteById(MUNICIPIO_DELETE id) {
        try {
            service.deleteById(id.id());
        } catch (Exception e) {
        }
    }

}