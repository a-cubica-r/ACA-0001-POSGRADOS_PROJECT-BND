package ufps.edu.co.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import ufps.edu.co.persistence.entities.UsuarioEntity;
import ufps.edu.co.persistence.repositories.UsuarioRepository;

/**
 * Pruebas de repositorio para UsuarioRepository usando H2 en modo MySQL.
 * Cubre consultas derivadas (findByNombreusuario, findByIdPersona) y las
 * consultas @Query para proyecciones de idPersona.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:usuariorepo;MODE=MySQL;DB_CLOSE_DELAY=-1;"
                + "INIT=SET REFERENTIAL_INTEGRITY FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ContextConfiguration(classes = UsuarioRepositoryTest.Config.class)
class UsuarioRepositoryTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackages = "ufps.edu.co.persistence.entities")
    @EnableJpaRepositories(basePackages = "ufps.edu.co.persistence.repositories")
    static class Config {
    }

    @Autowired
    private UsuarioRepository usuarioRepository;

    private UsuarioEntity crearUsuario(String nombreusuario, Integer idPersona) {
        return usuarioRepository.save(UsuarioEntity.builder()
                .nombreusuario(nombreusuario)
                .idClave(10)
                .idPersona(idPersona)
                .idRol(1)
                .build());
    }

    // ─── findByNombreusuario ───────────────────────────────────────────────

    @Test
    @DisplayName("findByNombreusuario con usuario existente retorna Optional con la entidad")
    void findByNombreusuario_usuarioExistente_retornaUsuario() {
        crearUsuario("admin@ufps.edu.co", 100);

        Optional<UsuarioEntity> resultado = usuarioRepository.findByNombreusuario("admin@ufps.edu.co");

        assertTrue(resultado.isPresent());
        assertEquals("admin@ufps.edu.co", resultado.get().getNombreusuario());
    }

    @Test
    @DisplayName("findByNombreusuario con usuario inexistente retorna Optional vacío")
    void findByNombreusuario_usuarioInexistente_retornaVacio() {
        Optional<UsuarioEntity> resultado = usuarioRepository.findByNombreusuario("noexiste@ufps.edu.co");

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("findByNombreusuario distingue mayúsculas (consulta exacta)")
    void findByNombreusuario_diferenciaCase_retornaVacio() {
        crearUsuario("minusculas@ufps.edu.co", 101);

        Optional<UsuarioEntity> resultado = usuarioRepository.findByNombreusuario("MINUSCULAS@UFPS.EDU.CO");

        assertTrue(resultado.isEmpty());
    }

    // ─── findByIdPersona ──────────────────────────────────────────────────

    @Test
    @DisplayName("findByIdPersona con idPersona existente retorna el usuario")
    void findByIdPersona_existente_retornaUsuario() {
        crearUsuario("aspirante@ufps.edu.co", 200);

        Optional<UsuarioEntity> resultado = usuarioRepository.findByIdPersona(200);

        assertTrue(resultado.isPresent());
        assertEquals("aspirante@ufps.edu.co", resultado.get().getNombreusuario());
        assertEquals(200, resultado.get().getIdPersona());
    }

    @Test
    @DisplayName("findByIdPersona con idPersona inexistente retorna Optional vacío")
    void findByIdPersona_inexistente_retornaVacio() {
        Optional<UsuarioEntity> resultado = usuarioRepository.findByIdPersona(9999);

        assertTrue(resultado.isEmpty());
    }

    // ─── findIdPersonaByNombreusuario (@Query JPQL) ───────────────────────

    @Test
    @DisplayName("findIdPersonaByNombreusuario retorna el idPersona correcto")
    void findIdPersonaByNombreusuario_usuarioExistente_retornaIdPersona() {
        crearUsuario("director@ufps.edu.co", 300);

        Optional<Integer> resultado = usuarioRepository.findIdPersonaByNombreusuario("director@ufps.edu.co");

        assertTrue(resultado.isPresent());
        assertEquals(300, resultado.get());
    }

    @Test
    @DisplayName("findIdPersonaByNombreusuario con usuario inexistente retorna Optional vacío")
    void findIdPersonaByNombreusuario_inexistente_retornaVacio() {
        Optional<Integer> resultado = usuarioRepository.findIdPersonaByNombreusuario("fantasma@ufps.edu.co");

        assertTrue(resultado.isEmpty());
    }

    // ─── findIdPersonaById (@Query JPQL) ──────────────────────────────────

    @Test
    @DisplayName("findIdPersonaById con id existente retorna el idPersona")
    void findIdPersonaById_idExistente_retornaIdPersona() {
        UsuarioEntity guardado = crearUsuario("posgrados@ufps.edu.co", 400);

        Optional<Integer> resultado = usuarioRepository.findIdPersonaById(guardado.getId());

        assertTrue(resultado.isPresent());
        assertEquals(400, resultado.get());
    }

    @Test
    @DisplayName("findIdPersonaById con id inexistente retorna Optional vacío")
    void findIdPersonaById_idInexistente_retornaVacio() {
        Optional<Integer> resultado = usuarioRepository.findIdPersonaById(99999);

        assertFalse(resultado.isPresent());
    }

    // ─── Restricción NOT NULL en nombreusuario ────────────────────────────

    @Test
    @DisplayName("Dos usuarios con el mismo idPersona pueden coexistir (sin restricción UNIQUE)")
    void crearDosUsuariosMismaPersona_ambosGuardados() {
        crearUsuario("usuario1@ufps.edu.co", 500);
        crearUsuario("usuario2@ufps.edu.co", 500);

        assertEquals(2, usuarioRepository.findAll().stream()
                .filter(u -> u.getIdPersona().equals(500))
                .count());
    }
}
