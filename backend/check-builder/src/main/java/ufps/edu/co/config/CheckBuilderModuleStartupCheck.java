package ufps.edu.co.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CheckBuilderModuleStartupCheck {

    private static final Logger logger = LoggerFactory.getLogger(CheckBuilderModuleStartupCheck.class);

    @Value("${app.module.name.check-builder}")
    private String moduleName;

    @PostConstruct
    void validateAndLogModuleLoad() {
        if (moduleName == null || moduleName.isBlank()) {
            throw new IllegalStateException("app.module.name.check-builder is required for check-builder module");
        }

        String banner = """
                ======================================================
                =                MODULE %s LOADED                    =
                ======================================================
                """.formatted(moduleName);

        logger.warn("\n" + banner);
    }
}