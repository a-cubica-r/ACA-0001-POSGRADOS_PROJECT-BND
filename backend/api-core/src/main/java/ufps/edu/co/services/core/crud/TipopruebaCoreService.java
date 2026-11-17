package ufps.edu.co.services.core.crud;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufps.edu.co.maps.specific.TipopruebaMap;
import ufps.edu.co.records.input.entity.TipopruebaInput.*;
import ufps.edu.co.records.output.entity.TipopruebaOutput;
import ufps.edu.co.rest.dto.TipopruebaDTO;
import ufps.edu.co.rest.services.TipopruebaService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class TipopruebaCoreService implements
        GlobalUseCase<TIPOPRUEBA_CREATE, TIPOPRUEBA_UPDATE, TIPOPRUEBA_DELETE, TIPOPRUEBA_PATCH, TIPOPRUEBA_FIND, TipopruebaOutput> {

    @Autowired
    private TipopruebaService service;

    @Autowired
    private TipopruebaMap map;

    @Override
    public TipopruebaOutput create(TIPOPRUEBA_CREATE input) {
        TipopruebaDTO dto = map.toDto(input);
        TipopruebaDTO created = service.create(dto);
        return map.toOutput(created);

    }

    @Override
    public TipopruebaOutput update(TIPOPRUEBA_UPDATE input) {
        TipopruebaDTO dto = map.toDto(input);
        TipopruebaDTO updated = service.update(input.id(), dto);
        return map.toOutput(updated);

    }

    @Override
    public TipopruebaOutput patch(TIPOPRUEBA_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Tipoprueba");
    }

    @Override
    public TipopruebaOutput findById(TIPOPRUEBA_FIND input) {
        TipopruebaDTO dto = service.findById(input.id());
        return map.toOutput(dto);

    }

    @Override
    public List<TipopruebaOutput> findAll() {
        return service.findAll().stream().map(map::toOutput).toList();

    }

    @Override
    public void deleteById(TIPOPRUEBA_DELETE input) {
        service.deleteById(input.id());
    }
}
