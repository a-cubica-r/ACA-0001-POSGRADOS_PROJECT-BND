package ufps.edu.co.auth.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import ufps.edu.co.auth.migration.MigracionClavesService.Resultado;

/**
 * Ejecuta la migración una sola vez con el perfil {@code migrar-claves}. Solo
 * registra conteos e ids; nunca el valor de una clave.
 */
@Component
@Profile("migrar-claves")
public class MigracionClavesRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MigracionClavesRunner.class);

    private final MigracionClavesService migracionClavesService;
    private final ApplicationContext applicationContext;

    public MigracionClavesRunner(MigracionClavesService migracionClavesService,
            ApplicationContext applicationContext) {
        this.migracionClavesService = migracionClavesService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) {
        int codigo = runMigration();
        System.exit(SpringApplication.exit(applicationContext, () -> codigo));
    }

    /**
     * Separado de {@link #run(ApplicationArguments)} para poder probarlo sin
     * cerrar la JVM.
     */
    int runMigration() {
        Resultado resultado = migracionClavesService.migrar();

        log.info("Migración de claves: total={}, yaCifradas={}, migradas={}, vacias={}",
                resultado.total(), resultado.yaCifradas(), resultado.migradas(), resultado.vacias().size());

        if (!resultado.vacias().isEmpty()) {
            log.warn("Claves vacías/nulas (ids): {}", resultado.vacias());
        }

        if (!resultado.pendientes().isEmpty()) {
            log.error("Claves que siguen sin BCrypt (ids): {}", resultado.pendientes());
            return 1;
        }

        log.info("Migración de claves completada");
        return 0;
    }
}
