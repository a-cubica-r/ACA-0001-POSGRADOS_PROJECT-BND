package ufps.edu.co.services.core.crud;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufps.edu.co.maps.specific.TipoplazoMap;
import ufps.edu.co.records.input.entity.TipoplazoInput.*;
import ufps.edu.co.records.output.entity.TipoplazoOutput;
import ufps.edu.co.rest.dto.TipoplazoDTO;
import ufps.edu.co.rest.services.TipoplazoService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class TipoplazoCoreService implements
        GlobalUseCase<TIPOPLAZO_CREATE, TIPOPLAZO_UPDATE, TIPOPLAZO_DELETE, TIPOPLAZO_PATCH, TIPOPLAZO_FIND, TipoplazoOutput> {

    @Autowired
    private TipoplazoService service;

    @Autowired
    private TipoplazoMap map;

    @Override
    public TipoplazoOutput create(TIPOPLAZO_CREATE input) {
        TipoplazoDTO dto = map.toDto(input);
        TipoplazoDTO created = service.create(dto);
        return map.toOutput(created);

    }

    @Override
    public TipoplazoOutput update(TIPOPLAZO_UPDATE input) {
        TipoplazoDTO dto = map.toDto(input);
        TipoplazoDTO updated = service.update(input.id(), dto);
        return map.toOutput(updated);

    }

    @Override
    public TipoplazoOutput patch(TIPOPLAZO_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Tipoplazo");
    }

    @Override
    public TipoplazoOutput findById(TIPOPLAZO_FIND input) {
        TipoplazoDTO dto = service.findById(input.id());
        return map.toOutput(dto);

    }

    @Override
    public List<TipoplazoOutput> findAll() {
        return service.findAll().stream().map(map::toOutput).toList();

    }

    @Override
    public void deleteById(TIPOPLAZO_DELETE input) {
        service.deleteById(input.id());
    }
}
