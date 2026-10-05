package ufps.edu.co.services.core.crud;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ufps.edu.co.maps.specific.PersonaMap;
import ufps.edu.co.records.input.entity.PersonaInput.PERSONA_CREATE;
import ufps.edu.co.records.input.entity.PersonaInput.PERSONA_DELETE;
import ufps.edu.co.records.input.entity.PersonaInput.PERSONA_FIND;
import ufps.edu.co.records.input.entity.PersonaInput.PERSONA_PATCH;
import ufps.edu.co.records.input.entity.PersonaInput.PERSONA_UPDATE;
import ufps.edu.co.records.output.entity.PersonaOutput;
import ufps.edu.co.rest.dto.PersonaDTO;
import ufps.edu.co.rest.services.PersonaService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class PersonaCoreService implements
        GlobalUseCase<PERSONA_CREATE, PERSONA_UPDATE, PERSONA_DELETE, PERSONA_PATCH, PERSONA_FIND, PersonaOutput> {

    @Autowired
    private PersonaService service;

    @Autowired
    private PersonaMap map;

    @Override
    public PersonaOutput create(PERSONA_CREATE input) {
        try {
            PersonaDTO dto = map.toDto(input);
            PersonaDTO created = service.create(dto);
            return map.toOutput(created);
        } catch (Exception e) {
            throw new RuntimeException("Error creating Persona: " + e.getMessage(), e);
        }
    }

    @Override
    public PersonaOutput update(PERSONA_UPDATE input) {
        try {
            PersonaDTO dto = map.toDto(input);
            PersonaDTO updated = service.update(input.id(), dto);
            return map.toOutput(updated);
        } catch (Exception e) {
            throw new RuntimeException("Error updating Persona: " + e.getMessage(), e);
        }
    }

    @Override
    public PersonaOutput patch(PERSONA_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Persona");
    }

    @Override
    public PersonaOutput findById(PERSONA_FIND input) {
        try {
            PersonaDTO dto = service.findById(input.id());
            return map.toOutput(dto);
        } catch (Exception e) {
            throw new RuntimeException("Error finding Persona by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PersonaOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Personas: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(PERSONA_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Persona by ID: " + e.getMessage(), e);
        }
    }
}
