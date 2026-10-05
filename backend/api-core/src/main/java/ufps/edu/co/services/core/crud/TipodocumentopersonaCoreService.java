package ufps.edu.co.services.core.crud;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufps.edu.co.maps.specific.TipodocumentopersonaMap;
import ufps.edu.co.records.input.entity.TipodocumentopersonaInput.*;
import ufps.edu.co.records.output.entity.TipodocumentopersonaOutput;
import ufps.edu.co.rest.dto.TipodocumentopersonaDTO;
import ufps.edu.co.rest.services.TipodocumentopersonaService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class TipodocumentopersonaCoreService implements
        GlobalUseCase<TIPODOCUMENTOPERSONA_CREATE, TIPODOCUMENTOPERSONA_UPDATE, TIPODOCUMENTOPERSONA_DELETE, TIPODOCUMENTOPERSONA_PATCH, TIPODOCUMENTOPERSONA_FIND, TipodocumentopersonaOutput> {

    @Autowired
    private TipodocumentopersonaService service;

    @Autowired
    private TipodocumentopersonaMap map;

    @Override
    public TipodocumentopersonaOutput create(TIPODOCUMENTOPERSONA_CREATE input) {
        try {
            TipodocumentopersonaDTO dto = map.toDto(input);
            TipodocumentopersonaDTO created = service.create(dto);
            return map.toOutput(created);
        } catch (Exception e) {
            throw new RuntimeException("Error creating Tipodocumentopersona: " + e.getMessage(), e);
        }
    }

    @Override
    public TipodocumentopersonaOutput update(TIPODOCUMENTOPERSONA_UPDATE input) {
        try {
            TipodocumentopersonaDTO dto = map.toDto(input);
            TipodocumentopersonaDTO updated = service.update(input.id(), dto);
            return map.toOutput(updated);
        } catch (Exception e) {
            throw new RuntimeException("Error updating Tipodocumentopersona: " + e.getMessage(), e);
        }
    }

    @Override
    public TipodocumentopersonaOutput patch(TIPODOCUMENTOPERSONA_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Tipodocumentopersona");
    }

    @Override
    public TipodocumentopersonaOutput findById(TIPODOCUMENTOPERSONA_FIND input) {
        try {
            TipodocumentopersonaDTO dto = service.findById(input.id());
            return map.toOutput(dto);
        } catch (Exception e) {
            throw new RuntimeException("Error finding Tipodocumentopersona by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<TipodocumentopersonaOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Tipodocumentopersonas: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(TIPODOCUMENTOPERSONA_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Tipodocumentopersona by ID: " + e.getMessage(), e);
        }
    }
}
