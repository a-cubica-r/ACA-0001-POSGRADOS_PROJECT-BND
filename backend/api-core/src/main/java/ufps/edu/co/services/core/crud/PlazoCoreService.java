package ufps.edu.co.services.core.crud;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufps.edu.co.maps.specific.PlazoMap;
import ufps.edu.co.records.input.entity.PlazoInput.*;
import ufps.edu.co.records.output.entity.PlazoOutput;
import ufps.edu.co.rest.dto.PlazoDTO;
import ufps.edu.co.rest.services.PlazoService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class PlazoCoreService implements
        GlobalUseCase<PLAZO_CREATE, PLAZO_UPDATE, PLAZO_DELETE, PLAZO_PATCH, PLAZO_FIND, PlazoOutput> {

    @Autowired
    private PlazoService service;

    @Autowired
    private PlazoMap map;

    @Override
    public PlazoOutput create(PLAZO_CREATE input) {
        PlazoDTO dto = map.toDto(input);
        PlazoDTO created = service.create(dto);
        return map.toOutput(created);

    }

    @Override
    public PlazoOutput update(PLAZO_UPDATE input) {
        PlazoDTO dto = map.toDto(input);
        PlazoDTO updated = service.update(input.id(), dto);
        return map.toOutput(updated);

    }

    @Override
    public PlazoOutput patch(PLAZO_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Plazo");
    }

    @Override
    public PlazoOutput findById(PLAZO_FIND input) {
        PlazoDTO dto = service.findById(input.id());
        return map.toOutput(dto);

    }

    @Override
    public List<PlazoOutput> findAll() {
        return service.findAll().stream().map(map::toOutput).toList();

    }

    @Override
    public void deleteById(PLAZO_DELETE input) {
        service.deleteById(input.id());
    }
}
