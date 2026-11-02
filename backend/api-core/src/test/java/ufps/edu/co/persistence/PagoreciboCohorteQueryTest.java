package ufps.edu.co.persistence;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import ufps.edu.co.persistence.repositories.PagoreciboinscripcionRepository;
import ufps.edu.co.persistence.repositories.PagorecibomatriculaRepository;
import ufps.edu.co.rest.dto.PagoreciboDirectorProjectionDTO;

/**
 * D-00 T1 — Valida las consultas JPQL {@code findDirectorByCohorteId} de los
 * recibos de inscripción y de matrícula sobre un contexto de persistencia real
 * (H2 en modo MySQL), igual que {@code EliminarConHibernateTest}.
 *
 * <p>Las consultas se compilan al inicializar los repositorios, así que un
 * error en el JPQL (campo o join inexistente) rompe este test en lugar de
 * aparecer recién al levantar la aplicación.</p>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:pagoscohorte;MODE=MySQL;DB_CLOSE_DELAY=-1;"
                + "INIT=SET REFERENTIAL_INTEGRITY FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ContextConfiguration(classes = PagoreciboCohorteQueryTest.Config.class)
class PagoreciboCohorteQueryTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackages = "ufps.edu.co.persistence.entities")
    @EnableJpaRepositories(basePackages = "ufps.edu.co.persistence.repositories")
    static class Config {
    }

    @Autowired
    private PagoreciboinscripcionRepository inscripcionRepository;

    @Autowired
    private PagorecibomatriculaRepository matriculaRepository;

    @Test
    @DisplayName("Inscripción por cohorte sin datos → lista vacía y página vacía")
    void inscripcionPorCohorteDevuelveVacio() {
        assertTrue(inscripcionRepository.findDirectorByCohorteId(9999).isEmpty());

        Page<PagoreciboDirectorProjectionDTO> pagina = inscripcionRepository
                .findDirectorByCohorteId(9999, PageRequest.of(0, 10));
        assertTrue(pagina.getContent().isEmpty());
        assertTrue(pagina.getTotalElements() == 0L);
    }

    @Test
    @DisplayName("Matrícula por cohorte sin datos → lista vacía y página vacía")
    void matriculaPorCohorteDevuelveVacio() {
        assertTrue(matriculaRepository.findDirectorByCohorteId(9999).isEmpty());

        Page<PagoreciboDirectorProjectionDTO> pagina = matriculaRepository
                .findDirectorByCohorteId(9999, PageRequest.of(0, 10));
        assertTrue(pagina.getContent().isEmpty());
        assertTrue(pagina.getTotalElements() == 0L);
    }
}
