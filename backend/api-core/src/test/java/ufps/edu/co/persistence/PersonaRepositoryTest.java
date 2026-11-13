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

import ufps.edu.co.persistence.entities.PersonaEntity;
import ufps.edu.co.persistence.repositories.PersonaRepository;

/**
 * Pruebas de repositorio para PersonaRepository usando H2 en modo MySQL.
 * Verifica las consultas derivadas de email (case-insensitive) y la búsqueda
 * por correo.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:personarepo;MODE=MySQL;DB_CLOSE_DELAY=-1;"
                + "INIT=SET REFERENTIAL_INTEGRITY FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ContextConfiguration(classes = PersonaRepositoryTest.Config.class)
class PersonaRepositoryTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackages = "ufps.edu.co.persistence.entities")
    @EnableJpaRepositories(basePackages = "ufps.edu.co.persistence.repositories")
    static class Config {
    }

    @Autowired
    private PersonaRepository personaRepository;

    private PersonaEntity crearPersona(String correo) {
        return personaRepository.save(PersonaEntity.builder()
                .nombres("Juan")
                .apellidos("Pérez")
                .celular("3001234567")
                .correo(correo)
                .build());
    }

    // ─── existsByCorreoIgnoreCase ──────────────────────────────────────────

    @Test
    @DisplayName("existsByCorreoIgnoreCase con correo exacto retorna true")
    void existsByCorreoIgnoreCase_correoExacto_retornaTrue() {
        crearPersona("usuario@ufps.edu.co");

        assertTrue(personaRepository.existsByCorreoIgnoreCase("usuario@ufps.edu.co"));
    }

    @Test
    @DisplayName("existsByCorreoIgnoreCase con correo en mayúsculas retorna true (case-insensitive)")
    void existsByCorreoIgnoreCase_correoEnMayusculas_retornaTrue() {
        crearPersona("usuario@ufps.edu.co");

        assertTrue(personaRepository.existsByCorreoIgnoreCase("USUARIO@UFPS.EDU.CO"));
    }

    @Test
    @DisplayName("existsByCorreoIgnoreCase con correo mixto retorna true")
    void existsByCorreoIgnoreCase_correoMixto_retornaTrue() {
        crearPersona("usuario@ufps.edu.co");

        assertTrue(personaRepository.existsByCorreoIgnoreCase("Usuario@Ufps.Edu.Co"));
    }

    @Test
    @DisplayName("existsByCorreoIgnoreCase con correo inexistente retorna false")
    void existsByCorreoIgnoreCase_correoInexistente_retornaFalse() {
        assertFalse(personaRepository.existsByCorreoIgnoreCase("noexiste@ufps.edu.co"));
    }

    // ─── existsByCorreoIgnoreCaseAndIdNot ──────────────────────────────────

    @Test
    @DisplayName("existsByCorreoIgnoreCaseAndIdNot retorna false para el mismo id (actualización propia)")
    void existsByCorreoIgnoreCaseAndIdNot_mismoId_retornaFalse() {
        PersonaEntity persona = crearPersona("duplicado@test.co");

        assertFalse(personaRepository.existsByCorreoIgnoreCaseAndIdNot("duplicado@test.co", persona.getId()));
    }

    @Test
    @DisplayName("existsByCorreoIgnoreCaseAndIdNot retorna true si otro id tiene el mismo correo")
    void existsByCorreoIgnoreCaseAndIdNot_otroIdMismoCorreo_retornaTrue() {
        crearPersona("duplicado@test.co");
        PersonaEntity otraPersona = crearPersona("otro@test.co");

        assertTrue(personaRepository.existsByCorreoIgnoreCaseAndIdNot("duplicado@test.co", otraPersona.getId()));
    }

    @Test
    @DisplayName("existsByCorreoIgnoreCaseAndIdNot ignora diferencias de mayúsculas en el correo")
    void existsByCorreoIgnoreCaseAndIdNot_correoMayusculas_retornaTrue() {
        crearPersona("duplicado@test.co");
        PersonaEntity otra = crearPersona("otro@test.co");

        assertTrue(personaRepository.existsByCorreoIgnoreCaseAndIdNot("DUPLICADO@TEST.CO", otra.getId()));
    }

    // ─── findByCorreo ──────────────────────────────────────────────────────

    @Test
    @DisplayName("findByCorreo con correo existente retorna la persona")
    void findByCorreo_correoExistente_retornaPersona() {
        PersonaEntity guardada = crearPersona("buscar@ufps.edu.co");

        Optional<PersonaEntity> resultado = personaRepository.findByCorreo("buscar@ufps.edu.co");

        assertTrue(resultado.isPresent());
        assertEquals(guardada.getId(), resultado.get().getId());
        assertEquals("buscar@ufps.edu.co", resultado.get().getCorreo());
    }

    @Test
    @DisplayName("findByCorreo con correo inexistente retorna Optional vacío")
    void findByCorreo_correoInexistente_retornaVacio() {
        Optional<PersonaEntity> resultado = personaRepository.findByCorreo("nadie@ufps.edu.co");

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("findByCorreo distingue mayúsculas (consulta exacta)")
    void findByCorreo_correoMayusculas_retornaVacio() {
        crearPersona("exacto@ufps.edu.co");

        // findByCorreo NO es case-insensitive (a diferencia de existsByCorreoIgnoreCase)
        Optional<PersonaEntity> resultado = personaRepository.findByCorreo("EXACTO@UFPS.EDU.CO");

        assertTrue(resultado.isEmpty());
    }
}
