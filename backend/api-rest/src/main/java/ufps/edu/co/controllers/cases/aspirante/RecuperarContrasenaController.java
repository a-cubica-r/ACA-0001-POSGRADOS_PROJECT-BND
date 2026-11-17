package ufps.edu.co.controllers.cases.aspirante;

import java.util.List;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import ufps.edu.co.auth.contract.PasswordHashService;
import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.PersonaErrorCode;
import ufps.edu.co.domain.exceptions.errorcodes.UsuarioErrorCode;
import ufps.edu.co.exception.ErrorResponse;
import ufps.edu.co.persistence.entities.PersonaEntity;
import ufps.edu.co.persistence.entities.UsuarioEntity;
import ufps.edu.co.persistence.repositories.PersonaRepository;
import ufps.edu.co.persistence.repositories.UsuarioRepository;
import ufps.edu.co.rest.dto.ClaveDTO;
import ufps.edu.co.rest.services.ClaveService;
import ufps.edu.co.services.PasswordResetTokenService;
import ufps.edu.co.services.SESService;
import ufps.edu.co.util.CorreoNormalizer;
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
    public ResponseEntity<Void> solicitarRecuperacion(@Valid @RequestBody SolicitarRecuperacionRequest body) {
        String correo = CorreoNormalizer.normalizar(body.correo());
        PersonaEntity persona = personaRepository.findByCorreoIgnoreCase(correo).orElse(null);
        if (persona == null) {
            return ResponseEntity.ok().build();
        }
        List<UsuarioEntity> usuarios = usuarioRepository.findAllByIdPersona(persona.getId());
        if (usuarios.isEmpty()) {
            return ResponseEntity.ok().build();
        }
        String token = passwordResetTokenService.generateToken(correo);
        String enlace = frontendResetPasswordUrl + "?token=" + token;
        try {
            sesService.enviarCorreoAsync(
                    correo,
                    EmailTemplates.ASUNTO_RECUPERAR_CONTRASENA,
                    EmailTemplates.cuerpoRecuperacionContrasena(persona.getNombres(), enlace));
        } catch (Exception ex) {
            log.error("[RESET_PASSWORD] Fallo al enviar correo de recuperación a '{}': {}", correo, ex.getMessage(), ex);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/cambiar", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<ErrorResponse> cambiarContrasena(@Valid @RequestBody CambiarContrasenaRequest body) {
        String correo;
        try {
            correo = passwordResetTokenService.validateAndExtract(body.token());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("TOKEN_RECUPERACION_INVALIDO",
                            "El token de recuperación no es válido o expiró", null));
        }
        correo = CorreoNormalizer.normalizar(correo);
        String correoNormalizado = correo;

        PersonaEntity persona = personaRepository.findByCorreoIgnoreCase(correoNormalizado)
                .orElseThrow(() -> new DomainException(PersonaErrorCode.PERSONA_NOT_FOUND, correoNormalizado));

        List<UsuarioEntity> usuarios = usuarioRepository.findAllByIdPersona(persona.getId());
        if (usuarios.isEmpty()) {
            throw new DomainException(UsuarioErrorCode.USUARIO_NOT_FOUND, persona.getId());
        }

        String hash = passwordHashService.encode(body.nuevaContrasena());
        for (UsuarioEntity usuario : usuarios) {
            claveService.update(usuario.getIdClave(), ClaveDTO.builder().valor(hash).build());
        }
        return ResponseEntity.ok().build();
    }

    public static record SolicitarRecuperacionRequest(
            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo electrónico no tiene un formato válido")
            @Size(max = 100, message = "El correo no puede superar 100 caracteres")
            String correo) {
    }

    public static record CambiarContrasenaRequest(
            @NotBlank(message = "El token es obligatorio")
            String token,
            @NotBlank(message = "La nueva contraseña es obligatoria")
            @Size(max = 72, message = "La nueva contraseña no puede superar 72 caracteres")
            String nuevaContrasena) {
        @Override
        public String toString() {
            return "CambiarContrasenaRequest[token=***, nuevaContrasena=***]";
        }
    }
}
