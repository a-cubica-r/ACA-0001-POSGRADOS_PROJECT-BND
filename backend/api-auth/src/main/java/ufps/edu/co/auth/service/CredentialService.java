package ufps.edu.co.auth.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ufps.edu.co.auth.contract.PasswordHashService;
import ufps.edu.co.auth.exception.InvalidCredentialsException;
import ufps.edu.co.auth.model.AuthPrincipal;
import ufps.edu.co.persistence.entities.UsuarioEntity;
import ufps.edu.co.persistence.repositories.UsuarioRepository;

@Service
public class CredentialService {

    private static final Logger log = LoggerFactory.getLogger(CredentialService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordHashService passwordHashService;
    private final PasswordRehashService passwordRehashService;

    public CredentialService(
            UsuarioRepository usuarioRepository,
            PasswordHashService passwordHashService,
            RoleNameNormalizer roleNameNormalizer,
            PasswordRehashService passwordRehashService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHashService = passwordHashService;
        this.passwordRehashService = passwordRehashService;
    }

    /**
     * Sin {@code @Transactional}: las relaciones del usuario son EAGER y la
     * única escritura (el rehash) ocurre en {@link PasswordRehashService} con su
     * propia transacción. Así el login no retiene dos conexiones del pool durante
     * la ventana de migración.
     */
    public AuthPrincipal authenticate(String username, String rawPassword) {
        UsuarioEntity usuario = usuarioRepository.findByNombreusuario(username)
                .orElseThrow(InvalidCredentialsException::new);

        String storedPassword = Optional.ofNullable(usuario.getClave())
                .map(clave -> clave.getValor())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHashService.matches(rawPassword, storedPassword)) {
            throw new InvalidCredentialsException();
        }

        if (passwordHashService.needsRehash(storedPassword)) {
            try {
                passwordRehashService.rehash(usuario.getClave().getId(), rawPassword);
            } catch (RuntimeException ex) {
                // Nunca se registra la contraseña ni el valor almacenado.
                log.warn("No se pudo migrar la clave a BCrypt para el usuario {}", usuario.getId());
            }
        }

        return toPrincipal(usuario);
    }

    public AuthPrincipal toPrincipal(UsuarioEntity usuario) {
        List<String> roles = resolveRoles(usuario);
        return new AuthPrincipal(usuario.getId(), usuario.getNombreusuario(), roles);
    }

    public List<String> resolveRoles(UsuarioEntity usuario) {
        return Optional.ofNullable(usuario.getRol())
                .map(rol -> RoleNameNormalizer.normalize(rol.getNombre()))
                .filter(role -> !role.isBlank())
                .map(List::of)
                .orElseGet(List::of);
    }
}
