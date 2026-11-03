package ufps.edu.co.config;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Construye enlaces públicos del backend garantizando el context path.
 */
@Component
public class EnlacesPublicos {

    private static final Logger log = LoggerFactory.getLogger(EnlacesPublicos.class);
    private static final String RUTA_CONFIRMACION = "/api/application/case/aspirantes/confirmar-correo?token=";

    private final String base;
    private final String contextPath;
    private final boolean prefijoAgregado;

    public EnlacesPublicos(
            @Value("${app.base-url}") String baseUrl,
            @Value("${server.servlet.context-path:}") String contextPath) {
        this.contextPath = normalizarPrefijo(contextPath);
        String baseNormalizada = quitarBarraFinal(baseUrl != null ? baseUrl.trim() : "");
        this.prefijoAgregado = !this.contextPath.isEmpty() && !baseNormalizada.endsWith(this.contextPath);
        this.base = this.prefijoAgregado ? baseNormalizada + this.contextPath : baseNormalizada;
    }

    @PostConstruct
    void advertirSiFaltaPrefijo() {
        if (prefijoAgregado) {
            log.warn("app.base-url (APP_BASE_URL) no incluye el context path {}; se agrega automáticamente a los enlaces",
                    contextPath);
        }
    }

    public String confirmacionCorreo(String token) {
        String tokenCodificado = URLEncoder.encode(token != null ? token : "", StandardCharsets.UTF_8);
        return base + RUTA_CONFIRMACION + tokenCodificado;
    }

    String getBase() {
        return base;
    }

    private static String normalizarPrefijo(String prefijo) {
        if (prefijo == null || prefijo.isBlank()) {
            return "";
        }
        String normalizado = prefijo.trim();
        if (!normalizado.startsWith("/")) {
            normalizado = "/" + normalizado;
        }
        normalizado = quitarBarraFinal(normalizado);
        return normalizado;
    }

    private static String quitarBarraFinal(String valor) {
        String resultado = valor;
        while (resultado.endsWith("/")) {
            resultado = resultado.substring(0, resultado.length() - 1);
        }
        return resultado;
    }
}
