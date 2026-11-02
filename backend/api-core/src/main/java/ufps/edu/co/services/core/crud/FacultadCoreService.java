package ufps.edu.co.services.core.crud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.FacultadErrorCode;
import ufps.edu.co.maps.specific.FacultadMap;
import ufps.edu.co.records.input.entity.FacultadInput.*;
import ufps.edu.co.records.output.entity.FacultadOutput;
import ufps.edu.co.rest.dto.FacultadDTO;
import ufps.edu.co.rest.exceptions.RegistroNoEncontradoException;
import ufps.edu.co.rest.services.FacultadService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class FacultadCoreService implements GlobalUseCase<FACULTAD_CREATE, FACULTAD_UPDATE, FACULTAD_DELETE, FACULTAD_PATCH, FACULTAD_FIND, FacultadOutput> {

    @Autowired
    private FacultadService service;

    @Autowired
    private FacultadMap map;

    @Override
    public FacultadOutput create(FACULTAD_CREATE input) {
        try {
            FacultadDTO dto = map.toDto(input);
            String nombre = normalizarNombre(dto.getNombre());
            dto.setNombre(nombre);
            validarNombreUnico(nombre, null);

            FacultadDTO created = service.create(dto);
            return map.toOutput(created);
        } catch (DomainException | DataIntegrityViolationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error creating Facultad: " + e.getMessage(), e);
        }
    }

    @Override
    public FacultadOutput update(FACULTAD_UPDATE input) {
        try {
            if (input.id() == null) {
                throw new DomainException(FacultadErrorCode.FACULTAD_PARAMETRO_REQUERIDO, "id");
            }
            if (service.findById(input.id()) == null) {
                throw new DomainException(FacultadErrorCode.FACULTAD_NOT_FOUND, input.id());
            }

            FacultadDTO dto = map.toDto(input);
            String nombre = normalizarNombre(dto.getNombre());
            dto.setNombre(nombre);
            validarNombreUnico(nombre, input.id());

            FacultadDTO updated = service.update(input.id(), dto);
            return map.toOutput(updated);
        } catch (DomainException | DataIntegrityViolationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error updating Facultad: " + e.getMessage(), e);
        }
    }

    @Override
    public FacultadOutput patch(FACULTAD_PATCH input) {
        throw new UnsupportedOperationException("Patch not supported for Facultad");
    }

    @Override
    public FacultadOutput findById(FACULTAD_FIND input) {
        try {
            return map.toOutput(service.findById(input.id()));
        } catch (DomainException | DataIntegrityViolationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error finding Facultad: " + e.getMessage(), e);
        }
    }

    @Override
    public List<FacultadOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Facultades: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(FACULTAD_DELETE input) {
        try {
            if (input.id() == null) {
                throw new DomainException(FacultadErrorCode.FACULTAD_PARAMETRO_REQUERIDO, "id");
            }
            if (service.findById(input.id()) == null) {
                throw new DomainException(FacultadErrorCode.FACULTAD_NOT_FOUND, input.id());
            }
            service.deleteById(input.id());
        } catch (DomainException | DataIntegrityViolationException e) {
            throw e;
        } catch (RegistroNoEncontradoException e) {
            throw new DomainException(FacultadErrorCode.FACULTAD_NOT_FOUND, input.id());
        }
    }

    /**
     * Rechaza el nombre si ya lo usa otra facultad. La comparación la hace la
     * base (collation {@code utf8mb4_0900_ai_ci}: sin distinguir mayúsculas ni
     * tildes), igual que su índice UNIQUE.
     */
    private void validarNombreUnico(String nombre, Integer idQueSeEdita) {
        if (nombre == null || nombre.isEmpty()) {
            return;
        }
        Integer existente = service.findIdByNombreIgnoreCase(nombre);
        if (existente != null && !existente.equals(idQueSeEdita)) {
            Map<String, Object> param = new HashMap<>();
            param.put("campo", "nombre");
            param.put("valor", nombre);
            param.put("idExistente", existente);
            throw new DomainException(FacultadErrorCode.FACULTAD_NOMBRE_DUPLICADO, param);
        }
    }

    /** trim() y colapsa los espacios internos repetidos. */
    static String normalizarNombre(String nombre) {
        if (nombre == null) {
            return null;
        }
        return nombre.trim().replaceAll("\\s+", " ");
    }
}
