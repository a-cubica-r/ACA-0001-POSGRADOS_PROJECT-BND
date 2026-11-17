package ufps.edu.co.services.core.crud;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.UsuarioErrorCode;
import ufps.edu.co.maps.specific.UsuarioMap;
import ufps.edu.co.records.input.entity.UsuarioInput.*;
import ufps.edu.co.records.output.entity.UsuarioOutput;
import ufps.edu.co.rest.dto.UsuarioDTO;
import ufps.edu.co.rest.services.UsuarioService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class UsuarioCoreService implements
        GlobalUseCase<USUARIO_CREATE, USUARIO_UPDATE, USUARIO_DELETE, USUARIO_PATCH, USUARIO_FIND, UsuarioOutput> {

    @Autowired
    private UsuarioService service;

    @Autowired
    private UsuarioMap map;

    @Override
    public UsuarioOutput create(USUARIO_CREATE input) {
        String nombreusuario = normalizar(input.nombreusuario());
        if (nombreusuario == null || nombreusuario.isEmpty()) {
            throw new DomainException(UsuarioErrorCode.USUARIO_PARAMETRO_REQUERIDO, "nombreusuario");
        }
        if (service.existsByNombreusuario(nombreusuario)) {
            throw new DomainException(UsuarioErrorCode.USUARIO_YA_EXISTE_CONFLICT, nombreusuario);
        }
        UsuarioDTO dto = map.toDto(input);
        dto.setNombreusuario(nombreusuario);
        UsuarioDTO created = service.create(dto);
        return map.toOutput(created);
    }

    @Override
    public UsuarioOutput update(USUARIO_UPDATE input) {
        if (input.id() == null) {
            throw new DomainException(UsuarioErrorCode.USUARIO_PARAMETRO_REQUERIDO, "id");
        }
        String nombreusuario = normalizar(input.nombreusuario());
        if (nombreusuario == null || nombreusuario.isEmpty()) {
            throw new DomainException(UsuarioErrorCode.USUARIO_PARAMETRO_REQUERIDO, "nombreusuario");
        }
        if (service.existsByNombreusuarioAndIdNot(nombreusuario, input.id())) {
            throw new DomainException(UsuarioErrorCode.USUARIO_YA_EXISTE_CONFLICT, nombreusuario);
        }
        UsuarioDTO dto = map.toDto(input);
        dto.setNombreusuario(nombreusuario);
        UsuarioDTO updated = service.update(input.id(), dto);
        return map.toOutput(updated);
    }

    @Override
    public UsuarioOutput patch(USUARIO_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Usuario");
    }

    @Override
    public UsuarioOutput findById(USUARIO_FIND input) {
        UsuarioDTO dto = service.findById(input.id());
        return map.toOutput(dto);
    }

    @Override
    public List<UsuarioOutput> findAll() {
        return service.findAll().stream().map(map::toOutput).toList();
    }

    @Override
    public void deleteById(USUARIO_DELETE input) {
        service.deleteById(input.id());
    }

    private String normalizar(String nombreusuario) {
        return nombreusuario == null ? null : nombreusuario.trim();
    }
}
