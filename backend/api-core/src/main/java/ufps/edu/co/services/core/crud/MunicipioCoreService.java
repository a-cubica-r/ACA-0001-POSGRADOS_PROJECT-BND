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
        MunicipioDTO dto = map.toDto(input);
        return map.toOutput(service.create(dto));
    }

    @Override
    public MunicipioOutput update(MUNICIPIO_UPDATE input) {
        MunicipioDTO dto = map.toDto(input);
        return map.toOutput(service.update(input.id(), dto));
    }

    @Override
    public MunicipioOutput patch(MUNICIPIO_PATCH input) {
        throw new UnsupportedOperationException("Unimplemented method 'patch'");
    }

    @Override
    public MunicipioOutput findById(MUNICIPIO_FIND id) {
        return map.toOutput(service.findById(id.id()));
    }

    @Override
    public List<MunicipioOutput> findAll() {
        return service.findAll().stream().map(map::toOutput).toList();
    }

    @Override
    public void deleteById(MUNICIPIO_DELETE id) {
        service.deleteById(id.id());
    }

}
