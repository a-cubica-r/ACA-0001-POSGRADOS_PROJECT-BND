package ufps.edu.co.auth.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import ufps.edu.co.auth.util.PasswordFormats;
import ufps.edu.co.persistence.entities.ClaveEntity;
import ufps.edu.co.persistence.repositories.ClaveRepository;

@ExtendWith(MockitoExtension.class)
class MigracionClavesRunnerTest {

    @Mock
    private ClaveRepository claveRepository;

    @Mock
    private ApplicationContext applicationContext;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private MigracionClavesRunner runner;

    @BeforeEach
    void setUp() {
        MigracionClavesService service = new MigracionClavesService(claveRepository, passwordEncoder);
        runner = new MigracionClavesRunner(service, applicationContext);
    }

    private static ClaveEntity clave(int id, String valor) {
        return ClaveEntity.builder().id(id).valor(valor).build();
    }

    private void repositorioCon(List<ClaveEntity> claves) {
        when(claveRepository.findAll()).thenReturn(claves);
        when(claveRepository.save(any(ClaveEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("3 claves en texto plano + 1 BCrypt: se cifran 3, la BCrypt queda igual y código 0")
    void migraSoloLasQueNoSonBcrypt() {
        String bcryptOriginal = passwordEncoder.encode("Bcrypt123*");
        List<ClaveEntity> claves = new ArrayList<>(List.of(
                clave(1, "Clave123*"),
                clave(2, "Otra123*"),
                clave(3, "Tercera123*"),
                clave(4, bcryptOriginal)));
        repositorioCon(claves);

        int codigo = runner.runMigration();

        assertEquals(0, codigo);
        verify(claveRepository, times(3)).save(any(ClaveEntity.class));
        assertEquals(bcryptOriginal, claves.get(3).getValor());
        assertTrue(PasswordFormats.isBcrypt(claves.get(0).getValor()));
        assertTrue(passwordEncoder.matches("Clave123*", claves.get(0).getValor()));
    }

    @Test
    @DisplayName("Segunda ejecución: no vuelve a migrar ninguna clave")
    void segundaEjecucion_noMigraNada() {
        List<ClaveEntity> claves = new ArrayList<>(List.of(
                clave(1, "Clave123*"),
                clave(2, "Otra123*")));
        repositorioCon(claves);

        assertEquals(0, runner.runMigration());
        assertEquals(0, runner.runMigration());

        verify(claveRepository, times(2)).save(any(ClaveEntity.class));
    }

    @Test
    @DisplayName("Clave vacía: no se cifra, se reporta su id y el código es 1")
    void claveVacia_noSeCifraYDevuelveCodigo1() {
        List<ClaveEntity> claves = new ArrayList<>(List.of(clave(5, "")));
        when(claveRepository.findAll()).thenReturn(claves);

        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        Logger logger = (Logger) LoggerFactory.getLogger(MigracionClavesRunner.class);
        logger.addAppender(appender);
        int codigo;
        try {
            codigo = runner.runMigration();
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }

        assertEquals(1, codigo);
        verify(claveRepository, never()).save(any(ClaveEntity.class));
        assertFalse(PasswordFormats.isBcrypt(claves.get(0).getValor()));
        assertTrue(appender.list.stream()
                .anyMatch(event -> event.getFormattedMessage().contains("[5]")));
    }

    @Test
    @DisplayName("El log solo contiene conteos e ids, nunca el valor de una clave")
    void log_noContieneValoresDeClave() {
        List<ClaveEntity> claves = new ArrayList<>(List.of(clave(1, "Clave123*")));
        repositorioCon(claves);

        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        Logger logger = (Logger) LoggerFactory.getLogger(MigracionClavesRunner.class);
        logger.addAppender(appender);
        try {
            assertEquals(0, runner.runMigration());
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }

        assertTrue(appender.list.stream()
                .noneMatch(event -> event.getFormattedMessage().contains("Clave123*")));
        assertTrue(appender.list.stream()
                .anyMatch(event -> event.getFormattedMessage().contains("migradas=1")));
    }
}
