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
import ufps.edu.co.rest.dto.ClaveDTO;
import ufps.edu.co.rest.dto.UsuarioDTO;
import ufps.edu.co.rest.services.ClaveService;
import ufps.edu.co.rest.services.UsuarioService;

@RestController
@RequestMapping(path = "/registroAspirante", produces = MediaType.APPLICATION_JSON_VALUE)
public class RegistroAspiranteController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ClaveService claveService;

    @Autowired
    private PasswordHashService passwordHashService;

    @PostMapping(value = "/registro", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioDTO> registrarUsuario(@RequestBody RegistroUsuarioRequest body) {
        ClaveDTO clave = ClaveDTO.builder()
                .valor(passwordHashService.encode(body.contrasena()))
                .build();
        ClaveDTO savedClave = claveService.create(clave);

        UsuarioDTO usuario = UsuarioDTO.builder()
                .idPersona(body.idPersona())
                .idClave(savedClave.getId())
                .nombreusuario(body.usuario())
                .build();

        UsuarioDTO saved = usuarioService.create(usuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    public static record RegistroUsuarioRequest(Integer idPersona, String usuario, String contrasena) {
    }
}
