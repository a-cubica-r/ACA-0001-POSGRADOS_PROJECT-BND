package ufps.edu.co.processor.crud;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.hibernate.TransientPropertyValueException;
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

import ufps.edu.co.persistence.entities.CargoEntity;
import ufps.edu.co.persistence.entities.CohorteEntity;
import ufps.edu.co.persistence.entities.DocumentosrequisitoconsejocohorteEntity;
import ufps.edu.co.persistence.entities.ProgramaEntity;
import ufps.edu.co.persistence.repositories.CargoRepository;
import ufps.edu.co.persistence.repositories.CohorteRepository;
import ufps.edu.co.persistence.repositories.DocumentosrequisitoconsejocohorteRepository;
import ufps.edu.co.persistence.repositories.ProgramaRepository;

/**
 * D-09b T3 — Verifica con un contexto de persistencia real (H2 en modo MySQL)
 * el patrón de borrado de hijos y padre.
 *
 * <p>El caso A y el B reproducen el orden que usan {@code CohorteCoreService} y
 * {@code ProgramaCoreService}: borrar los hijos como entidades, hacer
 * {@code flush()} y después borrar el padre. El caso C documenta el patrón
 * prohibido: {@code deleteAllInBatch} sobre los hijos seguido del borrado del
 * padre revienta el {@code flush()} con
 * {@link TransientPropertyValueException}.</p>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:borrado;MODE=MySQL;DB_CLOSE_DELAY=-1;"
                + "INIT=SET REFERENTIAL_INTEGRITY FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ContextConfiguration(classes = EliminarConHibernateTest.Config.class)
class EliminarConHibernateTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackages = "ufps.edu.co.persistence.entities")
    @EnableJpaRepositories(basePackages = "ufps.edu.co.persistence.repositories")
    static class Config {
    }

    @PersistenceContext
    private EntityManager em;

    @Autowired private CohorteRepository cohorteRepository;
    @Autowired private DocumentosrequisitoconsejocohorteRepository documentosConsejoRepository;
    @Autowired private ProgramaRepository programaRepository;
    @Autowired private CargoRepository cargoRepository;

    private Integer crearCohorteConDocumento() {
        CohorteEntity cohorte = CohorteEntity.builder()
                .nombre("Cohorte de prueba")
                .cupos(10)
                .idEstado(1)
                .idSemestre(1)
                .idModalidad(1)
                .idPlazodocumentacion(1)
                .idPlazoinscripcion(2)
                .idPlazopago(3)
                .idPrograma(1)
                .build();
        cohorte = cohorteRepository.save(cohorte);

        documentosConsejoRepository.save(DocumentosrequisitoconsejocohorteEntity.builder()
                .idDocrequisito(1)
                .idCohorte(cohorte.getId())
                .build());

        return cohorte.getId();
    }

    private Integer crearProgramaConCargo() {
        ProgramaEntity programa = ProgramaEntity.builder()
                .nombre("Programa de prueba")
                .duracion(4)
                .idFacultad(1)
                .idSede(1)
                .idTiporegistro(1)
                .idModalidad(1)
                .build();
        programa = programaRepository.save(programa);

        cargoRepository.save(CargoEntity.builder()
                .nombre("Director de prueba")
                .idPrograma(programa.getId())
                .build());

        return programa.getId();
    }

    @Test
    @DisplayName("Cohorte: hijos como entidades, flush y luego el padre → sin excepción y sin filas")
    void cohorteConDocumentos() {
        Integer id = crearCohorteConDocumento();
        em.flush();
        em.clear();

        List<DocumentosrequisitoconsejocohorteEntity> hijos =
                documentosConsejoRepository.findByIdCohorte(id);
        assertFalse(hijos.isEmpty(), "Debe existir el documento de consejo de la cohorte");

        documentosConsejoRepository.deleteAll(hijos);
        em.flush();

        cohorteRepository.deleteById(id);
        em.flush();

        assertTrue(cohorteRepository.findById(id).isEmpty(), "La cohorte no debe quedar");
        assertTrue(documentosConsejoRepository.findByIdCohorte(id).isEmpty(),
                "No deben quedar documentos de consejo de la cohorte");
    }

    @Test
    @DisplayName("Programa: cargos como entidades, flush y luego el programa → sin excepción y sin filas")
    void programaConCargo() {
        Integer id = crearProgramaConCargo();
        em.flush();
        em.clear();

        List<CargoEntity> cargos = cargoRepository.findByIdPrograma(id);
        assertFalse(cargos.isEmpty(), "Debe existir el cargo del programa");

        cargoRepository.deleteAll(cargos);
        em.flush();

        programaRepository.deleteById(id);
        em.flush();

        assertTrue(programaRepository.findById(id).isEmpty(), "El programa no debe quedar");
        assertTrue(cargoRepository.findByIdPrograma(id).isEmpty(), "No deben quedar cargos del programa");
    }

    @Test
    @DisplayName("Regresión: deleteAllInBatch de los hijos + borrar el padre revienta el flush")
    void patronProhibidoConDeleteAllInBatch() {
        Integer id = crearProgramaConCargo();
        em.flush();
        em.clear();

        List<CargoEntity> cargos = cargoRepository.findByIdPrograma(id);
        assertFalse(cargos.isEmpty(), "Debe existir el cargo del programa");

        cargoRepository.deleteAllInBatch(cargos);
        programaRepository.deleteById(id);

        Throwable thrown = assertThrows(Throwable.class, () -> em.flush());

        assertTrue(tieneCausa(thrown, TransientPropertyValueException.class),
                "Se esperaba TransientPropertyValueException en la cadena de causas, pero fue: " + thrown);
    }

    private static boolean tieneCausa(Throwable throwable, Class<? extends Throwable> tipo) {
        for (Throwable actual = throwable; actual != null; actual = actual.getCause()) {
            if (tipo.isInstance(actual)) {
                return true;
            }
        }
        return false;
    }
}
