package ufps.edu.co.controllers.cases.aspirante;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import ufps.edu.co.auth.contract.PasswordHashService;
import ufps.edu.co.persistence.entities.PersonaEntity;
import ufps.edu.co.persistence.entities.UsuarioEntity;
import ufps.edu.co.persistence.repositories.PersonaRepository;
import ufps.edu.co.persistence.repositories.UsuarioRepository;
import ufps.edu.co.rest.dto.ClaveDTO;
import ufps.edu.co.rest.services.ClaveService;
import ufps.edu.co.services.PasswordResetTokenService;
import ufps.edu.co.services.SESService;
import ufps.edu.co.utils.EmailTemplates;

@RestController
@RequestMapping(path = "/recuperarContrasena", produces = MediaType.APPLICATION_JSON_VALUE)
public class RecuperarContrasenaController {

    private static final Logger log = LoggerFactory.getLogger(RecuperarContrasenaController.class);

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClaveService claveService;

    @Autowired
    private PasswordHashService passwordHashService;

    @Autowired
    private PasswordResetTokenService passwordResetTokenService;

    @Autowired
    private SESService sesService;

    @Value("${app.frontend-reset-password-url}")
    private String frontendResetPasswordUrl;

    @PostMapping(value = "/solicitar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> solicitarRecuperacion(@RequestBody SolicitarRecuperacionRequest body) {
        PersonaEntity persona = personaRepository.findByCorreo(body.correo()).orElse(null);
        if (persona == null) {
            return ResponseEntity.ok().build();
        }
        UsuarioEntity usuario = usuarioRepository.findByIdPersona(persona.getId()).orElse(null);
        if (usuario == null) {
            return ResponseEntity.ok().build();
        }
        String token = passwordResetTokenService.generateToken(body.correo());
        String enlace = frontendResetPasswordUrl + "?token=" + token;
        try {
            sesService.enviarCorreoAsync(
                    body.correo(),
                    EmailTemplates.ASUNTO_RECUPERAR_CONTRASENA,
                    EmailTemplates.cuerpoRecuperacionContrasena(persona.getNombres(), enlace));
        } catch (Exception ex) {
            log.error("[RESET_PASSWORD] Fallo al enviar correo de recuperación a '{}': {}", body.correo(), ex.getMessage(), ex);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/cambiar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> cambiarContrasena(@RequestBody CambiarContrasenaRequest body) {
        String correo;
        try {
            correo = passwordResetTokenService.validateAndExtract(body.token());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        PersonaEntity persona = personaRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("No se encontró un usuario con ese correo"));
        UsuarioEntity usuario = usuarioRepository.findByIdPersona(persona.getId())
                .orElseThrow(() -> new RuntimeException("No se encontró un usuario asociado a ese correo"));
        ClaveDTO claveActualizada = ClaveDTO.builder()
                .valor(passwordHashService.encode(body.nuevaContrasena()))
                .build();
        claveService.update(usuario.getIdClave(), claveActualizada);
        return ResponseEntity.ok().build();
    }

    public static record SolicitarRecuperacionRequest(String correo) {}

    public static record CambiarContrasenaRequest(String token, String nuevaContrasena) {}
}
