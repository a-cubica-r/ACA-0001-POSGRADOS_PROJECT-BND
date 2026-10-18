package ufps.edu.co.controllers.cases.aspirante;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ufps.edu.co.auth.contract.PasswordHashService;
import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.UsuarioErrorCode;
import ufps.edu.co.enums.Rol;
import ufps.edu.co.rest.dto.ClaveDTO;
import ufps.edu.co.rest.dto.UsuarioDTO;
import ufps.edu.co.rest.services.ClaveService;
import ufps.edu.co.rest.services.UsuarioService;
import ufps.edu.co.services.core.crud.RolCoreService;

@RestController
@RequestMapping(path = "/registroAspirante", produces = MediaType.APPLICATION_JSON_VALUE)
public class RegistroAspiranteController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ClaveService claveService;

    @Autowired
    private PasswordHashService passwordHashService;

    @Autowired
    private RolCoreService rolProcessor;

    @PostMapping(value = "/registro", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioDTO> registrarUsuario(@RequestBody RegistroUsuarioRequest body) {
        // Antes de crear la clave: si el usuario ya existe, se evita dejar una
        // fila de clave huérfana.
        if (body.usuario() != null && usuarioService.findByNombreusuario(body.usuario()) != null) {
            throw new DomainException(UsuarioErrorCode.USUARIO_YA_EXISTE_CONFLICT, body.usuario());
        }

        ClaveDTO clave = ClaveDTO.builder()
                .valor(passwordHashService.encode(body.contrasena()))
                .build();
        ClaveDTO savedClave = claveService.create(clave);

        UsuarioDTO usuario = UsuarioDTO.builder()
                .idPersona(body.idPersona())
                .idClave(savedClave.getId())
                .idRol(rolProcessor.findByNombre(Rol.ASPIRANTE.getValor()).id())
                .nombreusuario(body.usuario())
                .build();

        UsuarioDTO saved = usuarioService.create(usuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    public static record RegistroUsuarioRequest(Integer idPersona, String usuario, String contrasena) {
    }
}
