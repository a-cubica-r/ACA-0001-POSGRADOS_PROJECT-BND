package ufps.edu.co.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EnlacesPublicosTest {

    private static final String RUTA = "/api/application/case/aspirantes/confirmar-correo?token=";

    @Test
    void agregaElPrefijoCuandoLaBaseNoLoTiene() {
        EnlacesPublicos enlaces = new EnlacesPublicos("https://x.onrender.com", "/posgrados-project");

        assertEquals("https://x.onrender.com/posgrados-project" + RUTA + "abc",
                enlaces.confirmacionCorreo("abc"));
    }

    @Test
    void noDuplicaElPrefijoSiLaBaseYaLoTiene() {
        EnlacesPublicos enlaces = new EnlacesPublicos("https://x.onrender.com/posgrados-project", "/posgrados-project");

        assertEquals("https://x.onrender.com/posgrados-project" + RUTA + "abc",
                enlaces.confirmacionCorreo("abc"));
    }

    @Test
    void quitaLaBarraFinalDeLaBase() {
        EnlacesPublicos enlaces = new EnlacesPublicos("https://x.onrender.com/", "/posgrados-project");

        assertEquals("https://x.onrender.com/posgrados-project" + RUTA + "abc",
                enlaces.confirmacionCorreo("abc"));
    }

    @Test
    void codificaElToken() {
        EnlacesPublicos enlaces = new EnlacesPublicos("https://x.onrender.com", "/posgrados-project");

        assertEquals("https://x.onrender.com/posgrados-project" + RUTA + "a+b%2Bc",
                enlaces.confirmacionCorreo("a b+c"));
    }

    @Test
    void normalizaUnPrefijoSinBarraInicial() {
        EnlacesPublicos enlaces = new EnlacesPublicos("https://x.onrender.com", "posgrados-project");

        assertEquals("https://x.onrender.com/posgrados-project" + RUTA + "abc",
                enlaces.confirmacionCorreo("abc"));
    }
}
