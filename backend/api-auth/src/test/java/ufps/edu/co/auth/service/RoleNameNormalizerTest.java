package ufps.edu.co.auth.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoleNameNormalizerTest {

    // --- Casos límite / valores nulos ---

    @Test
    void normalize_conNull_retornaVacio() {
        // Dado/Cuando/Entonces
        assertEquals("", RoleNameNormalizer.normalize(null));
    }

    @Test
    void normalize_conCadenaVacia_retornaVacio() {
        assertEquals("", RoleNameNormalizer.normalize(""));
    }

    @Test
    void normalize_conSoloEspacios_retornaVacio() {
        assertEquals("", RoleNameNormalizer.normalize("   "));
    }

    // --- Transformación de minúsculas a mayúsculas ---

    @Test
    void normalize_conMinusculas_retornaMayusculas() {
        assertEquals("ASPIRANTE", RoleNameNormalizer.normalize("aspirante"));
    }

    @Test
    void normalize_conMixtoMayusMinusc_retornaMayusculas() {
        assertEquals("ADMIN", RoleNameNormalizer.normalize("Admin"));
    }

    // --- Eliminación de acentos ---

    @Test
    void normalize_conTildeAcentuada_eliminaAcento() {
        assertEquals("ADMINISTRACION", RoleNameNormalizer.normalize("Administración"));
    }

    @Test
    void normalize_conMultiplesAcentos_eliminaTodos() {
        assertEquals("COORDINACION_ACADEMICA", RoleNameNormalizer.normalize("Coordinación Académica"));
    }

    // --- Espacios → guion bajo ---

    @Test
    void normalize_conUnEspacio_reemplazaConGuionBajo() {
        assertEquals("DIRECTOR_POSGRADOS", RoleNameNormalizer.normalize("Director Posgrados"));
    }

    @Test
    void normalize_conMultiplesEspacios_unificaAUnGuionBajo() {
        assertEquals("DIRECTOR_POSGRADOS", RoleNameNormalizer.normalize("Director  Posgrados"));
    }

    // --- Guiones → guion bajo ---

    @Test
    void normalize_conGuionSimple_reemplazaConGuionBajo() {
        assertEquals("ADMIN_ROL", RoleNameNormalizer.normalize("admin-rol"));
    }

    @Test
    void normalize_conGuionesMultiples_unificaAUnGuionBajo() {
        assertEquals("DIRECTOR_POSGRADOS", RoleNameNormalizer.normalize("director--posgrados"));
    }

    // --- Eliminación de caracteres especiales ---

    @Test
    void normalize_conCaracteresEspeciales_losElimina() {
        assertEquals("ADMIN", RoleNameNormalizer.normalize("admin!@#$%"));
    }

    @Test
    void normalize_conParentesis_losElimina() {
        assertEquals("ROL_TEST", RoleNameNormalizer.normalize("rol (test)"));
    }

    // --- Guiones bajos al inicio y al final ---

    @Test
    void normalize_conGuionesExtremos_losElimina() {
        assertEquals("ADMIN", RoleNameNormalizer.normalize("_admin_"));
    }

    // --- Casos de uso reales del dominio ---

    @Test
    void normalize_rolDirectorDePosgrados_retormaFormato() {
        assertEquals("DIRECTOR_DE_POSGRADOS", RoleNameNormalizer.normalize("Director de Posgrados"));
    }

    @Test
    void normalize_rolEstudianteConEspaciosPadding_retornaFormato() {
        assertEquals("ESTUDIANTE", RoleNameNormalizer.normalize("  ESTUDIANTE  "));
    }
}
