package ufps.edu.co.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import ufps.edu.co.auth.config.AuthProperties;
import ufps.edu.co.auth.exception.InvalidCredentialsException;
import ufps.edu.co.auth.model.AuthPrincipal;
import ufps.edu.co.auth.util.PasswordFormats;
import ufps.edu.co.persistence.entities.ClaveEntity;
import ufps.edu.co.persistence.entities.RolEntity;
import ufps.edu.co.persistence.entities.UsuarioEntity;
import ufps.edu.co.persistence.repositories.ClaveRepository;
import ufps.edu.co.persistence.repositories.UsuarioRepository;

/**
 * T2 — el login conserva la compatibilidad con texto plano y migra a BCrypt
 * cuando la clave almacenada todavía no es un hash.
 */
@ExtendWith(MockitoExtension.class)
class CredentialServiceTest {

    private static final String CLAVE = "Clave123*";
    private static final Integer ID_CLAVE = 5;
    private static final Integer ID_USUARIO = 7;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClaveRepository claveRepository;

    private CredentialService service;

    @BeforeEach
    void setUp() {
        service = service(true);
    }

    private CredentialService service(boolean legacy) {
        AuthProperties properties = new AuthProperties();
        properties.getAuth().getPassword().setAllowPlainTextLegacy(legacy);
        BCryptPasswordHashService passwordHashService = new BCryptPasswordHashService(
                new BCryptPasswordEncoder(), properties);
        PasswordRehashService rehashService = new PasswordRehashService(claveRepository, passwordHashService);
        return new CredentialService(usuarioRepository, passwordHashService, new RoleNameNormalizer(), rehashService);
    }

    private static UsuarioEntity usuario(String valorClave) {
        ClaveEntity clave = ClaveEntity.builder().id(ID_CLAVE).valor(valorClave).build();
        RolEntity rol = RolEntity.builder().id(2).nombre("Super Administrador").build();
        return UsuarioEntity.builder()
                .id(ID_USUARIO)
                .nombreusuario("admin")
                .clave(clave)
                .rol(rol)
                .build();
    }

    private void usuarioExiste(String valorClave) {
        when(usuarioRepository.findByNombreusuario("admin")).thenReturn(Optional.of(usuario(valorClave)));
    }

    @Test
    @DisplayName("Clave en texto plano correcta + legacy=true: login OK y se guarda un hash BCrypt")
    void textoPlanoCorrecto_legacyTrue_rehashea() {
        UsuarioEntity usuario = usuario(CLAVE);
        when(usuarioRepository.findByNombreusuario("admin")).thenReturn(Optional.of(usuario));
        when(claveRepository.findById(ID_CLAVE)).thenReturn(Optional.of(usuario.getClave()));
        when(claveRepository.saveAndFlush(any(ClaveEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthPrincipal principal = service.authenticate("admin", CLAVE);

        assertEquals(ID_USUARIO, principal.userId());
        ArgumentCaptor<ClaveEntity> captor = ArgumentCaptor.forClass(ClaveEntity.class);
        verify(claveRepository).saveAndFlush(captor.capture());
        assertTrue(PasswordFormats.isBcrypt(captor.getValue().getValor()));
        assertTrue(new BCryptPasswordEncoder().matches(CLAVE, captor.getValue().getValor()));
    }

    @Test
    @DisplayName("Clave BCrypt correcta: login OK y no se toca la clave")
    void bcryptCorrecto_noRehashea() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        usuarioExiste(encoder.encode(CLAVE));

        AuthPrincipal principal = service.authenticate("admin", CLAVE);

        assertEquals(ID_USUARIO, principal.userId());
        verifyNoInteractions(claveRepository);
    }

    @Test
    @DisplayName("Clave incorrecta en texto plano: InvalidCredentials y no se guarda nada")
    void textoPlanoIncorrecto_lanzaInvalidCredentials() {
        usuarioExiste(CLAVE);

        assertThrows(InvalidCredentialsException.class, () -> service.authenticate("admin", "Otra123*"));
        verifyNoInteractions(claveRepository);
    }

    @Test
    @DisplayName("Clave incorrecta en BCrypt: InvalidCredentials y no se guarda nada")
    void bcryptIncorrecto_lanzaInvalidCredentials() {
        usuarioExiste(new BCryptPasswordEncoder().encode(CLAVE));

        assertThrows(InvalidCredentialsException.class, () -> service.authenticate("admin", "Otra123*"));
        verifyNoInteractions(claveRepository);
    }

    @Test
    @DisplayName("legacy=false + clave en texto plano: InvalidCredentials aunque coincida")
    void textoPlano_legacyFalse_lanzaInvalidCredentials() {
        usuarioExiste(CLAVE);
        CredentialService strict = service(false);

        assertThrows(InvalidCredentialsException.class, () -> strict.authenticate("admin", CLAVE));
        verifyNoInteractions(claveRepository);
    }

    @Test
    @DisplayName("Si falla el rehash, el login continúa y no se registra la clave")
    void falloDeRehash_noRompeElLogin() {
        UsuarioEntity usuario = usuario(CLAVE);
        when(usuarioRepository.findByNombreusuario("admin")).thenReturn(Optional.of(usuario));
        when(claveRepository.findById(ID_CLAVE)).thenReturn(Optional.of(usuario.getClave()));
        when(claveRepository.saveAndFlush(any(ClaveEntity.class)))
                .thenThrow(new DataIntegrityViolationException("fallo simulado"));

        AuthPrincipal principal = service.authenticate("admin", CLAVE);

        assertEquals(ID_USUARIO, principal.userId());
        verify(claveRepository).saveAndFlush(any(ClaveEntity.class));
    }
}
