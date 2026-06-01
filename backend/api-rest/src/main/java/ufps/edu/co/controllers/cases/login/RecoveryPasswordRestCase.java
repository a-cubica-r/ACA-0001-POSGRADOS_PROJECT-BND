package ufps.edu.co.controllers.cases.login;

import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ufps.edu.co.rest.dto.ClaveDTO;
import ufps.edu.co.rest.dto.PersonaDTO;
import ufps.edu.co.rest.dto.UsuarioDTO;
import ufps.edu.co.rest.services.ClaveService;
import ufps.edu.co.rest.services.PersonaService;
import ufps.edu.co.rest.services.UsuarioService;
import ufps.edu.co.services.SESService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
public class RecoveryPasswordRestCase {

	private static final Logger log = LoggerFactory.getLogger(RecoveryPasswordRestCase.class);

	@Autowired
	private PersonaService personaService;

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private ClaveService claveService;

	@Autowired
	private SESService sesService;

	@PostMapping(value = "/recoveryPassword", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> recoveryPassword(@RequestBody String correo) {
		String email = normalizeEmail(correo);
		if (email.isBlank()) {
			return ResponseEntity.badRequest().body("Correo invalido");
		}

		Optional<PersonaDTO> persona = personaService.findAll().stream()
				.filter(p -> p.getCorreo() != null
						&& p.getCorreo().trim().toLowerCase(Locale.ROOT).equals(email))
				.findFirst();
		if (persona.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		Optional<UsuarioDTO> usuario = usuarioService.findAll().stream()
				.filter(u -> u.getIdPersona() == persona.get().getId())
				.findFirst();
		if (usuario.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		ClaveDTO clave = claveService.findById(usuario.get().getIdClave());
		if (clave == null || clave.getValor() == null) {
			return ResponseEntity.notFound().build();
		}

		String htmlBody = "<p>Hola " + persona.get().getNombres() + ",</p>"
				+ "<p>Pediste recuperar tu contrasena. Esta es tu clave en texto plano:</p>"
				+ "<p><strong>" + clave.getValor() + "</strong></p>"
				+ "<p>Si no hiciste esta solicitud, ignora este correo.</p>";

		try {
			sesService.enviarCorreo(email, "Pediste recuperar tu contrasena", htmlBody);
		} catch (Exception ex) {
			log.error("[RECOVERY_EMAIL] Fallo al enviar correo de recuperación a '{}': {}", email, ex.getMessage(), ex);
			return ResponseEntity.internalServerError().body("No se pudo enviar el correo de recuperación");
		}
		return ResponseEntity.ok("Correo enviado");
	}

	private String normalizeEmail(String correo) {
		if (correo == null) {
			return "";
		}
		String trimmed = correo.trim();
		if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() > 1) {
			trimmed = trimmed.substring(1, trimmed.length() - 1);
		}
		return trimmed.toLowerCase(Locale.ROOT).trim();
	}
}
